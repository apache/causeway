package org.nakedobjects.utility;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.NakedObjectMember;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;

import java.util.Enumeration;
import java.util.Vector;


public class Dump {

    private static void aboutDetail(DebugString text, NakedObjectMember member) {
        text.appendln(4, member.toString());
    }

    private static void collectionGraph(
            final NakedCollection collection,
            final int level,
            final Vector recursiveElements,
            DebugString s) {

        if (recursiveElements.contains(collection)) {
            s.append("*\n");
        } else {
            recursiveElements.addElement(collection);
            Enumeration e = ((NakedCollection) collection).elements();
            while (e.hasMoreElements()) {
                graphIndent(s, level);
                NakedObject element = ((NakedObject) e.nextElement());
                s.append(element);
                graph(element, level + 1, recursiveElements, s);
            }
        }
    }

    /**
     * Creates an ascii object graph diagram for the specified object, up to three levels deep.
     */
    public static String graph(Naked object) {
        DebugString s = new DebugString();
        graph(object, s);
        return s.toString();
    }

    public static void graph(Naked object, DebugString s) {
        s.append(object);
        graph(object, 0, new Vector(25, 10), s);
    }

    private static void graph(final Naked object, final int level, final Vector ignoreObjects, DebugString info) {
        if (level > 3) {
            info.appendln("..."); // only go 3 levels?
        } else {
            info.blankLine();
            if (object instanceof NakedCollection) {
                collectionGraph((NakedCollection) object, level, ignoreObjects, info);
            } else {
                objectGraph((NakedObject) object, level, ignoreObjects, info);
            }
        }
    }

    /**
     * Creates an ascii object graph diagram for the specified object, up to three levels deep, and
     * not expanding any of the objects in the excludedObjects vector.
     */
    public static String graph(Naked object, Vector excludedObjects) {
        DebugString s = new DebugString();
        s.append(object);
        graph(object, 0, excludedObjects, s);
        return s.toString();
    }

    private static void graphIndent(DebugString s, int level) {
        for (int indent = 0; indent < level; indent++) {
            s.append(Debug.indentString(4) + "|");
        }
        s.append(Debug.indentString(4) + "+--");
    }

    public static String object(Naked object) {
        DebugString s = new DebugString();
        object(object, s);
        return s.toString();
    }
    
    public static void object(Naked object, DebugString string) {
        string.appendln(0, "Specification", object.getSpecification().getFullName());
        string.appendln(0, "Class", object.getObject() == null ? "none" : object.getObject().getClass().getName());
        string.appendln(0, "Adapter", object.getClass().getName());
        string.appendAsHexln(0, "Hash", object.hashCode());
        string.appendln(0, "Title", object.titleString());
        string.appendln(0, "Object", object.getObject());

        if (object instanceof NakedObject) {
            NakedObject nakedObject = (NakedObject) object;
            string.appendln(0, "OID", nakedObject.getOid());
            string.appendln(0, "State", nakedObject.getResolveState());
            string.appendln(0, "Version", nakedObject.getVersion());
            string.appendln(0, "Icon", nakedObject.getIconName());
            string.appendln(0, "Persistable", nakedObject.persistable());
        }
    }

    private static void objectGraph(final NakedObject object, final int level, final Vector ignoreObjects, DebugString s) {
        ignoreObjects.addElement(object);

        // work through all its fields
        NakedObjectField[] fields;
        fields = object.getSpecification().getFields();
        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];
            Naked obj = object.getField(field);
            String name = field.getName();
            graphIndent(s, level);

            if (obj == null) {
                s.append(name + ": null\n");
            } else if (obj.getSpecification().isValue()) {
                s.append(name + ": " + obj);
                s.append("\n");
            } else {
                if (ignoreObjects.contains(obj)) {
                    s.append(name + ": " + obj + "*\n");
                } else {
                    s.append(name + ": " + obj);
                    graph(obj, level + 1, ignoreObjects, s);
                }
            }
        }
    }

    public static String specification(Naked object) {
        DebugString s = new DebugString();
        specification(object, s);
        return s.toString();
    }
      
    public static void specification(Naked naked, DebugString debug) {
        NakedObjectSpecification specification = naked.getSpecification();

        debug.appendln(0, "Full Name", specification.getFullName());
        debug.appendln(0, "Short Name", specification.getShortName());
        debug.appendln(0, "Plural Name", specification.getPluralName());
        debug.appendln(0, "Singular Name", specification.getSingularName());
     
        debug.blankLine();
        
        debug.appendln(0, "Abstract", specification.isAbstract());
        debug.appendln(0, "Lookup", specification.isLookup());
        debug.appendln(0, "Object", specification.isObject());
        debug.appendln(0, "Value", specification.isValue());
        debug.appendln(0, "Persistable", specification.persistable());

        
        if (specification.superclass() != null) {
            debug.appendln(0, "Superclass", specification.superclass().getFullName());
        }
        debug.appendln(0, "Subclasses", specificationNames(specification.subclasses()));
        debug.appendln(0, "Interfaces", specificationNames(specification.interfaces()));

        debug.appendln("Fields");
        specificationFields(specification, debug);
        debug.appendln("Object Actions");
        specificationActionMethods(specification, debug);
        debug.appendln("Class Actions");
        specificationClassMethods(specification, debug);
    }

    private static void specificationActionMethods(NakedObjectSpecification specification, DebugString debug) {
        Action[] userActions = specification.getObjectActions(Action.USER);
        Action[] explActions = specification.getObjectActions(Action.EXPLORATION);
        Action[] debActions = specification.getObjectActions(Action.DEBUG);
        specificationMethods(userActions, explActions, debActions, debug);
    }

    private static void specificationClassMethods(NakedObjectSpecification specification, DebugString debug) {
        Action[] userActions = specification.getClassActions(Action.USER);
        Action[] explActions = specification.getClassActions(Action.EXPLORATION);
        Action[] debActions = specification.getClassActions(Action.DEBUG);
        specificationMethods(userActions, explActions, debActions, debug);
     }

    private static void specificationFields(NakedObjectSpecification specification, DebugString debug) {
        NakedObjectField[] fields = specification.getFields();
        if (fields.length == 0) {
            debug.appendln(4, "none");
        } else {
            for (int i = 0; i < fields.length; i++) {
                if (fields[i] instanceof OneToManyAssociation) {
                    OneToManyAssociation f = (OneToManyAssociation) fields[i];
                    aboutDetail(debug, f);
                } else if (fields[i] instanceof OneToOneAssociation) {
                    OneToOneAssociation f = (OneToOneAssociation) fields[i];
                    aboutDetail(debug, f);
                }
            }
        }
    }

    private static void specificationMethods(Action[] userActions, Action[] explActions, Action[] debActions, DebugString debug) {
        if (userActions.length == 0 && explActions.length == 0 && debActions.length == 0) {
            debug.append("    none\n");
        } else {
            for (int i = 0; i < userActions.length; i++) {
                aboutDetail(debug, userActions[i]);
            }
            for (int i = 0; i < explActions.length; i++) {
                aboutDetail(debug, explActions[i]);
            }
            for (int i = 0; i < debActions.length; i++) {
                aboutDetail(debug, debActions[i]);
            }
        }
    }

    private static String[] specificationNames(NakedObjectSpecification[] specifications) {
        String[] names = new String[specifications.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = specifications[i].getFullName();
        }
        return names;
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