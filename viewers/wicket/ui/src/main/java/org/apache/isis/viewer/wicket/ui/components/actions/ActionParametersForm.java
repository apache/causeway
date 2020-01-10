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
package org.apache.isis.viewer.wicket.ui.components.actions;

import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.wicket.model.hints.IsisActionCompletedEvent;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionArgumentModel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorStrategy;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import lombok.val;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;

class ActionParametersForm extends PromptFormAbstract<ActionModel> {

    private static final long serialVersionUID = 1L;

    public ActionParametersForm(
            final String id,
            final Component parentPanel,
            final WicketViewerSettings settings,
            final ActionModel actionModel) {
        super(id, parentPanel, settings, actionModel);
    }

    private ActionModel getActionModel() {
        return (ActionModel) super.getModel();
    }

    @Override
    protected void addParameters() {
        final ActionModel actionModel = getActionModel();

        final RepeatingView rv = new RepeatingView(ActionParametersFormPanel.ID_ACTION_PARAMETERS);
        add(rv);

        paramPanels.clear();
        List<ActionParameterMemento> parameterMementos = actionModel.primeArgumentModels();
        for (final ActionParameterMemento apm : parameterMementos) {
            final WebMarkupContainer container = new WebMarkupContainer(rv.newChildId());
            rv.add(container);

            final ActionArgumentModel actionArgumentModel = actionModel.getArgumentModel(apm);
            actionArgumentModel.setActionArgsHint(actionModel.getArgumentsAsImmutable());
            final ScalarPanelAbstract2 paramPanel = newParamPanel(container, actionArgumentModel);
            paramPanels.add(paramPanel);

            // TODO: maybe this logic should move instead to ScalarModel.Kind#whether{Hidden/Disabled}
            val targetAdapter = actionModel.getTargetAdapter();
            val realTargetAdapter = actionModel.getActionMemento().getAction(getSpecificationLoader())
                    .realTargetAdapter(targetAdapter);
            final Consent consent = apm.getActionParameter(getSpecificationLoader())
                    .isVisible(realTargetAdapter, Collections.emptyList(), InteractionInitiatedBy.USER);
            final boolean allowed = consent.isAllowed();
            paramPanel.setVisible(allowed);
        }

        setOutputMarkupId(true);


    }

    private ScalarPanelAbstract2 newParamPanel(final WebMarkupContainer container, final ActionArgumentModel model) {
        final Component component = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ComponentType.SCALAR_NAME_AND_VALUE, model);

        if(component instanceof MarkupContainer) {
            final MarkupContainer markupContainer = (MarkupContainer) component;

            // TODO: copy-n-paste of ScalarModel.Kind#getCssClass(ScalarModel), so could perhaps unify
            final ObjectActionParameter actionParameter = model.getParameterMemento()
                    .getActionParameter(getSpecificationLoader());

            final ObjectAction action = actionParameter.getAction();
            final String objectSpecId = action.getOnType().getSpecId().asString().replace(".", "-");
            final String parmId = actionParameter.getId();

            final String css = "isis-" + objectSpecId + "-" + action.getId() + "-" + parmId;

            CssClassAppender.appendCssClassTo(markupContainer, CssClassAppender.asCssStyle(css));
        }
        final ScalarPanelAbstract2 paramPanel =
                component instanceof ScalarPanelAbstract2
                ? (ScalarPanelAbstract2) component
                        : null;
                if (paramPanel != null) {
                    paramPanel.setOutputMarkupId(true);
                    paramPanel.notifyOnChange(this);
                }
                return paramPanel;
    }

    @Override
    protected Object newCompletedEvent(final AjaxRequestTarget target, final Form<?> form) {
        return new IsisActionCompletedEvent(getActionModel(), target, form);
    }

    @Override
    protected void doConfigureOkButton(final AjaxButton okButton) {
        applyAreYouSure(okButton);
    }

    /**
     * If the {@literal @}Action has "are you sure?" semantics then apply {@link ConfirmationBehavior}
     * that will ask for confirmation before executing the Ajax request.
     *
     * @param button The button which action should be confirmed
     */
    private void applyAreYouSure(AjaxButton button) {
        ActionModel actionModel = getActionModel();
        final ObjectAction action = actionModel.getActionMemento().getAction(getSpecificationLoader());
        SemanticsOf semanticsOf = action.getSemantics();

        PanelUtil.addConfirmationDialogIfAreYouSureSemantics(super.getTranslationService(), button, semanticsOf);
    }

    @Override
    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract2 scalarPanelUpdated) {

        final ActionModel actionModel = getActionModel();

        final int paramNumberUpdated = scalarPanelUpdated.getModel().getParameterMemento().getNumber();
        
        val action = actionModel.getActionMemento().getAction(getSpecificationLoader());

        final int numParams = action.getParameterCount();

        // only updates subsequent parameter panels starting from [paramNumberUpdated + 1] 
        for (int i = paramNumberUpdated + 1; i < numParams; i++) {

            val paramNumToUpdate = i;
            val paramPanel = paramPanels.get(paramNumToUpdate);
            val repaint = paramPanel
                    .updateIfNecessary(actionModel, paramNumberUpdated, paramNumToUpdate, target);
            
            //final boolean multiPart = isMultiPart(); // side-effects(?) or remove

            switch (repaint) {
            case ENTIRE_FORM:
                target.add(this);
                break;
            case PARAM_ONLY:
                paramPanel.repaint(target);
                break;
            case NOTHING:
                break;
            default:
                throw _Exceptions.unmatchedCase(repaint);
            }
        }

        // previously this method was also doing:
        // target.add(this);
        // ie to update the entire form (in addition to the updates to the individual impacted parameter fields
        // done in the loop above).  However, that logic is wrong, because any values entered in the browser
        // get trampled over (ISIS-629).
    }

    @Override
    protected FormExecutorStrategy<ActionModel> getFormExecutorStrategy() {
        ActionModel actionModel = getActionModel();
        return new ActionFormExecutorStrategy(actionModel);
    }

}
