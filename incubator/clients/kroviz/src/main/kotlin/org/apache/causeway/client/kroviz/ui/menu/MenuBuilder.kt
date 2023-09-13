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
package org.apache.causeway.client.kroviz.ui.menu

import io.kvision.core.Component
import io.kvision.html.Link
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.utils.IconManager
import org.apache.causeway.client.kroviz.utils.StringUtils

open class MenuBuilder {
    protected fun switchCssClass(menuItem: Component, from: String, to: String) {
        menuItem.removeCssClass(from)
        menuItem.addCssClass(to)
    }

    fun buildActionLink(
        label: String,
        menuTitle: String,
    ): Link {
        val actionTitle = StringUtils.deCamel(label)
        val actionLink: Link = ddLink(
            label = actionTitle,
            icon = IconManager.find(label),
            className = IconManager.findStyleFor(label)
        )
        val id = "$menuTitle${Constants.actionSeparator}$actionTitle"
        actionLink.setDragDropData(Constants.stdMimeType, id)
        actionLink.id = id
        return actionLink
    }

    private fun ddLink(
        label: String,
        icon: String? = null,
        className: String? = null,
        init: (Link.() -> Unit)? = null,
    ): Link {
        val link = Link(
            label = label,
            url = null,
            icon = icon,
            image = null,
            separator = null,
            labelFirst = true,
            className = className
        )
        link.addCssClass("dropdown-item")
        return link.apply {
            init?.invoke(this)
        }
    }

}