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
package org.apache.isis.viewer.wicket.ui.panels;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.AppendingStringBuffer;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.FormExecutor;
import org.apache.isis.viewer.wicket.model.models.FormExecutorContext;
import org.apache.isis.viewer.wicket.model.models.ParentEntityModelProvider;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarModelSubscriber2;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormFeedbackPanel;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlBehaviour;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

public abstract class PromptFormAbstract<T extends BookmarkableModel<ObjectAdapter>
                                                    & ParentEntityModelProvider
                                                    & IModel<ObjectAdapter>
                                                    & FormExecutorContext>
        extends FormAbstract<ObjectAdapter>
        implements ScalarModelSubscriber2 {

    private static final String ID_OK_BUTTON = "okButton";
    public static final String ID_CANCEL_BUTTON = "cancelButton";

    private static final String ID_FEEDBACK = "feedback";

    protected final List<ScalarPanelAbstract2> paramPanels = Lists.newArrayList();

    private final Component parentPanel;
    private final WicketViewerSettings settings;
    private final T formExecutorContext;

    private final AjaxButton okButton;
    private final AjaxButton cancelButton;

    public PromptFormAbstract(
            final String id,
            final Component parentPanel,
            final WicketViewerSettings settings,
            final T model) {
        super(id, model);
        this.parentPanel = parentPanel;
        this.settings = settings;
        this.formExecutorContext = model;

        setOutputMarkupId(true); // for ajax button
        addParameters();

        FormFeedbackPanel formFeedback = new FormFeedbackPanel(ID_FEEDBACK);
        addOrReplace(formFeedback);

        okButton = addOkButton();
        cancelButton = addCancelButton();
        doConfigureOkButton(okButton);
        doConfigureCancelButton(cancelButton);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        response.render(OnDomReadyHeaderItem.forScript(
                String.format("Wicket.Event.publish(Isis.Topic.FOCUS_FIRST_PARAMETER, '%s')", getMarkupId())));

    }

    protected abstract void addParameters();

    protected AjaxButton addOkButton() {

        AjaxButton okButton = settings.isUseIndicatorForFormSubmit()
        ? new IndicatingAjaxButton(ID_OK_BUTTON, new ResourceModel("okLabel")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                onOkSubmittedOf(target, form, this);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                if (settings.isPreventDoubleClickForFormSubmit()) {
                    PanelUtil.disableBeforeReenableOnComplete(attributes, this);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        }
        : new AjaxButton(ID_OK_BUTTON, new ResourceModel("okLabel")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                onOkSubmittedOf(target, form, this);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                if (settings.isPreventDoubleClickForFormSubmit()) {
                    PanelUtil.disableBeforeReenableOnComplete(attributes, this);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        };
        okButton.add(new JGrowlBehaviour());
        setDefaultButton(okButton);
        add(okButton);
        return okButton;
    }

    protected AjaxButton addCancelButton() {
        final AjaxButton cancelButton = new AjaxButton(ID_CANCEL_BUTTON, new ResourceModel("cancelLabel")) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(final AjaxRequestTarget target, Form<?> form) {
                closePromptIfAny(target);

                onCancelSubmitted(target);
            }
        };
        // so can submit with invalid content (eg mandatory params missing)
        cancelButton.setDefaultFormProcessing(false);

        if (formExecutorContext.getPromptStyle().isInlineOrInlineAsIfEdit()) {
            cancelButton.add(new FireOnEscapeKey() {
                @Override
                protected void respond(final AjaxRequestTarget target) {
                    onCancelSubmitted(target);
                }
            });
        }

        add(cancelButton);

        return cancelButton;
    }

    protected void closePromptIfAny(final AjaxRequestTarget target) {

        final ActionPromptProvider promptProvider = ActionPromptProvider.Util.getFrom(parentPanel);
        if(promptProvider != null) {
            promptProvider.closePrompt(target);
        }
    }

    /**
     * Optional hook
     */
    protected void doConfigureOkButton(final AjaxButton okButton) {
    }

    /**
     * Optional hook
     */
    protected void doConfigureCancelButton(final AjaxButton cancelButton) {
    }

    private UiHintContainer getPageUiHintContainerIfAny() {
        Page page = getPage();
        if (page instanceof EntityPage) {
            EntityPage entityPage = (EntityPage) page;
            return entityPage.getUiHintContainerIfAny();
        }
        return null;
    }

    private void onOkSubmittedOf(
            final AjaxRequestTarget target,
            final Form<?> form,
            final AjaxButton okButton) {

        setLastFocusHint();

        final FormExecutor formExecutor = new FormExecutorDefault<>(getFormExecutorStrategy());

        final boolean withinPrompt = formExecutorContext.isWithinPrompt();
        boolean succeeded = formExecutor.executeAndProcessResults(target.getPage(), target, form, withinPrompt);

        if (succeeded) {
            completePrompt(target);

            okButton.send(target.getPage(), Broadcast.EXACT, newCompletedEvent(target, form));
            target.add(form);
        }

    }

    protected abstract FormExecutorStrategy<T> getFormExecutorStrategy();


    private void setLastFocusHint() {

        final UiHintContainer entityModel = getPageUiHintContainerIfAny();
        if (entityModel == null) {
            return;
        }
        MarkupContainer parent = this.parentPanel.getParent();
        if (parent != null) {
            entityModel.setHint(getPage(), PageAbstract.UIHINT_FOCUS, parent.getPageRelativePath());
        }
    }

    protected abstract Object newCompletedEvent(
            final AjaxRequestTarget target,
            final Form<?> form);

    @Override
    public void onError(AjaxRequestTarget target, ScalarPanelAbstract2 scalarPanel) {
        if (scalarPanel != null) {
            // ensure that any feedback error associated with the providing component is shown.
            target.add(scalarPanel);
        }
    }

    public void onCancelSubmitted(final AjaxRequestTarget target) {

        setLastFocusHint();
        completePrompt(target);
    }

    private void completePrompt(final AjaxRequestTarget target) {

        if (isWithinPrompt()) {
            formExecutorContext.reset();
            rebuildGuiAfterInlinePromptDone(target);
        } else {
            closePromptIfAny(target);
        }
    }

    private boolean isWithinPrompt() {
        return FormExecutorContext.Util.isWithinPrompt(this.formExecutorContext);
    }

    private void rebuildGuiAfterInlinePromptDone(final AjaxRequestTarget target) {
        // replace
        final String id = parentPanel.getId();
        final MarkupContainer parent = parentPanel.getParent();

        final WebMarkupContainer replacementPropertyEditFormPanel = new WebMarkupContainer(id);
        replacementPropertyEditFormPanel.setVisible(false);

        parent.addOrReplace(replacementPropertyEditFormPanel);

        // change visibility of inline components
        formExecutorContext.getInlinePromptContext().onCancel();

        // redraw
        MarkupContainer scalarTypeContainer = formExecutorContext.getInlinePromptContext()
                .getScalarTypeContainer();

        if (scalarTypeContainer != null) {
            String markupId = scalarTypeContainer.getMarkupId();
            target.appendJavaScript(
                    String.format("Wicket.Event.publish(Isis.Topic.FOCUS_FIRST_PROPERTY, '%s')",
                            markupId));
        }

        target.add(parent);
    }

    private AjaxButton defaultSubmittingComponent() {
        return okButton;
    }

    // workaround for https://issues.apache.org/jira/browse/WICKET-6364
    @Override
    protected void appendDefaultButtonField() {
        AppendingStringBuffer buffer = new AppendingStringBuffer();
        buffer.append(
                "<div style=\"width:0px;height:0px;position:absolute;left:-100px;top:-100px;overflow:hidden\">");
        buffer.append("<input type=\"text\" tabindex=\"-1\" autocomplete=\"off\"/>");
        Component submittingComponent = (Component) this.defaultSubmittingComponent();
        buffer.append("<input type=\"submit\" tabindex=\"-1\" name=\"");
        buffer.append(this.defaultSubmittingComponent().getInputName());
        buffer.append("\" onclick=\" var b=document.getElementById(\'");
        buffer.append(submittingComponent.getMarkupId());
        buffer.append(
                "\'); if (b!=null&amp;&amp;b.onclick!=null&amp;&amp;typeof(b.onclick) != \'undefined\') {  var r = Wicket.bind(b.onclick, b)(); if (r != false) b.click(); } else { b.click(); };  return false;\" ");
        buffer.append(" />");
        buffer.append("</div>");
        this.getResponse().write(buffer);
    }

    static abstract class FireOnEscapeKey extends AbstractDefaultAjaxBehavior {

        private static final String PRE_JS =
                "" + "$(document).ready( function() { \n"
                        + "  $(document).bind('keyup', function(evt) { \n"
                        + "    if (evt.keyCode == 27) { \n";
        private static final String POST_JS =
                "" + "      evt.preventDefault(); \n   "
                        + "    } \n"
                        + "  }); \n"
                        + "});";

        @Override
        public void renderHead(final Component component, final IHeaderResponse response) {
            super.renderHead(component, response);

            final String javascript = PRE_JS + getCallbackScript() + POST_JS;
            response.render(
                    JavaScriptContentHeaderItem.forScript(javascript, null, null));
        }

        @Override
        protected abstract void respond(final AjaxRequestTarget target);

    }
}
