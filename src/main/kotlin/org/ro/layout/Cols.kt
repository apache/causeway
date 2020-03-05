package org.ro.layout

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.internal.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonException
import kotlinx.serialization.json.JsonObject

@UseExperimental(ImplicitReflectionSerializer::class)
@Serializable
data class Cols(val content: Any? = null) {

    fun getCol(): Col {
        return colList.first()
    }

    val colList = mutableListOf<Col>()

    @Serializer(forClass = Cols::class)
    companion object : KSerializer<Cols> {
        override fun serialize(encoder: Encoder, obj: Cols) {
            // Not required yet
        }

        override val descriptor: SerialDescriptor =
                StringDescriptor.withName("Cols")

        @UnstableDefault
        override fun deserialize(decoder: Decoder): Cols {
            val cols = Cols()
            try {
                val serializer = JsonObject.serializer().nullable
                val jsObj = decoder.decode(serializer)!!
                console.log("[Cols.deserialize] -> values")
                console.log(jsObj.values)
                console.log("[Cols.deserialize] -> content")
                console.log(jsObj.content)
                val jsSet = jsObj.entries
                val jit = jsSet.iterator()
                while (jit.hasNext()) {
                    val c = jit.next().value as JsonObject
                    val col = Json.parse(Col.serializer(), c.toString())
                    cols.colList.add(col)
                }
                console.log("[Cols.deserialize] cols: ${cols.colList.size}")
            } catch (je: JsonException) {
                console.log("[Cols.deserialize] -> JsonException")
            }
            return cols
        }
    }

}
