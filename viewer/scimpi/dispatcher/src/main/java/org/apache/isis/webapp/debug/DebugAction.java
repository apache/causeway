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


package org.apache.isis.webapp.debug;

import java.io.IOException;
import java.util.Arrays;

import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.filters.AbstractFilter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.util.Dump;
import org.apache.isis.webapp.Action;
import org.apache.isis.webapp.Dispatcher;
import org.apache.isis.webapp.context.RequestContext;


public class DebugAction implements Action {
    private Dispatcher dispatcher;

    public DebugAction(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public String getName() {
        return "debug";
    }

    public void debug(DebugView view) {}

    public void process(RequestContext context) throws IOException {
        DebugView view = new DebugView(context.getWriter());
        view.header();
        view.appendln("<div class=\"links\">");
        view.appendln("<a href=\"debug.app?action=system\">System</a>");
        view.appendln(" | <a href=\"debug.app?action=specifications\">List specifications</a>");
        view.appendln(" | <a href=\"debug.app?action=context\">Context</a>");
        view.appendln(" | <a href=\"debug.app?action=dispatcher\">Dispatcher</a>");
        view.appendln("</div>");
        view.startTable();
        
        String action = context.getParameter("action");
        if ("specifications".equals(action)) {
            listSpecifications(view);
        } else   if ("specification".equals(action)) {
            specification(context, view);            
        } else   if ("object".equals(action)) {
            object(context, view);            
        } else   if ("system".equals(action)) {
            system(context, view);            
        } else   if ("context".equals(action)) {
            context.append(view);
        } else   if ("dispatcher".equals(action)) {
            dispatcher.debug(view);
        }
        
        view.endTable();
        view.footer();
        context.clearRequestedPath();
    }

    private void object(RequestContext context, DebugView view) {
        ObjectAdapter object = context.getMappedObjectOrResult(context.getParameter("object"));
        DebugString str = new DebugString();
        Dump.adapter(object, str);
        Dump.graph(object, str, IsisContext.getAuthenticationSession());
        view.divider(object.getSpecification().getFullName());
        view.appendRow("<pre class=\"debug\">" + str + "</pre>");
    }

    private void system(RequestContext context, DebugView view) {
        DebugInfo[] debug = IsisContext.debugSystem();
        view.divider("System");
        for (int i = 0; i < debug.length; i++) {
            DebugString str = new DebugString();
            debug[i].debugData(str);
            view.divider(debug[i].debugTitle());
            view.appendRow("<pre class=\"debug\">" + str + "</pre>");
        }
    }

    private void specification(RequestContext context, DebugView view) {
        String name = context.getParameter("name");
        ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(name);
        DebugString str = new DebugString();
        Dump.specification(spec, str);
        view.divider(spec.getFullName());
        view.appendRow("Hash code", "#" + Integer.toHexString(spec.hashCode()));
        view.appendRow("ID", spec.getIdentifier());
        view.appendRow("Full name", spec.getFullName());
        view.appendRow("Short name", spec.getShortName());
        view.appendRow("Singular name", spec.getSingularName());
        view.appendRow("Plural name", spec.getPluralName());
        view.appendRow("Description", spec.getDescription());

        view.appendRow("Type", "?");
        view.appendRow("Value/aggregated", String.valueOf(!spec.isValueOrIsAggregated()));

        
        view.appendRow("Parent specification", specificationLink(spec.superclass()));
        specificationClasses(view, "Child specifications",  spec.subclasses());
        specificationClasses(view, "Implemented interfaces", spec.interfaces());
        speficationFacets(view, spec);
        
        
        ObjectAssociation[] fields = spec.getAssociations();
        specificationMembers(view, "Fields", fields);
        ObjectAction[] userActions = spec.getObjectActions(ObjectActionType.USER);
        specificationMembers(view, "User Actions", userActions);
        specificationMembers(view, "Exploration Actions", spec.getObjectActions(ObjectActionType.EXPLORATION));
        specificationMembers(view, "Prototype Actions", spec.getObjectActions(ObjectActionType.PROTOTYPE));
        specificationMembers(view, "Debug Actions", spec.getObjectActions(ObjectActionType.DEBUG));

        
        for (int i = 0; i < fields.length; i++) {
            ObjectAssociation field = fields[i];
            view.divider("<span id=\"" + field.getId() + "\">Field: " + field.getName() + "</span>");
            view.appendRow("ID", field.getIdentifier());
            view.appendRow("Short ID", field.getId());
            view.appendRow("Name", field.getName());
            view.appendRow("Specification", specificationLink(field.getSpecification()));

            view.appendRow("Type",  field.isOneToManyAssociation() ? "Collection" : field.isOneToOneAssociation() ? "Object" : "Unknown");
            view.appendRow("Flags", (field.isAlwaysHidden() ? "": "Visible ") + (field.isNotPersisted() ? "Not Persisted": " ")
                    + (field.isMandatory() ? "Mandatory " : ""));

            speficationFacets(view, field);
        }
        
        for (int i = 0; i < userActions.length; i++) {
            ObjectAction action = userActions[i];
            view.divider("<span id=\"" + action.getId() + "\">Action: " + action.getName() + "</span>");
            view.appendRow("ID", action.getIdentifier());
            view.appendRow("Short ID", action.getId());
            view.appendRow("Name", action.getName());
            view.appendRow("Specification", specificationLink(action.getSpecification()));

            view.appendRow("Target", action.getTarget());
            view.appendRow("On type", specificationLink(action.getOnType()));

            ObjectSpecification returnType = action.getReturnType();
            view.appendRow("Returns", returnType == null ? "VOID" : specificationLink(returnType));
            
            speficationFacets(view, action);

            ObjectActionParameter[] parameters = action.getParameters();
            StringBuffer buffer = new StringBuffer();
            if (parameters.length == 0) {
                buffer.append("none");
            } else {
                ObjectActionParameter[] p = action.getParameters();
                for (int j = 0; j < parameters.length; j++) {
                    buffer.append(p[j].getName());
                    buffer.append(" (");
                    buffer.append(specificationLink(parameters[j].getSpecification()));
                    buffer.append(")<br>");
                    Class<? extends Facet>[] parameterFacets = p[j].getFacetTypes();
                    for (int k = 0; k < parameterFacets.length; k++) {
                        buffer.append("&nbsp;&nbsp;" + p[j].getFacet(parameterFacets[k]).toString() + "<br>");
                    }
                }
            }
            view.appendRow("Parameters", buffer.toString());
        }
        
        /*
        
        view.divider(spec.getFullName());
        view.appendRow("<pre class=\"debug\">" + str + "</pre>");
        */
    }

    private void specificationMembers(DebugView view, String label, ObjectMember[] members) {
        StringBuffer buffer = new StringBuffer();
        if (members.length == 0) {
            buffer.append("none");
        } else {
            for (int i = 0; i < members.length; i++) {
                buffer.append("<a href=\"#" + members[i].getId() + "\">" + members[i].getName() + "</a><br>");
            }
        }
        view.appendRow(label, buffer.toString());
    }

    private void speficationFacets(DebugView view, FacetHolder facetHolder) {
        Facet[] facets = facetHolder.getFacets(new AbstractFilter<Facet>() {
            public boolean accept(Facet facet) {
                return true;
            }
        });
        StringBuffer buffer = new StringBuffer();
        if (facets == null || facets.length == 0) {
            buffer.append("none");
        } else {
            for (int i = 0; i < facets.length; i++) {
                String facetType = facets[i].facetType().getName();
                buffer.append("<span class=\"facet-type\">" + facetType .substring(facetType.lastIndexOf('.') + 1)  + "</span>:  " + facets[i] + "<br>");
            }
        }
        view.appendRow("Facets", buffer.toString());
    }

    private void specificationClasses(DebugView view, String label, ObjectSpecification[] subclasses) {
        StringBuffer buffer = new StringBuffer();
        if (subclasses.length == 0) {
            buffer.append("none");
        } else {
            for (int i = 0; i < subclasses.length; i++) {
                buffer.append(specificationLink(subclasses[i]) + "<br>");
            }
        }
        view.appendRow(label, buffer.toString());
    }

    private String specificationLink(ObjectSpecification specification) {
        if (specification == null) {
            return "none";
        } else {
            String name = specification.getFullName();
            return "<a href=\"debug.app?action=specification&name=" + name + "\">" + name + "</a>";
        }
    }
    
    private void listSpecifications(DebugView view) {
        ObjectSpecification[] allSpecifications = IsisContext.getSpecificationLoader().allSpecifications();
        String[] names = new String[allSpecifications.length];
        for (int j = 0; j < allSpecifications.length; j++) {
            names[j] = allSpecifications[j].getFullName();
        }
        Arrays.sort(names);
        view.divider("Specifications");
        for (int j = 0; j < names.length; j++) {
            ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(names[j]);
            String name = spec.getSingularName();
            view.appendRow(name, specificationLink(spec));
        }
    }

    public void init() {}
}

