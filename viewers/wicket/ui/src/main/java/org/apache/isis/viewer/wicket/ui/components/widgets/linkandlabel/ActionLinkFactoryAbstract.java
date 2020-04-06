/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */

package org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;

import java.io.Serializable;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.postprocessors.param.ActionParameterDefaultsFacetFromAssociatedCollection;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.viewer.common.model.link.ActionLinkFactory;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.ActionPromptWithExtraContent;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.InlinePromptContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ToggledMementosProvider;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionFormExecutorStrategy;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersPanel;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.BS3GridPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorDefault;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import lombok.val;

public abstract class ActionLinkFactoryAbstract 
implements ActionLinkFactory<LinkAndLabel>, Serializable {

    private static final long serialVersionUID = 1L;

    protected final EntityModel targetEntityModel;
    private final ScalarModel scalarModelForAssociationIfAny;

    protected ActionLinkFactoryAbstract(
            final EntityModel targetEntityModel,
            final ScalarModel scalarModelForAssociationIfAny) {
        
        this.targetEntityModel = targetEntityModel;
        this.scalarModelForAssociationIfAny = scalarModelForAssociationIfAny;
    }
    
    public abstract LinkAndLabel newLink(
            ObjectAction objectAction,
            String linkId,
            ToggledMementosProvider toggledMementosProviderIfAny);

    protected ActionLink newLink(
            final String linkId,
            final ObjectAction action,
            final ToggledMementosProvider toggledMementosProviderIfAny) {

        val actionModel = ActionModel.create(this.targetEntityModel, action);
        val commonContext = actionModel.getCommonContext();

        final ActionLink link = new ActionLink(commonContext, linkId, actionModel, action) {
            
            private static final long serialVersionUID = 1L;

            @Override
            protected void doOnClick(final AjaxRequestTarget target) {

                if(toggledMementosProviderIfAny != null) {

                    val selectedMementos = toggledMementosProviderIfAny.getToggles();
                    val selectedPojos = selectedMementos
                            .map(super.getCommonContext()::reconstructObject)
                            .map(ManagedObject::getPojo);
                    
                    val actionPrompt = ActionParameterDefaultsFacetFromAssociatedCollection
                            .applyWithSelected(
                                    selectedPojos,
                                    // if this lambda still needs to be serializable uncomment the cast ... 
                                    //(Function<AjaxRequestTarget, ActionPrompt>&Serializable) 
                                    this::performOnClick,
                                    target);
                    
                    if(actionPrompt != null) {
                        actionPrompt.setOnClose(new ActionPrompt.CloseHandler() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void close(final AjaxRequestTarget target) {
                                toggledMementosProviderIfAny.clearToggles(target);
                            }
                        });
                    }

                } else {
                    performOnClick(target);
                }
            }

            private ActionPrompt performOnClick(final AjaxRequestTarget target) {
                return ActionLinkFactoryAbstract.this.onClick(this, target);
            }

        };

        link.add(new CssClassAppender("noVeil"));
        return link;
    }

    /**
     * @return the prompt, if not inline prompt
     */
    private ActionPrompt onClick(
            final ActionLink actionLink,
            final AjaxRequestTarget target) {

        val actionModel = actionLink.getActionModel();

        val inlinePromptContext = determineInlinePromptContext();
        val promptStyle = actionModel.getPromptStyle();

        if(inlinePromptContext == null || promptStyle.isDialog()) {
            final ActionPromptProvider promptProvider = ActionPromptProvider.getFrom(actionLink.getPage());
            val spec = actionModel.getTargetAdapter().getSpecification();

            final ActionPrompt prompt = promptProvider.getActionPrompt(promptStyle, spec.getBeanSort());


            //
            // previously this if/else was in the ActionParametersPanel
            //
            // now though we only build that panel if we know that there *are* parameters.
            //
            if(actionModel.hasParameters()) {

                final ActionParametersPanel actionParametersPanel =
                        (ActionParametersPanel) getComponentFactoryRegistry().createComponent(
                                ComponentType.ACTION_PROMPT, prompt.getContentId(), actionModel);

                actionParametersPanel.setShowHeader(false);

                final Label label = new Label(prompt.getTitleId(), new IModel<String>() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public String getObject() {
                        final ObjectAction action = actionModel.getActionMemento().getAction(getSpecificationLoader());
                        return action.getName();
                    }
                });
                prompt.setTitle(label, target);
                prompt.setPanel(actionParametersPanel, target);
                actionParametersPanel.setActionPrompt(prompt);
                prompt.showPrompt(target);

                if(prompt instanceof ActionPromptWithExtraContent) {
                    final ActionPromptWithExtraContent promptWithExtraContent =
                            (ActionPromptWithExtraContent) prompt;

                    final ObjectAction action = actionModel.getActionMemento().getAction(getSpecificationLoader());
                    if(action instanceof ObjectActionMixedIn) {
                        final ObjectActionMixedIn actionMixedIn = (ObjectActionMixedIn) action;
                        final ObjectSpecification mixinSpec = actionMixedIn.getMixinType();

                        if(mixinSpec.isViewModel()) {

                            val commonContext = getCommonContext();
                            final ManagedObject targetAdapterForMixin = action.realTargetAdapter(actionModel.getTargetAdapter());
                            final EntityModel entityModelForMixin = 
                                    EntityModel.ofAdapter(commonContext, targetAdapterForMixin);

                            final GridFacet facet = mixinSpec.getFacet(GridFacet.class);
                            final Grid gridForMixin = facet.getGrid(targetAdapterForMixin);

                            final String extraContentId = promptWithExtraContent.getExtraContentId();

                            if(gridForMixin instanceof BS3Grid) {
                                final BS3Grid bs3Grid = (BS3Grid) gridForMixin;
                                final BS3GridPanel gridPanel = new BS3GridPanel(extraContentId, entityModelForMixin, bs3Grid);
                                promptWithExtraContent.setExtraContentPanel(gridPanel, target);
                            }
                        }
                    }
                }

                return prompt;

            } else {


                final Page page = actionLink.getPage();

                // returns true - if redirecting to new page, or repainting all components.
                // returns false - if invalid args; if concurrency exception;

                final FormExecutor formExecutor =
                        new FormExecutorDefault<>(new ActionFormExecutorStrategy(actionModel));
                boolean succeeded = formExecutor.executeAndProcessResults(page, null, null, actionModel.isWithinPrompt());

                if(succeeded) {

                    // nothing to do

                    //
                    // the formExecutor will have either redirected, or scheduled a response,
                    // or repainted components as required
                    //

                } else {

                    // render the target entity again
                    //
                    // (One way this can occur is if an event subscriber has a defect and throws an exception; in which case
                    // the EventBus' exception handler will automatically veto.  This results in a growl message rather than
                    // an error page, but is probably 'good enough').
                    val targetAdapter = actionModel.getTargetAdapter();

                    final EntityPage entityPage = new EntityPage(getCommonContext(), targetAdapter);

                    getCommonContext().getTransactionService().flushTransaction();

                    // "redirect-after-post"
                    final RequestCycle requestCycle = RequestCycle.get();
                    requestCycle.setResponsePage(entityPage);

                }
            }


        } else {

            MarkupContainer scalarTypeContainer = inlinePromptContext.getScalarTypeContainer();

            actionModel.setInlinePromptContext(inlinePromptContext);
            getComponentFactoryRegistry().addOrReplaceComponent(scalarTypeContainer,
                    ScalarPanelAbstract2.ID_SCALAR_IF_REGULAR_INLINE_PROMPT_FORM, ComponentType.PARAMETERS, actionModel);

            inlinePromptContext.getScalarIfRegular().setVisible(false);
            inlinePromptContext.getScalarIfRegularInlinePromptForm().setVisible(true);

            target.add(scalarTypeContainer);
        }

        return null;
    }

    protected LinkAndLabel newLinkAndLabel(
            final ManagedObject objectAdapter,
            final ObjectAction objectAction,
            final AbstractLink link) {

        final boolean whetherReturnsBlobOrClob = ObjectAction.Util.returnsBlobOrClob(objectAction);

        return LinkAndLabel.newLinkAndLabel(link, objectAdapter, objectAction, whetherReturnsBlobOrClob);
    }

    private InlinePromptContext determineInlinePromptContext() {
        return scalarModelForAssociationIfAny != null
                ? scalarModelForAssociationIfAny.getInlinePromptContext()
                        : null;
    }


    // -- dependencies

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) Application.get()).getComponentFactoryRegistry();
    }

    protected PageClassRegistry getPageClassRegistry() {
        return ((PageClassRegistryAccessor) Application.get()).getPageClassRegistry();
    }

    protected WicketViewerSettings getSettings() {
        return ((WicketViewerSettingsAccessor)Application.get()).getSettings();
    }

    protected ServiceRegistry getServiceRegistry() {
        return getCommonContext().getServiceRegistry();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return getCommonContext().getSpecificationLoader();
    }

    protected IsisWebAppCommonContext getCommonContext() {
        return targetEntityModel.getCommonContext();
    }

    

}
