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

/**
 * 
 */
package org.apache.isis.viewer.html.component.html;

import java.io.PrintWriter;

import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.Component;

final class UserSwapLink implements Component {
    private final PathBuilder pathBuilder;
    private final String name;

    UserSwapLink(final PathBuilder pathBuilder, final String name) {
        this.pathBuilder = pathBuilder;
        this.name = name;
    }

    @Override
    public void write(final PrintWriter writer) {
        writer.print("<a class=\"user\" href=\"" + pathTo("setuser") + "?name=");
        writer.print(name);
        writer.print("\" title=\"Change user to " + name);
        writer.print("\">");
        writer.print(name);
        writer.println("</a>");
    }

    protected String pathTo(final String prefix) {
        return pathBuilder.pathTo(prefix);
    }
}
