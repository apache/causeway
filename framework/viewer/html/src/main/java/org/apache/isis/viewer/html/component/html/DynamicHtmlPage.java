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
import org.apache.isis.viewer.html.component.DebugPane;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.component.ViewPane;

public class DynamicHtmlPage extends AbstractHtmlPage implements Page {

    private final Block navigation;
    private final ViewPane viewPane;

    private Component crumbs;
    private DebugPane debugPane;

    public DynamicHtmlPage(final PathBuilder pathBuilder, final String styleSheet, final String header, final String footer) {
        super(pathBuilder, styleSheet, header, footer);
        this.navigation = new Div(pathBuilder, null, "navigation");
        this.viewPane = new ViewDiv(pathBuilder);
    }

    @Override
    public Block getNavigation() {
        return navigation;
    }

    @Override
    public ViewPane getViewPane() {
        return viewPane;
    }

    @Override
    public void setCrumbs(final Component crumbs) {
        this.crumbs = crumbs;
    }

    @Override
    public void setDebug(final DebugPane debugPane) {
        this.debugPane = debugPane;
    }

    @Override
    protected void writeContent(final PrintWriter writer) {
        if (debugPane != null) {
            debugPane.write(writer);
        } else {
            writer.println();
            writer.println("<div id=\"body\">");
            navigation.write(writer);
            if (crumbs != null) {
                crumbs.write(writer);
            }
            viewPane.write(writer);
            writer.println();
            writer.println("</div>");
        }
    }
}
