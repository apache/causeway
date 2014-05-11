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

package org.apache.isis.viewer.wicket.ui.components.scalars.reference;

import java.util.List;

import com.google.common.collect.Lists;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2Choice;
import com.vaynberg.wicket.select2.Settings;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithPending;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithPending.Util;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.ObjectAdapterMementoProviderAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.Select2ChoiceUtil;
import org.apache.isis.viewer.wicket.ui.components.widgets.entitysimplelink.EntityLinkSimplePanel;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * Panel for rendering scalars which of are of reference type (as opposed to
 * value types).
 */
public class ReferencePanel extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    private static final String ID_SCALAR_NAME = "scalarName";

    private static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";

    static final String ID_AUTO_COMPLETE = "autoComplete";
    static final String ID_ENTITY_ICON_AND_TITLE = "entityIconAndTitle";

    private EntityLinkSelect2Panel entityLink;
    private EntityLinkSimplePanel entitySimpleLink;

    public ReferencePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected void onBeforeRenderWhenEnabled() {
        super.onBeforeRenderWhenEnabled();
        entityLink.setEnabled(true);
        entityLink.owningPanel.syncVisibilityAndUsability(entityLink, entityLink.select2Field);
    }

    @Override
    protected void onBeforeRenderWhenViewMode() {
        super.onBeforeRenderWhenViewMode();
        entityLink.setEnabled(true);
        entityLink.owningPanel.syncVisibilityAndUsability(entityLink, entityLink.select2Field);
    }

    @Override
    protected void onBeforeRenderWhenDisabled(final String disableReason) {
        super.onBeforeRenderWhenDisabled(disableReason);
        final EntityModel entityLinkModel = (EntityModel) entityLink.getModel();
        entityLinkModel.toViewMode();
        setTitleAttribute(disableReason);
        entityLink.owningPanel.syncVisibilityAndUsability(entityLink, entityLink.select2Field);
    }

    private void setTitleAttribute(final String titleAttribute) {
        entityLink.add(new AttributeModifier("title", Model.of(titleAttribute)));
    }

    @Override
    protected FormComponentLabel addComponentForRegular() {
        final ScalarModel scalarModel = getModel();
        final String name = scalarModel.getName();
        
        entityLink = new EntityLinkSelect2Panel(ComponentType.ENTITY_LINK.getWicketId(), this);
        
        setOutputMarkupId(true);
        entityLink.setOutputMarkupId(true);
        entityLink.setLabel(Model.of(name));
        
        final FormComponentLabel labelIfRegular = new FormComponentLabel(ID_SCALAR_IF_REGULAR, entityLink);
        labelIfRegular.add(entityLink);
        
        final String describedAs = getModel().getDescribedAs();
        if(describedAs != null) {
            labelIfRegular.add(new AttributeModifier("title", Model.of(describedAs)));
        }
        
        final Label scalarName = new Label(ID_SCALAR_NAME, getRendering().getLabelCaption(entityLink));
        labelIfRegular.add(scalarName);
        
        addOrReplace(labelIfRegular);
        
        addFeedbackTo(labelIfRegular, entityLink);
        addAdditionalLinksTo(labelIfRegular);
        
        addStandardSemantics();
        addSemantics();

        if(getModel().isRequired()) {
            labelIfRegular.add(new CssClassAppender("mandatory"));
        }
        return labelIfRegular;
    }

    protected void addStandardSemantics() {
        setRequiredIfSpecified();
    }

    private void setRequiredIfSpecified() {
        final ScalarModel scalarModel = getModel();
        final boolean required = scalarModel.isRequired();
        entityLink.setRequired(required);
    }

    protected void addSemantics() {

        addObjectAdapterValidator();
    }

    private void addObjectAdapterValidator() {
        final ScalarModel scalarModel = getModel();

        entityLink.add(new IValidator<ObjectAdapter>() {

            private static final long serialVersionUID = 1L;

            @Override
            public void validate(final IValidatable<ObjectAdapter> validatable) {
                final ObjectAdapter proposedAdapter = validatable.getValue();
                final String reasonIfAny = scalarModel.validate(proposedAdapter);
                if (reasonIfAny != null) {
                    final ValidationError error = new ValidationError();
                    error.setMessage(reasonIfAny);
                    validatable.error(error);
                }
            }
        });
    }

    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Rendering#COMPACT compact} format.
     */
    @Override
    protected Component addComponentForCompact() {

        final ScalarModel scalarModel = getModel();
        final String name = scalarModel.getName();
        
        entitySimpleLink = (EntityLinkSimplePanel) getComponentFactoryRegistry().createComponent(ComponentType.ENTITY_LINK, getModel());
        
        entitySimpleLink.setOutputMarkupId(true);
        entitySimpleLink.setLabel(Model.of(name));
        
        final FormComponentLabel labelIfCompact = new FormComponentLabel(ID_SCALAR_IF_COMPACT, entitySimpleLink);
        labelIfCompact.add(entitySimpleLink);
        
        addOrReplace(labelIfCompact);
        
        return labelIfCompact;
    }

    // //////////////////////////////////////

    @Override
    protected void addFormComponentBehavior(Behavior behavior) {
        if(entityLink.select2Field != null) {
            entityLink.select2Field.add(behavior);
        }
    }

    @Override
    public boolean updateChoices(ObjectAdapter[] argsIfAvailable) {
        if(entityLink.select2Field != null) {
            setProviderAndCurrAndPending(entityLink.select2Field, argsIfAvailable);
            return true;
        } else {
            return false;
        }
    }

    // //////////////////////////////////////

    
    boolean isEditableWithEitherAutoCompleteOrChoices() {
        if(getModel().getRenderingHint().isInTable()) {
            return false;
        }
        // doesn't apply if not editable, either
        if(getModel().isViewMode()) {
            return false;
        }
        return getModel().hasChoices() || hasParamOrPropertyAutoComplete() || hasObjectAutoComplete();
    }

    boolean hasParamOrPropertyAutoComplete() {
        return getModel().hasAutoComplete();
    }

    boolean hasObjectAutoComplete() {
        boolean hasAutoComplete = getModel().hasAutoComplete();
        if(hasAutoComplete) {
            return true;
        }
        
        // else on underlying type
        final ObjectSpecification typeOfSpecification = getModel().getTypeOfSpecification();
        final AutoCompleteFacet autoCompleteFacet = 
                (typeOfSpecification != null)? typeOfSpecification.getFacet(AutoCompleteFacet.class):null;
        return autoCompleteFacet != null;
    }

    void setProviderAndCurrAndPending(final Select2Choice<ObjectAdapterMemento> select2Field, final ObjectAdapter[] argsIfAvailable) {
        if (getModel().hasChoices()) {
            
            final List<ObjectAdapterMemento> choiceMementos = getChoiceMementos(argsIfAvailable);
            ObjectAdapterMementoProviderAbstract providerForChoices = providerForChoices(choiceMementos);
            
            select2Field.setProvider(providerForChoices);
            getModel().clearPending();
            
            resetIfCurrentNotInChoices(select2Field, choiceMementos);
            
        } else if(hasParamOrPropertyAutoComplete()) {
            select2Field.setProvider(providerForParamOrPropertyAutoComplete());
            getModel().clearPending();
        } else {
            select2Field.setProvider(providerForObjectAutoComplete());
            getModel().clearPending();
        }
    }

    List<ObjectAdapterMemento> getChoiceMementos(final ObjectAdapter[] argsIfAvailable) {
        final List<ObjectAdapter> choices = Lists.newArrayList();
        if(getModel().hasChoices()) {
            choices.addAll(getModel().getChoices(argsIfAvailable));
        }
        // take a copy otherwise is only lazily evaluated
        return Lists.newArrayList(Lists.transform(choices, ObjectAdapterMemento.Functions.fromAdapter()));
    }


    ChoiceProvider<ObjectAdapterMemento> providerForObjectAutoComplete() {
        return new ObjectAdapterMementoProviderAbstract(getModel()) {

            private static final long serialVersionUID = 1L;

            @Override
            protected List<ObjectAdapterMemento> obtainMementos(String term) {
                final ObjectSpecification typeOfSpecification = getScalarModel().getTypeOfSpecification();
                final AutoCompleteFacet autoCompleteFacet = typeOfSpecification.getFacet(AutoCompleteFacet.class);
                final List<ObjectAdapter> results = autoCompleteFacet.execute(term);
                return Lists.transform(results, ObjectAdapterMemento.Functions.fromAdapter());
            }
        };
    }


    ChoiceProvider<ObjectAdapterMemento> providerForParamOrPropertyAutoComplete() {
        return new ObjectAdapterMementoProviderAbstract(getModel()) {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            protected List<ObjectAdapterMemento> obtainMementos(String term) {
                final List<ObjectAdapter> autoCompleteChoices = Lists.newArrayList();
                if(getScalarModel().hasAutoComplete()) {
                    autoCompleteChoices.addAll(getScalarModel().getAutoComplete(term));
                }
                // take a copy otherwise is only lazily evaluated
                return Lists.newArrayList(Lists.transform(autoCompleteChoices, ObjectAdapterMemento.Functions.fromAdapter()));
            }
            
        };
    }
    

    void resetIfCurrentNotInChoices(final Select2Choice<ObjectAdapterMemento> select2Field, final List<ObjectAdapterMemento> choiceMementos) {
        final ObjectAdapterMemento curr = select2Field.getModelObject();
        if(curr == null) {
            select2Field.getModel().setObject(null);
            this.getModel().setObject(null);
            return;
        }
        if(!curr.containedIn(choiceMementos)) {
            if(!choiceMementos.isEmpty()) {
                final ObjectAdapterMemento newAdapterMemento = choiceMementos.get(0);
                select2Field.getModel().setObject(newAdapterMemento);
                this.getModel().setObject(newAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK));
            } else {
                select2Field.getModel().setObject(null);
                this.getModel().setObject(null);
            }
        }
    }

    ObjectAdapterMementoProviderAbstract providerForChoices(final List<ObjectAdapterMemento> choiceMementos) {
        return new ObjectAdapterMementoProviderAbstract(getModel()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<ObjectAdapterMemento> obtainMementos(String unused) {
                return choiceMementos;
            }
        };
    }

    void syncLinkWithInput(EntityLinkSelect2Panel linkPanel, final ObjectAdapter adapter) {
        if (adapter != null) {
            addOrReplaceIconAndTitle(linkPanel, adapter);
        } else {
            Components.permanentlyHide(linkPanel, ReferencePanel.ID_ENTITY_ICON_AND_TITLE);
        }
    }

    void addOrReplaceIconAndTitle(EntityLinkSelect2Panel linkPanel, ObjectAdapter pendingOrCurrentAdapter) {

        final EntityModel entityModelForLink = new EntityModel(pendingOrCurrentAdapter);
        
        entityModelForLink.setContextAdapterIfAny(getModel().getContextAdapterIfAny());
        entityModelForLink.setRenderingHint(getModel().getRenderingHint());
        
        final ComponentFactory componentFactory = getComponentFactoryRegistry().findComponentFactory(ComponentType.ENTITY_ICON_AND_TITLE, entityModelForLink);
        final Component component = componentFactory.createComponent(entityModelForLink);
        
        linkPanel.addOrReplace(component);
    }


    void onSelected(EntityLinkSelect2Panel linkPanel, final ObjectAdapterMemento selectedAdapterMemento) {

        getModel().setPending(selectedAdapterMemento);
        getModel().setObject(selectedAdapterMemento!=null?selectedAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK):null);
        if(linkPanel.select2Field != null) {
            linkPanel.select2Field.getModel().setObject(selectedAdapterMemento);
        }
    }

    /**
     * Must be called after {@link #setEnabled(boolean)}, apparently...
     * originally to ensure that the findUsing button and entityClearLink were
     * shown/not shown as required, however these have now gone.  Which beckons the question,
     * is it still important?
     * 
     * <p>
     * REVIEW: there ought to be a better way to do this. I'd hoped to override
     * {@link #setEnabled(boolean)}, but it is <tt>final</tt>, and there doesn't
     * seem to be anyway to install a listener. One option might be to move it
     * to {@link #onBeforeRender()} ?
     */
    void syncVisibilityAndUsability(EntityLinkSelect2Panel linkPanel, Select2Choice<ObjectAdapterMemento> select2Field) {
        final boolean mutability = linkPanel.isEnableAllowed() && !getModel().isViewMode();
    
        if(select2Field != null) {
            select2Field.setEnabled(mutability);
        }
        
        if(isEditableWithEitherAutoCompleteOrChoices()) {
            Components.permanentlyHide(linkPanel, ReferencePanel.ID_ENTITY_ICON_AND_TITLE);
        }
    }

    void convertInput(EntityLinkSelect2Panel linkPanel) {
        if(getModel().isEditMode() && isEditableWithEitherAutoCompleteOrChoices()) {
            // flush changes to pending
            this.onSelected(linkPanel, linkPanel.select2Field.getConvertedInput());
        }
    
        final ObjectAdapter pendingAdapter = getModel().getPendingAdapter();
        linkPanel.setConvertedInput(pendingAdapter);
    }

    void syncWithInput(EntityLinkSelect2Panel linkPanel) {
        final ObjectAdapter adapter = getModel().getPendingElseCurrentAdapter();
        syncLinkWithInput(linkPanel, adapter);
        doSyncWithInputIfAutoCompleteOrChoices(linkPanel);
        syncVisibilityAndUsability(linkPanel, linkPanel.select2Field);
    }

    void doSyncWithInputIfAutoCompleteOrChoices(EntityLinkSelect2Panel linkPanel) {
        if(!isEditableWithEitherAutoCompleteOrChoices()) {
            // this is horrid; adds a label to the id
            // should instead be a 'temporary hide'
            Components.permanentlyHide(linkPanel, ReferencePanel.ID_AUTO_COMPLETE);
            linkPanel.select2Field = null; // this forces recreation next time around
            return;
        }
    
        final IModel<ObjectAdapterMemento> model = Util.createModel(getModel().asScalarModelWithPending());       
    
        if(linkPanel.select2Field == null) {
            linkPanel.setRequired(getModel().isRequired());
            linkPanel.select2Field = Select2ChoiceUtil.newSelect2Choice(ReferencePanel.ID_AUTO_COMPLETE, model, getModel());
            setProviderAndCurrAndPending(linkPanel.select2Field, getModel().getActionArgsHint());
            if(!getModel().hasChoices()) {
                final Settings settings = linkPanel.select2Field.getSettings();
                final int minLength = getModel().getAutoCompleteMinLength();
                settings.setMinimumInputLength(minLength);
                settings.setPlaceholder(getModel().getName());
            }
            linkPanel.addOrReplace(linkPanel.select2Field);
        } else {
            //
            // the select2Field already exists, so the widget has been rendered before.  If it is
            // being re-rendered now, it may be because some other property/parameter was invalid.
            // when the form was submitted, the selected object (its oid as a string) would have
            // been saved as rawInput.  If the property/parameter had been valid, then this rawInput
            // would be correctly converted and processed by the select2Field's choiceProvider.  However,
            // an invalid property/parameter means that the webpage is re-rendered in another request,
            // and the rawInput can no longer be interpreted.  The net result is that the field appears
            // with no input.
            //
            // The fix is therefore (I think) simply to clear any rawInput, so that the select2Field
            // renders its state from its model.
            //
            // see: FormComponent#getInputAsArray()
            // see: Select2Choice#renderInitializationScript()
            //
            linkPanel.select2Field.clearInput();
        }
        
        // no need for link, since can see in drop-down
        Components.permanentlyHide(linkPanel, ReferencePanel.ID_ENTITY_ICON_AND_TITLE);
    
        // no need for the 'null' title, since if there is no object yet
        // can represent this fact in the drop-down
        // permanentlyHide(ID_ENTITY_TITLE_NULL);
    }


}
