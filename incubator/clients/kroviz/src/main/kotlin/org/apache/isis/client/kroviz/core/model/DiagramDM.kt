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
package org.apache.isis.client.kroviz.core.model

import org.apache.isis.client.kroviz.to.DomainType
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.to.TransferObject

class DiagramDM(override val title: String) : BaseDisplayModel() {

    val classes = mutableSetOf<DomainType>()
    val properties = mutableSetOf<Property>()
    var numberOfClasses = -1
    private var numberOfProperties = 0

    fun incNumberOfProperties(inc: Int) {
        numberOfProperties += inc
    }

    fun decNumberOfClasses() {
        numberOfClasses--
    }

    override fun canBeDisplayed(): Boolean {
        if (isRendered) return false
        return (numberOfClasses == classes.size)
                //TODO && numberOfProperties == properties.size

    }

    override fun addData(obj: TransferObject) {
        when (obj) {
            is DomainType -> classes.add(obj)
            is Property -> properties.add(obj)
            else -> {
            }
        }
    }

}
