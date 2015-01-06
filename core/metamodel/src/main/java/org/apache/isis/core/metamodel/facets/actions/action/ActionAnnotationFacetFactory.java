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

package org.apache.isis.core.metamodel.facets.actions.action;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Command;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.actions.bulk.annotation.BulkFacetAnnotation;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.command.annotation.CommandFacetAnnotation;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.annotation.PublishedActionFacetAnnotation;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.annotations.actionsemantics.ActionSemanticsFacetAnnotation;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class ActionAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware, IsisConfigurationAware {

    private ServicesInjector servicesInjector;
    private IsisConfiguration configuration;

    public ActionAnnotationFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        processInteraction(processMethodContext);
        processHidden(processMethodContext);
        processRestrictTo(processMethodContext);
        processSemantics(processMethodContext);
        processInvokeOn(processMethodContext);

        // must come after processing semantics
        processCommand(processMethodContext);

        // must come after processing semantics
        processPublishing(processMethodContext);

        processTypeOf(processMethodContext);
    }

    private void processInteraction(final ProcessMethodContext processMethodContext) {

        // interaction is handled by ActionInteractionFacetFactory, because the
        // deprecated annotations must also be supported.

    }

    private void processHidden(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                HiddenFacetForActionAnnotation.create(action, holder));
    }

    private void processRestrictTo(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                PrototypeFacetForActionAnnotation.create(action, holder));
    }

    private void processSemantics(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        ActionSemanticsFacet facet;

        //
        // check for the deprecated @ActionSemantics first, because the
        // @Action(semantics=...) has a default of NON_IDEMPOTENT that would otherwise be used
        //
        final ActionSemantics actionSemantics = Annotations.getAnnotation(method, ActionSemantics.class);
        facet = ActionSemanticsFacetAnnotation.create(actionSemantics, holder);

        // else check for @Action(semantics=...)
        if(facet == null) {
            facet = ActionSemanticsFacetForActionAnnotation.create(action, holder);
        }
        FacetUtil.addFacet(facet);
    }

    private void processInvokeOn(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        BulkFacet bulkFacet;

        // check for the deprecated @Bulk annotation first
        final Bulk annotation = Annotations.getAnnotation(method, Bulk.class);
        bulkFacet = BulkFacetAnnotation.create(annotation, holder);

        // else check for @Action(invokeOn=...)
        if(bulkFacet == null) {
            bulkFacet = BulkFacetForActionAnnotation.create(action, holder);
        }

        FacetUtil.addFacet(bulkFacet);
    }

    private void processCommand(
            final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasTransactionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        CommandFacet commandFacet;

        // check for deprecated @Command annotation first
        final Command annotation = Annotations.getAnnotation(processMethodContext.getMethod(), Command.class);
        commandFacet = CommandFacetAnnotation.create(annotation, processMethodContext.getFacetHolder());

        // else check for @Action(command=...)
        if(commandFacet == null) {
            commandFacet = CommandFacetForActionAnnotation.create(action, configuration, holder);
        }

        FacetUtil.addFacet(commandFacet);
    }

    private void processPublishing(
            final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasTransactionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }


        PublishedActionFacet publishedActionFacet;

        // check for deprecated @PublishedAction annotation first
        final PublishedAction annotation = Annotations.getAnnotation(processMethodContext.getMethod(), PublishedAction.class);
        publishedActionFacet = PublishedActionFacetAnnotation.create(annotation, holder);

        // else check for @Action(publishing=...)
        if(publishedActionFacet == null) {
            publishedActionFacet = PublishedActionFacetForActionAnnotation.create(action, configuration, holder);
        }

        FacetUtil.addFacet(publishedActionFacet);
    }


    private void processTypeOf(final ProcessMethodContext processMethodContext) {

        // typeOf is handled by TypeOfFacetOnActionAnnotationFactory, because the
        // deprecated annotations etc that must also be supported.

    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }
}
