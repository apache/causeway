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

package org.apache.isis.core.metamodel.util;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebugUtils;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.facets.object.choices.ChoicesFacetUtils;
import org.apache.isis.core.metamodel.facets.object.cached.CachedFacetUtils;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacetUtils;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public final class Dump {

    private static DebugBuilder debugBuilder;

    // REVIEW: should provide this rendering context, rather than hardcoding.
    // the net effect currently is that class members annotated with 
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly) 
    // for any other value for Where
    private final static Where where = Where.ANYWHERE;

    private Dump() {
    }

    // /////////////////////////////////////////////////////////////////////
    // specification
    // /////////////////////////////////////////////////////////////////////

    /**
     * @see #specification(ObjectAdapter, DebugBuilder)
     * @see #specification(ObjectSpecification, DebugBuilder)
     */
    public static String specification(final ObjectAdapter adapter) {
        final DebugBuilder debugBuilder = new DebugString();
        specification(adapter, debugBuilder);
        return debugBuilder.toString();
    }

    /**
     * Convenience overload of
     * {@link #specification(ObjectSpecification, DebugBuilder)} that takes the
     * {@link ObjectSpecification} ( {@link ObjectAdapter#getSpecification()})
     * of the provided {@link ObjectAdapter}
     * 
     * @see #specification(ObjectAdapter)
     * @see #specification(ObjectSpecification, DebugBuilder)
     */
    public static void specification(final ObjectAdapter adapter, final DebugBuilder debugBuilder) {
        final ObjectSpecification specification = adapter.getSpecification();
        specification(specification, debugBuilder);
    }

    public static void specification(final ObjectSpecification specification, final DebugBuilder debugBuilder) {
        try {
            debugBuilder.appendTitle(specification.getClass().getName());
            debugBuilder.appendAsHexln("Hash code", specification.hashCode());
            debugBuilder.appendln("ID", specification.getIdentifier());
            debugBuilder.appendln("Full Name", specification.getFullIdentifier());
            debugBuilder.appendln("Short Name", specification.getShortIdentifier());
            debugBuilder.appendln("Singular Name", specification.getSingularName());
            debugBuilder.appendln("Plural Name", specification.getPluralName());
            debugBuilder.appendln("Description", specification.getDescription());
            debugBuilder.blankLine();
            debugBuilder.appendln("Features", featureList(specification));
            debugBuilder.appendln("Type", specification.isParentedOrFreeCollection() ? "Collection" : "Object");
            if (specification.superclass() != null) {
                debugBuilder.appendln("Superclass", specification.superclass().getFullIdentifier());
            }
            debugBuilder.appendln("Interfaces", specificationNames(specification.interfaces()));
            debugBuilder.appendln("Subclasses", specificationNames(specification.subclasses()));
            debugBuilder.blankLine();
            debugBuilder.appendln("Service", specification.isService());
            debugBuilder.appendln("Encodable", specification.isEncodeable());
            debugBuilder.appendln("Parseable", specification.isParseable());
            debugBuilder.appendln("Aggregated", specification.isValueOrIsParented());
        } catch (final RuntimeException e) {
            debugBuilder.appendException(e);
        }

        if (specification instanceof DebuggableWithTitle) {
            ((DebuggableWithTitle) specification).debugData(debugBuilder);
        }

        debugBuilder.blankLine();

        debugBuilder.appendln("Facets");
        final Class<? extends Facet>[] facetTypes = specification.getFacetTypes();
        debugBuilder.indent();
        if (facetTypes.length == 0) {
            debugBuilder.appendln("none");
        } else {
            for (final Class<? extends Facet> type : facetTypes) {
                final Facet facet = specification.getFacet(type);
                debugBuilder.appendln(facet.toString());
            }
        }
        debugBuilder.unindent();
        debugBuilder.blankLine();

        debugBuilder.appendln("Fields");
        debugBuilder.indent();
        specificationFields(specification, debugBuilder);
        debugBuilder.unindent();

        debugBuilder.appendln("Object Actions");
        debugBuilder.indent();
        specificationActionMethods(specification, debugBuilder);
        debugBuilder.unindent();
    }

    private static String[] specificationNames(final List<ObjectSpecification> specifications) {
        final String[] names = new String[specifications.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = specifications.get(i).getFullIdentifier();
        }
        return names;
    }

    private static void specificationActionMethods(final ObjectSpecification specification, final DebugBuilder debugBuilder) {
        try {
            final List<ObjectAction> userActions = specification.getObjectActions(ActionType.USER, Contributed.INCLUDED, Filters.<ObjectAction>any());
            final List<ObjectAction> explActions = specification.getObjectActions(ActionType.EXPLORATION, Contributed.INCLUDED, Filters.<ObjectAction>any());
            final List<ObjectAction> prototypeActions = specification.getObjectActions(ActionType.PROTOTYPE, Contributed.INCLUDED, Filters.<ObjectAction>any());
            final List<ObjectAction> debActions = specification.getObjectActions(ActionType.DEBUG, Contributed.INCLUDED, Filters.<ObjectAction>any());
            specificationMethods(userActions, explActions, prototypeActions, debActions, debugBuilder);
        } catch (final RuntimeException e) {
            debugBuilder.appendException(e);
        }
    }

    private static void specificationFields(final ObjectSpecification specification, final DebugBuilder debugBuilder) {
        final List<ObjectAssociation> fields = specification.getAssociations(Contributed.EXCLUDED);
        debugBuilder.appendln("All");
        debugBuilder.indent();
        for (int i = 0; i < fields.size(); i++) {
            debugBuilder.appendln((i + 1) + "." + fields.get(i).getId());
        }
        debugBuilder.unindent();

        final List<ObjectAssociation> fields2 = specification.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.VISIBLE_AT_LEAST_SOMETIMES);
        debugBuilder.appendln("Static");
        debugBuilder.indent();
        for (int i = 0; i < fields2.size(); i++) {
            debugBuilder.appendln((i + 1) + "." + fields2.get(i).getId());
        }
        debugBuilder.unindent();
        debugBuilder.appendln();

        try {
            if (fields.size() == 0) {
                debugBuilder.appendln("none");
            } else {
                for (int i = 0; i < fields.size(); i++) {

                    final ObjectAssociation field = fields.get(i);
                    debugBuilder.appendln((i + 1) + "." + field.getId() + "  (" + field.getClass().getName() + ")");

                    debugBuilder.indent();
                    final String description = field.getDescription();
                    if (description != null && !description.equals("")) {
                        debugBuilder.appendln("Description", description);
                    }
                    final String help = field.getHelp();
                    if (help != null && !help.equals("")) {
                        debugBuilder.appendln("Help", help.substring(0, Math.min(30, help.length())) + (help.length() > 30 ? "..." : ""));
                    }

                    
                    debugBuilder.appendln("ID", field.getIdentifier());
                    debugBuilder.appendln("Short ID", field.getId());
                    debugBuilder.appendln("Name", field.getName());
                    final String type = field.isOneToManyAssociation() ? "Collection" : field.isOneToOneAssociation() ? "Object" : "Unknown";
                    debugBuilder.appendln("Type", type);
                    final ObjectSpecification fieldSpec = field.getSpecification();
                    final boolean hasIdentity = !(fieldSpec.isParentedOrFreeCollection() || fieldSpec.isParented() || fieldSpec.isValue());
                    debugBuilder.appendln("Has identity", hasIdentity);
                    debugBuilder.appendln("Spec", fieldSpec.getFullIdentifier());

                    debugBuilder.appendln("Flags", (field.isAlwaysHidden() ? "" : "Visible ") + (field.isNotPersisted() ? "Not Persisted " : " ") + (field.isMandatory() ? "Mandatory " : ""));

                    final Class<? extends Facet>[] facets = field.getFacetTypes();
                    if (facets.length > 0) {
                        debugBuilder.appendln("Facets");
                        debugBuilder.indent();
                        boolean none = true;
                        for (final Class<? extends Facet> facet : facets) {
                            debugBuilder.appendln(field.getFacet(facet).toString());
                            none = false;
                        }
                        if (none) {
                            debugBuilder.appendln("none");
                        }
                        debugBuilder.unindent();
                    }

                    debugBuilder.appendln(field.debugData());

                    debugBuilder.unindent();
                }
            }
        } catch (final RuntimeException e) {
            debugBuilder.appendException(e);
        }

    }

    private static void specificationMethods(final List<ObjectAction> userActions, final List<ObjectAction> explActions, final List<ObjectAction> prototypeActions, final List<ObjectAction> debugActions, final DebugBuilder debugBuilder) {
        if (userActions.size() == 0 && explActions.size() == 0 && prototypeActions.size() == 0 && debugActions.size() == 0) {
            debugBuilder.appendln("no actions...");
        } else {
            appendActionDetails(debugBuilder, "User actions", userActions);
            appendActionDetails(debugBuilder, "Exploration actions", explActions);
            appendActionDetails(debugBuilder, "Prototype actions", prototypeActions);
            appendActionDetails(debugBuilder, "Debug actions", debugActions);
        }
    }

    private static void appendActionDetails(final DebugBuilder debug, final String desc, final List<ObjectAction> actions) {
        debug.appendln(desc);
        debug.indent();
        for (int i = 0; i < actions.size(); i++) {
            actionDetails(actions.get(i), 8, i, debug);
        }
        debug.unindent();
    }

    private static void actionDetails(final ObjectAction objectAction, final int indent, final int count, final DebugBuilder debugBuilder) {
        debugBuilder.appendln((count + 1) + "." + objectAction.getId() + " (" + objectAction.getClass().getName() + ")");
        debugBuilder.indent();
        try {
            if (objectAction.getDescription() != null && !objectAction.getDescription().equals("")) {
                debugBuilder.appendln("Description", objectAction.getDescription());
            }
            debugBuilder.appendln("ID", objectAction.getId());

            debugBuilder.appendln(objectAction.debugData());
            debugBuilder.appendln("On type", objectAction.getOnType());

            final Class<? extends Facet>[] facets = objectAction.getFacetTypes();
            if (facets.length > 0) {
                debugBuilder.appendln("Facets");
                debugBuilder.indent();
                for (final Class<? extends Facet> facet : facets) {
                    debugBuilder.appendln(objectAction.getFacet(facet).toString());
                }
                debugBuilder.unindent();
            }

            final ObjectSpecification returnType = objectAction.getReturnType();
            debugBuilder.appendln("Returns", returnType == null ? "VOID" : returnType.toString());

            final List<ObjectActionParameter> parameters = objectAction.getParameters();
            if (parameters.size() == 0) {
                debugBuilder.appendln("Parameters", "none");
            } else {
                debugBuilder.appendln("Parameters");
                debugBuilder.indent();
                final List<ObjectActionParameter> p = objectAction.getParameters();
                for (int j = 0; j < parameters.size(); j++) {
                    debugBuilder.append(p.get(j).getName());
                    debugBuilder.append(" (");
                    debugBuilder.append(parameters.get(j).getSpecification().getFullIdentifier());
                    debugBuilder.appendln(")");
                    debugBuilder.indent();
                    final Class<? extends Facet>[] parameterFacets = p.get(j).getFacetTypes();
                    for (final Class<? extends Facet> parameterFacet : parameterFacets) {
                        debugBuilder.appendln(p.get(j).getFacet(parameterFacet).toString());
                    }
                    debugBuilder.unindent();
                }
                debugBuilder.unindent();
            }
        } catch (final RuntimeException e) {
            debugBuilder.appendException(e);
        }

        debugBuilder.unindent();
    }

    private static String featureList(final ObjectSpecification specification) {
        final StringBuilder str = new StringBuilder();
        if (specification.isAbstract()) {
            str.append("Abstract ");
        }
        if (ChoicesFacetUtils.hasChoices(specification)) {
            str.append("WithChoices ");
        }
        if (CachedFacetUtils.isCached(specification)) {
            str.append("Cached ");
        }
        if (ImmutableFacetUtils.isAlwaysImmutable(specification)) {
            str.append("Immutable (always) ");
        }
        if (ImmutableFacetUtils.isImmutableOncePersisted(specification)) {
            str.append("Immutable (once persisted) ");
        }
        if (specification.isService()) {
            str.append("Service ");
        }
        return str.toString();
    }

    // /////////////////////////////////////////////////////////////////////
    // adapter
    // /////////////////////////////////////////////////////////////////////

    /**
     * @see #adapter(ObjectAdapter, DebugBuilder)
     */
    public static String adapter(final ObjectAdapter adapter) {
        final DebugBuilder debugBuilder = new DebugString();
        adapter(adapter, debugBuilder);
        return debugBuilder.toString();
    }

    /**
     * @see #adapter(ObjectAdapter)
     */
    public static void adapter(final ObjectAdapter adapter, final DebugBuilder builder) {
        try {
            builder.appendln("Adapter", adapter.getClass().getName());
            builder.appendln("Class", adapter.getObject() == null ? "none" : adapter.getObject().getClass().getName());
            builder.appendAsHexln("Hash", adapter.hashCode());
            builder.appendln("Object", adapter.getObject());
            builder.appendln("Title", adapter.titleString());
            builder.appendln("Specification", adapter.getSpecification().getFullIdentifier());

            builder.appendln();

            builder.appendln("Icon", adapter.getIconName());
            builder.appendln("OID", adapter.getOid());
            builder.appendln("State", adapter.getResolveState());
            builder.appendln("Version", adapter.getVersion());

        } catch (final RuntimeException e) {
            builder.appendException(e);
        }

    }

    // /////////////////////////////////////////////////////////////////////
    // graph
    // /////////////////////////////////////////////////////////////////////

    /**
     * Creates an ascii object graph diagram for the specified object, up to
     * three levels deep.
     * 
     * @see #graph(ObjectAdapter, AuthenticationSession, DebugBuilder)
     */
    public static String graph(final ObjectAdapter adapter, final AuthenticationSession authenticationSession) {
        debugBuilder = new DebugString();
        graph(adapter, authenticationSession, debugBuilder);
        return debugBuilder.toString();
    }

    /**
     * As {@link #graph(ObjectAdapter, AuthenticationSession)}, but appending to
     * the provided {@link DebugBuilder}.
     * 
     * @see #graph(ObjectAdapter, AuthenticationSession)
     */
    public static void graph(final ObjectAdapter object, final AuthenticationSession authenticationSession, final DebugBuilder debugBuilder) {
        simpleObject(object, debugBuilder);
        debugBuilder.appendln();
        debugBuilder.append(object);
        graph(object, 0, Lists.<ObjectAdapter> newArrayList(), authenticationSession, debugBuilder);
    }

    private static void simpleObject(final ObjectAdapter collectionAdapter, final DebugBuilder debugBuilder) {
        debugBuilder.appendln(collectionAdapter.titleString());
        final ObjectSpecification objectSpec = collectionAdapter.getSpecification();
        if (objectSpec.isParentedOrFreeCollection()) {
            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collectionAdapter);
            int i = 1;
            for (final ObjectAdapter element : facet.collection(collectionAdapter)) {
                debugBuilder.appendln(i++ + " " + element.titleString());
            }
        } else {
            // object is a regular Object
            try {
                final List<ObjectAssociation> fields = objectSpec.getAssociations(Contributed.EXCLUDED);
                for (int i = 0; i < fields.size(); i++) {
                    final ObjectAssociation field = fields.get(i);
                    final ObjectAdapter obj = field.get(collectionAdapter);

                    final String name = field.getId();
                    if (obj == null) {
                        debugBuilder.appendln(name, "null");
                    } else {
                        debugBuilder.appendln(name, obj.titleString());
                    }
                }
            } catch (final RuntimeException e) {
                debugBuilder.appendException(e);
            }
        }
    }

    private static void collectionGraph(final ObjectAdapter collectionAdapter, final int level, final List<ObjectAdapter> ignoreAdapters, final AuthenticationSession authenticationSession, final DebugBuilder debugBuilder) {

        if (ignoreAdapters.contains(collectionAdapter)) {
            debugBuilder.append("*\n");
        } else {
            ignoreAdapters.add(collectionAdapter);
            final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(collectionAdapter);
            for (final ObjectAdapter element : facet.collection(collectionAdapter)) {
                graphIndent(level, debugBuilder);
                debugBuilder.append(element);
                if (ignoreAdapters.contains(element)) {
                    debugBuilder.append("*\n");
                } else {
                    graph(element, level + 1, ignoreAdapters, authenticationSession, debugBuilder);
                }
            }
        }
    }

    private static void graph(final ObjectAdapter adapter, final int level, final List<ObjectAdapter> ignoreAdapters, final AuthenticationSession authenticationSession, final DebugBuilder debugBuilder) {
        if (level > 3) {
            debugBuilder.appendln("..."); // only go 3 levels?
        } else {
            debugBuilder.append("\n");
            if (adapter.getSpecification().isParentedOrFreeCollection()) {
                collectionGraph(adapter, level, ignoreAdapters, authenticationSession, debugBuilder);
            } else if (adapter.getSpecification().isNotCollection()) {
                objectGraph(adapter, level, ignoreAdapters, debugBuilder, authenticationSession);
            } else {
                debugBuilder.append("??? " + adapter);
            }
        }
    }

    private static void graphIndent(final int level, final DebugBuilder debugBuilder) {
        for (int indent = 0; indent < level; indent++) {
            debugBuilder.append(DebugUtils.indentString(4) + "|");
        }
        debugBuilder.append(DebugUtils.indentString(4) + "+--");
    }

    private static void objectGraph(final ObjectAdapter adapter, final int level, final List<ObjectAdapter> ignoreAdapters, final DebugBuilder s, final AuthenticationSession authenticationSession) {
        ignoreAdapters.add(adapter);

        try {
            // work through all its fields
            final List<ObjectAssociation> fields = adapter.getSpecification().getAssociations(Contributed.EXCLUDED);
            for (int i = 0; i < fields.size(); i++) {
                final ObjectAssociation field = fields.get(i);
                final ObjectAdapter obj = field.get(adapter);
                final String name = field.getId();
                graphIndent(level, s);

                if (field.isVisible(authenticationSession, adapter, where).isVetoed()) {
                    s.append(name + ": (not visible)");
                    s.append("\n");
                } else {
                    if (obj == null) {
                        s.append(name + ": null\n");
                        /*
                         * } else if (obj.getSpecification().isParseable()) {
                         * s.append(name + ": " + obj.titleString());
                         * s.append("\n");
                         */} else {
                        if (ignoreAdapters.contains(obj)) {
                            s.append(name + ": " + obj + "*\n");
                        } else {
                            s.append(name + ": " + obj);
                            graph(obj, level + 1, ignoreAdapters, authenticationSession, s);

                        }
                    }
                }
            }
        } catch (final RuntimeException e) {
            s.appendException(e);
        }
    }

}
