package org.ro.to

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.internal.makeNullable
import kotlinx.serialization.json.*

/**
 *  Custom data structure to handle 'untyped' value in Member and Property.
 *  "value" can either be:
 *  @Item 'null'
 *  @Item String
 *  @Item Link
 *  @Item Long with format "utc-millisec"
 *  @Item Int with format "int"
 */
@Serializable
data class Value(
        @ContextualSerialization @SerialName("value") val content: Any? = null
) : TransferObject {

    @Serializer(forClass = Value::class)
    companion object : KSerializer<Value> {
        override fun serialize(encoder: Encoder, obj: Value) {
            // Not required yet
        }

        override val descriptor: SerialDescriptor =
                StringDescriptor.withName("Value")

        @UnstableDefault
        override fun deserialize(decoder: Decoder): Value {
            var jse: JsonElement? = null
            try {
                val nss = makeNullable(JsonElement.serializer())
                jse = decoder.decode(nss)!!
//                console.log("[Value.deserialize] JsonElement: $jse")
                //TODO use when () to be more expressive
                if (jse.isNull) {
                    return Value(null)
                }
                val nStr = jse.content
                var result: Any? = null
                result = asInt(nStr)
                if (result == null) {
                    result = asLong(nStr)
                }
                if (result == null) {
                    result = nStr // String
                }
                return Value(result)
            } catch (jetme: JsonElementTypeMismatchException) {
//                console.log("[Value.deserialize] JsonElementTypeMismatchException: $jse")
                return Value(asLink(jse.toString()))
            }
        }

        private fun asInt(raw: String): Int? {
            var result: Int? = null
            try {
                result = raw.toInt()
            } catch (nfe: NumberFormatException) {
            }
            return result
        }

        private fun asLong(raw: String): Long? {
            var result: Long? = null
            try {
                result = raw.toLong()
            } catch (nfe: NumberFormatException) {
            }
            return result
        }

        @UnstableDefault
        private fun asLink(raw: String): Link? {
            var result: Link? = null
            try {
                result = Json.parse(Link.serializer(), raw)
            } catch (jpe: JsonParsingException) {
                console.log("[Value.asLink] $jpe")
                console.log("[Value.asLink] $raw")
            }
            return result
        }

    }

}
