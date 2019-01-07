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

import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.postprocessors.param.ActionParameterDefaultsFacetFromAssociatedCollection;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
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
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorDefault;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public abstract class ActionLinkFactoryAbstract implements ActionLinkFactory {

    private static final long serialVersionUID = 1L;

    protected final EntityModel targetEntityModel;
    private final ScalarModel scalarModelForAssociationIfAny;

    protected ActionLinkFactoryAbstract(
            final EntityModel targetEntityModel,
            final ScalarModel scalarModelForAssociationIfAny) {
        this.targetEntityModel = targetEntityModel;
        this.scalarModelForAssociationIfAny = scalarModelForAssociationIfAny;
    }

    protected ActionLink newLink(
            final String linkId,
            final ObjectAction action,
            final ToggledMementosProvider toggledMementosProviderIfAny) {

        final ActionModel actionModel = ActionModel.create(this.targetEntityModel, action);

        final ActionLink link =
                new ActionLink(linkId, actionModel, action) {
                    private static final long serialVersionUID = 1L;

                    protected void doOnClick(final AjaxRequestTarget target) {

                        if(toggledMementosProviderIfAny != null) {

                            final PersistenceSession persistenceSession = getIsisSessionFactory()
                                    .getCurrentSession().getPersistenceSession();
                            final SpecificationLoader specificationLoader =
                                    getIsisSessionFactory().getSpecificationLoader();

                            final List<ObjectAdapterMemento> selectedMementos =
                                    toggledMementosProviderIfAny.getToggles();

                            final ImmutableList<Object> selectedPojos = FluentIterable.from(selectedMementos)
                                    .transform(new Function<ObjectAdapterMemento, Object>() {
                                        @Nullable @Override
                                        public Object apply(@Nullable final ObjectAdapterMemento input) {
                                            if(input == null) {
                                                return null;
                                            }
                                            final ObjectAdapter objectAdapter = input.getObjectAdapter(
                                                    AdapterManager.ConcurrencyChecking.NO_CHECK,
                                                    persistenceSession, specificationLoader);
                                            return objectAdapter != null ? objectAdapter.getObject() : null;
                                        }
                                    })
                                    .filter(Predicates.notNull())
                                    .toList();

                            final ActionPrompt actionPrompt = ActionParameterDefaultsFacetFromAssociatedCollection.withSelected(
                                    selectedPojos,
                                    new ActionParameterDefaultsFacetFromAssociatedCollection.SerializableRunnable<ActionPrompt>() {
                                        public ActionPrompt call() {
                                            return performOnClick(target);
                                        }
                                    }
                            );
                            if(actionPrompt != null) {
                                actionPrompt.setOnClose(new ActionPrompt.CloseHandler() {
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

        final ActionModel actionModel = actionLink.getActionModel();

        InlinePromptContext inlinePromptContext = determineInlinePromptContext();
        PromptStyle promptStyle = actionModel.getPromptStyle();

        if(inlinePromptContext == null || promptStyle.isDialog()) {
            final ActionPromptProvider promptProvider = ActionPromptProvider.Util.getFrom(actionLink.getPage());
            final ActionPrompt prompt = promptProvider.getActionPrompt(promptStyle);


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

                final Label label = new Label(prompt.getTitleId(), new AbstractReadOnlyModel<String>() {
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
                    final ObjectAdapter targetAdapter = actionModel.getTargetAdapter();

                    final EntityPage entityPage =

                            // disabling concurrency checking after the layout XML (grid) feature
                            // was throwing an exception when rebuild grid after invoking action
                            // not certain why that would be the case, but think it should be
                            // safe to simply disable while recreating the page to re-render back to user.
                            AdapterManager.ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(
                                    new Callable<EntityPage>() {
                                        @Override public EntityPage call() throws Exception {
                                            return new EntityPage(targetAdapter, null);
                                        }
                                    }
                            );

                    getIsisSessionFactory().getCurrentSession().getPersistenceSession().getTransactionManager().flushTransaction();

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
            final ObjectAdapter objectAdapter,
            final ObjectAction objectAction,
            final AbstractLink link,
            final String disabledReasonIfAny) {

        final boolean whetherReturnsBlobOrClob = ObjectAction.Util.returnsBlobOrClob(objectAction);

        return LinkAndLabel.newLinkAndLabel(objectAdapter, objectAction, link, disabledReasonIfAny, whetherReturnsBlobOrClob);
    }

    private InlinePromptContext determineInlinePromptContext() {
        return scalarModelForAssociationIfAny != null
                ? scalarModelForAssociationIfAny.getInlinePromptContext()
                : null;
    }


    //region > dependencies

    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) Application.get()).getComponentFactoryRegistry();
    }

    protected PageClassRegistry getPageClassRegistry() {
        return ((PageClassRegistryAccessor) Application.get()).getPageClassRegistry();
    }

    protected WicketViewerSettings getSettings() {
        return ((WicketViewerSettingsAccessor)Application.get()).getSettings();
    }

    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    private SpecificationLoader getSpecificationLoader() {
        return getIsisSessionFactory().getSpecificationLoader();
    }

    //endregion

}
