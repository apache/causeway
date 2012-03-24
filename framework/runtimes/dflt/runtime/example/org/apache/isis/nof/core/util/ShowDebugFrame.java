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


package org.apache.isis.nof.core.util;

import org.apache.isis.noa.util.DebugInfo;
import org.apache.isis.noa.util.DebugString;


public class ShowDebugFrame {
    public static void main(final String[] args) {
        DebugFrame frame = new DebugFrame() {
            DebugInfo info1 = new DebugInfo() {
                public void debugData(final DebugString debug) {
                    debug.appendln("Debug data");
                }

                public String debugTitle() {
                    return "Debug title";
                }
            };

            DebugInfo info2 = new DebugInfo() {
                public void debugData(final DebugString debug) {
                    debug.appendln("Debug data 2");
                }

                public String debugTitle() {
                    return "Debug title 2";
                }
            };

            DebugInfo info3 = new DebugInfo() {
                public void debugData(final DebugString debug) {
                    debug.appendln("Debug data 3");
                }

                public String debugTitle() {
                    return "Debug 3";
                }
            };

            protected DebugInfo[] getInfo() {
                return new DebugInfo[] { info1, info2, info3 };
            }
        };

        frame.show(10, 10);
    }
}
