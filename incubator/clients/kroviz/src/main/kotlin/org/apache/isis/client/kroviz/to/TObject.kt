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
package org.apache.isis.client.kroviz.to

import kotlinx.serialization.Serializable

@Serializable
data class TObject(override val links: List<Link> = emptyList(),
                   val extensions: Extensions,
                   val title: String = "",
                   val domainType: String = "",
                   val instanceId: String? = null,
                   val members: Map<String, Member> = emptyMap()
) : TransferObject, WithLinks {

    fun getProperties(): MutableList<Member> {
        return getMembersOfType(MemberType.PROPERTY)
    }

    fun getActions(): MutableList<Member> {
        return getMembersOfType(MemberType.ACTION)
    }

    fun getCollections(): MutableList<Member> {
        return getMembersOfType(MemberType.COLLECTION)
    }

    private fun getMembersOfType(memberType: MemberType): MutableList<Member> {
        val result = mutableListOf<Member>()
        members.forEach {
            if (it.value.memberType == memberType.type) {
                result.add(it.value)
            }
        }
        return result
    }

    fun getProperty(key: String): Member? {
        members.forEach {
            if (it.key == key) {
                return it.value
            }
        }
        return null
    }

    fun getLayoutLink() : Link {
        return this.links.first {
            it.relation() == Relation.OBJECT_LAYOUT
        }
    }

    fun getSelfLink() : Link {
        return this.links.first {
            it.relation() == Relation.SELF
        }
    }
}
