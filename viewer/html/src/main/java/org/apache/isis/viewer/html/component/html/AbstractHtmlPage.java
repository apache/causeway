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
import java.util.StringTokenizer;

import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.Block;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.component.Page;

public abstract class AbstractHtmlPage implements Component, Page {

    private final Block pageHeader;
    private final String siteFooter;
    private final String siteHeader;
    private final String styleSheet;
    private final StringBuffer debug = new StringBuffer();

    private String title = "Apache Isis";
    protected final PathBuilder pathBuilder;

    public AbstractHtmlPage(final PathBuilder pathBuilder, final String styleSheet, final String header, final String footer) {
        this.pathBuilder = pathBuilder;
        this.pageHeader = new Div(pathBuilder, null, "page-header");
        this.styleSheet = styleSheet == null ? "default.css" : styleSheet;
        this.siteHeader = header;
        this.siteFooter = footer;
    }

    @Override
    public void addDebug(final String html) {
        debug.append("<div class=\"detail\">");
        debug.append(html);
        debug.append("</div>");
    }

    @Override
    public void addDebug(final String name, final String value) {
        debug.append("<div class=\"detail\">");
        debug.append("<span class=\"label\">");
        debug.append(name);
        debug.append("</span>: ");
        debug.append(value);
        debug.append("</div>");
    }

    @Override
    public Block getPageHeader() {
        return pageHeader;
    }

    @Override
    public void setTitle(final String title) {
        this.title = title;
    }

    @Override
    public void write(final PrintWriter writer) {
        writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
        writer.println("  <head>");
        writer.print("  <title>");
        writer.print(title);
        writer.println("</title>");
        writer.println("  <meta name=\"description\" content=\"Apache Isis Application Web Page\" />");

        final StringTokenizer st = new StringTokenizer(styleSheet, ",");
        int i = 0;
        while (st.hasMoreTokens()) {
            final String style = st.nextToken().trim();
            writer.print("  <link rel=\"");
            if (i++ > 0) {
                writer.print("alternate ");
            }
            writer.print("stylesheet\" title=\"Style " + i + "\" href=\"");
            writer.print(style);
            writer.println("\" type=\"text/css\" media=\"all\"/>");
        }
        writer.println("  <script src=\"jquery-1.7.1.js\" type=\"text/javascript\"></script>");
        writer.println("  <script src=\"htmlviewer.js\" type=\"text/javascript\"></script>");
        writer.println("  </head>");
        writer.println("  <body onLoad=\"window.document.form.fld0.focus()\">");
        writer.println("  <div id=\"wrapper\">");

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

        writer.println("</div>");
        writer.println("  </body>");
        writer.println("</html>");
    }

    protected abstract void writeContent(PrintWriter writer);
}
