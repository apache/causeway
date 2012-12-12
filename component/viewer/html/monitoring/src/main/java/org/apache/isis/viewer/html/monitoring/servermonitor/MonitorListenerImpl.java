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

package org.apache.isis.viewer.html.monitoring.servermonitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class MonitorListenerImpl implements MonitorListener {
    private final List<MonitorEvent> requests = new ArrayList<MonitorEvent>();

    public MonitorListenerImpl() {
        org.apache.isis.viewer.html.monitoring.servermonitor.Monitor.addListener(this);
    }

    @Override
    public void postEvent(final MonitorEvent event) {
        // TODO use a stack of limited size so we have FIFO list
        if (requests.size() > 50) {
            requests.remove(0);
        }
        requests.add(event);
    }

    public void writeHtmlPage(final String section, final PrintWriter writer) throws IOException {
        Assert.assertNotNull(section);
        Assert.assertNotNull(writer);
        final String sectionName = section.equals("") ? "Overview" : section;

        writer.println("<HTML><HEAD><TITLE>NOF System Monitor - " + sectionName + "</TITLE></HEAD>");
        writer.println("<BODY>");

        writer.println("<h1>" + sectionName + "</h1>");

        final StringBuffer navigation = new StringBuffer("<p>");
        // final String[] options = target.debugSectionNames();
        final DebuggableWithTitle[] infos = IsisContext.debugSystem();
        for (int i = 0; i < infos.length; i++) {
            final String name = infos[i].debugTitle();
            appendNavigationLink(navigation, name, i > 0);
        }
        appendNavigationLink(navigation, "Requests", true);
        navigation.append("</p>");

        writer.println(navigation);
        writer.println("<pre>");
        if (sectionName.equals("Requests")) {
            int i = 1;
            for (final MonitorEvent event : requests) {
                writer.print("<a href=\"monitor?request=" + event.getSerialId() + "\">");
                writer.print(i++ + ". " + event);
                writer.println("</a>");
            }
        } else if (sectionName.startsWith("request=")) {
            final int requestId = Integer.valueOf(sectionName.substring("request=".length())).intValue();
            for (final MonitorEvent request : requests) {
                if (request.getSerialId() == requestId) {
                    writer.println(request.getDebug());
                    break;
                }
            }
        } else {
            for (final DebuggableWithTitle info : infos) {
                if (info.debugTitle().equals(sectionName)) {
                    // TODO use an HTML debug string
                    final DebugString debug = new DebugString();
                    info.debugData(debug);
                    writer.println(debug.toString());
                    break;
                }
            }
        }
        writer.println("</pre>");

        writer.println(navigation);
        writer.println("</BODY></HTML>");
    }

    private void appendNavigationLink(final StringBuffer navigation, final String name, final boolean appendDivider) throws UnsupportedEncodingException {
        if (appendDivider) {
            navigation.append(" | ");
        }
        navigation.append("<a href=\"monitor?");
        navigation.append(URLEncoder.encode(name, "UTF-8"));
        navigation.append("\">");
        navigation.append(name);
        navigation.append("</a>");
    }

    public void writeTextPage(final String section, final PrintWriter writer) throws IOException {
        Assert.assertNotNull(section);
        Assert.assertNotNull(writer);
        final String sectionName = section.equals("") ? "Overview" : section;

        writer.println(sectionName);

        final DebuggableWithTitle[] infos = IsisContext.debugSystem();
        if (sectionName.equals("Events")) {
            int i = 1;
            for (final MonitorEvent event : requests) {
                writer.println(i++ + ". " + event);
            }
            // TODO add clause for request
        } else {
            for (final DebuggableWithTitle info : infos) {
                if (info.debugTitle().equals(sectionName)) {
                    final DebugString debug = new DebugString();
                    info.debugData(debug);
                    writer.println(debug.toString());
                }
            }
        }

        writer.print("[Options: ");
        // final String[] options = target.debugSectionNames();
        for (final DebuggableWithTitle info : infos) {
            writer.print(info.debugTitle() + " ");
        }
        // writer.println();
    }
    /*
     * public void setTarget(final DebugSelection debugInfo2) { target =
     * debugInfo2; }
     */
}
