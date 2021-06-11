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
package org.apache.isis.client.kroviz.snapshots.demo2_0_0

import org.apache.isis.client.kroviz.handler.*

object Response2Handler {

    val map = mapOf(
            ACTIONS_STRINGS to ActionHandler(),
            ACTIONS_STRINGS_INVOKE to TObjectHandler(),
            ACTIONS_WHEREINTHEWORLD_INVOKE to TObjectHandler(),
            ACTIONS_TEXT_INVOKE to TObjectHandler(),
            ASSOCIATED_ACTION_OBJECT_LAYOUT to LayoutHandler(),
            OBJECT_COLLECTION to CollectionHandler(),
            DOMAIN_TYPES_PROPERTY to PropertyHandler(),
            FILE_NODE to DomainTypeHandler(),
            HTTP_ERROR_405 to HttpErrorHandler(),
            HTTP_ERROR_500 to HttpErrorHandler(),
            MENUBARS to MenuBarsHandler(),
            OBJECT_LAYOUT to LayoutHandler(),
            PRIMITIVES to TObjectHandler(),
            PROPERTY to PropertyHandler(),
            PROPERTY_DESCRIPTION to PropertyHandler(),
            RESTFUL to RestfulHandler(),
            RESTFUL_DOMAIN_TYPES to DomainTypesHandler(),
            TAB_OBJECT_LAYOUT to LayoutHandler(),
            TAB_LAYOUT_XML to LayoutXmlHandler(),
            TEMPORALS to TObjectHandler(),
            TEXT_LAYOUT to LayoutHandler(),
            TOOLTIP_OBJECT_LAYOUT to LayoutHandler(),
            TUPLE_OBJECT_LAYOUT to LayoutHandler(),
    )

}
