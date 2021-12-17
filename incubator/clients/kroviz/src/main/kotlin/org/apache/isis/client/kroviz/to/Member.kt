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
data class Member(val id: String,
                  val memberType: String,
                  override val links: List<Link> = emptyList(),
        //IMROVE: make value immutable (again) and handle property edits eg. via a wrapper
        // members of type property have a value, those of type action don't
                  var value: Value? = null,
                  val format: String = "",
                  val extensions: Extensions? = null,
                  val disabledReason: String = "",
                  val optional: Boolean = false
) : TransferObject, WithLinks {

    var type: String? = ValueType.TEXT.type

    init {
        if (isProperty()
                && value == null
                && extensions != null
                && extensions.xIsisFormat == "string") {
            value = Value("")
        }
        type = TypeMapper().match(this)
    }

    fun isProperty(): Boolean {
        return memberType == MemberType.PROPERTY.type
    }

    fun isAction(): Boolean {
        return memberType == MemberType.ACTION.type
    }

    fun isReadOnly(): Boolean {
        return !isReadWrite()
    }

    fun isReadWrite(): Boolean {
        return (memberType == MemberType.PROPERTY.type && disabledReason == "")
    }

    fun getInvokeLink(): Link? {
        return links.firstOrNull { it.rel.indexOf(id) > 0 }
    }



}
