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
package org.apache.causeway.viewer.wicket.ui.panels;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;

import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.debug._Probe.EntryPoint;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.hints.CausewayActionCompletedEvent;
import org.apache.causeway.viewer.wicket.model.hints.CausewayPropertyEditCompletedEvent;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.causeway.viewer.wicket.model.models.FormExecutorContext;
import org.apache.causeway.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarModelChangeListener;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;
import org.apache.causeway.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.Wkt.EventTopic;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

import lombok.val;

public abstract class PromptFormAbstract<T extends
    FormExecutorContext
    & IModel<ManagedObject>>
extends OkCancelForm<T>
implements ScalarModelChangeListener {

    private static final long serialVersionUID = 1L;

    protected final List<ScalarPanelAbstract> paramPanels = _Lists.newArrayList();

    private final Component parentPanel;

    protected PromptFormAbstract(
            final String id,
            final Component parentPanel,
            final T model) {

        super(id, model);
        this.parentPanel = parentPanel;

        addParameters();
    }

    @SuppressWarnings("unchecked")
    private FormExecutorContext formExecutorContext() {
        return (T)getModel();
    }

    // -- SETUP

    protected abstract void addParameters();
    protected abstract Either<ActionModel, ScalarPropertyModel> getMemberModel();

    @Override
    public final void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        /*[CAUSEWAY-3217] in the context of a modal dialog with a select2 causes:
         * java.io.IOException: An established connection was aborted by the software in your host machine
         */
//        Wkt.javaScriptAdd(response, EventTopic.FOCUS_FIRST_PARAMETER, getMarkupId());
    }

    @Override
    protected final void configureCancelButton(final AjaxButton cancelButton) {
        super.configureCancelButton(cancelButton);
        if (formExecutorContext().getPromptStyle().isInlineAny()) {
            Wkt.behaviorAddFireOnEscapeKey(cancelButton, this::onCancelSubmitted);
        }
    }

    // -- BEHAVIOR

    @Override
    protected final void onOkSubmitted(
            final AjaxButton okButton,
            final AjaxRequestTarget target) {

        _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                + "originating from User clicking OK on an inline editing form or "
                + "action prompt.");

        setLastFocusHint();

        val form = okButton.getForm();
        val formExecutor = FormExecutorDefault.forMember(getMemberModel());

        val outcome = formExecutor
                .executeAndProcessResults(target, form, formExecutorContext());

        if (outcome.isSuccess()) {
            completePrompt(target);
            okButton.send(target.getPage(), Broadcast.EXACT, getMemberModel().fold(
                act->new CausewayActionCompletedEvent(target),
                prop->new CausewayPropertyEditCompletedEvent(target)));
            WktComponents.addToAjaxRequest(target, form);
        }
    }

    @Override
    public final void onCancelSubmitted(final AjaxRequestTarget target) {

        _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                + "originating from User clicking CANCEL (or hitting ESC) on an inline editing form or "
                + "action prompt.");

        setLastFocusHint();
        completePrompt(target);
    }

    @Override
    public final void onError(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanel) {
        if (scalarPanel != null) {
            // ensure that any feedback error associated with the providing component is shown.
            target.add(scalarPanel);
        }
    }

    // -- HELPER

    private void completePrompt(final AjaxRequestTarget target) {
        if (formExecutorContext().isWithinInlinePrompt()) {
            rebuildGuiAfterInlinePromptDone(target);
        } else {
            closePromptIfAny(target);
        }
    }

    private void closePromptIfAny(final AjaxRequestTarget target) {
        try {
            ActionPromptProvider.getFrom(parentPanel)
            .closePrompt(target);
        } catch (org.apache.wicket.WicketRuntimeException ex) {
            // if "No Page found for component"
            // do nothing
        }
    }

    private void setLastFocusHint() {
        final UiHintContainer entityModel = pageUiHintContainerIfAny();
        if (entityModel == null) {
            return;
        }
        final MarkupContainer parentContainer = this.parentPanel.getParent();
        if (parentContainer == null) {
            return;

        }
        entityModel.setHint(getPage(), PageAbstract.UIHINT_FOCUS, parentContainer.getPageRelativePath());
    }

    private UiHintContainer pageUiHintContainerIfAny() {
        final Page page;
        try {
            page = getPage();
        } catch(org.apache.wicket.WicketRuntimeException ex) {
            return null;
        }
        if (page instanceof EntityPage) {
            EntityPage entityPage = (EntityPage) page;
            return entityPage.getUiHintContainerIfAny();
        }
        return null;
    }

    private void rebuildGuiAfterInlinePromptDone(final AjaxRequestTarget target) {

        // enlist for redraw
        target.add(parentPanel.getParent());

        // replace parent panel with new invisible instance
        Wkt.containerAdd(parentPanel.getParent(), parentPanel.getId())
            .setVisible(false);

        // change visibility of inline components
        formExecutorContext().getInlinePromptContext().onCancel(getMemberModel());

        Optional.ofNullable(formExecutorContext().getInlinePromptContext().getScalarTypeContainer())
        .ifPresent(scalarTypeContainer->{
            Wkt.javaScriptAdd(target, EventTopic.FOCUS_FIRST_PROPERTY, scalarTypeContainer.getMarkupId());
        });
    }

}
