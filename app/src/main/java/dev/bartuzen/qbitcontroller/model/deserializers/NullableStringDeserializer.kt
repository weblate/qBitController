package dev.bartuzen.qbitcontroller.model.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class NullableStringDeserializer : JsonDeserializer<String?>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): String? {
        return parser?.valueAsString?.ifEmpty { null }
    }
}
