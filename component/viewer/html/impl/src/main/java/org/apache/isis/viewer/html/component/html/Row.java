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
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.component.ComponentComposite;

class Row extends ComponentComposite {

    public Row(final PathBuilder pathBuilder) {
        super(pathBuilder);
    }

    private static final int TRUNCATE_LENGTH = 18;

    @Override
    protected void write(final PrintWriter writer, final Component component) {
        writer.print("<td>");
        component.write(writer);
        writer.println("</td>");
    }

    public void addCell(final String string, final boolean truncate) {
        String s;
        if (truncate) {
            s = string.substring(0, Math.min(TRUNCATE_LENGTH, string.length()));
            if (string.length() > TRUNCATE_LENGTH) {
                s += "...";
            }
        } else {
            s = string;
        }
        add(new Html(pathBuilder, s));
    }

    public void addCell(final Component component) {
        add(component);
    }

}
