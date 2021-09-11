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
package org.apache.isis.valuetypes.asciidoc.applib.ui;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Prism {
//    WICKET("1.24.1", "coy"),
//    VAADIN("1.24.1", "coy"),
    WICKET("1.20", "default"),
    VAADIN("1.20", "default");
    final String version;
    final String theme;
    public String cssFile() {
        return "css/prism" + version + "-" + theme + ".css";
    }
    public String jsFile() {
        return "js/prism" + version + ".js";
    }
}
