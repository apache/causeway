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
        @Optional @ContextualSerialization @SerialName("value") val content: Any? = null
) : TransferObject {

    @Serializer(forClass = Value::class)
    companion object : KSerializer<Value> {
        override fun serialize(encoder: Encoder, obj: Value) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override val descriptor: SerialDescriptor =
                StringDescriptor.withName("Value")

        override fun deserialize(decoder: Decoder): Value {
            //TODO can function and type be passed on in order to be less verbose?

            var result: Any? = asNull(decoder)
            if (result == null) {
                result = asInt(decoder)
            }
            if (result == null) {
                result = asLong(decoder)
            }
            // Sequence is important, Link has to be checked before String
            if (result == null) {
                result = asLink(decoder)
            }
            if (result == null) {
                result = asString(decoder)
            }
            return Value(result)
        }

        private fun asLink(decoder: Decoder): Link? {
            var result: Link? = null
            try {
                result = decoder.decodeSerializableValue(Link.serializer())
            } catch (jpe: JsonParsingException) {
            }
            return result
        }

        private fun asLong(decoder: Decoder): Long? {
            var result: Long? = null
            try {
                result = decoder.decodeLong()
            } catch (jpe: JsonParsingException) {
            } catch (nfe: NumberFormatException) {
            }
            return result
        }

        private fun asInt(decoder: Decoder): Int? {
            var result: Int? = null
            try {
                result = decoder.decodeInt()
            } catch (jpe: JsonParsingException) {
            } catch (nfe: NumberFormatException) {
            }
            return result
        }

        private fun asString(decoder: Decoder): String? {
            var result: String? = null
            try {
                result = decoder.decodeString()
            } catch (jpe: JsonParsingException) {
                console.log("[Value.asString] Decoder.decodeString didn't find String")
                result = decodeStringMayBeWrong(decoder)
            }
            return result
        }

        private fun asNull(decoder: Decoder): Any? {
            var result: Any? = null
            try {
                result = decoder.decodeNull()
            } catch (jpe: JsonParsingException) {
            }
            return result
        }

        private fun decodeStringMayBeWrong(decoder: Decoder): String {
            val keyword = "value"
            val nl = "\\n"
            val delim = ":"

            val inputAsJsonStr = JSON.stringify(decoder.asJsObject())
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
            val result = unQuoted.trim().split(",")[0]
            console.log("[${this::class}.decodeStringMayBeWrong] found String: $result")

            return result
        }
    }

}
