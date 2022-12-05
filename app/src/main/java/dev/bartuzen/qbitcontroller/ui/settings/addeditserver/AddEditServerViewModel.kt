package dev.bartuzen.qbitcontroller.ui.settings.addeditserver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bartuzen.qbitcontroller.data.SettingsManager
import dev.bartuzen.qbitcontroller.model.Protocol
import dev.bartuzen.qbitcontroller.model.ServerConfig
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditServerViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {
    fun addServer(
        name: String?,
        protocol: Protocol,
        host: String,
        port: Int?,
        path: String?,
        username: String,
        password: String
    ) = viewModelScope.launch {
        settingsManager.addServer(name, protocol, host, port, path, username, password)
    }

    fun editServer(serverConfig: ServerConfig) = viewModelScope.launch {
        settingsManager.editServer(serverConfig)
    }

    fun removeServer(serverConfig: ServerConfig) = viewModelScope.launch {
        settingsManager.removeServer(serverConfig)
    }
}
