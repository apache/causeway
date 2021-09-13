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
package org.apache.isis.client.kroviz.utils

import org.apache.isis.client.kroviz.to.Relation

object UrlUtils {

    fun isIcon(url: String): Boolean {
        return isApplicationIcon(url) || isObjectIcon(url)
    }

    fun isObjectIcon(url: String): Boolean {
         return url.endsWith(Relation.OBJECT_ICON.type)
    }

    fun isApplicationIcon(url: String): Boolean {
        return url.contains("/images/")
    }

    fun isProperties(url: String): Boolean {
        return url.contains("/properties/")
    }

    fun isLayout(url: String): Boolean {
        return url.endsWith(Relation.LAYOUT.type)
    }

    fun isObjectLayout(url: String): Boolean {
        return url.endsWith(Relation.OBJECT_LAYOUT.type)
    }
}
