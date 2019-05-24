package org.ro.to

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
        @Optional val value: Any? = null
) : TransferObject {

    @Serializer(forClass = Value::class)
    companion object : KSerializer<Value> {

        override val descriptor: SerialDescriptor =
                StringDescriptor.withName("Value")

        override fun serialize(output: Encoder, obj: Value) {
            console.log("[Value.serialize] ${obj.value}")
        }

        override fun deserialize(input: Decoder): Value {
            console.log("[Value.deserialize] class ${input::class}") 
            
            var result: Any? = asNull(input)
            if (result == null) {
                result = asString(input)
            }
            if (result == null) {
                result = asInt(input)
            }
            if (result == null) {
                result = asLong(input)
            }
            if (result == null) {
                result = asLink(input)
            }
            return Value(result)
        }

        private fun asLink(input: Decoder) : Link? {
            var result: Link? = null
            try {
                result = input.decodeSerializableValue(Link.serializer())
            } catch (jpe: JsonParsingException) {
                console.log("[Value.deserialize] input != Link")
            }
            return result
        }

        private fun asLong(input: Decoder) : Long? {
            var result: Long? = null
            try {
                result = input.decodeLong()
            } catch (jpe: JsonParsingException) {
                console.log("[Value.deserialize] input != Long")
            }
            return result
        }

        private fun asInt(input: Decoder) : Int? {
            var result: Int? = null
            try {
                result = input.decodeInt()
            } catch (jpe: JsonParsingException) {
                console.log("[Value.deserialize] input != Int")
            }
            return result
        }
        private fun asString(input: Decoder) : String? {
            var result: String? = null
            try {
                result = input.decodeString()
            } catch (jpe: JsonParsingException) {
                console.log("[Value.deserialize] input != String")
            }
            return result
        }

        private fun asNull(input: Decoder) : Any? {
            var result: Any? = null
            try {
                result = input.decodeNull()
            } catch (jpe: JsonParsingException) {
                console.log("[Value.deserialize] input != null")
            }
            return result
        }
    }
    
}