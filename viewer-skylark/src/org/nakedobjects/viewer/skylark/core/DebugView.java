package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.collection.InstanceCollection;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.security.Session;
import org.nakedobjects.utility.Debug;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


public class DebugView implements DebugInfo {
	private static final Logger LOG = Logger.getLogger(AbstractView.class);
	 
	
	private View view;

    public DebugView(View display) {
        this.view = display;
    }

    public String getDebugData() {
        StringBuffer info = new StringBuffer();

        info.append(view.getView());
        info.append("\n\n");

        // display details
        info.append("VIEW\n");
        info.append("----\n");

        info.append(view.debugDetails());
        info.append("\n");

        // content
        Content content = view.getContent();
        info.append("CONTENT\n");
        info.append("------\n");
        info.append("Content:      " + (content == null ? "none" : content.debugDetails()) + "\n");

        if(content instanceof ObjectContent) {
        	NakedObject object = ((ObjectContent) content).getObject();
	        info.append(dumpObject(object));
	        info.append("\n");
	        info.append(dumpGraph(object));
        }

        info.append("\n\nDRAWING\n");
        info.append("------\n");
		view.draw(new DebugCanvas(info, new Bounds(view.getLocationWithinViewer(), view.getSize())));

        return info.toString();
    }

    public String getDebugTitle() {
        return "Debug: " + view + "/" + view.getContent();
    }

    public String debugGraph(NakedObject object, String name, int level, Vector recursiveElements) {
        if (level > 3) {
            return "...\n"; // only go 3 levels?
        }

        if (recursiveElements == null) {
            recursiveElements = new Vector(25, 10);
        }

        if (object instanceof NakedCollection) {
            return "\n" +
            debugCollectionGraph((NakedCollection) object, name, level, recursiveElements);
        } else {
            return "\n" + debugObjectGraph(object, name, level, recursiveElements);
        }
    }

    public String dumpGraph(NakedObject object) {
        StringBuffer info = new StringBuffer();

        // object details - exploded/recursive
        info.append("GRAPH\n");
        info.append("------\n");
        info.append(object);
        info.append(debugGraph(object, object.getSpecification().getShortName(), 0, new Vector()));

        return info.toString();
    }

    public String dumpObject(NakedObject object) {
        // compile details
        StringBuffer info = new StringBuffer();

        // object
//        info.append(object);

        info.append("\n");

        // object details - summary
        info.append(dumpSummary(object));
        info.append("\n");

        // object interface
        if (object != null) {
            NakedObjectSpecification nc = object.getSpecification();
            info.append("Class:        " + nc + "\n");
            LOG.debug("Class details for " + nc);
            //       if(! (object instanceof NakedClass || object instanceof
            // InternalCollection)) {
            if (!(object instanceof NakedClass || object instanceof InstanceCollection)) {
                info.append(nc.debugInterface());
            }
        }
        return info.toString();
    }

    public String dumpSummary(NakedObject object) {
        StringBuffer text = new StringBuffer();

        if (object != null) {
            text.append("Summary" + "\n");

            text.append("  Hash:       " + object.hashCode() + "\n");
            text.append("  ID:         " + object.getOid() + "\n");
            text.append("  Class:      " + object.getClass().getName() + "\n");
            text.append("  NakedClass: " + object.getSpecification() + "\n");
            text.append("  Context:    " + object.getClass().getName() + "\n");
            
            StringBuffer types = new StringBuffer();

            if (object instanceof NakedCollection) {
                types.append("Collection ");
            } else {
                types.append("Object ");
            }

            NakedObjectContext context = Session.getSession().getContext();
            text.append("  Type:       " + types + "\n");
            text.append("  Context:    " + context + "\n");
            text.append("  Persistent: " + object.getOid() != null + "\n");
            text.append("  Resolved:   " + object.isResolved() + "\n");
            text.append("  Title:      '" + object.titleString() + "'\n");
        }
        return text.toString();
    }


    private String debugCollectionGraph(NakedCollection collection, String name, int level,
        Vector recursiveElements) {
        StringBuffer s = new StringBuffer();

        //	indent(s, level - 1);
        if (recursiveElements.contains(collection)) {
            s.append("*\n");
        } else {
            //		s.append("\n");
            recursiveElements.addElement(collection);

            Enumeration e = ((NakedCollection) collection).elements();

            while (e.hasMoreElements()) {
                indent(s, level);

                NakedObject element = ((NakedObject) e.nextElement());

                s.append(element);
                s.append(debugGraph(element, name, level + 1, recursiveElements));
            }
        }

        return s.toString();
    }

    private String debugObjectGraph(NakedObject object, String name, int level,
        Vector recursiveElements) {
        StringBuffer s = new StringBuffer();

        recursiveElements.addElement(object);

        // work through all its fields
        FieldSpecification[] fields;
        
        fields = object.getSpecification().getVisibleFields(object, Session.getSession().getContext());

        for (int i = 0; i < fields.length; i++) {
            FieldSpecification field = fields[i];
            Object obj = field.get(object);

            name = field.getName();
            indent(s, level);

            if (obj instanceof NakedValue) {
                s.append(name + ": "); // name

                if (obj == null) {
                    s.append("unitialised - error");
                    s.append("\n");
                } else {
                    s.append(((NakedValue) obj).titleString());
                    s.append("\n");
                }
            } else if (obj instanceof NakedObject) {
                if (recursiveElements.contains(obj)) {
                    s.append(name + ": " + obj + "*\n");
                } else {
                    s.append(name + ": " + obj);
                    s.append(debugGraph((NakedObject) obj, name, level + 1, recursiveElements));
                }
            } else {
                s.append(name + ": " + obj);
                s.append("\n");
            }
        }

        return s.toString();
    }

    private void indent(StringBuffer s, int level) {
        for (int indent = 0; indent < level; indent++) {
            s.append(Debug.indentString(4) + "|");
        }

        s.append(Debug.indentString(4) + "+--");
    }
    
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
