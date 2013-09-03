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

package org.apache.isis.viewer.scimpi.dispatcher.view.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugHtmlString;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.util.Dump;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class Debug extends AbstractElementProcessor {

    private final Dispatcher dispatcher;

    public Debug(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void process(final Request request) {
        if (request.getContext().isDebugDisabled()) {
            return;
        }
        
        final String type = request.getOptionalProperty(TYPE);

        final boolean alwaysShow = request.isRequested("force", false);
        if (type != null) {
            if (type.equals("system")) {
                displaySystem(request);
            } else if (type.equals("session")) {
                displaySession(request);
            } else if (type.equals("test")) {
                final DebugBuilder debug = new DebugHtmlString();
                debug.appendTitle("Title");
                debug.appendln("boolean", true);
                debug.appendln("number", 213);
                debug.startSection("Section 1");
                debug.appendln("boolean", false);
                debug.appendln("number", 12348);
                debug.endSection();
                debug.startSection("Section 2");
                debug.appendln("boolean", false);
                debug.appendln("number", 12348);
                debug.appendTitle("Another title");
                debug.appendln("boolean", false);
                debug.appendln("number", 12348);
                debug.endSection();

                debug.startSection("Section 3");
                debug.appendln("boolean", false);
                debug.appendln("number", 89878);
                debug.endSection();
                debug.startSection("Subsection 2");
                debug.appendln("boolean", false);
                debug.appendln("number", 12348);
                debug.endSection();

                debug.startSection("Section 4");
                debug.appendln("boolean", false);
                debug.indent();
                debug.appendln("boolean", false);
                debug.appendln("number", 12348);
                debug.unindent();
                debug.appendln("number", 12348);
                debug.appendPreformatted("code", "line 1\nline 2\nline 3");
                debug.appendln("A lot of text etc.");
                debug.endSection();

                request.appendHtml(debug.toString());
                //request.appendHtml("<pre>" + debug.toString() + "</pre>");
                
                debug.close();
                
            } else if (type.equals("variables")) {
                displayVariables(request);
            } else if (type.equals("dispatcher")) {
                displayDispatcher(request);
            } else if (type.equals("context")) {
                displayContext(request);
            } else if (type.equals("specifications")) {
                listSpecifications(request);
            } else if (type.equals("specification-for")) {
                specificationFor(request);
            } else if (type.equals("specification")) {
                specification(request);
            } else if (type.equals("specification-graph")) {
                specificationGraph(request);
            } else if (type.equals("object-graph")) {
                objectGraph(request);

            } else if (type.equals("object")) {
                final String value = request.getOptionalProperty(VALUE);
                final RequestContext context = request.getContext();
                final ObjectAdapter object = context.getMappedObject(value);
                final DebugString str = new DebugString();
                Dump.adapter(object, str);
                Dump.graph(object, IsisContext.getAuthenticationSession(), str);
                request.appendHtml("<h2>" + object.getSpecification().getFullIdentifier() + "</h2>");
                request.appendHtml("<pre class=\"debug\">" + str + "</pre>");
            }

        }

        if (alwaysShow || request.getContext().getDebug() == RequestContext.Debug.ON) {

            final RequestContext context = request.getContext();

            final String id = request.getOptionalProperty("object");
            if (id != null) {
                final ObjectAdapter object = context.getMappedObject(id);
                if (object instanceof DebuggableWithTitle) {
                    final DebugString debug = new DebugString();
                    ((DebuggableWithTitle) object).debugData(debug);
                    request.appendHtml("<pre class=\"debug\">" + debug + "</pre>");
                } else {
                    request.appendHtml(object.toString());
                }
            }

            final String variable = request.getOptionalProperty("variable");
            if (variable != null) {
                final Object object = context.getVariable(variable);
                request.appendHtml(variable + " => " + (object == null ? "null" : object.toString()));
            }

            final String list = request.getOptionalProperty("list");
            if (list != null) {
                final DebugString debug = new DebugString();
                context.append(debug, list);
                request.appendHtml(debug.toString());
            }

            final String uri = request.getOptionalProperty("uri");
            if (uri != null) {
                request.appendHtml("<pre class=\"debug\">");
                request.appendHtml(context.getUri());
                request.appendHtml("</pre>");
            }

        }
    }

    protected void objectGraph(final Request request) {
        final String id = request.getOptionalProperty(VALUE);
        final ObjectAdapter object = request.getContext().getMappedObjectOrResult(id);
        request.appendHtml("<h1>Object Graph - " + object + "</h1>");
        request.appendHtml("<pre>");
        final DebugBuilder debug = new DebugString();
        Dump.graph(object, null, debug);
        request.appendHtml(debug.toString());
        request.appendHtml("</pre>");
    }

    protected void specificationFor(final Request request) {
        final String id = request.getOptionalProperty(VALUE);
        final ObjectAdapter object = request.getContext().getMappedObjectOrResult(id);
        specification(request, object.getSpecification());
    }

    protected void specification(final Request request) {
        final String name = request.getOptionalProperty(VALUE);
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(name);
        specification(request, spec);
    }

    private void specification(final Request request, final ObjectSpecification spec) {
        request.appendHtml("<h1>Specification - " + spec.getFullIdentifier() + "</h1>");
        request.appendHtml("<p><a href=\"./debug.shtml?type=specification-graph&value=" + spec.getFullIdentifier() + "\">Specification Graph</a></p>");
        final DebugBuilder debug = new DebugHtmlString();
        specification(spec, debug);
        request.appendHtml(debug.toString());
    }

    protected void specificationGraph(final Request request) {
        final String name = request.getOptionalProperty(VALUE);
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(name);
        request.appendHtml("<h1>Specification Graph - " + spec.getFullIdentifier() + "</h1>");
        request.appendHtml("<p><a href=\"./debug.shtml?type=specification&value=" + spec.getFullIdentifier() + "\">Full Specification</a></p>");
        request.appendHtml("<pre>");
        final DebugBuilder debug = new DebugString();
        debug.appendln(spec.getFullIdentifier());
        debug.indent();
        specificationGraph(spec, debug, new ArrayList<ObjectSpecification>());
        debug.unindent();
        request.appendHtml(debug.toString());
        request.appendHtml("</pre>");
    }

    private void displayContext(final Request request) {
        request.appendHtml("<h1>Context</h1>");
        final DebugHtmlString debugString = new DebugHtmlString();
        request.getContext().append(debugString);
        debugString.close();
        request.appendHtml(debugString.toString());
    }

    private void displayDispatcher(final Request request) {
        request.appendHtml("<h1>Dispatcher</h1>");
        final DebugHtmlString debugString = new DebugHtmlString();
        dispatcher.debug(debugString);
        debugString.close();
        request.appendHtml(debugString.toString());
    }

    protected void displayVariables(final Request request) {
        request.appendHtml("<h1>Variables</h1>");
        final DebugHtmlString debug = new DebugHtmlString();
        final RequestContext context = request.getContext();
        context.append(debug, "variables");
        debug.close();
        request.appendHtml(debug.toString());
    }

    protected void displaySystem(final Request request) {
        request.appendHtml("<h1>System</h1>");
        final DebuggableWithTitle[] debugItems = IsisContext.debugSystem();
        for (final DebuggableWithTitle debug : debugItems) {
            final DebugHtmlString debugBuffer = new DebugHtmlString();
            debugBuffer.startSection(debug.debugTitle());
            debug.debugData(debugBuffer);
            debugBuffer.endSection();
            debugBuffer.close();
            request.appendHtml(debugBuffer.toString());
        }
    }

    protected void displaySession(final Request request) {
        request.appendHtml("<h1>Session</h1>");
        final DebuggableWithTitle[] debugItems = IsisContext.debugSession();
        for (final DebuggableWithTitle debug : debugItems) {
            final DebugHtmlString debugBuffer = new DebugHtmlString();
            debugBuffer.startSection(debug.debugTitle());
            debug.debugData(debugBuffer);
            debugBuffer.endSection();
            debugBuffer.close();
            request.appendHtml(debugBuffer.toString());
        }
    }

    protected void listSpecifications(final Request request) {
        request.appendHtml("<h1>Specifications</h1>");
        final List<ObjectSpecification> fullIdentifierList = new ArrayList<ObjectSpecification>(getSpecificationLoader().allSpecifications());
        Collections.sort(fullIdentifierList, ObjectSpecification.COMPARATOR_SHORT_IDENTIFIER_IGNORE_CASE);
        final DebugHtmlString debug = new DebugHtmlString();
        for (final ObjectSpecification spec : fullIdentifierList) {
            final String name = spec.getSingularName();
            debug.appendln(name, specificationLink(spec));
        }
        debug.close();
        request.appendHtml(debug.toString());
    }

    private String specificationLink(final ObjectSpecification specification) {
        if (specification == null) {
            return "none";
        } else {
            final String name = specification.getFullIdentifier();
            return "<a href=\"./debug.shtml?type=specification&value=" + name + "\">" + name + "</a>";
        }
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    @Override
    public String getName() {
        return "debug";
    }

    private void specificationGraph(final ObjectSpecification spec, final DebugBuilder view, final ArrayList<ObjectSpecification> visited) {
        final List<ObjectAssociation> fields = new ArrayList<ObjectAssociation>(spec.getAssociations(Contributed.EXCLUDED));
        Collections.sort(fields, new Comparator<ObjectAssociation>() {
            @Override
            public int compare(final ObjectAssociation o1, final ObjectAssociation o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            final ObjectSpecification specification = field.getSpecification();
            if (!specification.isValue()) {
                final boolean contains = visited.contains(specification);
                final String aggregated = specification.isParented() ? "++" : "";
                view.appendln(aggregated + field.getName() + "  (<a href=\"./debug.shtml?type=specification-graph&value=" + specification.getFullIdentifier() + "\">" + specification.getFullIdentifier() + "</a>" + (contains ? "..." : "") + ")");
                if (!contains) {
                    visited.add(specification);
                    view.indent();
                    specificationGraph(specification, view, visited);
                    view.unindent();
                }
            }
        }

    }

    private void specification(final ObjectSpecification spec, final DebugBuilder view) {
        view.startSection("Summary");
        view.appendln("Hash code", "#" + Integer.toHexString(spec.hashCode()));
        view.appendln("ID", spec.getIdentifier());
        view.appendln("Full name", spec.getFullIdentifier());
        view.appendln("Short name", spec.getShortIdentifier());
        view.appendln("Singular name", spec.getSingularName());
        view.appendln("Plural name", spec.getPluralName());
        view.appendln("Description", spec.getDescription());

        view.appendln("Type", "?");
        view.appendln("Value/aggregated", String.valueOf(!spec.isValueOrIsParented()));

        view.appendln("Parent specification", specificationLink(spec.superclass()));
        specificationClasses(view, "Child specifications", spec.subclasses());
        specificationClasses(view, "Implemented interfaces", spec.interfaces());
        speficationFacets(view, spec);

        final List<ObjectAssociation> fields = spec.getAssociations(Contributed.EXCLUDED);
        specificationMembers(view, "Fields", fields);
        final List<ObjectAction> userActions = spec.getObjectActions(ActionType.USER, Contributed.INCLUDED, Filters.<ObjectAction>any());
        specificationMembers(view, "User Actions", userActions);
        specificationMembers(view, "Exploration Actions", spec.getObjectActions(ActionType.EXPLORATION, Contributed.INCLUDED, Filters.<ObjectAction>any()));
        specificationMembers(view, "Prototype Actions", spec.getObjectActions(ActionType.PROTOTYPE, Contributed.INCLUDED, Filters.<ObjectAction>any()));
        specificationMembers(view, "Debug Actions", spec.getObjectActions(ActionType.DEBUG, Contributed.INCLUDED, Filters.<ObjectAction>any()));
        view.endSection();

        view.startSection("Fields");
        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            view.appendTitle("<span id=\"" + field.getId() + "\"><em>Field:</em> " + field.getId() + "</span>");
            view.appendln("ID", field.getIdentifier());
            view.appendln("Short ID", field.getId());
            view.appendln("Name", field.getName());
            view.appendln("Specification", specificationLink(field.getSpecification()));

            view.appendln("Type", field.isOneToManyAssociation() ? "Collection" : field.isOneToOneAssociation() ? "Object" : "Unknown");
            view.appendln("Flags", (field.isAlwaysHidden() ? "" : "Visible ") + (field.isNotPersisted() ? "Not-Persisted " : " ") + (field.isMandatory() ? "Mandatory " : ""));

            speficationFacets(view, field);
        }
        view.endSection();

        view.startSection("Actions");
        for (int i = 0; i < userActions.size(); i++) {
            final ObjectAction action = userActions.get(i);
            view.appendTitle("<span id=\"" + action.getId() + "\"><em>Action:</em> " + action.getId() + "</span>");
            view.appendln("ID", action.getIdentifier());
            view.appendln("Short ID", action.getId());
            view.appendln("Name", action.getName());
            view.appendln("Specification", specificationLink(action.getSpecification()));

            view.appendln("On type", specificationLink(action.getOnType()));

            final ObjectSpecification returnType = action.getReturnType();
            view.appendln("Returns", returnType == null ? "VOID" : specificationLink(returnType));

            speficationFacets(view, action);

            final List<ObjectActionParameter> parameters = action.getParameters();
            if (parameters.size() == 0) {
                view.appendln("Parameters", "none");
            } else {
                final StringBuffer buffer = new StringBuffer();
                final List<ObjectActionParameter> p = action.getParameters();
                for (int j = 0; j < parameters.size(); j++) {
                    buffer.append(p.get(j).getName());
                    buffer.append(" (");
                    buffer.append(specificationLink(parameters.get(j).getSpecification()));
                    buffer.append(")");
                    view.appendln("Parameters", buffer.toString());

                    view.indent();
                    final Class<? extends Facet>[] parameterFacets = p.get(j).getFacetTypes();
                    for (final Class<? extends Facet> parameterFacet : parameterFacets) {
                        view.append(p.get(j).getFacet(parameterFacet).toString());
                    }
                    view.unindent();
                }
            }
        }
    }

    private void specificationClasses(final DebugBuilder view, final String label, final List<ObjectSpecification> subclasses) {
        final StringBuffer buffer = new StringBuffer();
        if (subclasses.size() == 0) {
            buffer.append("none");
        } else {
            for (int i = 0; i < subclasses.size(); i++) {
                buffer.append(specificationLink(subclasses.get(i)) + "<br>");
            }
        }
        view.appendln(label, buffer.toString());
    }

    private void specificationMembers(final DebugBuilder view, final String label, final List<? extends ObjectMember> members) {
        final StringBuffer buffer = new StringBuffer();
        if (members.size() == 0) {
            buffer.append("none");
        } else {
            for (int i = 0; i < members.size(); i++) {
                final ObjectMember member = members.get(i);
                buffer.append("<a href=\"#" + members.get(i).getId() + "\">" + member.getId() + "</a>   <small>");
                buffer.append(member.isAlwaysHidden() ? "" : "Visible ");
                if (member.isPropertyOrCollection()) {
                    buffer.append(((ObjectAssociation) member).isNotPersisted() ? "Not-Persisted " : " ");
                    buffer.append(((ObjectAssociation) member).isMandatory() ? "Mandatory " : "");
                }
                buffer.append("</small></a><br>");
            }
        }
        view.appendln(label, buffer.toString());
    }

    private void speficationFacets(final DebugBuilder view, final FacetHolder facetHolder) {
        final List<Facet> facets = facetHolder.getFacets(new Filter<Facet>() {
            @Override
            public boolean accept(final Facet facet) {
                return true;
            }
        });
        final StringBuffer buffer = new StringBuffer();
        if (facets == null || facets.size() == 0) {
            buffer.append("none");
        } else {
            Collections.sort(facets, new Comparator<Facet>() {
                @Override
                public int compare(final Facet o1, final Facet o2) {
                    final String facetType1 = o1.facetType().getName();
                    final String facetType2 = o2.facetType().getName();
                    return facetType1.substring(facetType1.lastIndexOf('.') + 1).compareTo(facetType2.substring(facetType2.lastIndexOf('.') + 1));
                }
            });
            for (final Facet facet : facets) {
                final String facetType = facet.facetType().getName();
                buffer.append("<span class=\"facet-type\">" + facetType.substring(facetType.lastIndexOf('.') + 1) + "</span>:  " + facet + "<br>");
            }
        }
        view.appendln("Facets", buffer.toString());
    }

}
