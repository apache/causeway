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
package org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;

import java.util.Optional;

import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug._Probe.EntryPoint;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.security.authentication.logout.LogoutMenu.LoginRedirect;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.ActionPromptWithExtraContent;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersPanel;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.BS3GridPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorDefault;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.NonNull;
import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;

public final class ActionLink
extends AjaxLink<ManagedObject>
implements IAjaxIndicatorAware {

    private static final long serialVersionUID = 1L;

    public static ActionLink create(
            final @NonNull String linkId,
            final @NonNull ActionModel actionModel) {

        val actionLink = new ActionLink(linkId, actionModel);
        actionLink.add(new CssClassAppender("noVeil"));
        return actionLink;
    }

    private final AjaxIndicatorAppender indicatorAppenderIfAny;
    protected transient IsisAppCommonContext commonContext;

    private ActionLink(
            final String id,
            final ActionModel model) {
        super(id, model);
        this.commonContext = model.getCommonContext();

        final boolean useIndicatorForNoArgAction = getSettings().isUseIndicatorForNoArgAction();
        this.indicatorAppenderIfAny =
                useIndicatorForNoArgAction
                ? new AjaxIndicatorAppender()
                : null;

        if(this.indicatorAppenderIfAny != null) {
            this.add(this.indicatorAppenderIfAny);
        }
    }

    @Override
    public void onClick(final AjaxRequestTarget target) {

        _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                + "originating from User clicking an Action Link.");

        doOnClick(this, target);
    }

    ActionModel getActionModel() {
        return (ActionModel) getModel();
    }

    public ObjectAction getObjectAction() {
        return getActionModel().getAction();
    }

    @Override
    protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
        super.updateAjaxAttributes(attributes);
        if(getSettings().isPreventDoubleClickForNoArgAction()) {
            PanelUtil.disableBeforeReenableOnComplete(attributes, this);
        }

        // allow the event to bubble so the menu is hidden after click on an item
        attributes.setEventPropagation(AjaxRequestAttributes.EventPropagation.BUBBLE);
    }

    public String getReasonDisabledIfAny() {
        // no point evaluating if not visible
        return isVisible() ? getActionModel().getUsabilityConsent().getReason() : null;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        // according to
        //   http://wicketinaction.com/2011/11/implement-wicket-component-visibility-changes-properly/
        // and
        //   http://apache-wicket.1842946.n4.nabble.com/vote-release-Wicket-1-4-14-td3056628.html#a3063795
        // should be using onConfigure rather than overloading.
        //
        // eg:
        //        setVisible(determineIfVisible());
        //        setEnabled(determineIfEnabled());
        //
        // and no longer override isVisible() and isEnabled().
        //
        // however, in the case of a button already rendered as visible/enabled that (due to changes
        // elsewhere in the state of the server-side system) should then become invisible/disabled, it seems
        // that onConfigure isn't called and so the action continues to display the prompt.
        // A check is only made when hit OK of the prompt.  This is too late to display a message, so (until
        // figure out a better way) gonna continue to override isVisible() and isEnabled()
    }

    @Override
    public boolean isVisible() {
        return getActionModel().getVisibilityConsent().isAllowed();
    }

    @Override
    @Programmatic
    public boolean isEnabled() {
        return getActionModel().getUsabilityConsent().isAllowed();
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);
        Buttons.fixDisabledState(this, tag);
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return this.indicatorAppenderIfAny != null
                ? this.indicatorAppenderIfAny.getMarkupId()
                : null;
    }

    protected WicketViewerSettings getSettings() {
        return ((WicketViewerSettingsAccessor) Application.get()).getSettings();
    }

    /**
     * @return the prompt, if not inline prompt
     */
    private ActionPrompt doOnClick(
            final ActionLink actionLink,
            final AjaxRequestTarget target) {

        val actionModel = actionLink.getActionModel();
        val inlinePromptContext = actionModel.getInlinePromptContext();
        val promptStyle = actionModel.getPromptStyle();

        if(inlinePromptContext == null || promptStyle.isDialog()) {
            val promptProvider = ActionPromptProvider.getFrom(actionLink.getPage());
            val actionOwnerSpec = actionModel.getActionOwner().getSpecification();
            val actionPrompt = promptProvider.getActionPrompt(promptStyle, actionOwnerSpec.getBeanSort());

            //
            // previously this if/else was in the ActionParametersPanel
            //
            // now though we only build that panel if we know that there *are* parameters.
            //
            if(actionModel.hasParameters()) {

                val actionParametersPanel = (ActionParametersPanel)
                        getComponentFactoryRegistry()
                        .createComponent(
                                ComponentType.ACTION_PROMPT, actionPrompt.getContentId(), actionModel);

                actionParametersPanel.setShowHeader(false);

                val label = Wkt.label(actionPrompt.getTitleId(), actionModel::getFriendlyName);
                actionPrompt.setTitle(label, target);
                actionPrompt.setPanel(actionParametersPanel, target);
                actionPrompt.showPrompt(target);

                if(actionPrompt instanceof ActionPromptWithExtraContent) {
                    final ActionPromptWithExtraContent promptWithExtraContent =
                            (ActionPromptWithExtraContent) actionPrompt;

                    final ObjectAction action = actionModel.getAction();
                    if(action instanceof ObjectActionMixedIn) {
                        final ObjectActionMixedIn actionMixedIn = (ObjectActionMixedIn) action;
                        final ObjectSpecification mixinSpec = actionMixedIn.getMixinType();

                        if(mixinSpec.isViewModel()) {

                            val commonContext = getCommonContext();
                            final ManagedObject targetAdapterForMixin = action.realTargetAdapter(actionModel.getActionOwner());
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

                return actionPrompt;

            } else {


                final Page page = actionLink.getPage();

                // returns true - if redirecting to new page, or repainting all components.
                // returns false - if invalid args; if concurrency exception;

                final FormExecutor formExecutor =
                        new FormExecutorDefault(_Either.left(actionModel));
                boolean succeeded = formExecutor.executeAndProcessResults(page, null, null, actionModel.isWithinPrompt());

                if(succeeded) {

                    // intercept redirect request to sign-in page
                    Optional.ofNullable(actionModel.getObject())
                    .ifPresent(actionResultAdapter->{
                        val actionResultObjectType = actionResultAdapter.getSpecification().getLogicalTypeName();
                        if(LoginRedirect.LOGICAL_TYPE_NAME.equals(actionResultObjectType)) {
                            val commonContext = actionModel.getCommonContext();
                            val pageClassRegistry = commonContext.lookupServiceElseFail(PageClassRegistry.class);
                            val signInPage = pageClassRegistry.getPageClass(PageType.SIGN_IN);
                            RequestCycle.get().setResponsePage(signInPage);
                        }
                    });

                    // else nothing to do

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
                    val targetAdapter = actionModel.getParentObject();

                    final EntityPage entityPage = EntityPage.ofAdapter(getCommonContext(), targetAdapter);

                    getCommonContext().getTransactionService().flushTransaction();

                    // "redirect-after-post"
                    RequestCycle.get().setResponsePage(entityPage);

                }
            }


        } else {

            MarkupContainer scalarTypeContainer = inlinePromptContext.getScalarTypeContainer();

            getComponentFactoryRegistry().addOrReplaceComponent(scalarTypeContainer,
                    ScalarPanelAbstract.ID_SCALAR_IF_REGULAR_INLINE_PROMPT_FORM, ComponentType.PARAMETERS, actionModel);

            inlinePromptContext.getScalarIfRegular().setVisible(false);
            inlinePromptContext.getScalarIfRegularInlinePromptForm().setVisible(true);

            target.add(scalarTypeContainer);
        }

        return null;
    }

    // -- DEPENDENCIES

    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

    private ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor) Application.get()).getComponentFactoryRegistry();
    }

}
