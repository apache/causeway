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
package org.apache.causeway.viewer.wicket.ui.components.widgets.actionlink;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.request.cycle.RequestCycle;
import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.debug._Probe.EntryPoint;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.commons.model.action.HasManagedAction;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.links.Menuable;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.causeway.viewer.wicket.model.models.ActionPromptWithExtraContent;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.app.registry.HasComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.FrameFragment;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.BSGridPanelFactory;
import org.apache.causeway.viewer.wicket.ui.exec.FormExecutorDefault;
import org.apache.causeway.viewer.wicket.ui.pages.obj.DomainObjectPage;
import org.apache.causeway.viewer.wicket.ui.panels.PanelUtil;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import static org.apache.causeway.commons.internal.base._Casts.castTo;

/**
 *
 * @implNote <pre>
 * according to
 * http://wicketinaction.com/2011/11/implement-wicket-component-visibility-changes-properly/
 * and
 * http://apache-wicket.1842946.n4.nabble.com/vote-release-Wicket-1-4-14-td3056628.html#a3063795
 * should be using onConfigure rather than overloading.
 * eg:
 *  setVisible(determineIfVisible());
 *  setEnabled(determineIfEnabled());
 *
 * and no longer override isVisible() and isEnabled().
 *
 * however, in the case of a button already rendered as visible/enabled that (due to changes
 * elsewhere in the state of the server-side system) should then become invisible/disabled, it seems
 * that onConfigure isn't called and so the action continues to display the prompt.
 * A check is only made when hit OK of the prompt.  This is too late to display a message, so (until
 * figure out a better way) gonna continue to override isVisible() and isEnabled()
 *  </pre>
 */
public final class ActionLink
extends IndicatingAjaxLink<ManagedObject>
implements HasMetaModelContext, Menuable, HasManagedAction {

    private static final long serialVersionUID = 1L;
    private static final String ID_ACTION_LINK = "actionLink";

    public static ActionLink create(
            final @NonNull ActionModel actionModel) {

        var actionLink = new ActionLink(ID_ACTION_LINK, actionModel);
        return Wkt.cssAppend(actionLink, "noVeil");
    }

    private final AjaxIndicatorAppender indicatorAppenderIfAny;

    private ActionLink(
            final String id,
            final ActionModel model) {
        super(id, model);

        _Assert.assertNotNull(model.getAction(), "ActionLink requires an Action");

        this.indicatorAppenderIfAny = getSettings().isUseIndicatorForNoArgAction()
                ? new AjaxIndicatorAppender()
                : null;

        if(this.indicatorAppenderIfAny != null) {
            this.add(this.indicatorAppenderIfAny);
        }
    }

    public ActionModel getActionModel() {
        return (ActionModel) getModel();
    }

    @Override
    public ManagedAction getManagedAction() {
        return getActionModel().getManagedAction();
    }

    @Override
    public ObjectAction getAction() {
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
        return isVisible()
                ? getActionModel().getUsabilityConsent().getReasonAsString().orElse(null)
                : null;
    }

    @Override
    public boolean isVisible() {
        return getActionModel().getVisibilityConsent().isAllowed();
    }

    @Override
    public boolean isEnabled() {
        return getActionModel().getUsabilityConsent().isAllowed();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);
        Wkt.fixDisabledState(this, tag);
    }

    @Override
    public String getAjaxIndicatorMarkupId() {
        return this.indicatorAppenderIfAny != null
                ? this.indicatorAppenderIfAny.getMarkupId()
                : null;
    }

    @Override
    public void onClick(final AjaxRequestTarget ajaxTarget) {

        _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                + "originating from User clicking an Action Link.");

        var actionModel = this.getActionModel();

        if(actionModel.getPromptStyle().isDialogAny()
                || actionModel.getInlinePromptContext() == null) {

            if(actionModel.hasParameters()) {
                startDialogWithParams(ajaxTarget);
            } else {
                executeWithoutParams();
            }

        } else {
            startDialogInline(ajaxTarget);
        }
    }

    // -- HELPER

    private void executeWithoutParams() {
        var actionModel = getActionModel();

        // on non-recoverable exception throws
        var outcome = FormExecutorDefault
                .forAction(actionModel)
                .executeAndProcessResults(null, null, actionModel);

        // on recoverable exception stay on page (eg. validation failure)
        if(outcome.isFailure()) {

            // render the target entity again
            //
            // (One way this can occur is if an event subscriber has a defect and throws an exception; in which case
            // the EventBus' exception handler will automatically veto.  This results in a growl message rather than
            // an error page, but is probably 'good enough').
            var targetAdapter = actionModel.getParentObject();

            var bookmark = targetAdapter.refreshBookmark().orElseThrow();
            getTransactionService().flushTransaction();

            // "redirect-after-post"
            RequestCycle.get().setResponsePage(DomainObjectPage.class,
                    PageParameterUtils.createPageParametersForBookmark(bookmark));
        }
    }

    private void startDialogWithParams(final AjaxRequestTarget target) {
        var actionModel = this.getActionModel();
        var actionOwnerSpec = actionModel.getActionOwner().objSpec();
        var actionPrompt = ActionPromptProvider
                .getFrom(this.getPage())
                .getActionPrompt(actionModel.getPromptStyle(), actionOwnerSpec.getBeanSort());

        var actionParametersPanel = getComponentFactoryRegistry()
                .createComponent(actionPrompt.getContentId(),
                        UiComponentType.ACTION_PROMPT,
                        actionModel);

        var label = Wkt.label(actionPrompt.getTitleId(), actionModel::getFriendlyName);
        actionPrompt.setTitle(label, target);
        actionPrompt.setPanel(actionParametersPanel, target);
        actionPrompt.showPrompt(target);

        castTo(ActionPromptWithExtraContent.class, actionPrompt)
        .ifPresent(promptWithExtraContent->{
            BSGridPanelFactory.extraContentForMixin(promptWithExtraContent.getExtraContentId(), actionModel)
            .ifPresent(gridPanel->promptWithExtraContent.setExtraContentPanel(gridPanel, target));
        });
    }

    private void startDialogInline(final AjaxRequestTarget target) {
        var actionModel = this.getActionModel();
        var inlinePromptContext = actionModel.getInlinePromptContext();
        var scalarTypeContainer = inlinePromptContext.scalarTypeContainer();

        getComponentFactoryRegistry().addOrReplaceComponent(scalarTypeContainer,
                FrameFragment.INLINE_PROMPT_FORM.getContainerId(),
                UiComponentType.PARAMETERS,
                actionModel);

        inlinePromptContext.onPrompt();

        Wkt.javaScriptAdd(target, Wkt.EventTopic.FOCUS_FIRST_PARAMETER, scalarTypeContainer.getMarkupId());

        target.add(scalarTypeContainer);
    }

    // -- DEPENDENCIES

    private ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((HasComponentFactoryRegistry) Application.get()).getComponentFactoryRegistry();
    }

    public Wicket getSettings() {
        return getMetaModelContext().getConfiguration().getViewer().getWicket();
    }

    @Override
    public Kind menuableKind() {
        return Kind.LINK;
    }

}
