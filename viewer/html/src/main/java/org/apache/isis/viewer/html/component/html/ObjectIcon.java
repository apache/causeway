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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.image.ImageLookup;
import org.apache.isis.viewer.html.request.Request;

public class ObjectIcon implements Component {
    private final ObjectAdapter element;
    private final String id;
    private final String style;
    private final String description;
    private final PathBuilder pathBuilder;

    public ObjectIcon(final PathBuilder pathBuilder, final ObjectAdapter element, final String description, final String id, final String style) {
        this.pathBuilder = pathBuilder;
        this.element = element;
        this.description = description;
        this.id = id;
        this.style = style;
    }

    @Override
    public void write(final PrintWriter writer) {
        writer.print("<div class=\"");
        writer.print(style);
        writer.print("\"");
        if (description != null) {
            writer.print(" title=\"");
            writer.print(description);
            writer.print("\"");
        }
        writer.print(">");

        writer.print("<a href=\"");
        writer.print(pathTo(Request.OBJECT_COMMAND) + "?id=");
        writer.print(id);
        writer.print("\"><img src=\"");
        writer.print(ImageLookup.image(element));
        writer.print("\" alt=\"");
        final String singularName = element.getSpecification().getSingularName();
        writer.print(singularName);
        writer.print("\"");
        writer.print("/>");
        writer.print(element.titleString());
        writer.print("</a>");

        writer.println("</div>");

    }

    protected String pathTo(final String prefix) {
        return pathBuilder.pathTo(prefix);
    }

}
