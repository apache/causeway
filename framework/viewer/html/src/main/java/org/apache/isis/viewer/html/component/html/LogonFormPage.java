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

import org.apache.isis.viewer.html.HtmlViewerContext;
import org.apache.isis.viewer.html.component.Block;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.component.DebugPane;
import org.apache.isis.viewer.html.component.ViewPane;

public class LogonFormPage extends AbstractHtmlPage {
    private final String user;
    private final String password;

    public LogonFormPage(final HtmlViewerContext htmlViewerContext, final String styleSheet, final String header, final String footer,
        final String user, final String password) {
        super(htmlViewerContext, styleSheet, header, footer);
        this.user = user;
        this.password = password;
    }

    @Override
    protected void writeContent(final PrintWriter writer) {
        writer.println("<div id=\"view\">");
        writer.println("<div class=\"header\">");
        if (user.equals("")) {
            writer.println("<span class=\"header-text\">Please enter a user name and password.</span>");
        } else {
            writer.println("<span class=\"header-text\">Please enter a valid user name and password.</span>");
        }
        writer.println("</div>");
        writer.println("<FORM ACTION=\"" + pathTo("logon") + "\" METHOD=\"post\">");
        writer.println("<div id=\"content\">");
        writer.println("<div class=\"field\"><span class=\"label\">User name</span>"
            + "<span class=\"separator\">: </span><INPUT NAME=\"username\" value=\"" + user + "\"></DIV>");
        writer.println("<div class=\"field\"><span class=\"label\">Password</span>"
            + "<span class=\"separator\">: </span><INPUT TYPE=\"password\" NAME=\"password\" value=\"" + password
            + "\"></DIV>");
        writer.println("<div class=\"action-button\"><INPUT TYPE=\"submit\" VALUE=\"Log in\" NAME=\"Log in\"></div>");
        writer.println("</div>");
        writer.println("</FORM>");
        writer.println("</div>");

    }

    protected String pathTo(final String prefix) {
        return htmlViewerContext.pathTo(prefix);
    }

    @Override
    public Block getNavigation() {
        return null;
    }

    @Override
    public ViewPane getViewPane() {
        return null;
    }

    @Override
    public void setCrumbs(final Component component) {
    }

    @Override
    public void setDebug(final DebugPane debugPane) {
    }

}
