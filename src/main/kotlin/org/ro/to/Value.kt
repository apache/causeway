package org.ro.to

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.internal.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonException
import kotlinx.serialization.json.content

/**
 *  Custom data structure to handle 'untyped' value in Member, Property, Parameter
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
                val nss = JsonElement.serializer().nullable
                jse = decoder.decode(nss)!!
                val jsct = jse.content
                when {
                    jse.isNull -> {
                        return Value(null)
                    }
                    isInt(jsct) -> {
                        return Value(jsct.toInt())
                    }
                    isLong(jsct) -> {
                        return Value(jsct.toLong())
                    }
                    else -> {
                        return Value(jsct)
                    }
                }
            } catch (je: JsonException) {
                val linkStr = jse.toString()
                val link = Json.parse(Link.serializer(), linkStr)
                return Value(link)
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

    }

}
