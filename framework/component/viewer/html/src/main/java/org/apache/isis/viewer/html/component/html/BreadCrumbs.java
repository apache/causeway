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
import org.apache.isis.viewer.html.component.ComponentAbstract;

public class BreadCrumbs extends ComponentAbstract {
    private final String[] names;
    private final boolean[] isLinked;

    public BreadCrumbs(final PathBuilder pathBuilder, final String[] names, final boolean[] isLinked) {
        super(pathBuilder);
        this.names = names;
        this.isLinked = isLinked;
    }

    @Override
    public void write(final PrintWriter writer) {
        writer.println("<div id=\"context\">");

        final int length = names.length;
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                writer.print("<span class=\"separator\"> &gt; </span>");
            }
            if (isLinked[i]) {
                writer.print("<a class=\"linked\" href=\"" + pathTo("context") + "?id=");
                writer.print(i);
                writer.print("\">");
                writer.print(names[i]);
                writer.print("</a>");
            } else if (!(i == length - 1 && names[i] == null)) {
                writer.print("<span class=\"disabled\">");
                writer.print(names[i]);
                writer.print("</span>");
            }
        }

        writer.print("</div>");
    }

    protected String pathTo(final String prefix) {
        return pathBuilder.pathTo(prefix);
    }

}
