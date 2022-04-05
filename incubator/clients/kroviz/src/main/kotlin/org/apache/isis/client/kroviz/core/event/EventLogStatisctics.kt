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
package org.apache.isis.client.kroviz.core.event

import org.apache.isis.client.kroviz.ui.core.SessionManager

class EventLogStatistics {
    var numberOfEntries = 0
    var totalDuration = 0.0
    var averageDuration = 0.0
    var averageResponseLength = 0.0
    var averageRunningAtStart = 0.0

    init {
        val logEntries = SessionManager.getEventStore().log.filter { !it.isView() }
        numberOfEntries = logEntries.size
        totalDuration = calculateTotalDuration(logEntries)
        averageDuration = calculateAverageDuration(logEntries)
        averageResponseLength = calculateAverageResponseLength(logEntries)
        averageRunningAtStart = calculateAverageRunningAtStart(logEntries)
    }

    private fun calculateTotalDuration(logEntries: List<LogEntry>): Double {
        val firstTime = logEntries.first().createdAt.getTime()
        val lastUpdatedAt = logEntries.last().updatedAt
        if (lastUpdatedAt != null) {
            return lastUpdatedAt.getTime() - firstTime
        }
        return 0.0
    }

    private fun calculateAverageDuration(logEntries: List<LogEntry>): Double {
        var sum = 0.0
        logEntries.forEach {
            sum += it.duration
        }
        return sum / logEntries.size
    }

    private fun calculateAverageResponseLength(logEntries: List<LogEntry>): Double {
        var sum = 0.0
        logEntries.forEach {
            sum += it.responseLength
        }
        return sum / logEntries.size
    }

    private fun calculateAverageRunningAtStart(logEntries: List<LogEntry>): Double {
        var sum = 0.0
        logEntries.forEach {
            sum += it.runningAtStart
        }
        return sum / logEntries.size
    }

}