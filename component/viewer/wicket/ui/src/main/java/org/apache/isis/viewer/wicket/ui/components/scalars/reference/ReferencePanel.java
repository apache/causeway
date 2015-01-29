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

import javax.inject.Inject;
import java.util.List;
import com.google.common.collect.Lists;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2Choice;
import com.vaynberg.wicket.select2.Settings;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithPending.Util;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.ObjectAdapterMementoProviderAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.components.widgets.entitysimplelink.EntityLinkSimplePanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2ChoiceUtil;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * Panel for rendering scalars which of are of reference type (as opposed to
 * value types).
 */
public class ReferencePanel extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_AUTO_COMPLETE = "autoComplete";
    private static final String ID_ENTITY_ICON_TITLE = "entityIconAndTitle";

    private EntityLinkSelect2Panel entityLink;
    Select2Choice<ObjectAdapterMemento> select2Field;

    private EntityLinkSimplePanel entitySimpleLink;

    public ReferencePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    
    // //////////////////////////////////////
    // addComponentFor{Compact/Regular}
    // //////////////////////////////////////

    // First called as a side-effect of {@link #beforeRender()}
    @Override
    protected Component addComponentForCompact() {

        final ScalarModel scalarModel = getModel();
        final String name = scalarModel.getName();
        
        entitySimpleLink = (EntityLinkSimplePanel) getComponentFactoryRegistry().createComponent(ComponentType.ENTITY_LINK, getModel());
        
        entitySimpleLink.setOutputMarkupId(true);
        entitySimpleLink.setLabel(Model.of(name));
        
        final WebMarkupContainer labelIfCompact = new WebMarkupContainer(ID_SCALAR_IF_COMPACT);
        labelIfCompact.add(entitySimpleLink);
        
        addOrReplace(labelIfCompact);
        
        return labelIfCompact;
    }


    // First called as a side-effect of {@link #beforeRender()}
    @Override
    protected FormGroup addComponentForRegular() {
        final ScalarModel scalarModel = getModel();
        final String name = scalarModel.getName();
        
        entityLink = new EntityLinkSelect2Panel(ComponentType.ENTITY_LINK.getWicketId(), this);
        syncWithInput();

        setOutputMarkupId(true);
        entityLink.setOutputMarkupId(true);
        entityLink.setLabel(Model.of(name));

        final FormGroup labelIfRegular = new FormGroup(ID_SCALAR_IF_REGULAR, entityLink);
        labelIfRegular.add(entityLink);
        
        final String describedAs = getModel().getDescribedAs();
        if(describedAs != null) {
            labelIfRegular.add(new AttributeModifier("title", Model.of(describedAs)));
        }
        
        final Label scalarName = new Label(ID_SCALAR_NAME, getRendering().getLabelCaption(entityLink));
        labelIfRegular.add(scalarName);
        NamedFacet namedFacet = getModel().getFacet(NamedFacet.class);
        if (namedFacet != null) {
            scalarName.setEscapeModelStrings(namedFacet.escaped());
        }

        // find the links...
        final List<LinkAndLabel> entityActions = EntityActionUtil.getEntityActionLinksForAssociation(this.scalarModel, getDeploymentType());

        addPositioningCssTo(labelIfRegular, entityActions);

        addOrReplace(labelIfRegular);
        
        addFeedbackTo(labelIfRegular, entityLink);

        // ... add entity links to panel (below and to right)
        addEntityActionLinksBelowAndRight(labelIfRegular, entityActions);

        // add semantics
        entityLink.setRequired(getModel().isRequired());
        entityLink.add(new IValidator<ObjectAdapter>() {
        
            private static final long serialVersionUID = 1L;
        
            @Override
            public void validate(final IValidatable<ObjectAdapter> validatable) {
                final ObjectAdapter proposedAdapter = validatable.getValue();
                final String reasonIfAny = getModel().validate(proposedAdapter);
                if (reasonIfAny != null) {
                    final ValidationError error = new ValidationError();
                    error.setMessage(reasonIfAny);
                    validatable.error(error);
                }
            }
        });

        if(getModel().isRequired()) {
            labelIfRegular.add(new CssClassAppender("mandatory"));
        }
        return labelIfRegular;
    }

    // //////////////////////////////////////

    // called from buildGui
    @Override
    protected void addFormComponentBehavior(Behavior behavior) {
        if(select2Field != null) {
            select2Field.add(behavior);
        }
    }

    
    // //////////////////////////////////////
    // onBeforeRender*
    // //////////////////////////////////////

    @Override
    protected void onBeforeRenderWhenEnabled() {
        super.onBeforeRenderWhenEnabled();
        entityLink.setEnabled(true);
        syncWithInput();
    }

    @Override
    protected void onBeforeRenderWhenViewMode() {
        super.onBeforeRenderWhenViewMode();
        entityLink.setEnabled(false);
        syncWithInput();
    }

    @Override
    protected void onBeforeRenderWhenDisabled(final String disableReason) {
        super.onBeforeRenderWhenDisabled(disableReason);
        syncWithInput();
        final EntityModel entityLinkModel = (EntityModel) entityLink.getModel();
        entityLinkModel.toViewMode();
        entityLink.setEnabled(false);
        entityLink.add(new AttributeModifier("title", Model.of(disableReason)));
    }

    
    // //////////////////////////////////////
    // syncWithInput
    // //////////////////////////////////////


    // called from onBeforeRender*
    // (was previous called by EntityLinkSelect2Panel in onBeforeRender, this responsibility now moved)
    private void syncWithInput() {
        final ObjectAdapter adapter = getModel().getPendingElseCurrentAdapter();

        // syncLinkWithInput
        final MarkupContainer componentForRegular = (MarkupContainer) getComponentForRegular();
        if (adapter != null) {
            if(componentForRegular != null) {
                final EntityModel entityModelForLink = new EntityModel(adapter);
                
                entityModelForLink.setContextAdapterIfAny(getModel().getContextAdapterIfAny());
                entityModelForLink.setRenderingHint(getModel().getRenderingHint());
                
                final ComponentFactory componentFactory = 
                        getComponentFactoryRegistry().findComponentFactory(ComponentType.ENTITY_ICON_AND_TITLE, entityModelForLink);
                final Component component = componentFactory.createComponent(entityModelForLink);
                
                componentForRegular.addOrReplace(component);

                Components.permanentlyHide(componentForRegular, "entityTitleIfNull");

            }


        } else {

            if(componentForRegular != null) {
                componentForRegular.addOrReplace(new Label("entityTitleIfNull", "(none)"));
                //Components.permanentlyHide(componentForRegular, "entityTitleIfNull");
                Components.permanentlyHide(componentForRegular, ID_ENTITY_ICON_TITLE);
            }
        }


        // syncLinkWithInputIfAutoCompleteOrChoices
        if(isEditableWithEitherAutoCompleteOrChoices()) {
            final IModel<ObjectAdapterMemento> model = Util.createModel(getModel().asScalarModelWithPending());       
            
            if(select2Field == null) {
                entityLink.setRequired(getModel().isRequired());
                select2Field = Select2ChoiceUtil.newSelect2Choice(ID_AUTO_COMPLETE, model, getModel());
                setProviderAndCurrAndPending(select2Field, getModel().getActionArgsHint());
                if(!getModel().hasChoices()) {
                    final Settings settings = select2Field.getSettings();
                    final int minLength = getModel().getAutoCompleteMinLength();
                    settings.setMinimumInputLength(minLength);
                    settings.setPlaceholder(getModel().getName());
                }
                entityLink.addOrReplace(select2Field);
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
                select2Field.clearInput();
            }

            if(getComponentForRegular() != null) {
                Components.permanentlyHide((MarkupContainer)getComponentForRegular(), ID_ENTITY_ICON_TITLE);
                Components.permanentlyHide(componentForRegular, "entityTitleIfNull");
            }



            // syncUsability
            if(select2Field != null) {
                final boolean mutability = entityLink.isEnableAllowed() && !getModel().isViewMode();
                select2Field.setEnabled(mutability);
            }

            Components.permanentlyHide(entityLink, "entityLinkIfNull");
        } else {
            // this is horrid; adds a label to the id
            // should instead be a 'temporary hide'
            Components.permanentlyHide(entityLink, ID_AUTO_COMPLETE);
            select2Field = null; // this forces recreation next time around
        }
        
    }

    // called by syncWithInput
    private void permanentlyHideEntityIconAndTitleIfInRegularMode() {
        if(getComponentForRegular() != null) {
            Components.permanentlyHide((MarkupContainer)getComponentForRegular(), ID_ENTITY_ICON_TITLE);
        }
    }


    // //////////////////////////////////////
    // setProviderAndCurrAndPending
    // //////////////////////////////////////
    
    // called by syncWithInput, updateChoices
    private void setProviderAndCurrAndPending(
            final Select2Choice<ObjectAdapterMemento> select2Field, 
            final ObjectAdapter[] argsIfAvailable) {
        if (getModel().hasChoices()) {
            
            final List<ObjectAdapterMemento> choiceMementos = obtainChoiceMementos(argsIfAvailable);
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

    // called by setProviderAndCurrAndPending
    private List<ObjectAdapterMemento> obtainChoiceMementos(final ObjectAdapter[] argsIfAvailable) {
        final List<ObjectAdapter> choices = Lists.newArrayList();
        if(getModel().hasChoices()) {
            choices.addAll(getModel().getChoices(argsIfAvailable));
        }
        // take a copy otherwise is only lazily evaluated
        return Lists.newArrayList(Lists.transform(choices, ObjectAdapterMemento.Functions.fromAdapter()));
    }

    // called by setProviderAndCurrAndPending
    private void resetIfCurrentNotInChoices(final Select2Choice<ObjectAdapterMemento> select2Field, final List<ObjectAdapterMemento> choiceMementos) {
        final ObjectAdapterMemento curr = select2Field.getModelObject();
        if(curr == null) {
            select2Field.getModel().setObject(null);
            getModel().setObject(null);
            return;
        }
        
        if(!curr.containedIn(choiceMementos)) {
            if(!choiceMementos.isEmpty()) {
                final ObjectAdapterMemento newAdapterMemento = choiceMementos.get(0);
                select2Field.getModel().setObject(newAdapterMemento);
                getModel().setObject(newAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK));
            } else {
                select2Field.getModel().setObject(null);
                getModel().setObject(null);
            }
        }
    }

    // called by setProviderAndCurrAndPending
    private ChoiceProvider<ObjectAdapterMemento> providerForObjectAutoComplete() {
        return new ObjectAdapterMementoProviderAbstract(getModel(), wicketViewerSettings) {

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

    // called by setProviderAndCurrAndPending
    private ChoiceProvider<ObjectAdapterMemento> providerForParamOrPropertyAutoComplete() {
        return new ObjectAdapterMementoProviderAbstract(getModel(), wicketViewerSettings) {
            
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

    // called by setProviderAndCurrAndPending
    private ObjectAdapterMementoProviderAbstract providerForChoices(final List<ObjectAdapterMemento> choiceMementos) {
        return new ObjectAdapterMementoProviderAbstract(getModel(), wicketViewerSettings) {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<ObjectAdapterMemento> obtainMementos(String unused) {
                return choiceMementos;
            }
        };
    }

    
    // //////////////////////////////////////
    // getInput, convertInput
    // //////////////////////////////////////
    
    // called by EntityLinkSelect2Panel
    String getInput() {
        final ObjectAdapter pendingElseCurrentAdapter = getModel().getPendingElseCurrentAdapter();
        return pendingElseCurrentAdapter != null? pendingElseCurrentAdapter.titleString(null): "(no object)";
    }

    // //////////////////////////////////////

    // called by EntityLinkSelect2Panel
    void convertInput() {
        if(isEditableWithEitherAutoCompleteOrChoices()) {

            // flush changes to pending
            ObjectAdapterMemento convertedInput = select2Field.getConvertedInput();
            
            getModel().setPending(convertedInput);
            if(select2Field != null) {
                select2Field.getModel().setObject(convertedInput);
            }
            
            final ObjectAdapter adapter = convertedInput!=null?convertedInput.getObjectAdapter(ConcurrencyChecking.NO_CHECK):null;
            getModel().setObject(adapter);
        }
    
        final ObjectAdapter pendingAdapter = getModel().getPendingAdapter();
        entityLink.setConvertedInput(pendingAdapter);
    }

    // //////////////////////////////////////
    // updateChoices
    // //////////////////////////////////////

    /**
     * Hook method to refresh choices when changing.
     * 
     * <p>
     * called from onUpdate callback
     */
    @Override
    public boolean updateChoices(ObjectAdapter[] argsIfAvailable) {
        if(select2Field != null) {
            setProviderAndCurrAndPending(select2Field, argsIfAvailable);
            return true;
        } else {
            return false;
        }
    }

    
    // //////////////////////////////////////
    // helpers querying model state
    // //////////////////////////////////////

    // called from convertInput, syncWithInput
    private boolean isEditableWithEitherAutoCompleteOrChoices() {
        if(getModel().getRenderingHint().isInTable()) {
            return false;
        }
        // doesn't apply if not editable, either
        if(getModel().isViewMode()) {
            return false;
        }
        return getModel().hasChoices() || hasParamOrPropertyAutoComplete() || hasObjectAutoComplete();
    }

    // called by isEditableWithEitherAutoCompleteOrChoices, also syncProviderAndCurrAndPending
    private boolean hasParamOrPropertyAutoComplete() {
        return getModel().hasAutoComplete();
    }

    // called by isEditableWithEitherAutoCompleteOrChoices
    private boolean hasObjectAutoComplete() {
        final ObjectSpecification typeOfSpecification = getModel().getTypeOfSpecification();
        final AutoCompleteFacet autoCompleteFacet = 
                (typeOfSpecification != null)? typeOfSpecification.getFacet(AutoCompleteFacet.class):null;
        return autoCompleteFacet != null;
    }

    @Inject
    private WicketViewerSettings wicketViewerSettings;

}
