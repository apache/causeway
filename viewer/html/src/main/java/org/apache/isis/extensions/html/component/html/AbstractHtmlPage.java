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
import java.util.StringTokenizer;

import org.apache.isis.extensions.html.component.Block;
import org.apache.isis.extensions.html.component.Component;
import org.apache.isis.extensions.html.component.Page;


public abstract class AbstractHtmlPage implements Component, Page {
    private final StringBuffer debug = new StringBuffer();
    private final Block pageHeader = new Div(null, "page-header");
    private final String siteFooter;
    private final String siteHeader;
    private final String styleSheet;
    private String title = "[[NAME]]";

    public AbstractHtmlPage(final String styleSheet, final String header, final String footer) {
        this.styleSheet = styleSheet == null ? "default.css" : styleSheet;
        this.siteHeader = header;
        this.siteFooter = footer;
    }

    public void addDebug(final String html) {
        debug.append("<div class=\"detail\">");
        debug.append(html);
        debug.append("</div>");
    }

    public void addDebug(final String name, final String value) {
        debug.append("<div class=\"detail\">");
        debug.append("<span class=\"label\">");
        debug.append(name);
        debug.append("</span>: ");
        debug.append(value);
        debug.append("</div>");
    }

    public Block getPageHeader() {
        return pageHeader;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public void write(final PrintWriter writer) {
        writer
                .println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
        writer.println("  <head>");
        writer.print("  <title>");
        writer.print(title);
        writer.println("</title>");
        writer.println("  <meta name=\"description\" content=\"[[NAME]] Application Web Page\" />");

        final StringTokenizer st = new StringTokenizer(styleSheet, ",");
        int i = 0;
        while (st.hasMoreTokens()) {
            final String style = st.nextToken().trim();

            writer.print("  <link rel=\"");
            if (i++ > 0) {
                writer.print("alternate ");
            }
            writer.print("stylesheet\" title=\"Style " + i + "\" href=\"./");
            writer.print(style);
            writer.println("\" type=\"text/css\" media=\"all\"/>");
        }
        writer.println("  </head>");
        writer.println("  <body onLoad=\"window.document.form.fld0.focus()\">");

        if (siteHeader != null) {
            writer.println("  <!-- the following block is added externally via configuration -->");
            writer.println(siteHeader);
        }

        writeContent(writer);

        if (siteFooter != null) {
            writer.println("  <!-- the following block is added externally via configuration -->");
            writer.println(siteFooter);
        }

        if (debug.length() > 0) {
            writer.println("<div id=\"debug\">");
            writer.println("<h4>Debug</h4>");
            writer.println(debug);
            writer.println("</div>");
        }

        writer.println("  </body>");
        writer.println("</html>");
    }

    protected abstract void writeContent(PrintWriter writer);
}

