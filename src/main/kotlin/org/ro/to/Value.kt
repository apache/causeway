package org.ro.to

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.JsonParsingException

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
            // No need to serialize this
        }

        override val descriptor: SerialDescriptor =
                StringDescriptor.withName("Value")

        override fun deserialize(decoder: Decoder): Value {
            //TODO can function and type be passed on in order to be less verbose?

            val raw = decoder.decodeString()
            console.log("[Value.deserialize] $raw")

            var result: Any? = null //asNull(raw)
            if (result == null) {
                result = asInt(raw)
            }
            if (result == null) {
                result = asLong(raw)
            }
            // Sequence is important, Link has to be checked before String
            if (result == null) {
                result = asLink(raw, decoder)
            }
            if (result == null) {
                result = raw // String
            }

            return Value(result)
        }

        private fun asInt(raw: String): Int? {
            var result: Int? = null
            try {
                result = raw.toInt()
            } catch (nfe: NumberFormatException) {
                console.log("not an Int")
            }
            return result
        }

        private fun asLong(raw: String): Long? {
            var result: Long? = null
            try {
                result = raw.toLong()
            } catch (nfe: NumberFormatException) {
                console.log("not a Long")
            }
            return result
        }

        private fun asLink(raw: String, decoder: Decoder): Link? {
            var result: Link? = null
            try {
                result = decoder.decodeSerializableValue(Link.serializer())
            } catch (jpe: JsonParsingException) {
            }
            return result
        }

        private fun logSource(decoder: Decoder) {
            val dynDec = decoder.asDynamic()
            val dynReader = dynDec["reader_0"]
            val dynSource = dynReader["source_0"]
            console.log(dynSource)
        }

    }

}
