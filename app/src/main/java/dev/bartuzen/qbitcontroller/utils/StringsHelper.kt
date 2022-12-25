package dev.bartuzen.qbitcontroller.utils

import android.content.Context
import dev.bartuzen.qbitcontroller.R
import dev.bartuzen.qbitcontroller.model.TorrentFilePriority
import dev.bartuzen.qbitcontroller.model.TorrentState
import dev.bartuzen.qbitcontroller.network.RequestResult
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.roundToInt

fun formatBytes(context: Context, byte: Long) = when (byte) {
    in 0 until 1024 -> context.getString(R.string.size_bytes, byte.toString())
    in 1024 until 1024 * 1024 -> {
        val text = (byte.toDouble() / 1024).floorToDecimal(1).toString()
        context.getString(R.string.size_kibibytes, text)
    }
    in 1024 * 1024 until 1024 * 1024 * 1024 -> {
        val text = (byte.toDouble() / (1024 * 1024)).floorToDecimal(1).toString()
        context.getString(R.string.size_mebibytes, text)
    }
    else -> {
        val text = (byte.toDouble() / (1024 * 1024 * 1024)).floorToDecimal(2).toString()
        context.getString(R.string.size_gibibytes, text)
    }
}

fun formatBytesPerSecond(context: Context, byte: Long) = when (byte) {
    in 0 until 1024 -> context.getString(R.string.speed_bytes_per_second, byte.toString())
    in 1024 until 1024 * 1024 -> {
        val text = (byte.toDouble() / 1024).floorToDecimal(1).toString()
        context.getString(R.string.speed_kibibytes_per_second, text)
    }
    in 1024 * 1024 until 1024 * 1024 * 1024 -> {
        val text = (byte.toDouble() / (1024 * 1024)).floorToDecimal(1).toString()
        context.getString(R.string.speed_mebibytes_per_second, text)
    }
    else -> {
        val text = (byte.toDouble() / (1024 * 1024 * 1024)).floorToDecimal(2).toString()
        context.getString(R.string.speed_gibibytes_per_second, text)
    }
}

fun formatSeconds(context: Context, seconds: Int) = when (seconds) {
    in 0 until 60 -> {
        context.getString(R.string.eta_seconds, seconds.toString())
    }
    in 60 until 60 * 60 -> {
        val remainder = seconds % 60
        val minutes = (seconds / 60).toString()
        if (remainder != 0) {
            context.getString(R.string.eta_minutes_seconds, minutes, remainder.toString())
        } else {
            context.getString(R.string.eta_minutes, minutes)
        }
    }
    in 60 * 60 until 60 * 60 * 60 -> {
        val remainder = ((seconds % (60 * 60)) / 60.0).roundToInt()
        val hours = (seconds / (60 * 60)).toString()
        if (remainder != 0) {
            context.getString(R.string.eta_hours_minutes, hours, remainder.toString())
        } else {
            context.getString(R.string.eta_hours, hours)
        }
    }
    in 60 * 60 * 60 until 8640000 -> {
        val remainder = ((seconds % (24 * 60 * 60)) / (60.0 * 60)).roundToInt()
        val days = (seconds / (24 * 60 * 60)).toString()
        if (remainder != 0) {
            context.getString(R.string.eta_days_hours, days, remainder.toString())
        } else {
            context.getString(R.string.eta_days, days)
        }
    }
    else -> null
}

fun formatTorrentState(context: Context, state: TorrentState) = context.getString(
    when (state) {
        TorrentState.ERROR -> R.string.torrent_status_error
        TorrentState.MISSING_FILES -> R.string.torrent_status_missing_files
        TorrentState.UPLOADING -> R.string.torrent_status_seeding
        TorrentState.PAUSED_UP, TorrentState.PAUSED_DL -> R.string.torrent_status_paused
        TorrentState.QUEUED_UP, TorrentState.QUEUED_DL -> R.string.torrent_status_queued
        TorrentState.STALLED_UP, TorrentState.STALLED_DL -> R.string.torrent_status_stalled
        TorrentState.CHECKING_UP, TorrentState.CHECKING_DL, TorrentState.CHECKING_RESUME_DATA ->
            R.string.torrent_status_checking
        TorrentState.FORCED_UP -> R.string.torrent_status_force_seeding
        TorrentState.ALLOCATING -> R.string.torrent_status_allocating_space
        TorrentState.DOWNLOADING -> R.string.torrent_status_downloading
        TorrentState.META_DL -> R.string.torrent_status_downloading_metadata
        TorrentState.FORCED_DL -> R.string.torrent_status_force_downloading
        TorrentState.MOVING -> R.string.torrent_status_moving
        TorrentState.UNKNOWN -> R.string.torrent_status_unknown
    }
)

fun formatFilePriority(context: Context, priority: TorrentFilePriority) = context.getString(
    when (priority) {
        TorrentFilePriority.DO_NOT_DOWNLOAD -> R.string.torrent_file_priority_do_not_download
        TorrentFilePriority.NORMAL -> R.string.torrent_file_priority_normal
        TorrentFilePriority.HIGH -> R.string.torrent_file_priority_high
        TorrentFilePriority.MAXIMUM -> R.string.torrent_file_priority_maximum
    }
)

fun getErrorMessage(context: Context, error: RequestResult.Error) = context.getString(
    when (error) {
        RequestResult.Error.RequestError.Banned -> R.string.error_banned
        RequestResult.Error.RequestError.CannotConnect -> R.string.error_cannot_connect
        RequestResult.Error.RequestError.InvalidCredentials -> R.string.error_invalid_credentials
        RequestResult.Error.RequestError.Timeout -> R.string.error_timeout
        RequestResult.Error.RequestError.Unknown -> R.string.error_unknown
        RequestResult.Error.RequestError.UnknownHost -> R.string.error_unknown_host
        is RequestResult.Error.ApiError -> R.string.error_unknown
    }
)

fun formatDate(epochSecond: Long): String = Instant.ofEpochSecond(epochSecond)
    .atZone(ZoneId.systemDefault())
    .format(
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
    )
