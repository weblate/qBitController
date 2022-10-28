package dev.bartuzen.qbitcontroller.data.repositories

import dev.bartuzen.qbitcontroller.model.ServerConfig
import dev.bartuzen.qbitcontroller.network.RequestManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorrentRepository @Inject constructor(
    private val requestManager: RequestManager
) {
    suspend fun getTorrent(serverConfig: ServerConfig, hash: String) =
        requestManager.request(serverConfig) { service ->
            service.getTorrentList(hash)
        }

    suspend fun getFiles(serverConfig: ServerConfig, hash: String) =
        requestManager.request(serverConfig) { service ->
            service.getFiles(hash)
        }

    suspend fun getPieces(serverConfig: ServerConfig, hash: String) =
        requestManager.request(serverConfig) { service ->
            service.getTorrentPieces(hash)
        }

    suspend fun getProperties(serverConfig: ServerConfig, hash: String) =
        requestManager.request(serverConfig) { service ->
            service.getTorrentProperties(hash)
        }

    suspend fun deleteTorrent(serverConfig: ServerConfig, hash: String, deleteFiles: Boolean) =
        requestManager.request(serverConfig) { service ->
            service.deleteTorrents(hash, deleteFiles)
        }

    suspend fun pauseTorrent(serverConfig: ServerConfig, hash: String) =
        requestManager.request(serverConfig) { service ->
            service.pauseTorrents(hash)
        }

    suspend fun resumeTorrent(serverConfig: ServerConfig, hash: String) =
        requestManager.request(serverConfig) { service ->
            service.resumeTorrents(hash)
        }

    suspend fun getTrackers(serverConfig: ServerConfig, hash: String) =
        requestManager.request(serverConfig) { service ->
            service.getTorrentTrackers(hash)
        }

    suspend fun addTrackers(serverConfig: ServerConfig, hash: String, urls: String) =
        requestManager.request(serverConfig) { service ->
            service.addTorrentTrackers(hash, urls)
        }

    suspend fun deleteTrackers(serverConfig: ServerConfig, hash: String, urls: String) =
        requestManager.request(serverConfig) { service ->
            service.deleteTorrentTrackers(hash, urls)
        }
}