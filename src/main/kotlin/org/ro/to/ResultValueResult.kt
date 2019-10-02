package org.ro.org.ro.to

import kotlinx.serialization.Serializable
import org.ro.to.Extensions
import org.ro.to.IResult
import org.ro.to.Link
import org.ro.to.Value

@Serializable
data class ResultValueResult(
        val value: Value? = null,
        val links: List<Link> = emptyList(),
        val extensions: Extensions? = null
) : IResult
