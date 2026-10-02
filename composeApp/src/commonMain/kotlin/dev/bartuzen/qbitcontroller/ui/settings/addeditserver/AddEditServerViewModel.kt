package dev.bartuzen.qbitcontroller.ui.settings.addeditserver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bartuzen.qbitcontroller.data.ServerManager
import dev.bartuzen.qbitcontroller.model.ServerConfig
import dev.bartuzen.qbitcontroller.network.RequestManager
import dev.bartuzen.qbitcontroller.network.RequestResult
import dev.bartuzen.qbitcontroller.network.catchRequestError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AddEditServerViewModel(
    serverId: Int?,
    private val serverManager: ServerManager,
    private val requestManager: RequestManager,
) : ViewModel() {
    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting = _isTesting.asStateFlow()

    private var testJob: Job? = null

    val serverConfig = serverId?.let { serverManager.getServer(it) }

    fun addServer(serverConfig: ServerConfig) = viewModelScope.launch {
        serverManager.addServer(serverConfig)
    }

    fun editServer(serverConfig: ServerConfig) = viewModelScope.launch {
        serverManager.editServer(serverConfig)
    }

    fun removeServer(serverId: Int) = viewModelScope.launch {
        serverManager.removeServer(serverId)
    }

    fun testConnection(serverConfig: ServerConfig) {
        testJob?.cancel()

        _isTesting.value = true
        val job = viewModelScope.launch {
            val result = catchRequestError(
                block = {
                    val service =
                        requestManager.buildTorrentService(serverConfig, requestManager.buildHttpClient(serverConfig))

                    val response = service.login(serverConfig.username ?: "", serverConfig.password ?: "")

                    val code = response.code
                    val body = response.body

                    when {
                        code in 200..<300 && (body == "Ok." || body == null) -> RequestResult.Success(Unit)
                        code == 403 -> RequestResult.Error.RequestError.Banned
                        body == "Fails." || code == 401 -> RequestResult.Error.RequestError.InvalidCredentials
                        else -> RequestResult.Error.RequestError.UnknownLoginResponse(body)
                    }
                },
            )

            when (result) {
                is RequestResult.Success -> {
                    eventChannel.send(Event.TestSuccess)
                }
                is RequestResult.Error -> {
                    eventChannel.send(Event.TestFailure(result))
                }
            }
        }

        job.invokeOnCompletion { e ->
            if (e !is CancellationException) {
                _isTesting.value = false
                testJob = null
            }
        }

        testJob = job
    }

    sealed class Event {
        data class TestFailure(val error: RequestResult.Error) : Event()
        data object TestSuccess : Event()
    }
}
