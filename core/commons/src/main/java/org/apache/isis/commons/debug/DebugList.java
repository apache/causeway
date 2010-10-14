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


package org.apache.isis.commons.debug;

import java.util.ArrayList;
import java.util.List;


public class DebugList {
    private final List<DebugInfo> l = new ArrayList<DebugInfo>();
    private final DebugString summary = new DebugString();

    public DebugList(final String name) {
        l.add(new DebugInfo() {
            public void debugData(DebugString debug) {
                debug.append(summary.toString());
            }

            public String debugTitle() {
                return name;
            }
        });
    }

    public void add(String name, Object object) {
        boolean b = object instanceof DebugInfo;
        if (b) {
            l.add((DebugInfo) object);
        }
        if (object != null) {
            summary.appendln(name + (b ? "*" : ""), object.toString());
        }
    }

    public DebugInfo[] debug() {
        return l.toArray(new DebugInfo[l.size()]);
    }

}

