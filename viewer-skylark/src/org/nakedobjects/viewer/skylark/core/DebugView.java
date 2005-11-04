package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.Dump;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;


public class DebugView implements DebugInfo {
    private final View view;

    public DebugView(final View display) {
        this.view = display;
    }

    public String getDebugData() {
        DebugString debug = new DebugString();

        debug.append(view.getView());
        debug.blankLine();
        debug.blankLine();

        // display details
        debug.appendTitle("VIEW");

        debug.append(view.debugDetails());
        debug.append("\n");

        // content
        Content content = view.getContent();
        debug.appendTitle("CONTENT");
        if (content != null) {
            String type = content.getClass().getName();
            type = type.substring(type.lastIndexOf('.') + 1);
            debug.appendln(0, "Content", type);
            content.debugDetails(debug);

            debug.appendln(4, "Icon name", content.getIconName());
            debug.appendln(4, "Icon ", content.getIconPicture(32));
            debug.appendln(4, "Window title", content.windowTitle());
             
            debug.appendln(4, "Object", content.isObject());
            debug.appendln(4, "Collection", content.isCollection());
            debug.appendln(4, "Value", content.isValue());
        } else {
            debug.appendln(0, "Content", "none");
        }
        debug.blankLine();
        
        if (content instanceof ObjectContent) {
            NakedObject object = ((ObjectContent) content).getObject();
            dumpObject(object, debug);
            debug.blankLine();
            dumpSpecification(object, debug);
            debug.blankLine();
            dumpGraph(object, debug);

        } else if (content instanceof CollectionContent) {
            NakedCollection collection = ((CollectionContent) content).getCollection();
            dumpObject(collection, debug);
            debug.blankLine();
            dumpSpecification(collection, debug);
            debug.blankLine();
            dumpGraph(collection, debug);
        }

        debug.append("\n\nDRAWING\n");
        debug.append("------\n");
        view.draw(new DebugCanvas(debug, new Bounds(view.getBounds())));

        return debug.toString();
    }

    public String getDebugTitle() {
        return "Debug: " + view + view == null ? "" : ("/" + view.getContent());
    }

    public void dumpGraph(final Naked object, DebugString info) {
        if (object != null) {
            info.appendTitle("GRAPH");
            Dump.graph(object, info);
        }
    }

    public void dumpObject(final Naked object, DebugString info) {
        if (object != null) {
            info.appendTitle("OBJECT");
            Dump.object(object, info);
        }
    }

    private void dumpSpecification(Naked object, DebugString info) {
        if (object != null) {
            info.appendTitle("SPECIFICATION");
            Dump.specification(object, info);
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
