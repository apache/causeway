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

package org.apache.isis.viewer.dnd.view.debug;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.util.Dump;

public class DebugAdapter implements DebuggableWithTitle {
    private final ObjectAdapter object;

    public DebugAdapter(final ObjectAdapter object) {
        this.object = object;
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        dumpObject(object, debug);
    }

    @Override
    public String debugTitle() {
        return "Adapter";
    }

    private void dumpObject(final ObjectAdapter object, final DebugBuilder info) {
        if (object != null) {
            Dump.adapter(object, info);
        }
    }
}
