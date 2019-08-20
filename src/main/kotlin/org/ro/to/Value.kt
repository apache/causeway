package org.ro.to

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.internal.makeNullable
import kotlinx.serialization.json.*

/**
 *  Custom data structure to handle 'untyped' value in Member and Property.
 *  "value" can either be:
 *  @Item 'null'
 *  @Item Int with format "int"
 *  @Item Long with format "utc-millisec"
 *  @Item String
 *  @Item Link
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
                when {
                    jse.isNull -> {
                        return Value(null)
                    }
                    isInt(jse.content) -> {
                        return Value(jse.content.toInt())
                    }
                    isLong(jse.content) -> {
                        return Value(jse.content.toLong())
                    }
                    else -> {
                        return Value(jse.content)
                    }
                }
            } catch (jetme: JsonElementTypeMismatchException) {
//                console.log("[Value.deserialize] JsonElementTypeMismatchException: $jse")
                return Value(asLink(jse.toString()))
            }
        }


        private fun isInt(raw: String): Boolean {
            try {
                raw.toInt()
                return true
            } catch (nfe: NumberFormatException) {
                return false
            }
        }

        private fun isLong(raw: String): Boolean {
            try {
                raw.toLong()
                return true
            } catch (nfe: NumberFormatException) {
                return false
            }
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
