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
package org.apache.causeway.client.kroviz.utils

import org.apache.causeway.client.kroviz.core.event.LogEntry
import org.apache.causeway.client.kroviz.to.Icon
import org.w3c.dom.url.URL
import org.w3c.files.Blob

object ImageUtils {

    fun extractIcon(logEntry: LogEntry): Icon {
        val blob = logEntry.blob!!
        return Icon(blob.toImageUrl())
    }

    /**
     * Icons arrive as blobs.  The model only carries the resulting object URL, so
     * creating the DOM image is left to whoever displays it.
     */
    private fun Blob.toImageUrl(): String {
        return URL.createObjectURL(this)
    }

}
