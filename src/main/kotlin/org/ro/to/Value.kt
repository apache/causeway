package org.ro.to

import kotlinext.js.asJsObject
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.JsonParsingException
import org.ro.core.TransferObject

/**
 *  Custom data structure to handle 'untyped' value in Property (and Member).
 *  "value" can either be:
 *  @Item 'null'
 *  @Item String
 *  @Item Link
 *  @Item Long with format "utc-millisec"
 *  @Item Int with format "int"
 */
@Serializable
data class Value(
        @Optional @SerialName("value") val content: Any? = null
) : TransferObject {

    @Serializer(forClass = Value::class)
    companion object : KSerializer<Value> {

        override val descriptor: SerialDescriptor =
                StringDescriptor.withName("Value")

        override fun deserialize(input: Decoder): Value {
            //TODO can function and type be passed on in order to be less verbose?

            var result: Any? = asNull(input)
            if (result == null) {
                result = asInt(input)
            }
            if (result == null) {
                result = asLong(input)
            }
            // Sequence is important, Link has to be checked before String
            if (result == null) {
                result = asLink(input)
            }
            if (result == null) {
                result = asString(input)
            }
            return Value(result)
        }

        private fun asLink(input: Decoder): Link? {
            var result: Link? = null
            try {
                result = input.decodeSerializableValue(Link.serializer())
            } catch (jpe: JsonParsingException) {
            }
            return result
        }

        private fun asLong(input: Decoder): Long? {
            var result: Long? = null
            try {
                result = input.decodeLong()
            } catch (jpe: JsonParsingException) {
            } catch (nfe: NumberFormatException) {
            }
            return result
        }

        private fun asInt(input: Decoder): Int? {
            var result: Int? = null
            try {
                result = input.decodeInt()
            } catch (jpe: JsonParsingException) {
            } catch (nfe: NumberFormatException) {
            }
            return result
        }

        private fun asString(input: Decoder): String? {
            var result: String? = null
            try {
                result = input.decodeString()
            } catch (jpe: JsonParsingException) {
                console.log("[Value.asString] input:Decoder can not find String")
                result = decodeStringMayBeWrong(input)
            }
            return result
        }

        private fun asNull(input: Decoder): Any? {
            var result: Any? = null
            try {
                result = input.decodeNull()
            } catch (jpe: JsonParsingException) {
            }
            return result
        }

        private fun decodeStringMayBeWrong(input: Decoder): String {
            val keyword = "value"
            val nl = "\\n"
            val delim = ":"

            val inputAsJsonStr = JSON.stringify(input.asJsObject())
            val lines = inputAsJsonStr.split("{")
            var source: String = ""
            for (l in lines) {
                if (l.contains(keyword) && l.contains(delim) && (l.contains(nl))) {
                    source = l
                }
            }
//            console.log("[${this::class}.decodeStringMayBeWrong] source $source")

            val elements = source.split(nl)
            var keyValue: String = ""
            for (e in elements) {
                if (e.contains(keyword)) {
                    keyValue = e
                }
            }
//            console.log("[${this::class}.decodeStringMayBeWrong] keyValue $keyValue")

            val unEscaped = keyValue.replace("\\", "")
            val keywordRemoved = unEscaped.replaceFirst(keyword, "")
            val delimRemoved = keywordRemoved.replaceFirst(delim, "")
            val unQuoted = delimRemoved.replace("\"", "")
            val result = unQuoted.trim()
            console.log("[${this::class}.decodeStringMayBeWrong] found String: $result")

            return result
        }
    }

}