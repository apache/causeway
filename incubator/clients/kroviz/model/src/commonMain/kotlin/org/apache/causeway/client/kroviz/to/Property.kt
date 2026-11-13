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
package org.apache.causeway.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val id: String = "",
    val memberType: String = "",
    override val links: List<Link> = emptyList(),
    val optional: Boolean? = null,
    val title: String? = null,
    val value: Value? = null,
    val extensions: Extensions? = null,
    val format: String? = null,
    val disabledReason: String? = null,
    val parameters: List<Parameter> = emptyList(),
    val maxLength: Int = 0
) : TransferObject, WithLinks {

    fun isObjectProperty(): Boolean {
        return getType() == Represention.OBJECT_PROPERTY
    }

    fun getDescriptionLink(): Link? {
        return this.links.firstOrNull {
            it.rel == Relation.DESCRIBED_BY.type
        }
    }

    fun isPropertyDescription(): Boolean {
        return getType() == Represention.PROPERTY_DESCRIPTION
    }

    private fun getType(): Represention {
        return getSelfLink().representation()
    }

    private fun getSelfLink(): Link {
        return links.first {
            it.relation() == Relation.SELF
        }
    }
}

/**
 * Wraps Property in order to distinguish from PropertyDescription
 */
class ObjectProperty(val property: Property) {
    val id: String = property.id
    val memberType: String = property.memberType
    val links: List<Link> = property.links
    val value: Value? = property.value
    val extensions: Extensions? = property.extensions
    val disabledReason: String? = property.disabledReason

    fun getDescriptionLink(): Link? {
        return this.links.firstOrNull {
            it.rel == Relation.DESCRIBED_BY.type
        }
    }
}

/**
 * Wraps Property in order to distinguish from ObjectProperty
 */
class PropertyDescription(val property: Property) {
    val id: String = property.id
    val memberType: String = property.memberType
    val links: List<Link> = property.links
    val value: Value? = property.value
    val optional: Boolean? = property.optional
    val extensions: Extensions? = property.extensions

}
