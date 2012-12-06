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

package org.apache.isis.viewer.html.component.html;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import org.apache.isis.viewer.html.component.Component;

public class Span implements Component {
    private final String className;
    private final String value;
    private final String description;

    public Span(final String className, final String value, final String description) {
        this.className = className;
        this.value = value;
        this.description = description;
    }

    @Override
    public void write(final PrintWriter writer) {
        writer.print("<span class=\"");
        writer.print(className);
        writer.print("\"");
        if (description != null) {
            writer.print(" title=\"");
            writer.print(description);
            writer.print("\"");
        }
        writer.print(">");
        if (value != null) {
            writer.print(StringEscapeUtils.escapeHtml(value));
        }
        writer.print("</span>");
    }
}
