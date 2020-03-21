package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class DomainType(
        val links: List<Link>,
        val canonicalName: String,
        val members: List<Link>,
        val typeActions: List<Link>,
        val extensions: Extensions
) : TransferObject
