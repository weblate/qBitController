package dev.bartuzen.qbitcontroller.model.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class PriorityDeserializer : JsonDeserializer<Int?>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): Int? {
        return parser?.intValue.takeIf { it != 0 }
    }
}
