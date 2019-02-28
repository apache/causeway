package org.ro.to

import kotlinx.serialization.Serializable

enum class ResultType(val type: String) {
    LIST("list")
}

@Serializable // same as -> Services?
data class ResultList(val links: List<Link> = emptyList(),
                      val resulttype: String = ResultType.LIST.type,
                      val result: Result? = null)