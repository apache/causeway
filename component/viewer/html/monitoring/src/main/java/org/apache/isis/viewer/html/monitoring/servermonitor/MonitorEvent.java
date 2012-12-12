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

package org.apache.isis.viewer.html.monitoring.servermonitor;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;

public class MonitorEvent {
    private static int nextSerialId = 1;
    private final int serialId = nextSerialId++;
    private final String message;
    private final String category;
    private final DebugString debug;

    public MonitorEvent(final String category, final String message, final DebuggableWithTitle[] debugDetails) {
        this.message = message;
        this.category = category;
        debug = new DebugString();
        try {
            if (debugDetails != null) {
                for (final DebuggableWithTitle info : debugDetails) {
                    debug.appendTitle(info.debugTitle());
                    debug.indent();
                    info.debugData(debug);
                    debug.unindent();
                }
            }
        } catch (final RuntimeException e) {
            debug.appendException(e);
        }
    }

    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public int getSerialId() {
        return serialId;
    }

    public String getDebug() {
        return debug.toString();
    }

    @Override
    public String toString() {
        return category + ": " + message;
    }
}
