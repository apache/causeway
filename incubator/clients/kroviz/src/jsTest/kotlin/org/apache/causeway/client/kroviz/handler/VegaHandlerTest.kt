/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.causeway.client.kroviz.handler

import org.apache.causeway.client.kroviz.to.Vega5
import org.apache.causeway.client.kroviz.vega_samples.BAR_CHART
import org.apache.causeway.client.kroviz.vega_samples.UML_DIAGRAM
import kotlin.test.Test
import kotlin.test.assertTrue

class VegaHandlerTest {

    @Test
    fun testParseBarChart() {
        val json = BAR_CHART.str
        val vega = VegaHandler().parse(json)
        assertTrue(vega is Vega5)
    }

    @Test
    fun testParseUmlDiagram() {
        val json = UML_DIAGRAM.str
        val vega = VegaHandler().parse(json)
        assertTrue(vega is Vega5)
    }

}
