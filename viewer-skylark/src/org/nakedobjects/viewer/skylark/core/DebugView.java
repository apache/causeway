package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.collection.InstanceCollectionVector;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.NakedObjectMember;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.utility.Debug;
import org.nakedobjects.utility.DebugInfo;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.CollectionContent;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;

import java.util.Enumeration;
import java.util.Vector;


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
        debug.append("Content:     " + (content == null ? "none" : content.debugDetails()) + "\n");

        if(content instanceof ObjectContent) {
        	NakedObject object = ((ObjectContent) content).getObject();
        	dumpObjectMethods(object, debug);
	        dumpObject(object, debug);
	        debug.blankLine();
	        dumpGraph(object, debug);
        }

        if(content instanceof CollectionContent) {
        	NakedCollection collection = ((CollectionContent) content).getCollection();
	        dumpObject(collection, debug);
	        debug.blankLine();
	        dumpGraph(collection, debug);
        }

        debug.append("\n\nDRAWING\n");
        debug.append("------\n");
		view.draw(new DebugCanvas(debug, new Bounds(view.getBounds())));

        return debug.toString();
    }

    private String dumpObjectMethods(final NakedObject object, final DebugString debug) {

        if (object != null) {
            NakedObjectSpecification specification = object.getSpecification();
            listRelatedClasses(specification, debug);
            
            NakedObjectField[] fields = specification.getVisibleFields(object, ClientSession.getSession());
            Session session = ClientSession.getSession();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i] instanceof OneToManyAssociation) {
                    OneToManyAssociation f = (OneToManyAssociation) fields[i];
                    debugAboutDetail(debug, f, f.getHint(session, (NakedObject) object));
                } else if (fields[i] instanceof OneToOneAssociation) {
                    OneToOneAssociation f = (OneToOneAssociation) fields[i];
                    debugAboutDetail(debug, f, object.getHint(session, f, null));
                }
            }
 		}
        return debug.toString();
    }

    private void listRelatedClasses(NakedObjectSpecification specification, DebugString debug) {
        if(specification.superclass() != null) {
            debug.appendln(0, "Superclass", specification.superclass().getFullName());
        }
        debug.appendln(0, "Subclasses", specificationNames(specification.subclasses()));
        debug.appendln(0, "Interfaces", specificationNames(specification.interfaces()));
    }

    private String[] specificationNames(NakedObjectSpecification[] specifications) {
        String[] names = new String[specifications.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = specifications[i].getFullName();
        }
        return names;
    }

    private void debugAboutDetail(final DebugString debug, final NakedObjectMember member, final Hint hint) {
        debug.appendln(2, member.toString());

        String desc = hint.getDescription();

        if (desc != null && !desc.equals("")) {
            debug.appendln(4, "description", desc);
        }

        String aboutDesc = hint.debug();

        if (aboutDesc != null && !aboutDesc.equals("")) {
            debug.appendln(4, "about", aboutDesc);
        }
    }


    public String getDebugTitle() {
        return "Debug: " + view + view == null ? "" : ("/" + view.getContent());
    }

    public void debugGraph(final Naked object, final String name, final int level, final Vector recursiveElements, DebugString info) {
        if (level > 3) {
            info.appendln("..."); // only go 3 levels?
        } else {
	        Vector elements;
	        if (recursiveElements == null) {
	            elements = new Vector(25, 10);
	        } else {
	            elements = recursiveElements;
	        }
	
	        info.blankLine();
	        if (object instanceof NakedCollection) {
	            debugCollectionGraph((NakedCollection) object, name, level, elements, info);
	        } else {
	            debugObjectGraph((NakedObject) object, level, elements, info);
	        }
        }
    }

    public void dumpGraph(final Naked object, DebugString info) {
        if (object != null) {
            // object details - exploded/recursive
            info.appendTitle("GRAPH");
            info.append(object);
            debugGraph(object, object.getSpecification().getShortName(), 0, new Vector(), info);
        }
    }

    public String dumpObject(final Naked object, DebugString info) {
        // compile details
        
        info.blankLine();

        // object details - summary
        dumpSummary(object, info);
        info.blankLine();

        // object interface
        if (object != null) {
            NakedObjectSpecification spec = object.getSpecification();
            info.appendln(0, "Specification", spec);
            if (!(object.getObject() instanceof NakedClass || object.getObject() instanceof InstanceCollectionVector)) {
                info.append(spec.debugInterface());
            }
        }
        return info.toString();
    }

    private String dumpSummary(final Naked naked, DebugString text) {
        if (naked != null) {
            text.appendln("Summary");

            text.appendln(2, "  Hash", naked.hashCode());
            text.appendln(2, "  Adapter", naked.getClass().getName());
            text.appendln(2, "  Specification", naked.getSpecification());
            text.appendln(2, "  Class", naked.getObject() == null ? "none" : naked.getObject().getClass().getName());
            
            Session session = ClientSession.getSession();
            text.appendln(2, "  Session", session);
            text.appendln(2, "  Title", naked.titleString());

            if(naked instanceof NakedObject) {
                NakedObject object = (NakedObject) naked;
	            text.appendln(2, "  Context", object.getContext());
	            text.appendln(2, "  ID", object.getOid());
	            text.appendln(2, "  Persistent", (object.getOid() != null));
	            text.appendln(2, "  Resolved", object.isResolved());
            }
        }
        return text.toString();
    }


    private String debugCollectionGraph(final NakedCollection collection, final String name, final int level,
        final Vector recursiveElements, DebugString s) {

        //	indent(s, level - 1);
        if (recursiveElements.contains(collection)) {
            s.append("*\n");
        } else {
            //		s.append("\n");
            recursiveElements.addElement(collection);

            Enumeration e = ((NakedCollection) collection).elements();

            while (e.hasMoreElements()) {
                graphIndent(s, level);

                NakedObject element = ((NakedObject) e.nextElement());

                s.append(element);
                debugGraph(element, name, level + 1, recursiveElements, s);
            }
        }

        return s.toString();
    }

    private String debugObjectGraph(final NakedObject object, final int level,
        final Vector recursiveElements, DebugString s) {
        recursiveElements.addElement(object);

        // work through all its fields
        NakedObjectField[] fields;
        
        fields = object.getSpecification().getVisibleFields(object, ClientSession.getSession());

        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];
            Object obj = object.getField(field);

            String name = field.getName();
            graphIndent(s, level);

             if (obj instanceof NakedObject) {
                if (recursiveElements.contains(obj)) {
                    s.append(name + ": " + obj + "*\n");
                } else {
                    s.append(name + ": " + obj);
                   debugGraph((NakedObject) obj, name, level + 1, recursiveElements, s);
                }
            } else {
                s.append(name + ": " + obj);
                s.append("\n");
            }
        }

        return s.toString();
    }

    private void graphIndent(DebugString s, int level) {
        for (int indent = 0; indent < level; indent++) {
            s.append(Debug.indentString(4) + "|");
        }

        s.append(Debug.indentString(4) + "+--");
    }
    
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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
