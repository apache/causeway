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

package org.apache.isis.viewer.html.component;

import java.io.PrintWriter;

import org.apache.isis.viewer.html.PathBuilder;

public abstract class ComponentAbstract implements Component {

    private String id;
    private String cls;
    protected final PathBuilder pathBuilder;

    public ComponentAbstract(final PathBuilder pathBuilder) {
        this.pathBuilder = pathBuilder;
    }

    public void setClass(final String cls) {
        this.cls = cls;
    }

    public void setId(final String id) {
        this.id = id;
    }

    protected void writeTag(final PrintWriter writer, final String tagName) {
        tag(writer, tagName);
        writer.print(">");
    }

    private void tag(final PrintWriter writer, final String tagName) {
        writer.print("<");
        writer.print(tagName);
        if (id != null) {
            writer.print(" id=\"");
            writer.print(id);
            writer.print("\"");
        }
        if (cls != null) {
            writer.print(" class=\"");
            writer.print(cls);
            writer.print("\"");
        }
    }

}
