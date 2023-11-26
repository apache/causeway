/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.client.kroviz.to

import kotlinx.serialization.*
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

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
    //IMPROVE: make content immutable (again) and handle property edits e.g. via a wrapper
    @Contextual @SerialName("value") var content: Any? = null,
) : TransferObject {

    @ExperimentalSerializationApi
    @Serializer(forClass = Value::class)
    companion object : KSerializer<Value> {

        // @ExperimentalSerializationApi
        override fun deserialize(decoder: Decoder): Value {
            val nss = JsonElement.serializer().nullable
            val jse: JsonElement? = decoder.decodeNullableSerializableValue(nss)!!
            val result: Value = when {
                jse == null -> Value(null)
                isNumeric(jse) -> Value(jse.jsonPrimitive.content.toLong())
                jse is JsonObject -> toLink(jse)
                else -> toString(jse)
            }
            return result
        }

        private fun toString(jse: JsonElement): Value {
            val s = jse.jsonPrimitive.content
            return Value(s)
        }

        private fun toLink(jse: JsonElement): Value {
            val linkStr = jse.toString()
            val link = Json.decodeFromString<Link>(linkStr)
            return Value(link)
        }

        private fun isNumeric(jse: JsonElement): Boolean {
            return try {
                jse.jsonPrimitive.content.toLong()
                true
            } catch (nfe: NumberFormatException) {
                false
            } catch (ie: IllegalArgumentException) {
                false
            }
        }

        override fun serialize(encoder: Encoder, value: Value) {
            TODO("Not yet implemented")
        }
    }

}
