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
package org.apache.isis.client.kroviz.to.mb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.to.MenuBarPosition
import org.apache.isis.client.kroviz.to.TransferObject

@Serializable
data class Extensions(val oid: String = "",
                      val isService: Boolean = false,
                      val isPersistent: Boolean = false,
                      val menuBar: String? = MenuBarPosition.PRIMARY.position,
                      val actionScope: String? = null,
                      val actionSemantics: String? = null,
                      val actionType: String = "",
                      @SerialName("x-isis-format") val xIsisFormat: String? = null,
                      private val friendlyName: String = "",
                      private val friendlyNameForm: String = "",
                      val collectionSemantics: String? = null,
                      val pluralName: String = "",
                      private val description: String = "",
                      private val descriptionForm: String = ""
) : TransferObject {

    fun getFriendlyName(): String {
        if (friendlyName.isEmpty()) {
            console.log("[Extensions.getFriendlyName] is empty")
        }
        return friendlyName
    }

    fun getDescription(): String {
        if (description.isEmpty()) {
            console.log("[Extensions.getDescription] is empty")
        }
        return description
    }

}
