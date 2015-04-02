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

package org.apache.isis.viewer.scimpi.dispatcher.debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.util.Dump;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.ForbiddenException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

public class DebugAction implements Action {
    private final Dispatcher dispatcher;

    public DebugAction(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public void debug(final DebugBuilder debug) {
    }

    @Override
    public void process(final RequestContext context) throws IOException {
        if (context.isDebugDisabled()) {
            throw new ForbiddenException("Can't access debug action when debug is disabled");
        }

        final String action = context.getParameter("action");
        if ("list-i18n".equals(action)) {
            i18n(context, null);
        } else if ("list-authorization".equals(action)) {
            authorization(context, null);
        } else if (context.getParameter("mode") != null) {
            final boolean isDebugOn = context.getParameter("mode").equals("debug");
            context.addVariable("debug-on", isDebugOn, Scope.SESSION);
            // TODO need to use configuration to find path
            context.setRequestPath("/debug/debug.shtml");
        } else {

            // TODO remove - replaced by Debug tag
            final DebugHtmlWriter view = new DebugHtmlWriter(context.getWriter(), true);
            view.appendln("<div class=\"links\">");
            view.appendln("<a href=\"debug.app?action=system\">System</a>");
            view.appendln(" | <a href=\"debug.app?action=specifications\">List specifications</a>");
            view.appendln(" | <a href=\"debug.app?action=list-i18n\">I18N File</a>");
            view.appendln(" | <a href=\"debug.app?action=list-authorization\">Authorization File</a>");
            view.appendln(" | <a href=\"debug.app?action=context\">Context</a>");
            view.appendln(" | <a href=\"debug.app?action=dispatcher\">Dispatcher</a>");
            view.appendln("</div>");

            if ("specifications".equals(action)) {
                listSpecifications(view);
            } else if ("specification".equals(action)) {
                // specification(context, view);
            } else if ("object".equals(action)) {
                object(context, view);
            } else if ("system".equals(action)) {
                system(context, view);
            } else if ("context".equals(action)) {
                context.append(view);
            } else if ("dispatcher".equals(action)) {
                dispatcher.debug(view);
            }

            context.clearRequestedPath();
        }
    }

    private void object(final RequestContext context, final DebugHtmlWriter view) {
        final ObjectAdapter object = context.getMappedObjectOrResult(context.getParameter("object"));
        final DebugString str = new DebugString();
        Dump.adapter(object, str);
        Dump.graph(object, IsisContext.getAuthenticationSession(), str);
        view.appendTitle(object.getSpecification().getFullIdentifier());
        view.appendln("<pre class=\"debug\">" + str + "</pre>");
    }

    private void system(final RequestContext context, final DebugHtmlWriter view) {
        final DebuggableWithTitle[] debug = IsisContext.debugSystem();
        view.appendTitle("System");
        for (final DebuggableWithTitle element2 : debug) {
            final DebugString str = new DebugString();
            element2.debugData(str);
            view.appendTitle(element2.debugTitle());
            view.appendln("<pre class=\"debug\">" + str + "</pre>");
        }
    }

    private void i18n(final RequestContext context, final DebugHtmlWriter view) {
        final Collection<ObjectSpecification> allSpecifications = getSpecificationLoader().allSpecifications();
        final List<ObjectSpecification> specs = Lists.newArrayList(allSpecifications);
        Collections.sort(specs, new Comparator<ObjectSpecification>() {
            @Override
            public int compare(final ObjectSpecification o1, final ObjectSpecification o2) {
                return o1.getShortIdentifier().compareTo(o2.getShortIdentifier());
            }
        });
        final Function<ObjectSpecification, String> className = ObjectSpecification.FUNCTION_FULLY_QUALIFIED_CLASS_NAME;
        final List<String> fullIdentifierList = Lists.newArrayList(Collections2.transform(specs, className));
        for (final String fullIdentifier : fullIdentifierList) {
            final ObjectSpecification spec = getSpecificationLoader().loadSpecification(fullIdentifier);
            if (spec.getAssociations(Contributed.EXCLUDED).size() == 0 && spec.getObjectActions(Contributed.EXCLUDED).size() == 0) {
                continue;
            }
            final String name = spec.getIdentifier().toClassIdentityString();
            context.getWriter().append("# " + spec.getShortIdentifier() + "\n");
            for (final ObjectAssociation assoc : spec.getAssociations(Contributed.EXCLUDED)) {
                context.getWriter().append("#" + name + ".property." + assoc.getId() + ".name" + "=\n");
                context.getWriter().append("#" + name + ".property." + assoc.getId() + ".description" + "=\n");
                context.getWriter().append("#" + name + ".property." + assoc.getId() + ".help" + "=\n");
            }
            for (final ObjectAction action : spec.getObjectActions(Contributed.EXCLUDED)) {
                context.getWriter().append("#" + name + ".action." + action.getId() + ".name" + "=\n");
                context.getWriter().append("#" + name + ".action." + action.getId() + ".description" + "=\n");
                context.getWriter().append("#" + name + ".action." + action.getId() + ".help" + "=\n");
            }
            context.getWriter().append("\n");
        }
    }

    private void authorization(final RequestContext context, final DebugHtmlWriter view) {
        final Collection<ObjectSpecification> allSpecifications = getSpecificationLoader().allSpecifications();
        final List<ObjectSpecification> specs = Lists.newArrayList(allSpecifications);
        Collections.sort(specs, new Comparator<ObjectSpecification>() {
            @Override
            public int compare(final ObjectSpecification o1, final ObjectSpecification o2) {
                return o1.getShortIdentifier().compareTo(o2.getShortIdentifier());
            }
        });
        final Function<ObjectSpecification, String> className = ObjectSpecification.FUNCTION_FULLY_QUALIFIED_CLASS_NAME;
        final List<String> fullIdentifierList = Lists.newArrayList(Collections2.transform(specs, className));
        
        for (final String fullIdentifier : fullIdentifierList) {
            final ObjectSpecification spec = getSpecificationLoader().loadSpecification(fullIdentifier);
            if (spec.getAssociations(Contributed.EXCLUDED).size() == 0 && spec.getObjectActions(Contributed.EXCLUDED).size() == 0) {
                continue;
            }
            final String name = spec.getIdentifier().toClassIdentityString();
            boolean isAbstract = spec.isAbstract();
            context.getWriter().append("### " + spec.getShortIdentifier() + (isAbstract ? " (abstract)" : "") + " ###\n");
            context.getWriter().append((isAbstract ? "#" : "") + name + ":roles\n\n");
        }
        context.getWriter().append("\n\n");
        
        for (final String fullIdentifier : fullIdentifierList) {
            final ObjectSpecification spec = getSpecificationLoader().loadSpecification(fullIdentifier);
            if (spec.getAssociations(Contributed.EXCLUDED).size() == 0 && spec.getObjectActions(Contributed.EXCLUDED).size() == 0) {
                continue;
            }
            final String name = spec.getIdentifier().toClassIdentityString();
            boolean isAbstract = spec.isAbstract();
            context.getWriter().append("### " + spec.getShortIdentifier() + (isAbstract ? " (abstract)" : "") + " ###\n");
            context.getWriter().append((isAbstract ? "#" : "") + name + ":roles\n");
            for (final ObjectAssociation assoc : spec.getAssociations(Contributed.EXCLUDED)) {
                context.getWriter().append("#" + name + "#" + assoc.getId() + ":roles\n");
                // context.getWriter().append("#" + name + ".property." +
                // assoc.getId() + ".description" + "=\n");
                // context.getWriter().append("#" + name + ".property." +
                // assoc.getId() + ".help" + "=\n");
            }
            for (final ObjectAction action : spec.getObjectActions(Contributed.EXCLUDED)) {
                context.getWriter().append("#" + name + "#" + action.getId() + "():roles\n");
                // context.getWriter().append("#" + name + ".action." +
                // action.getId() + ".description" + "=\n");
                // context.getWriter().append("#" + name + ".action." +
                // action.getId() + ".help" + "=\n");
            }
            context.getWriter().append("\n");
        }
    }

    private void listSpecifications(final DebugHtmlWriter view) {
        final List<ObjectSpecification> fullIdentifierList = new ArrayList<ObjectSpecification>(getSpecificationLoader().allSpecifications());
        Collections.sort(fullIdentifierList, ObjectSpecification.COMPARATOR_SHORT_IDENTIFIER_IGNORE_CASE);
        view.appendTitle("Specifications");
        for (final ObjectSpecification spec : fullIdentifierList) {
            final String name = spec.getSingularName();
            view.appendln(name, "");
            // view.appendln(name, specificationLink(spec));
        }

        /*
         * new Comparator<ObjectSpecification>() { public int
         * compare(ObjectSpecification o1, ObjectSpecification o2) { return
         * o1.getSingularName().compareTo(o2.getSingularName()); }});
         * 
         * /* Collection<ObjectSpecification> allSpecifications =
         * getSpecificationLoader().allSpecifications(); Collection<String> list
         * = Collections2.transform(allSpecifications,
         * ObjectSpecification.COMPARATOR_SHORT_IDENTIFIER_IGNORE_CASE); final
         * List<String> fullIdentifierList = Lists.newArrayList(list); /*
         * Collections.sort(fullIdentifierList, new
         * Comparator<ObjectSpecification>() { public int
         * compare(ObjectSpecification o1, ObjectSpecification o2) { return
         * o1.getSingularName().compareTo(o2.getSingularName()); }});
         */
        /*
         * view.divider("Specifications"); for (String fullIdentifier :
         * fullIdentifierList) { ObjectSpecification spec =
         * getSpecificationLoader().loadSpecification(fullIdentifier); String
         * name = spec.getSingularName(); view.appendRow(name,
         * specificationLink(spec)); }
         */
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    @Override
    public void init() {
    }
}
