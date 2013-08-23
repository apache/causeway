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

package org.apache.isis.viewer.wicket.ui.components.widgets.entitylink;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2Choice;
import com.vaynberg.wicket.select2.Settings;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModelWithPending.Util;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionInvokeHandler;
import org.apache.isis.viewer.wicket.ui.components.widgets.ObjectAdapterMementoProviderAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormComponentPanelAbstract;

/**
 * {@link FormComponentPanel} representing a reference to an entity: a link and
 * (optionally) an autocomplete field.
 */
public class EntityLinkSelect2Panel extends FormComponentPanelAbstract<ObjectAdapter> implements CancelHintRequired, ActionInvokeHandler  {

    private static final long serialVersionUID = 1L;
    private static final String ID_AUTO_COMPLETE = "autoComplete";

    private static final String ID_ENTITY_ICON_AND_TITLE = "entityIconAndTitle";
    private static final String ID_ENTITY_TITLE_NULL = "entityTitleNull";

    private static final String ID_ENTITY_CLEAR_LINK = "entityClearLink";
    
    private Select2Choice<ObjectAdapterMemento> select2Field;
    private Link<String> entityDetailsLink;
    private Link<String> entityClearLink;

    public EntityLinkSelect2Panel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        setType(ObjectAdapter.class);
        buildGui();
    }

    public ScalarModel getScalarModel() {
        return (ScalarModel) getModel();
    }

    /**
     * Builds the parts of the GUI that are not dynamic.
     */
    private void buildGui() {
        syncWithInput();
    }


    /**
     * Must be called after {@link #setEnabled(boolean)} to ensure that the
     * <tt>findUsing</tt> button, and the <tt>entityClearLink</tt> are 
     * shown/not shown as required.
     * 
     * <p>
     * REVIEW: there ought to be a better way to do this. I'd hoped to override
     * {@link #setEnabled(boolean)}, but it is <tt>final</tt>, and there doesn't
     * seem to be anyway to install a listener. One option might be to move it
     * to {@link #onBeforeRender()} ?
     */
    public void syncVisibilityAndUsability() {
        
        final boolean mutability = isEnableAllowed() && !getScalarModel().isViewMode();

        if(entityClearLink != null) {
            entityClearLink.setVisible(mutability);
        }

        if(entityDetailsLink != null) {
            entityDetailsLink.setVisible(getScalarModel().getRenderingHint() == RenderingHint.REGULAR);
        }
        
        if(select2Field != null) {
            select2Field.setEnabled(mutability);
        }
        
        if(isEditableWithEitherAutoCompleteOrChoices()) {
            permanentlyHide(ID_ENTITY_ICON_AND_TITLE);
        }
    }

    protected void doSyncVisibilityAndUsability(boolean mutability) {
        if(select2Field != null) {
            select2Field.setEnabled(mutability);
        }

        if(isEditableWithEitherAutoCompleteOrChoices()) {
            permanentlyHide(ID_ENTITY_ICON_AND_TITLE);
        }
    }

    /**
     * Since we override {@link #convertInput()}, it is (apparently) enough to
     * just return a value that is suitable for error reporting.
     * 
     * @see DateField#getInput() for reference
     */
    @Override
    public String getInput() {
        final ObjectAdapter pendingElseCurrentAdapter = getScalarModel().getPendingElseCurrentAdapter();
        return pendingElseCurrentAdapter != null? pendingElseCurrentAdapter.titleString(null): "(no object)";
    }

    @Override
    protected void convertInput() {

        if(getScalarModel().isEditMode() && isEditableWithEitherAutoCompleteOrChoices()) {
            // flush changes to pending
            onSelected(select2Field.getConvertedInput());
        }

        final ObjectAdapter pendingAdapter = getScalarModel().getPendingAdapter();
        setConvertedInput(pendingAdapter);
    }


    @Override
    protected void onBeforeRender() {
        syncWithInput();
        super.onBeforeRender();
    }

    private void syncWithInput() {
        final ObjectAdapter adapter = getPendingElseCurrentAdapter();

        syncLinkWithInput(adapter);

        // represent no object by a simple label displaying '(null)'
        syncEntityTitleNullWithInput(adapter);

        syncEntityClearLinksWithInput(adapter);

        doSyncWithInputIfAutoCompleteOrChoices();
        
        syncVisibilityAndUsability();
    }

    private void doSyncWithInputIfAutoCompleteOrChoices() {
        
        if(!isEditableWithEitherAutoCompleteOrChoices()) {
            permanentlyHide(ID_AUTO_COMPLETE);
            return;
        }

        
        final IModel<ObjectAdapterMemento> model = Util.createModel(getScalarModel().asScalarModelWithPending());       

        if(select2Field == null) {
            select2Field = new Select2Choice<ObjectAdapterMemento>(ID_AUTO_COMPLETE, model);
            setChoices(getScalarModel().getActionArgsHint());
            if(!getScalarModel().hasChoices()) {
                final Settings settings = select2Field.getSettings();
                ScalarModel scalarModel = (ScalarModel) getScalarModel();
                final int minLength = scalarModel.getAutoCompleteMinLength();
                settings.setMinimumInputLength(minLength);
            }
            addOrReplace(select2Field);
        }
        
        
        
        // no need for link, since can see in drop-down
        permanentlyHide(ID_ENTITY_ICON_AND_TITLE);

        // no need for the 'null' title, since if there is no object yet
        // can represent this fact in the drop-down
        permanentlyHide(ID_ENTITY_TITLE_NULL);
    }


    private ChoiceProvider<ObjectAdapterMemento> providerForObjectAutoComplete() {
        final EntityModel entityModel = getScalarModel();
        return new ObjectAdapterMementoProviderAbstract() {

            private static final long serialVersionUID = 1L;

            @Override
            protected List<ObjectAdapterMemento> obtainMementos(String term) {
                final ObjectSpecification typeOfSpecification = entityModel.getTypeOfSpecification();
                final AutoCompleteFacet autoCompleteFacet = typeOfSpecification.getFacet(AutoCompleteFacet.class);
                final List<ObjectAdapter> results = autoCompleteFacet.execute(term);
                return Lists.transform(results, Mementos.fromAdapter());
            }

        };
    }

    private ChoiceProvider<ObjectAdapterMemento> providerForParamOrPropertyAutoComplete() {
        final EntityModel entityModel = getScalarModel();
        return new ObjectAdapterMementoProviderAbstract() {
            
            private static final long serialVersionUID = 1L;
            
            @Override
            protected List<ObjectAdapterMemento> obtainMementos(String term) {
                final ScalarModel scalarModel = (ScalarModel) entityModel;
                final boolean hasAutoComplete = scalarModel.hasAutoComplete();
                if(!hasAutoComplete) {
                    return Collections.emptyList();
                }
                final List<ObjectAdapter> autoCompleteChoices = scalarModel.getAutoComplete(term);
                if(autoCompleteChoices.isEmpty()) {
                    return Collections.emptyList();
                }
                // take a copy otherwise is only lazily evaluated
                return Lists.newArrayList(Lists.transform(autoCompleteChoices, Mementos.fromAdapter()));
            }
            
        };
    }
    
    private List<ObjectAdapterMemento> getChoiceMementos(final ObjectAdapter[] argsIfAvailable) {
        
        final ScalarModel scalarModel = (ScalarModel) getScalarModel();;
        final boolean hasChoices = scalarModel.hasChoices();
        if(!hasChoices) {
            return Collections.emptyList();
        }
        final List<ObjectAdapter> choices = scalarModel.getChoices(argsIfAvailable);
        if(choices.isEmpty()) {
            return Collections.emptyList();
        }
        // take a copy otherwise is only lazily evaluated
        return Lists.newArrayList(Lists.transform(choices, Mementos.fromAdapter()));
    }

    private void syncLinkWithInput(final ObjectAdapter adapter) {
        if (adapter != null) {
            addOrReplaceIconAndTitle(adapter);
        } else {
            permanentlyHide(ID_ENTITY_ICON_AND_TITLE);
        }
    }

    private void syncEntityTitleNullWithInput(final ObjectAdapter adapter) {
        if (adapter != null) {
            permanentlyHide(ID_ENTITY_TITLE_NULL);
        } else {
            addOrReplace(new Label(ID_ENTITY_TITLE_NULL, ""));
        }
    }


    private void syncEntityClearLinksWithInput(final ObjectAdapter adapter) {
        if (adapter == null) {
            permanentlyHide(ID_ENTITY_CLEAR_LINK);
            return;
        } 
        
        if(getScalarModel().isRequired()) {
            permanentlyHide(ID_ENTITY_CLEAR_LINK);
            return;
        }
        
        entityClearLink = new Link<String>(ID_ENTITY_CLEAR_LINK) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                onSelected((ObjectAdapterMemento)null);
            }
        };
        addOrReplace(entityClearLink);
    }


    private void addOrReplaceIconAndTitle(ObjectAdapter pendingOrCurrentAdapter) {
        final EntityModel entityModelForLink = new EntityModel(pendingOrCurrentAdapter);
        entityModelForLink.setContextAdapterIfAny(getScalarModel().getContextAdapterIfAny());
        entityModelForLink.setRenderingHint(getScalarModel().getRenderingHint());
        final ComponentFactory componentFactory = getComponentFactoryRegistry().findComponentFactory(ComponentType.ENTITY_ICON_AND_TITLE, entityModelForLink);
        final Component component = componentFactory.createComponent(entityModelForLink);
        addOrReplace(component);
    }


    @SuppressWarnings("unused")
    private static List<ObjectAction> findServiceActionsFor(final ObjectSpecification scalarTypeSpec) {
        final List<ObjectAction> actionList = Lists.newArrayList();
        addServiceActionsFor(scalarTypeSpec, ActionType.USER, actionList);
        if (IsisContext.getDeploymentType() == DeploymentType.EXPLORATION) {
            addServiceActionsFor(scalarTypeSpec, ActionType.EXPLORATION, actionList);
        }
        return actionList;
    }

    private static void addServiceActionsFor(final ObjectSpecification noSpec, final ActionType actionType, final List<ObjectAction> actionList) {
        final List<ObjectAction> serviceActionsFor = noSpec.getServiceActionsReturning(actionType);
        actionList.addAll(serviceActionsFor);
    }

    @Override
    public void onClick(final ActionModel actionModel) {
    }

    public void onSelected(final ObjectAdapterMemento selectedAdapterMemento) {
        getScalarModel().setPending(selectedAdapterMemento);
        renderSamePage();
    }

    public void onNoResults() {
        renderSamePage();
    }

    @Override
    public void onCancel() {
        getScalarModel().clearPending();
    }

    private ObjectAdapter getPendingElseCurrentAdapter() {
        return getScalarModel().getPendingElseCurrentAdapter();
    }

    private void renderSamePage() {
        setResponsePage(getPage());
    }
    
    private boolean isEditableWithEitherAutoCompleteOrChoices() {
        // never doesn't apply in compact rendering contexts (ie tables)
        if(getScalarModel().getRenderingHint().isInTable()) {
            return false;
        }
        // doesn't apply if not editable, either
        if(getScalarModel().isViewMode()) {
            return false;
        }
        return getScalarModel().hasChoices() || hasParamOrPropertyAutoComplete() || hasObjectAutoComplete();
    }

    private boolean hasParamOrPropertyAutoComplete() {
        return getScalarModel().hasAutoComplete();
    }

    private boolean hasObjectAutoComplete() {

        // on property/param
        boolean hasAutoComplete = getScalarModel().hasAutoComplete();
        if(hasAutoComplete) {
            return true;
        }
        
        // else on underlying type
        final ObjectSpecification typeOfSpecification = getScalarModel().getTypeOfSpecification();
        final AutoCompleteFacet autoCompleteFacet = 
                (typeOfSpecification != null)? typeOfSpecification.getFacet(AutoCompleteFacet.class):null;
        return autoCompleteFacet != null;
    }

    // //////////////////////////////////////

    public void addFormComponentBehavior(Behavior behavior) {
        select2Field.add(behavior);
    }

    public void updateChoices(ObjectAdapter[] argsIfAvailable) {
        setChoices(argsIfAvailable);
    }
    
    private void setChoices(final ObjectAdapter[] argsIfAvailable) {
        
        final ChoiceProvider<ObjectAdapterMemento> provider;
        if (getScalarModel().hasChoices()) {
            final List<ObjectAdapterMemento> choiceMementos = getChoiceMementos(argsIfAvailable);
            provider = new ObjectAdapterMementoProviderAbstract() {
                private static final long serialVersionUID = 1L;
                @Override
                protected List<ObjectAdapterMemento> obtainMementos(String unused) {
                    return choiceMementos;
                }
            };
            final ObjectAdapterMemento curr = select2Field.getModelObject();
            select2Field.setProvider(provider);
            getScalarModel().clearPending();
            if(curr == null || !curr.containedIn(choiceMementos)) {
                final ObjectAdapterMemento newAdapterMemento = 
                        !choiceMementos.isEmpty() 
                        ? choiceMementos.get(0) 
                                : null;
                        select2Field.getModel().setObject(newAdapterMemento);
                        getModel().setObject(
                                newAdapterMemento != null? newAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK): null);
            }
        } else if(hasParamOrPropertyAutoComplete()) {
            provider = providerForParamOrPropertyAutoComplete();
            select2Field.setProvider(provider);
            getScalarModel().setPending(null);
        } else {
            provider = providerForObjectAutoComplete();
            select2Field.setProvider(provider);
            getScalarModel().setPending(null);
        }
    }

}
