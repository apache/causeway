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


package org.apache.isis.extensions.dnd.view.debug;

import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.extensions.dnd.drawing.Bounds;
import org.apache.isis.extensions.dnd.drawing.DebugCanvas;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.ObjectContent;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.collection.CollectionContent;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.util.Dump;


public class DebugView implements DebugInfo {
    private final View view;

    public DebugView(final View display) {
        this.view = display;
    }

    public void debugData(final DebugString debug) {
        debug.append(view.getView());
        debug.blankLine();
        debug.blankLine();

        // display details
        debug.appendTitle("VIEW");

        view.debug(debug);
        debug.appendln();

        // content
        final Content content = view.getContent();
        debug.appendTitle("CONTENT");
        if (content != null) {
            String type = content.getClass().getName();
            type = type.substring(type.lastIndexOf('.') + 1);
            debug.appendln("Content", type);
            content.debugDetails(debug);

            debug.indent();
            debug.appendln("Icon name", content.getIconName());
            debug.appendln("Icon ", content.getIconPicture(32));
            debug.appendln("Window title", content.windowTitle());
            debug.appendln("Persistable", content.isPersistable());
            debug.appendln("Object", content.isObject());
            debug.appendln("Collection", content.isCollection());

            debug.appendln("Parseable", content.isTextParseable());
            debug.unindent();
        } else {
            debug.appendln("Content", "none");
        }
        debug.blankLine();

        if (content instanceof ObjectContent) {
            final ObjectAdapter object = ((ObjectContent) content).getObject();
            dumpObject(object, debug);
            debug.blankLine();
            dumpSpecification(object, debug);
            debug.blankLine();
            dumpGraph(object, debug);

        } else if (content instanceof CollectionContent) {
            final ObjectAdapter collection = ((CollectionContent) content).getCollection();
            debug.blankLine();
            dumpObject(collection, debug);
            dumpSpecification(collection, debug);
            debug.blankLine();
            dumpGraph(collection, debug);
        }

        debug.append("\n\nDRAWING\n");
        debug.append("------\n");
        view.draw(new DebugCanvas(debug, new Bounds(view.getBounds())));
    }

    public String debugTitle() {
        return "Debug: " + view + view == null ? "" : ("/" + view.getContent());
    }

    public void dumpGraph(final ObjectAdapter object, final DebugString info) {
        if (object != null) {
            info.appendTitle("GRAPH");
            Dump.graph(object, info, IsisContext.getAuthenticationSession());
        }
    }

    public void dumpObject(final ObjectAdapter object, final DebugString info) {
        if (object != null) {
            info.appendTitle("OBJECT");
            Dump.adapter(object, info);
        }
    }

    private void dumpSpecification(final ObjectAdapter object, final DebugString info) {
        if (object != null) {
            info.appendTitle("SPECIFICATION");
            Dump.specification(object, info);
        }
    }
}
