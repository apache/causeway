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
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.component.ComponentComposite;
import org.apache.isis.viewer.html.component.ViewPane;
import org.apache.isis.viewer.html.image.ImageLookup;

public class ViewDiv extends ComponentComposite implements ViewPane {

    private String iconName;
    private String objectId;
    private Component[] menu = new Component[0];
    private String title;
    private final List<String> messages = new ArrayList<String>();
    private final List<String> warnings = new ArrayList<String>();
    private String description;

    public ViewDiv(final PathBuilder pathBuilder) {
        super(pathBuilder);
    }

    @Override
    public void setIconName(final String iconName) {
        this.iconName = iconName;
    }

    public void setLink(final String objectId) {
        this.objectId = objectId;
    }

    @Override
    public void setMenu(final Component[] menu) {
        this.menu = menu;
    }

    @Override
    public void setTitle(final String title, final String description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public void setWarningsAndMessages(final List<String> messages, final List<String> warnings) {
        this.messages.addAll(messages);
        this.warnings.addAll(warnings);
    }

    @Override
    protected void writeBefore(final PrintWriter writer) {
        writer.println("<div id=\"view\">");
        writeHeader(writer);
        writeMenu(writer);
        writer.println("<div id=\"content\">");
    }

    @Override
    protected void writeAfter(final PrintWriter writer) {
        writer.println("</div>");
        writer.println("</div>");
        writeMessages(writer);
    }

    private void writeMessages(final PrintWriter writer) {
        if (warnings.size() > 0 || messages.size() > 0) {
            writer.print("<div class=\"message-header\">");
            for (final String warning : warnings) {
                writer.print("<div class=\"warning\">");
                writer.print(warning);
                writer.println("</div>");
            }
            for (final String message : messages) {
                writer.print("<div class=\"message\">");
                writer.print(message);
                writer.println("</div>");
            }
            writer.print("</div>");
        }
    }

    private void writeMenu(final PrintWriter writer) {
        writer.println("<div id=\"menu\">");
        writer.println("<h3>Actions</h3>");
        for (final Component element : menu) {
            element.write(writer);
        }
        writer.println("</div>");
    }

    private void writeHeader(final PrintWriter writer) {
        writer.print("<div class=\"header\"");
        if (description != null) {
            writer.print(" title=\"");
            writer.print(description);
            writer.print("\"");
        }
        writer.print(">");
        if (objectId != null) {
            writer.print("<a href=\"" + pathTo("object") + "?id=");
            writer.print(objectId);
            writer.print("\">");
        }
        if (iconName != null) {
            writer.print("<span class=\"header-icon\"><img src=\"");
            writer.print(ImageLookup.image(iconName));
            writer.print("\" alt=\"icon\" /></span>");
        }
        writer.print("<span class=\"header-text\"");
        writer.print(">");
        writer.print(title);
        writer.println("</span>");
        if (objectId != null) {
            writer.print("</a>");
        }
        writer.println("</div>");
    }

    @Override
    protected String pathTo(final String prefix) {
        return pathBuilder.pathTo(prefix);
    }
}
