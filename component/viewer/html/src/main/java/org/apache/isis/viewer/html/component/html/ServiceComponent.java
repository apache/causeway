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
import org.apache.isis.viewer.html.image.ImageLookup;

public class ServiceComponent implements Component {

    private final String id;
    private final String name;
    private final String iconName;
    private final PathBuilder pathBuilder;

    public ServiceComponent(final PathBuilder pathBuilder, final String id, final String name, final String iconName) {
        this.pathBuilder = pathBuilder;
        this.id = id;
        this.name = name;
        this.iconName = iconName;
    }

    @Override
    public void write(final PrintWriter writer) {
        writer.print("<div class=\"item\">");

        writer.print("<a href=\"");
        writer.print(pathTo("serviceOption") + "?id=");
        writer.print(id);
        writer.print("\"><img src=\"");
        writer.print(ImageLookup.image(iconName));
        writer.print("\" alt=\"service\" />");
        writer.print(name);
        writer.print("</a>");

        writer.println("</div>");
    }

    protected String pathTo(final String prefix) {
        return pathBuilder.pathTo(prefix);
    }

}
