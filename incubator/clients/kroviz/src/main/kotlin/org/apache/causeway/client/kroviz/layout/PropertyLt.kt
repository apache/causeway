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
data class PropertyLt(val id: String? = null,
                      val named: String? = null,
                      val describedAs: String? = null,
                      val action: List<ActionLt> = emptyList(),
                      var metadataError: String? = null,
                      val link: Link? = null,
                      val cssClass: String? = null,
                      val hidden: String? = null,  //ALL_TABLES
                      val labelPosition: String? = null,
                      val multiLine: Int? = 1,
                      val namedEscaped: Boolean? = false,
                      val promptStyle: String? = null,
                      val dateRenderAdjustDays: Int? = 0,
                      val renderDay: Boolean? = false,
                      val renderedAsDayBefore: String? = null,   //always omitted with 2.0.0?
                      val typicalLength: Int? = null,
                      val repainting: String? = null,
                      val unchanging: String? = null
)
