package org.ro.generated

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class Restful(val extensions: Extensions,
                   val links: List<Link>?)

@Serializable
data class Services(val extensions: Extensions,
                    val links: List<Link>?,
                    val value: List<Link>?)

@Serializable
data class Extensions(@Optional val oid: String = "",
                      @Optional val isService: Boolean = false,
                      @Optional val isPersistent: Boolean = false,
                      @Optional val menuBar: String? = null,    // TODO use constants [PRIMARY, , etc.]
                      @Optional val actionSemantics: String? = null,
                      @Optional val actionType: String = "",
                      @Optional val xIsisFormat: String? = null,
                      @Optional val friendlyName: String = "",
                      @Optional val collectionSemantics: String? = null)

@Serializable
data class Link(val method: String = "",
                val rel: String = "",
                val href: String = "",
                val type: String = "",
                @Optional val title: String = "")


@Serializable
data class Menu(val extensions: Extensions,
                val members: List<Action>,
                val links: List<Link>?,
                val title: String = "",
                val serviceId: String = "")

@Serializable
open class Member(val id: String = "",
                  val memberType: String? = null,
//                  val value: Any? = null,
                  val format: String? = null,
                  val extensions: Extensions? = null,
                  val disabledReason: String? = null,
                  val optional: Boolean? = null)

@Serializable
data class Action(val id: String,
                  val memberType: String,
                  val links: List<Link>?,
                  val parameters: List<Parameter>,
                  val extensions: Extensions)   //TODO check how inheritance can be used with data class

@Serializable
data class Parameter(val id: String,
                     val num: Int = 0,
                     val description: String,
                     val name: String,
                     val choices: List<Link>,
                     val defaultChoice: Link)

