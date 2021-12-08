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
package org.apache.isis.client.kroviz.core.event

import io.kvision.html.ButtonStyle
import kotlinx.serialization.Serializable
import org.apache.isis.client.kroviz.ui.core.Constants
import org.apache.isis.client.kroviz.utils.XmlHelper

enum class ChangeType(val id: String, val iconName: String, val style: ButtonStyle) {
    ADDED("ADDED", "fas fa-plus", ButtonStyle.INFO),
    MISSING("MISSING", "fas fa-minus", ButtonStyle.DANGER),
    DIFF("DIFF", "fas fa-bell", ButtonStyle.WARNING),
    INFO("INFO", "fas fa-info-circle", ButtonStyle.OUTLINEINFO),
    ERROR("ERROR", "fas fa-bug", ButtonStyle.OUTLINEDANGER),
}

@Serializable
data class LogEntryComparison(val title: String, val expected: LogEntry?, val actual: LogEntry?) {
    var changeType: ChangeType
    var iconName: String
    var expectedResponse: String? = null
    var actualResponse: String? = null
    var expectedBaseUrl = ""
    var actualBaseUrl = ""


    init {
        if (expected == null && actual == null) {
            throw Throwable("[LogEntryComparison.init] neither actual nor expected set")
        } else {
            changeType = compare()
            iconName = changeType.iconName
            actualResponse = actual?.response
            expectedResponse = expected?.response
            if (expected != null) {
                expectedBaseUrl = extractBaseUrl(expected)
                if (expected.subType == Constants.subTypeXml) {
                    expectedResponse = XmlHelper.format(expectedResponse!!)
                }
            }
            if (actual != null) {
                actualBaseUrl = extractBaseUrl(actual)
                if (actual.subType == Constants.subTypeXml) {
                    actualResponse = XmlHelper.format(actualResponse!!)
                }
            }
        }
    }

    private fun extractBaseUrl(event: LogEntry): String {
        val title = event.title
        return title.split(Constants.restInfix).first()
    }

    private fun compare(): ChangeType {
        val responsesAreEqual = areResponsesEqual()
        return when {
            expected == null -> ChangeType.ADDED
            actual == null -> ChangeType.MISSING
            responsesAreEqual -> ChangeType.INFO
            !responsesAreEqual -> ChangeType.DIFF
            else -> ChangeType.ERROR
        }
    }

    private fun areResponsesEqual(): Boolean {
        val expected = expectedResponse?.replace(expectedBaseUrl, "")
        val actual = actualResponse?.replace(actualBaseUrl, "")
        return (expected == actual)
    }

}