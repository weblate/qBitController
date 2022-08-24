package dev.bartuzen.qbitcontroller.network

import dev.bartuzen.qbitcontroller.data.TorrentSort
import dev.bartuzen.qbitcontroller.model.PieceState
import dev.bartuzen.qbitcontroller.model.Torrent
import dev.bartuzen.qbitcontroller.model.TorrentFile
import dev.bartuzen.qbitcontroller.model.TorrentProperties
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TorrentService {
    @FormUrlEncoded
    @POST("api/v2/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<String>

    @GET("api/v2/torrents/info")
    suspend fun getTorrentList(
        @Query("hashes") hashes: String? = null,
        @Query("sort") torrentSort: TorrentSort? = null
    ): Response<List<Torrent>>

    @GET("api/v2/torrents/files")
    suspend fun getFiles(@Query("hash") hash: String): Response<List<TorrentFile>>

    @POST("api/v2/torrents/pause")
    suspend fun pauseTorrent(@Query("hashes") hashes: String): Response<String>

    @POST("api/v2/torrents/resume")
    suspend fun resumeTorrent(@Query("hashes") hashes: String): Response<String>

    @GET("api/v2/torrents/pieceStates")
    suspend fun getTorrentPieces(@Query("hash") hash: String): Response<List<PieceState>>

    @GET("api/v2/torrents/properties")
    suspend fun getTorrentProperties(@Query("hash") hash: String): Response<TorrentProperties>
}