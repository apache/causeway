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

package org.apache.isis.core.commons.debug;

import java.util.List;

import com.google.common.collect.Lists;

public class DebugList {
    private final List<String[]> names = Lists.newArrayList();
    private final List<DebuggableWithTitle> debuggableList = Lists.newArrayList();

    public DebugList(final String name) {
        debuggableList.add(new DebuggableWithTitle() {
            @Override
            public void debugData(final DebugBuilder debug) {
                for (String[] name : names) {
                    debug.appendln(name[0], name[1]);
                }
            }

            @Override
            public String debugTitle() {
                return name;
            }
        });
    }

    public void add(final String name, final Object object) {
        final boolean b = object instanceof DebuggableWithTitle;
        if (b) {
            debuggableList.add((DebuggableWithTitle) object);
        }
        if (object != null) {
            String[] n = new String[2];
            n[0] = name + (b ? "*" : "");
            n[1] = object.toString();
            names.add(n);
        }
    }

    public DebuggableWithTitle[] debug() {
        return debuggableList.toArray(new DebuggableWithTitle[debuggableList.size()]);
    }
}
