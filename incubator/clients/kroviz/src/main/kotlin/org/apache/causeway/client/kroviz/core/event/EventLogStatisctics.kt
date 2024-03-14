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
package org.apache.causeway.client.kroviz.core.event

import org.apache.causeway.client.kroviz.ui.core.SessionManager

class EventLogStatistics {
    var cntRequests = 0
    var totalDurationMs = 0.0
    var totalResponseBytes = 0
    var avgRequestDurationMs = 0.0
    var avgResponseBytes = 0.0
    var avgRunningAtStart = 0.0
    var avgBytesPerSec = 0.0
    var requestsPerSec = 0.0

    init {
        val logEntries = SessionManager.getEventStore().log.filter { !it.isView() }
        cntRequests = logEntries.size
        totalDurationMs = calculateTotalDuration(logEntries)
        totalResponseBytes = calculateTotalResponseLength(logEntries)
        avgRequestDurationMs = totalDurationMs / cntRequests
        avgResponseBytes = (totalResponseBytes / cntRequests).toDouble()
        avgRunningAtStart = calculateTotalRunningAtStart(logEntries) / cntRequests
        avgBytesPerSec = totalResponseBytes / totalDurationMs * 1000
        requestsPerSec = cntRequests / totalDurationMs * 1000
    }

    private fun calculateTotalResponseLength(logEntries: List<LogEntry>): Int {
        var sum = 0
        logEntries.forEach {
            sum += it.responseLength
            if (it.blob != null && it.blob!!.isClosed) {
                sum += it.blob!!.size
            }
        }
        return sum
    }

    private fun calculateTotalDuration(logEntries: List<LogEntry>): Double {
        val firstTime = logEntries.first().createdAt.getTime()
        val lastUpdatedAt = logEntries.last().updatedAt
        if (lastUpdatedAt != null) {
            return lastUpdatedAt.getTime() - firstTime
        }
        return 0.0
    }

    private fun calculateTotalRunningAtStart(logEntries: List<LogEntry>): Double {
        var sum = 0.0
        logEntries.forEach {
            sum += it.runningAtStart
        }
        return sum
    }

}

private operator fun Int.plus(size: Number): Int {
    return this + size
}
