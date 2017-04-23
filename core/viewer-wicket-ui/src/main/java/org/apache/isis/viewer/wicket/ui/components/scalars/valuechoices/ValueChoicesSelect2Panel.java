/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.scalars.valuechoices;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.select2.ChoiceProvider;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithMultiPending;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithPending;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForValueChoices;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public class ValueChoicesSelect2Panel extends ScalarPanelAbstract implements ScalarModelWithPending, ScalarModelWithMultiPending {

    private static final long serialVersionUID = 1L;

    private Select2 select2;
    private ObjectAdapterMemento pending;

    public ValueChoicesSelect2Panel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        pending = scalarModel.getObjectAdapterMemento();
    }

    @Override
    protected MarkupContainer addComponentForRegular() {

        // same pattern as in ReferencePanel
        if(select2 == null) {
            if(getModel().isCollection()) {
                final IModel<ArrayList<ObjectAdapterMemento>> modelObject = ScalarModelWithMultiPending.Util.createModel(this);
                select2 = Select2.newSelect2MultiChoice(ID_SCALAR_VALUE, modelObject, scalarModel);
            } else {
                final IModel<ObjectAdapterMemento> modelObject = ScalarModelWithPending.Util.createModel(this);
                select2 = Select2.newSelect2Choice(ID_SCALAR_VALUE, modelObject, scalarModel);
            }

            final ObjectAdapter[] actionArgsHint = scalarModel.getActionArgsHint();
            setProviderAndCurrAndPending(select2, actionArgsHint);
            addStandardSemantics();
        } else {
            select2.clearInput();
        }


        final MarkupContainer scalarIfRegularFormGroup = createScalarIfRegularFormGroup();
        if(getModel().isRequired()) {
            scalarIfRegularFormGroup.add(new CssClassAppender("mandatory"));
        }
        
        addOrReplace(scalarIfRegularFormGroup);

        final Label scalarName = new Label(ID_SCALAR_NAME, getRendering().getLabelCaption(select2.component()));
        if(getModel().isRequired()) {
            final String label = scalarName.getDefaultModelObjectAsString();
            if(!Strings.isNullOrEmpty(label)) {
                scalarName.add(new CssClassAppender("mandatory"));
            }
        }
        scalarIfRegularFormGroup.addOrReplace(scalarName);
        NamedFacet namedFacet = getModel().getFacet(NamedFacet.class);
        if (namedFacet != null) {
            scalarName.setEscapeModelStrings(namedFacet.escaped());
        }

        // find the links...
        final List<LinkAndLabel> entityActions = EntityActionUtil.getEntityActionLinksForAssociation(this.scalarModel, getDeploymentCategory());

        addPositioningCssTo(scalarIfRegularFormGroup, entityActions);

        addFeedbackOnlyTo(scalarIfRegularFormGroup, select2.component());
        addEditPropertyTo(scalarIfRegularFormGroup, null, null, null);

        // ... add entity links to panel (below and to right)
        addEntityActionLinksBelowAndRight(scalarIfRegularFormGroup, entityActions);

        return scalarIfRegularFormGroup;
    }

    private List<ObjectAdapterMemento> getChoiceMementos(final ObjectAdapter[] argumentsIfAvailable) {
        final List<ObjectAdapter> choices =
                scalarModel.getChoices(argumentsIfAvailable, getAuthenticationSession(), getDeploymentCategory());
        
        // take a copy otherwise is only lazily evaluated
        return Lists.newArrayList(Lists.transform(choices, ObjectAdapterMemento.Functions.fromAdapter()));
    }

    protected void addStandardSemantics() {
        setRequiredIfSpecified();
    }

    private void setRequiredIfSpecified() {
        final ScalarModel scalarModel = getModel();
        final boolean required = scalarModel.isRequired();
        select2.setRequired(required);
    }

    protected MarkupContainer createScalarIfRegularFormGroup() {
        final String name = getModel().getName();
        select2.setLabel(Model.of(name));

        final FormGroup formGroup = new FormGroup(ID_SCALAR_IF_REGULAR, select2.component());

        final String describedAs = getModel().getDescribedAs();
        if(describedAs != null) {
            formGroup.add(new AttributeModifier("title", Model.of(describedAs)));
        }

        formGroup.add(select2.component());

        return formGroup;
    }

    @Override
    protected Component addComponentForCompact() {
        final Label labelIfCompact = new Label(ID_SCALAR_IF_COMPACT, getModel().getObjectAsString());
        addOrReplace(labelIfCompact);
        return labelIfCompact;
    }

    
    protected ChoiceProvider<ObjectAdapterMemento> newChoiceProvider(final List<ObjectAdapterMemento> choicesMementos) {
        return new ObjectAdapterMementoProviderForValueChoices(scalarModel, choicesMementos, wicketViewerSettings);
    }

    @Override
    protected boolean alwaysRebuildGui() {
        return true;
    }

    @Override
    protected void onBeforeRenderWhenViewMode() { 
        // View: Read only
        select2.setEnabled(false);
    }

    @Override
    protected void onBeforeRenderWhenEnabled() { 
        // Edit: read/write
        select2.setEnabled(true);

        // TODO: should the title AttributeModifier installed in onBeforeWhenDisabled be removed here?
    }

    @Override
    protected void onBeforeRenderWhenDisabled(final String disableReason) {
        super.onBeforeRenderWhenDisabled(disableReason);
        setTitleAttribute(disableReason);
        select2.setEnabled(false);
    }

    private void setTitleAttribute(final String titleAttribute) {
        getComponentForRegular().add(new AttributeModifier("title", Model.of(titleAttribute)));
    }

    
    @Override
    protected void addFormComponentBehavior(Behavior behavior) {
        for (Behavior b : select2.getBehaviors(ScalarUpdatingBehavior.class)) {
            select2.remove(b);
        }
        select2.add(behavior);
    }

    // //////////////////////////////////////

    @Override
    public boolean updateChoices(ObjectAdapter[] argsIfAvailable) {
        if(select2 != null) {
            setProviderAndCurrAndPending(select2, argsIfAvailable);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Repaints just the Select2 component
     *
     * @param target The Ajax request handler
     */
    @Override
    public void repaint(AjaxRequestTarget target) {
        target.add(select2.component());
    }

    /**
     * sets up the choices, also ensuring that any currently held value is compatible.
     */
    private void setProviderAndCurrAndPending(final Select2 select2, ObjectAdapter[] argsIfAvailable) {

        final ChoiceProvider<ObjectAdapterMemento> provider;

        // in corresponding code in ReferencePanelFactory, these is a branch for different types of providers
        // (choice vs autoComplete).  Here though - because values don't currently support autoComplete - no branch is required
        final List<ObjectAdapterMemento> choicesMementos = getChoiceMementos(argsIfAvailable);
        provider = newChoiceProvider(choicesMementos);

        select2.setProvider(provider);
        getModel().clearPending();

        if(provider instanceof ObjectAdapterMementoProviderForValueChoices) {
            final ObjectAdapterMementoProviderForValueChoices providerFixed = (ObjectAdapterMementoProviderForValueChoices) provider;
            final List<ObjectAdapterMemento> choicesMementos1 = providerFixed.getChoicesMementos();
            resetIfCurrentNotInChoices(select2, choicesMementos1);
        }
    }

    private void resetIfCurrentNotInChoices(final Select2 select2, final List<ObjectAdapterMemento> choicesMementos) {
        final ObjectAdapterMemento objectAdapterMemento = getModel().getObjectAdapterMemento();
        if(objectAdapterMemento == null) {
            select2.getModel().setObject(null);
        } else {

            if(!getModel().isCollection()) {

                // if currently held value is not compatible with choices, then replace with the first choice
                if(!choicesMementos.contains(objectAdapterMemento)) {

                    final ObjectAdapterMemento newAdapterMemento =
                            choicesMementos.isEmpty()
                                    ? null
                                    : choicesMementos.get(0);

                    select2.getModel().setObject(newAdapterMemento);
                    getModel().setObjectMemento(newAdapterMemento, getPersistenceSession(), getSpecificationLoader());
                }

            } else {

                // nothing to do
            }
        }
    }

    // //////////////////////////////////////

    @Override
    public ObjectAdapterMemento getPending() {
        return pending;
    }

    public void setPending(ObjectAdapterMemento pending) {
        this.pending = pending;
    }

    @Override
    public ArrayList<ObjectAdapterMemento> getMultiPending() {
        final ArrayList<ObjectAdapterMemento> mementos = pending != null ? pending.getList() : null;
        return mementos == null || mementos.isEmpty() ? null : mementos;
    }

    @Override
    public void setMultiPending(final ArrayList<ObjectAdapterMemento> pending) {
        this.pending = ObjectAdapterMemento.createForList(pending, scalarModel.getTypeOfSpecification().getSpecId());
    }

    public ScalarModel getScalarModel() {
        return scalarModel;
    }

    // //////////////////////////////////////

    @com.google.inject.Inject
    private WicketViewerSettings wicketViewerSettings;
}
