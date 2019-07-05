package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
data class ResultList(val links: List<Link> = emptyList(),
                      val resulttype: String = ResultType.LIST.type,
                      //IMPROVE? result.value contains a list of links that constitute the elements of this list
                      val result: Result? = null
) : TransferObject
