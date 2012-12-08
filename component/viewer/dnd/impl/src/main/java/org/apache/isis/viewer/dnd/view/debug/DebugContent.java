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

package org.apache.isis.viewer.dnd.view.debug;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.View;

public class DebugContent implements DebuggableWithTitle {
    private final View view;

    public DebugContent(final View display) {
        this.view = display;
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        final Content content = view.getContent();
        if (content != null) {
            String type = content.getClass().getName();
            type = type.substring(type.lastIndexOf('.') + 1);
            debug.appendln("Content", type);

            debug.indent();

            content.debugDetails(debug);

            debug.appendln("Icon name", content.getIconName());
            debug.appendln("Icon ", content.getIconPicture(32));
            debug.appendln("Window title", content.windowTitle());

            debug.appendln("Object", content.isObject());
            debug.appendln("Collection", content.isCollection());

            debug.appendln("Text Parseable", content.isTextParseable());

            debug.unindent();
        } else {
            debug.appendln("Content", "none");
        }
        debug.blankLine();
    }

    @Override
    public String debugTitle() {
        return "Content";
    }
}
