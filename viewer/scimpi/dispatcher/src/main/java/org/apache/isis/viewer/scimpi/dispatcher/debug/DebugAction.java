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

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.util.Dump;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.ForbiddenException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;


public class DebugAction implements Action {
    private Dispatcher dispatcher;

    public DebugAction(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public void debug(DebugBuilder debug) {}

    @Override
    public void process(RequestContext context) throws IOException {
        if (context.isDebugDisabled()) {
            throw new ForbiddenException("Can't access debug action when debug is disabled");
        }
        
        String action = context.getParameter("action");
        if ("list-i18n".equals(action)) {
            i18n(context, null);            
        } else if ("list-authorization".equals(action)) {
            authorization(context, null);      
        } else if (context.getParameter("mode") != null) {            
            boolean isDebugOn = context.getParameter("mode").equals("debug");
            context.addVariable("debug", isDebugOn, Scope.SESSION);
            // TODO need to use configuration to find path
            context.setRequestPath("/debug/debug.shtml");
        } else {
            
            
            
            // TODO remove - replaced by Debug tag
            DebugWriter view = new DebugWriter(context.getWriter(), true);
            view.appendln("<div class=\"links\">");
            view.appendln("<a href=\"debug.app?action=system\">System</a>");
            view.appendln(" | <a href=\"debug.app?action=specifications\">List specifications</a>");
            view.appendln(" | <a href=\"debug.app?action=i18n\">I18N File</a>");
            view.appendln(" | <a href=\"debug.app?action=authorization\">Authorization File</a>");
            view.appendln(" | <a href=\"debug.app?action=context\">Context</a>");
            view.appendln(" | <a href=\"debug.app?action=dispatcher\">Dispatcher</a>");
            view.appendln("</div>");
            
            if ("specifications".equals(action)) {
                listSpecifications(view);
            } else   if ("specification".equals(action)) {
          //      specification(context, view);            
            } else   if ("object".equals(action)) {
                object(context, view);            
            } else   if ("system".equals(action)) {
                system(context, view);            
            } else   if ("context".equals(action)) {
                context.append(view);
            } else   if ("dispatcher".equals(action)) {
                dispatcher.debug(view);
            }

            context.clearRequestedPath();
        }
    }

    private void object(RequestContext context, DebugWriter view) {
        ObjectAdapter object = context.getMappedObjectOrResult(context.getParameter("object"));
        DebugString str = new DebugString();
        Dump.adapter(object, str);
        Dump.graph(object, str, IsisContext.getAuthenticationSession());
        view.appendTitle(object.getSpecification().getFullIdentifier());
        view.appendln("<pre class=\"debug\">" + str + "</pre>");
    }

    private void system(RequestContext context, DebugWriter view) {
        DebuggableWithTitle[] debug = IsisContext.debugSystem();
        view.appendTitle("System");
        for (int i = 0; i < debug.length; i++) {
            DebugString str = new DebugString();
            debug[i].debugData(str);
            view.appendTitle(debug[i].debugTitle());
            view.appendln("<pre class=\"debug\">" + str + "</pre>");
        }
    }

    private void i18n(RequestContext context, DebugWriter view) {
        Collection<ObjectSpecification> allSpecifications = getSpecificationLoader().allSpecifications();
        final List<ObjectSpecification> specs = Lists.newArrayList(allSpecifications);
        Collections.sort(specs, new Comparator<ObjectSpecification>() {
            public int compare(ObjectSpecification o1, ObjectSpecification o2) {
                return o1.getShortIdentifier().compareTo(o2.getShortIdentifier());
            }
        });
        Function<ObjectSpecification, String> className = ObjectSpecification.FUNCTION_FULLY_QUALIFIED_CLASS_NAME;
        final List<String> fullIdentifierList = Lists.newArrayList(Collections2.transform(specs, className));
        for (String fullIdentifier : fullIdentifierList) {
            ObjectSpecification spec = getSpecificationLoader().loadSpecification(fullIdentifier);
            if (spec.getAssociations().size() == 0 && spec.getObjectActionsAll().size() == 0) {
                continue;
            }
            String name = spec.getIdentifier().toClassIdentityString();
            context.getWriter().append("# " + spec.getShortIdentifier() +"\n");
            for (ObjectAssociation assoc : spec.getAssociations()) {
                context.getWriter().append("#" + name + ".property." + assoc.getId() + ".name" + "=\n");
                context.getWriter().append("#" + name + ".property." + assoc.getId() + ".description" + "=\n");
                context.getWriter().append("#" + name + ".property." + assoc.getId() + ".help" + "=\n");
            }
            for (ObjectAction action : spec.getObjectActionsAll()) {
                context.getWriter().append("#" + name + ".action." + action.getId() + ".name" + "=\n");
                context.getWriter().append("#" + name + ".action." + action.getId() + ".description" + "=\n");
                context.getWriter().append("#" + name + ".action." + action.getId() + ".help" + "=\n");
            }
            context.getWriter().append("\n");
        }
    }

    private void authorization(RequestContext context, DebugWriter view) {
        Collection<ObjectSpecification> allSpecifications = getSpecificationLoader().allSpecifications();
        final List<ObjectSpecification> specs = Lists.newArrayList(allSpecifications);
        Collections.sort(specs, new Comparator<ObjectSpecification>() {
            public int compare(ObjectSpecification o1, ObjectSpecification o2) {
                return o1.getShortIdentifier().compareTo(o2.getShortIdentifier());
            }
        });
        Function<ObjectSpecification, String> className = ObjectSpecification.FUNCTION_FULLY_QUALIFIED_CLASS_NAME;
        final List<String> fullIdentifierList = Lists.newArrayList(Collections2.transform(specs, className));
        for (String fullIdentifier : fullIdentifierList) {
            ObjectSpecification spec = getSpecificationLoader().loadSpecification(fullIdentifier);
            if (spec.getAssociations().size() == 0 && spec.getObjectActionsAll().size() == 0) {
                continue;
            }
            String name = spec.getIdentifier().toClassIdentityString();
            context.getWriter().append("# " + spec.getShortIdentifier() +"\n");
            context.getWriter().append("" + name + ":roles\n");
            for (ObjectAssociation assoc : spec.getAssociations()) {
                context.getWriter().append("#" + name + "#" + assoc.getId() + ":roles\n");
             //   context.getWriter().append("#" + name + ".property." + assoc.getId() + ".description" + "=\n");
              //  context.getWriter().append("#" + name + ".property." + assoc.getId() + ".help" + "=\n");
            }
            for (ObjectAction action : spec.getObjectActionsAll()) {
                context.getWriter().append("#" + name + "#" + action.getId() + "():roles\n");
             //   context.getWriter().append("#" + name + ".action." + action.getId() + ".description" + "=\n");
            //    context.getWriter().append("#" + name + ".action." + action.getId() + ".help" + "=\n");
            }
            context.getWriter().append("\n");
        }
    }

    private void listSpecifications(DebugWriter view) {
        List<ObjectSpecification> fullIdentifierList = new ArrayList<ObjectSpecification>(getSpecificationLoader().allSpecifications());
        Collections.sort(fullIdentifierList, ObjectSpecification.COMPARATOR_SHORT_IDENTIFIER_IGNORE_CASE);
        view.appendTitle("Specifications");
        for (ObjectSpecification spec : fullIdentifierList) {
            String name = spec.getSingularName();
      //      view.appendln(name, specificationLink(spec));
        }

        
        
/*                new Comparator<ObjectSpecification>() {
            public int compare(ObjectSpecification o1, ObjectSpecification o2) {
                return o1.getSingularName().compareTo(o2.getSingularName());
            }});
        
/*        
        Collection<ObjectSpecification> allSpecifications = getSpecificationLoader().allSpecifications();
        Collection<String> list = Collections2.transform(allSpecifications, ObjectSpecification.COMPARATOR_SHORT_IDENTIFIER_IGNORE_CASE);
        final List<String> fullIdentifierList = Lists.newArrayList(list);
        /*
        Collections.sort(fullIdentifierList, new Comparator<ObjectSpecification>() {
            public int compare(ObjectSpecification o1, ObjectSpecification o2) {
                return o1.getSingularName().compareTo(o2.getSingularName());
            }});
            */
        /*
        view.divider("Specifications");
        for (String fullIdentifier : fullIdentifierList) {
            ObjectSpecification spec = getSpecificationLoader().loadSpecification(fullIdentifier);
            String name = spec.getSingularName();
            view.appendRow(name, specificationLink(spec));
        }
        */
    }

    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    @Override
    public void init() {}
}

