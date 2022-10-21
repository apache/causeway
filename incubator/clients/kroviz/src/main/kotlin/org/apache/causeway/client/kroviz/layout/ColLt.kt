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
package org.apache.causeway.client.kroviz.layout

import kotlinx.serialization.Serializable
import org.apache.causeway.client.kroviz.to.Link

@Serializable
data class ColLt(val sizeSpan: List<Int> = mutableListOf<Int>(),
                 val domainObject: DomainObjectLt? = null,
                 val row: List<RowLt> = mutableListOf<RowLt>(),
                 val fieldSet: List<FieldSetLt> = mutableListOf<FieldSetLt>(),
                 val action: List<ActionLt> = mutableListOf<ActionLt>(),
                 val collection: List<CollectionLt> = mutableListOf<CollectionLt>(),
                 val metadataError: String? = "",
                 val cssClass: String? = "",
                 val size: String? = "",
                 val id: String? = "",
                 val span: Int? = 0,
                 val unreferencedActions: Boolean? = false,
                 val unreferencedCollections: Boolean? = false,
                 val named: String? = "",
                 val describedAs: String? = "",
                 val plural: String? = "",
                 val link: Link? = null,
                 val bookmarking: String? = "",
                 val cssClassFa: String? = "",
                 val cssClassFaPosition: String? = "",
                 val namedEscaped: Boolean? = false,
                 val tabGroup: List<TabGroupLt> = mutableListOf<TabGroupLt>()
)
