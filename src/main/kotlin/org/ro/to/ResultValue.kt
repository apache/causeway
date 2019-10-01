package org.ro.to

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.internal.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonException
import kotlinx.serialization.json.content

/**
 *  Custom data structure to handle 'untyped' value in Argument
 *  "value" can either be:
 *  @Item String or
 *  @Item List<Link>
 */
@Serializable
data class ResultValue(
        @ContextualSerialization @SerialName("value") val content: Any? = null
) : TransferObject {

    @Serializer(forClass = ResultValue::class)
    companion object : KSerializer<ResultValue> {
        override fun serialize(encoder: Encoder, obj: ResultValue) {
            // Not required yet
        }

        override val descriptor: SerialDescriptor =
                StringDescriptor.withName("ResultValue")

        @UnstableDefault
        override fun deserialize(decoder: Decoder): ResultValue {
            var jse: JsonElement? = null
            try {
                val nss = JsonElement.serializer().nullable
                jse = decoder.decode(nss)!!
                val jsct = jse.content
                when {
                    jse.isNull -> {
                        return ResultValue(null)
                    }
                    else -> {
                        return ResultValue(jsct)
                    }
                }
            } catch (je: JsonException) {
                val linkStr = jse.toString()
                val links = Json.parse(Links.serializer(), linkStr)
                return ResultValue(links)
            }
        }

    }

}
