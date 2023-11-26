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

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Vega5(
    @SerialName("\$schema") val schema: String,
    val description: String? = null,
    val width: Int,
    val height: Int,
    val padding: Float? = null,
    val data: List<Data> = emptyList(),
    val signals: List<Signal> = emptyList(),
    val scales: List<Scale> = emptyList(),
    val axes: List<Axis> = emptyList(),
    val marks: List<Mark> = emptyList(),
) : TransferObject

@Serializable
data class Data(
    val name: String,
    val values: List<VegaValue> = emptyList(),
    val source: String? = null,
    val transform: List<Transformation> = emptyList()
) : TransferObject

@Serializable
data class Transformation(
    val type: String,
    val shape: String? = null,
    val expr: String? = null,
    val key: String? = null,
    val parentKey: String? = null,
    val method: String? = null,
    val separation: Boolean = true,
    val size: List<Size> = emptyList(),
) : TransferObject

@Serializable
data class Size(
    val signal: String
) : TransferObject

@Serializable
data class VegaValue(
    val category: String? = null,
    val amount: Float? = null,
    val id: String? = null,
    val parent: String? = null,
    val title: String? = null,
) : TransferObject

@Serializable
data class Signal(
    val name: String,
    val value: JsonObject? = null,
    val on: List<Event> = emptyList()
) : TransferObject

@Serializable
data class Event(
    val events: String,
    val update: String? = null
) : TransferObject

@Serializable
data class Scale(
    val name: String,
    val type: String? = null,
    val domain: Domain,
    val nice: Boolean = true,
    val range: String,
    val padding: Float? = null,
    val round: Boolean = true
) : TransferObject

@Serializable
data class Domain(
    val data: String,
    val field: String
) : TransferObject

@Serializable
data class Axis(
    val orient: String,
    val scale: String
) : TransferObject

@Serializable
data class Mark(
    val type: String,
    val from: From? = null,
    val encode: Encoding
) : TransferObject

@Serializable
data class From(
    val data: String
) : TransferObject

@Serializable
data class Encoding(
    val enter: Enter,
    val update: Update? = null,
    val hover: Fill? = null,
) : TransferObject

@Serializable
data class Update(
    val x: UpdateGroup? = null,
    val y: UpdateGroup? = null,
    val text: UpdateGroup? = null,
    val fill: StringValue? = null,
    val fillOpacity: List<FillOpacity>? = null,
) : TransferObject

@Serializable
data class FillOpacity(
    val test: String? = null,
    val value: Int? = null
) : TransferObject

@Serializable
data class UpdateGroup(
    val scale: String? = null,
    val signal: String? = null,
    val band: Float? = null,
    val offset: Float? = null,
) : TransferObject

@Serializable
data class Fill(
    val fill: StringValue? = null
) : TransferObject

@Serializable
data class Enter(
    val stroke: StringValue? = null,
    val text: Field? = null,
    val x: Field? = null,
    val y: Field? = null,
    val dx: IntValue? = null,
    val dy: IntValue? = null,
    val align: StringValue? = null,
    val baseline: StringValue? = null,
    val fill: StringValue? = null,
    val width: Field? = null,
    val height: IntValue? = null,
    val y2: Field? = null,
    val path: Path? = null,
) : TransferObject

@Serializable
data class Path(
    val field: String,
) : TransferObject

@Serializable
data class Field(
    val scale: String? = null,
    val field: String? = null,
    val band: Int? = null,
    val value: Int? = null,
) : TransferObject

@Serializable
data class StringValue(
    @Contextual @SerialName("value") val content: String
) : TransferObject

@Serializable
data class IntValue(
    @Contextual @SerialName("value") val content: Int
) : TransferObject
