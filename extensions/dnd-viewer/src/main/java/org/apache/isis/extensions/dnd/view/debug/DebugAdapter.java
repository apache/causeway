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


package org.apache.isis.extensions.dnd.view.debug;

import org.apache.isis.commons.debug.DebugInfo;
import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtime.util.Dump;


public class DebugAdapter implements DebugInfo {
    private final ObjectAdapter object;

    public DebugAdapter(final ObjectAdapter object) {
        this.object = object;
    }

    public void debugData(final DebugString debug) {
        dumpObject(object, debug);
    }

    public String debugTitle() {
        return "Adapter";
    }

    private void dumpObject(final ObjectAdapter object, final DebugString info) {
        if (object != null) {
            Dump.adapter(object, info);
        }
    }
}
