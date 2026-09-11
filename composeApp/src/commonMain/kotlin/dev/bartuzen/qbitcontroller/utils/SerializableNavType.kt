package dev.bartuzen.qbitcontroller.utils

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.io.encoding.Base64

inline fun <reified T : Any> serializableNavType(): NavType<T> {
    val serializer = serializer<T>()
    return object : NavType<T>(false) {
        override fun put(bundle: SavedState, key: String, value: T) {
            val json = Json.encodeToString(serializer, value)
            bundle.write { putString(key, Base64.encode(json.encodeToByteArray())) }
        }

        override fun get(bundle: SavedState, key: String): T {
            return Json.decodeFromString(serializer, Base64.decode(bundle.read { getString(key) }).decodeToString())
        }

        override fun parseValue(value: String): T {
            return Json.decodeFromString(serializer, Base64.decode(value).decodeToString())
        }

        override fun serializeAsValue(value: T): String {
            return Base64.encode(Json.encodeToString(serializer, value).encodeToByteArray())
        }
    }
}
