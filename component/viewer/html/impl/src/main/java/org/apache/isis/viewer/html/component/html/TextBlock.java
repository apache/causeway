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

class TextBlock extends ComponentAbstract {
    StringBuffer buffer = new StringBuffer();

    public TextBlock(final PathBuilder pathBuilder, final String text) {
        super(pathBuilder);
        append(text);
    }

    public TextBlock(final PathBuilder pathBuilder) {
        super(pathBuilder);
    }

    public void append(final String string) {
        buffer.append(string);
    }

    public void appendBold(final String string) {
        buffer.append("<b>");
        buffer.append(string);
        buffer.append("</b>");
    }

    @Override
    public void write(final PrintWriter writer) {
        writeTag(writer, "p class=\"unknown\"");
        writer.print(buffer.toString());
        writer.println("</p>");
    }

}
