package org.ro.org.ro.to

import kotlinx.serialization.Serializable
import org.ro.to.IResult
import org.ro.to.Link
import org.ro.to.ResultObjectResult
import org.ro.to.ResultType

@Serializable
data class ResultObject(
        val links: List<Link> = emptyList(),
        val resulttype: String = ResultType.DOMAINOBJECT.type,
        val result: ResultObjectResult? = null
) : IResult
