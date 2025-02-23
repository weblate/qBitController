package dev.bartuzen.qbitcontroller.model.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class PeerFilesDeserializer : JsonDeserializer<List<String>>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): List<String> {
        return parser?.valueAsString.takeIf { it?.isNotEmpty() == true }?.split("\n") ?: emptyList()
    }
}
