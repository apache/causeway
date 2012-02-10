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

import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.Block;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.component.ComponentComposite;

public class Div extends ComponentComposite implements Block {

    private final String className;
    private final String id;
    private final String description;

    public Div(final PathBuilder pathBuilder, final String className, final String description) {
        this(pathBuilder, className, description, null);
    }

    public Div(final PathBuilder pathBuilder, final String className, final String description, final String id) {
        super(pathBuilder);
        this.description = description;
        this.className = className;
        this.id = id;
    }

    @Override
    public void write(final PrintWriter writer) {
        super.write(writer);
    }

    @Override
    protected void writeBefore(final PrintWriter writer) {
        writer.print("<div");
        if (className != null) {
            writer.print(" class=\"");
            writer.print(className);
            writer.print("\"");
        }
        if (id != null) {
            writer.print(" id=\"");
            writer.print(id);
            writer.print("\"");
        }
        if (description != null) {
            writer.print(" title=\"");
            writer.print(description);
            writer.print("\"");
        }
        writer.print(">");
    }

    @Override
    protected void writeAfter(final PrintWriter writer) {
        writer.println("</div>");
    }

    @Override
    public void add(final Component component) {
        super.add(component);
    }

    public void add(final String text) {
        super.add(new Html(pathBuilder, text));
    }

}
