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


package org.apache.isis.core.runtime.util;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.apache.isis.core.commons.debug.Debug;
import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationFacets;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.util.CollectionFacetUtils;
import org.apache.isis.core.metamodel.util.SpecUtils;


public final class Dump {

	private Dump() {}
	
    private static void collectionGraph(
            final ObjectAdapter collection,
            final int level,
            final Vector<ObjectAdapter> ignoreObjects,
            final DebugString s, AuthenticationSession authenticationSession) {

        if (ignoreObjects.contains(collection)) {
            s.append("*\n");
        } else {
            ignoreObjects.addElement(collection);
            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collection);
            final Enumeration e = facet.elements(collection);
            while (e.hasMoreElements()) {
                graphIndent(s, level);
                final ObjectAdapter element = ((ObjectAdapter) e.nextElement());
                s.append(element);
                if (ignoreObjects.contains(element)) {
                    s.append("*\n");
                } else {
                    s.indent();
                    graph(element, level + 1, ignoreObjects, s, authenticationSession);
                    s.unindent();
                }
            }
        }
    }

    /**
     * Creates an ascii object graph diagram for the specified object, up to three levels deep.
     * @param authenticationSession TODO
     */
    public static String graph(final ObjectAdapter object, AuthenticationSession authenticationSession) {
        final DebugString s = new DebugString();
        graph(object, s, authenticationSession);
        return s.toString();
    }

    public static void graph(final ObjectAdapter object, final DebugString s, AuthenticationSession authenticationSession) {
        simpleObject(object, s);
        s.appendln();
        s.append(object);
        graph(object, 0, new Vector<ObjectAdapter>(25, 10), s, authenticationSession);
    }

    private static void simpleObject(final ObjectAdapter object, final DebugString s) {
        s.appendln(object.titleString());
        s.indent();
        final ObjectSpecification objectSpec = object.getSpecification();
        if (objectSpec.isCollection()) {
            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(object);
            final Enumeration e = facet.elements(object);
            int i = 1;
            while (e.hasMoreElements()) {
                final ObjectAdapter element = ((ObjectAdapter) e.nextElement());
                s.appendln(i++ + " " + element.titleString());
            }
        } else  {
            // object is a regular Object
            try {
                List<ObjectAssociation> fields = objectSpec.getAssociations();
                for (int i = 0; i < fields.size(); i++) {
                    final ObjectAssociation field = fields.get(i);
                    final ObjectAdapter obj = field.get(object);

                    final String name = field.getId();
                    if (obj == null) {
                        s.appendln(name, "null");
                    } else {
                        s.appendln(name, obj.titleString());
                    }
                }
            } catch (final RuntimeException e) {
                s.appendException(e);
            }
        }
        s.unindent();
    }

    private static void graph(
    		final ObjectAdapter object, 
    		final int level, 
    		final Vector<ObjectAdapter> ignoreObjects, 
    		final DebugString info, 
    		final AuthenticationSession authenticationSession) {
        if (level > 3) {
            info.appendln("..."); // only go 3 levels?
        } else {
            info.append("\n");
            if (object.getSpecification().isCollection()) {
                collectionGraph(object, level, ignoreObjects, info, authenticationSession);
            } else if (object.getSpecification().isNotCollection()) {
                objectGraph(object, level, ignoreObjects, info, authenticationSession);
            } else {
                info.append("??? " + object);
            }
        }
    }

    /**
     * Creates an ascii object graph diagram for the specified object, up to three levels deep, and not
     * expanding any of the objects in the excludedObjects vector.
     * @param authenticationSession TODO
     */
    public static String graph(final ObjectAdapter object, final Vector<ObjectAdapter> excludedObjects, AuthenticationSession authenticationSession) {
        final DebugString s = new DebugString();
        s.append(object);
        graph(object, 0, excludedObjects, s, authenticationSession);
        return s.toString();
    }

    private static void graphIndent(final DebugString s, final int level) {
        for (int indent = 0; indent < level; indent++) {
            s.append(Debug.indentString(4) + "|");
        }
        s.append(Debug.indentString(4) + "+--");
    }

    public static String adapter(final ObjectAdapter object) {
        final DebugString s = new DebugString();
        adapter(object, s);
        return s.toString();
    }

    public static void adapter(final ObjectAdapter adapter, final DebugString string) {
        try {
            string.appendln("Adapter", adapter.getClass().getName());
            string.appendln("Class", adapter.getObject() == null ? "none" : adapter.getObject().getClass().getName());
            string.appendAsHexln("Hash", adapter.hashCode());
            string.appendln("Object", adapter.getObject());
            string.appendln("Title", adapter.titleString());
            string.appendln("Specification", adapter.getSpecification().getFullName());

            string.appendln();

            string.appendln("Icon", adapter.getIconName());
            string.appendln("OID", adapter.getOid());
            string.appendln("State", adapter.getResolveState());
            string.appendln("Version", adapter.getVersion());
            
        } catch (final RuntimeException e) {
            string.appendException(e);
        }

    }

    private static void objectGraph(
    		final ObjectAdapter object, 
    		final int level, 
    		final Vector<ObjectAdapter> ignoreObjects, 
    		final DebugString s, 
    		final AuthenticationSession authenticationSession) {
    	ignoreObjects.addElement(object);

        try {
            // work through all its fields
            List<ObjectAssociation> fields = object.getSpecification().getAssociations();
            for (int i = 0; i < fields.size(); i++) {
                final ObjectAssociation field = fields.get(i);
                final ObjectAdapter obj = field.get(object);
                final String name = field.getId();
                graphIndent(s, level);

				if (field.isVisible(authenticationSession, object).isVetoed()) {
                    s.append(name + ": (not visible)");
                    s.append("\n");
                } else {
                    if (obj == null) {
                        s.append(name + ": null\n");
                        /*
                         * } else if (obj.getSpecification().isParseable()) { s.append(name + ": " +
                         * obj.titleString()); s.append("\n");
                         */} else {
                        if (ignoreObjects.contains(obj)) {
                            s.append(name + ": " + obj + "*\n");
                        } else {
                            s.append(name + ": " + obj);
                            graph(obj, level + 1, ignoreObjects, s, authenticationSession);

                        }
                    }
                }
            }
        } catch (final RuntimeException e) {
            s.appendException(e);
        }

    }

    public static String specification(final ObjectAdapter object) {
        final DebugString s = new DebugString();
        specification(object, s);
        return s.toString();
    }

    public static void specification(final ObjectAdapter adapter, final DebugString debug) {
        final ObjectSpecification specification = adapter.getSpecification();
        specification(specification, debug);
    }

    public static void specification(final ObjectSpecification specification, final DebugString debug) {
        try {
            debug.appendTitle(specification.getClass().getName());
            debug.appendAsHexln("Hash code", specification.hashCode());
            debug.appendln("ID", specification.getIdentifier());
            debug.appendln("Full Name", specification.getFullName());
            debug.appendln("Short Name", specification.getShortName());
            debug.appendln("Singular Name", specification.getSingularName());
            debug.appendln("Plural Name", specification.getPluralName());
            debug.appendln("Description", specification.getDescription());
            debug.blankLine();
            debug.appendln("Features", featureList(specification));
            debug.appendln("Type", SpecUtils.typeNameFor(specification));
            if (specification.superclass() != null) {
                debug.appendln("Superclass", specification.superclass().getFullName());
            }
            debug.appendln("Interfaces", specificationNames(specification.interfaces()));
            debug.appendln("Subclasses", specificationNames(specification.subclasses()));
            debug.blankLine();
            debug.appendln("Service", specification.isService());
            debug.appendln("Encodable", specification.isEncodeable());
            debug.appendln("Parseable", specification.isParseable());
            debug.appendln("Aggregated", specification.isValueOrIsAggregated());

        } catch (final RuntimeException e) {
            debug.appendException(e);
        }

        if (specification instanceof DebugInfo) {
            ((DebugInfo) specification).debugData(debug);
        }

        debug.blankLine();

        debug.appendln("Facets");
        final Class<? extends Facet>[] facetTypes = specification.getFacetTypes();
        debug.indent();
        if (facetTypes.length == 0) {
            debug.appendln("none");
        } else {
            for (int i = 0; i < facetTypes.length; i++) {
                final Class<? extends Facet> type = facetTypes[i];
                final Facet facet = specification.getFacet(type);
                debug.appendln(facet.toString());
            }
        }
        debug.unindent();
        debug.blankLine();

        debug.appendln("Fields");
        debug.indent();
        specificationFields(specification, debug);
        debug.unindent();

        debug.appendln("Object Actions");
        debug.indent();
        specificationActionMethods(specification, debug);
        debug.unindent();

        debug.appendln("Related Service Actions");
        debug.indent();
        specificationServiceMethods(specification, debug);
        debug.unindent();
    }

    public static String featureList(final ObjectSpecification specification) {
        final StringBuffer str = new StringBuffer();
        if (specification.isAbstract()) {
            str.append("Abstract ");
        }
        if (SpecificationFacets.isBoundedSet(specification)) {
            str.append("Bounded ");
        }
        if (SpecificationFacets.isCached(specification)) {
            str.append("Cached ");
        }
        if (SpecificationFacets.isAlwaysImmutable(specification)) {
            str.append("Immutable (always) ");
        }
        if (SpecificationFacets.isImmutableOncePersisted(specification)) {
            str.append("Immutable (once persisted) ");
        }
        if (specification.isService()) {
            str.append("Service ");
        }
        return str.toString();
    }

    private static void specificationActionMethods(final ObjectSpecification specification, final DebugString debug) {
        try {
            final List<ObjectAction> userActions = specification.getObjectActions(ObjectActionType.USER);
            final List<ObjectAction> explActions = specification.getObjectActions(ObjectActionType.EXPLORATION);
            final List<ObjectAction> prototypeActions = specification.getObjectActions(ObjectActionType.PROTOTYPE);
            final List<ObjectAction> debActions = specification.getObjectActions(ObjectActionType.DEBUG);
            specificationMethods(userActions, explActions, prototypeActions, debActions, debug);
        } catch (final RuntimeException e) {
            debug.appendException(e);
        }
    }

    private static void specificationServiceMethods(final ObjectSpecification specification, final DebugString debug) {
        try {
            final List<ObjectAction> userActions = specification.getServiceActionsFor(ObjectActionType.USER);
            final List<ObjectAction> explActions = specification.getServiceActionsFor(ObjectActionType.EXPLORATION);
            final List<ObjectAction> prototypeActions = specification.getServiceActionsFor(ObjectActionType.PROTOTYPE);
            final List<ObjectAction> debActions = specification.getServiceActionsFor(ObjectActionType.DEBUG);
            specificationMethods(userActions, explActions, prototypeActions,debActions, debug);
        } catch (final RuntimeException e) {
            debug.appendException(e);
        }
    }

    private static void specificationFields(final ObjectSpecification specification, final DebugString debug) {
        final List<ObjectAssociation> fields = specification.getAssociations();
        debug.appendln("All");
        debug.indent();
        for (int i = 0; i < fields.size(); i++) {
            debug.appendln((i + 1) + "." + fields.get(i).getId());
        }
        debug.unindent();

        final List<ObjectAssociation> fields2 = specification
                .getAssociations(ObjectAssociationFilters.STATICALLY_VISIBLE_ASSOCIATIONS);
        debug.appendln("Static");
        debug.indent();
        for (int i = 0; i < fields2.size(); i++) {
            debug.appendln((i + 1) + "." + fields2.get(i).getId());
        }
        debug.unindent();
        debug.appendln();

        try {
            if (fields.size() == 0) {
                debug.appendln("none");
            } else {
                for (int i = 0; i < fields.size(); i++) {

                    final ObjectAssociation field = fields.get(i);
                    debug.appendln((i + 1) + "." + field.getId() + "  (" + field.getClass().getName() + ")");

                    debug.indent();
                    final String description = field.getDescription();
                    if (description != null && !description.equals("")) {
                        debug.appendln("Description", description);
                    }
                    final String help = field.getHelp();
                    if (help != null && !help.equals("")) {
                        debug
                                .appendln("Help", help.substring(0, Math.min(30, help.length()))
                                        + (help.length() > 30 ? "..." : ""));
                    }

                    debug.appendln("ID", field.getIdentifier());
                    debug.appendln("Short ID", field.getId());
                    debug.appendln("Name", field.getName());
                    final String type = field.isOneToManyAssociation() ? "Collection" : field.isOneToOneAssociation() ? "Object" : "Unknown";
                    debug.appendln("Type", type);
                    debug.appendln("Has identity", !field.getSpecification().isCollectionOrIsAggregated());
                    debug.appendln("Spec", field.getSpecification().getFullName());

                    debug.appendln("Flags", (field.isAlwaysHidden() ? "" : "Visible ")
                            + (field.isNotPersisted() ? "Not Persisted " : " ")
                            + (field.isMandatory() ? "Mandatory " : ""));

                    final Class<? extends Facet>[] facets = field.getFacetTypes();
                    if (facets.length > 0) {
                        debug.appendln("Facets");
                        debug.indent();
                        boolean none = true;
                        for (int j = 0; j < facets.length; j++) {
                            debug.appendln(field.getFacet(facets[j]).toString());
                            none = false;
                        }
                        if (none) {
                            debug.appendln("none");
                        }
                        debug.unindent();
                    }

                    debug.appendln(field.debugData());

                    debug.unindent();
                    debug.unindent();
                    debug.indent();
                }
            }
        } catch (final RuntimeException e) {
            debug.appendException(e);
        }

    }

    private static void specificationMethods(
            final List<ObjectAction> userActions,
            final List<ObjectAction> explActions,
            final List<ObjectAction> prototypeActions,
            final List<ObjectAction> debugActions,
            final DebugString debug) {
		if (userActions.size() == 0 && explActions.size() == 0 && prototypeActions.size() == 0 && debugActions.size() == 0) {
            debug.appendln("no actions...");
        } else {
            appendActionDetails(debug, "User actions", userActions);
            appendActionDetails(debug, "Exploration actions", explActions);
            appendActionDetails(debug, "Prototype actions", prototypeActions);
            appendActionDetails(debug, "Debug actions", debugActions);
        }
    }

	private static void appendActionDetails(final DebugString debug, String desc,
	        List<ObjectAction> actions) {
		debug.appendln(desc);
		debug.indent();
		for (int i = 0; i < actions.size(); i++) {
		    actionDetails(debug, actions.get(i), 8, i);
		}
		debug.unindent();
	}

    private static void actionDetails(final DebugString debug, final ObjectAction a, final int indent, final int count) {
        debug.appendln((count + 1) + "." + a.getId() + " (" + a.getClass().getName() + ")");
        debug.indent();
        final int newIndent = indent + 4;
        try {
            final List<ObjectAction> debActions = a.getActions();
            if (debActions.size() > 0) {
                for (int i = 0; i < debActions.size(); i++) {
                    actionDetails(debug, debActions.get(i), newIndent, i);
                }

            } else {
                if (a.getDescription() != null && !a.getDescription().equals("")) {
                    debug.appendln("Description", a.getDescription());
                }
                debug.appendln("ID", a.getId());
                // debug.appendln(12, "Returns", f.getReturnType() == null ? "Nothing" :
                // f.getReturnType().getFullName());

                debug.appendln(a.debugData());
                debug.appendln("Target", a.getTarget());
                debug.appendln("On type", a.getOnType());

                final Class<? extends Facet>[] facets = a.getFacetTypes();
                if (facets.length > 0) {
                    debug.appendln("Facets");
                    debug.indent();
                    for (int j = 0; j < facets.length; j++) {
                        debug.appendln(a.getFacet(facets[j]).toString());
                    }
                    debug.unindent();
                }

                final ObjectSpecification returnType = a.getReturnType();
                debug.appendln("Returns", returnType == null ? "VOID" : returnType.toString());

                final List<ObjectActionParameter> parameters = a.getParameters();
                if (parameters.size() == 0) {
                    debug.appendln("Parameters", "none");
                } else {
                    debug.appendln("Parameters");
                    debug.indent();
                    final List<ObjectActionParameter> p = a.getParameters();
                    for (int j = 0; j < parameters.size(); j++) {
                        debug.append(p.get(j).getName());
                        debug.append(" (");
                        debug.append(parameters.get(j).getSpecification().getFullName());
                        debug.appendln(")");
                        debug.indent();
                        final Class<? extends Facet>[] parameterFacets = p.get(j).getFacetTypes();
                        for (int i = 0; i < parameterFacets.length; i++) {
                            debug.appendln(p.get(j).getFacet(parameterFacets[i]).toString());
                        }
                        debug.unindent();
                    }
                    debug.unindent();
                }
            }
        } catch (final RuntimeException e) {
            debug.appendException(e);
        }

        debug.unindent();
    }

    private static String[] specificationNames(final List<ObjectSpecification> specifications) {
        final String[] names = new String[specifications.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = specifications.get(i).getFullName();
        }
        return names;
    }

}
