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


package org.apache.isis.extensions.html.component.html;

import java.io.PrintWriter;

import org.apache.isis.extensions.html.component.Component;


class ActionComponent implements Component {
    private final String objectId;
    private final String name;
    private final String description;
    private final String field;
    private final String action;
    private final String elementId;

    public ActionComponent(
            final String action,
            final String name,
            final String description,
            final String objectId,
            final String elementId,
            final String field) {
        this.action = action;
        this.name = name;
        this.description = description;
        this.objectId = objectId;
        this.elementId = elementId;
        this.field = field;
    }

    public void write(final PrintWriter writer) {
        writer.print("<div class=\"action-button\">");
        writer.print("<a href=\"");
        writer.print(action);
        writer.print(".app?id=");
        writer.print(objectId);
        if (field != null) {
            writer.print("&amp;field=");
            writer.print(field);
        }
        if (elementId != null) {
            writer.print("&amp;element=");
            writer.print(elementId);
        }
        writer.print("\" title=\"");
        writer.print(description);
        writer.print("\"> ");
        writer.print(name);
        writer.print("</a>");
        writer.println("</div>");
    }

}

