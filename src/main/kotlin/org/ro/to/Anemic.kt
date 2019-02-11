package org.ro.to

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Method(val operation: String) {
    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    DELETE("DELETE")
}

enum class MenuBarPosition(val position: String) {
    PRIMARY("PRIMARY"),
    SECONDARY("SECONDARY"),
    TERNARY("TERNARY")
}

enum class MemberType(val type: String) {
    ACTION("action"),
    PROPERTY("property")
}

enum class ActionSemantics(val type: String) {
    IDEMPOTENT("idempotent"),
    NON_IDEMPOTENT("nonIdempotent"),
    NON_IDEMPOTENT_ARE_YOU_SURE("nonIdempotentAreYouSure")
}

@Serializable
data class Extensions(@Optional val oid: String = "",
                      @Optional val isService: Boolean = false,
                      @Optional val isPersistent: Boolean = false,
                      @Optional val menuBar: String? = MenuBarPosition.PRIMARY.position,
                      @Optional val actionSemantics: String? = null,
                      @Optional val actionType: String = "",
                      @Optional @SerialName("x-isis-format") val xIsisFormat: String? = null,
                      @Optional val friendlyName: String = "",
                      @Optional val collectionSemantics: String? = null)

@Serializable
data class Restful(val extensions: Extensions? = null,
                   val links: List<Link>? = null)

@Serializable
data class Service(@SerialId(1) val links: List<Link> = emptyList(),
                   @SerialId(2) val extensions: Extensions? = null,
                   @SerialId(3) val title: String = "",
                   @SerialId(4) val serviceId: String = "",
                   @SerialId(5) val members: List<Member> = emptyList())

@Serializable
data class Argument(val key: String = "",
                    val value: String = "",  //JsonObject?
                    val potFileName: String = "")

@Serializable // same as -> Services?
data class Result(val extensions: Extensions? = null,
                  val links: List<Link> = emptyList(),
                  val value: List<Link> = emptyList())

@Serializable // same as -> Services?
data class ResultList(val links: List<Link> = emptyList(),
                      val resultType: String? = null,
                      val result: Result? = null)