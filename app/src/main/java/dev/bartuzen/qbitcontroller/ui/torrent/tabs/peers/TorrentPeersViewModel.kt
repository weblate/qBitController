package dev.bartuzen.qbitcontroller.ui.torrent.tabs.peers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bartuzen.qbitcontroller.data.SettingsManager
import dev.bartuzen.qbitcontroller.data.repositories.torrent.TorrentPeersRepository
import dev.bartuzen.qbitcontroller.model.ServerConfig
import dev.bartuzen.qbitcontroller.model.TorrentPeer
import dev.bartuzen.qbitcontroller.network.RequestResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TorrentPeersViewModel @Inject constructor(
    settingsManager: SettingsManager,
    private val repository: TorrentPeersRepository
) : ViewModel() {
    private val _torrentPeers = MutableStateFlow<List<TorrentPeer>?>(null)
    val torrentPeers = _torrentPeers.asStateFlow()

    private val eventChannel = Channel<Event>()
    val eventFlow = eventChannel.receiveAsFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    var isInitialLoadStarted = false

    val autoRefreshInterval = settingsManager.autoRefreshInterval.flow

    private fun updatePeers(serverConfig: ServerConfig, torrentHash: String) = viewModelScope.launch {
        when (val result = repository.getPeers(serverConfig, torrentHash)) {
            is RequestResult.Success -> {
                _torrentPeers.value = result.data.peers.values.toList()
            }
            is RequestResult.Error -> {
                eventChannel.send(Event.Error(result))
            }
        }
    }

    fun loadPeers(serverConfig: ServerConfig, torrentHash: String) {
        if (!isLoading.value) {
            _isLoading.value = true
            updatePeers(serverConfig, torrentHash).invokeOnCompletion {
                _isLoading.value = false
            }
        }
    }

    fun refreshPeers(serverConfig: ServerConfig, torrentHash: String) {
        if (!isRefreshing.value) {
            _isRefreshing.value = true
            updatePeers(serverConfig, torrentHash).invokeOnCompletion {
                _isRefreshing.value = false
            }
        }
    }

    fun addPeers(serverConfig: ServerConfig, torrentHash: String, peers: List<String>) = viewModelScope.launch {
        when (val result = repository.addPeers(serverConfig, torrentHash, peers)) {
            is RequestResult.Success -> {
                eventChannel.send(Event.PeersAdded)
            }
            is RequestResult.Error -> {
                if (result is RequestResult.Error.ApiError && result.code == 400) {
                    eventChannel.send(Event.PeersInvalid)
                } else {
                    eventChannel.send(Event.Error(result))
                }
            }
        }
    }

    fun banPeers(serverConfig: ServerConfig, peers: List<String>) = viewModelScope.launch {
        when (val result = repository.banPeers(serverConfig, peers)) {
            is RequestResult.Success -> {
                eventChannel.send(Event.PeersBanned)
            }
            is RequestResult.Error -> {
                eventChannel.send(Event.Error(result))
            }
        }
    }

    sealed class Event {
        data class Error(val error: RequestResult.Error) : Event()
        object PeersInvalid : Event()
        object PeersAdded : Event()
        object PeersBanned : Event()
    }
}
