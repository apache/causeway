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

package org.apache.isis.client.kroviz.to

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ActionSemantics(val type: String) {
    IDEMPOTENT("idempotent"),
    NON_IDEMPOTENT("nonIdempotent"),
    NON_IDEMPOTENT_ARE_YOU_SURE("nonIdempotentAreYouSure")
}

@Serializable
data class DomainType(
        val links: List<Link>,
        val canonicalName: String,
        val members: List<Link>,
        val typeActions: List<Link>,
        val extensions: Extensions
) : TransferObject

@Serializable
data class DomainTypes(val links: List<Link> = emptyList(),
                       val values: List<Link> = emptyList(),
                       val extensions: Extensions? = null
) : TransferObject

@Serializable
data class Extensions(val oid: String = "",
                      val isService: Boolean = false,
                      val isPersistent: Boolean = false,
                      val menuBar: String? = MenuBarPosition.PRIMARY.position,
                      val actionSemantics: String? = null,
                      val actionType: String = "",
                      @SerialName("x-isis-format") val xIsisFormat: String? = null,
                      val friendlyName: String = "",
                      val collectionSemantics: String? = null,
                      val pluralName: String = "",
                      val description: String = ""
) : TransferObject

@Serializable
data class HttpError(
        val httpStatusCode: Int,
        val message: String,
        val detail: HttpErrorDetail? = null
) : TransferObject

@Serializable
data class HttpErrorDetail(
        val className: String,
        val message: String? = null,
        val element: List<String>,
        var causedBy: HttpErrorDetail? = null
) : TransferObject

@Serializable
data class Links(
        @SerialName("links") val content: List<Link> = emptyList()
) : TransferObject

enum class MemberType(val type: String) {
    ACTION("action"),
    PROPERTY("property"),
    COLLECTION("collection")
}

enum class MenuBarPosition(val position: String) {
    PRIMARY("PRIMARY"),
    SECONDARY("SECONDARY"),
    TERNARY("TERNARY")
}

enum class Method(val operation: String) {
    GET("GET"),
    PUT("PUT"),
    POST("POST")
//    DELETE("DELETE")  not used - Apache Isis defines delete operations on DomainObjects
}

@Serializable
data class Property(val id: String = "",
                    val memberType: String = "",
                    val links: List<Link> = emptyList(),
                    val optional: Boolean? = null,
                    val title: String? = null,
                    val value: Value? = null,
                    val extensions: Extensions? = null,
                    val format: String? = null,
                    val disabledReason: String? = null,
                    val parameters: List<Parameter> = emptyList(),
                    val maxLength: Int = 0
) : TransferObject

enum class RelType(val type: String) {
    SELF("self"),
    UP("up"),
    DESCRIBEDBY("describedby")
}

@Serializable
data class Restful(val links: List<Link> = emptyList(),
                   val extensions: Extensions
) : TransferObject

@Serializable
data class ResultList(
        val links: List<Link> = emptyList(),
        val resulttype: String = ResultType.LIST.type,
        val result: ResultListResult? = null
) : TransferObject

@Serializable
data class ResultListResult(
        val value: List<Link> = emptyList(),
        val links: List<Link> = emptyList(),
        val extensions: Extensions? = null
) : IResult

@Serializable
data class ResultObject(
        val links: List<Link> = emptyList(),
        val resulttype: String = ResultType.DOMAINOBJECT.type,
        val result: ResultObjectResult? = null
) : IResult

@Serializable
data class ResultObjectResult(
        val links: List<Link> = emptyList(),
        val extensions: Extensions? = null,
        val title: String = "",
        val domainType: String = "",
        val instanceId: Int,
        val members: Map<String, Member> = emptyMap()
) : IResult

enum class ResultType(val type: String) {
    LIST("list"),
    SCALARVALUE("scalarvalue"),
    DOMAINOBJECT("domainobject")
}

@Serializable
data class ResultValue(
        val links: List<Link> = emptyList(),
        val resulttype: String = ResultType.SCALARVALUE.type,
        val result: ResultValueResult? = null
) : TransferObject

@Serializable
data class ResultValueResult(
        val value: Value? = null,
        val links: List<Link> = emptyList(),
        val extensions: Extensions? = null
) : IResult

@Serializable
data class Service(val links: List<Link> = emptyList(),
                   val extensions: Extensions? = null,
                   val title: String = "",
                   val serviceId: String = "",
                   val members: Map<String, Member> = emptyMap()
) : TransferObject

@Serializable
data class User(val userName: String = "",
                val roles: List<String> = emptyList(),
                val links: List<Link> = emptyList(),
                val extensions: Extensions? = null
) : TransferObject

@Serializable
data class Version(val links: List<Link> = emptyList(),
                   val specVersion: String = "",
                   val implVersion: String = "",
                   val optionalCapabilities: Map<String, String> = emptyMap(),
                   val extensions: Extensions? = null
) : TransferObject

