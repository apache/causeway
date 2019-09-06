package org.ro.to

import kotlinx.serialization.Serializable

@Serializable
//TODO structure of json is changed >= 16.2
data class ResultList(val links: List<Link> = emptyList(),
                      val resulttype: String = ResultType.LIST.type,
        //IMPROVE? result.value contains a list of links that constitute the elements of this list
                      val result: Result? = null
) : TransferObject
