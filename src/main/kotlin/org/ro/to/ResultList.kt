package org.ro.to

import kotlinx.serialization.Serializable

@Serializable 
data class ResultList(val links: List<Link> = emptyList(),
                      val resulttype: String = ResultType.LIST.type,
                      val result: Result? = null)