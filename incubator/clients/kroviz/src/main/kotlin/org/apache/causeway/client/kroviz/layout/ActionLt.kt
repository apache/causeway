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
data class ActionLt(var named: String? = "",
                    var describedAs: String? = "",
                    var metadataError: String? = "",
                    var link: Link? = null,
                    var id: String? = "",
                    var bookmarking: String? = "",
                    var cssClass: String? = "",
                    var cssClassFa: String? = "",
                    var cssClassFaPosition: String? = "",
                    var hidden: String? = null,
                    var namedEscaped: String? = "",
                    var position: String? = "",
                    var promptStyle: String? = "",
                    val redirect: String? = null
)
