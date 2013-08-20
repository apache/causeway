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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.vaynberg.wicket.select2.ChoiceProvider;
import com.vaynberg.wicket.select2.Select2Choice;
import com.vaynberg.wicket.select2.Settings;
import com.vaynberg.wicket.select2.TextChoiceProvider;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.link.Link;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
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
import org.apache.isis.viewer.wicket.model.models.ModelAbstract;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionInvokeHandler;
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
    private static final String ID_FEEDBACK = "feedback";
    
    private Select2Choice<ObjectAdapterMemento> autoCompleteField;
    private Link<String> entityDetailsLink;
    private Link<String> entityClearLink;

    public EntityLinkSelect2Panel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        setType(ObjectAdapter.class);
        buildGui();
    }

    public EntityModel getEntityModel() {
        return (EntityModel) getModel();
    }

    /**
     * Builds the parts of the GUI that are not dynamic.
     */
    private void buildGui() {
        //addOrReplace(new ComponentFeedbackPanel(ID_FEEDBACK, this));
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
        
        final boolean mutability = isEnableAllowed() && !getEntityModel().isViewMode();

        if(entityClearLink != null) {
            entityClearLink.setVisible(mutability);
        }

        if(entityDetailsLink != null) {
            entityDetailsLink.setVisible(getEntityModel().getRenderingHint() == RenderingHint.REGULAR);
        }
        
        if(autoCompleteField != null) {
            autoCompleteField.setEnabled(mutability);
        }
        
        if(isEditableWithEitherAutoCompleteOrChoices()) {
            permanentlyHide(ID_ENTITY_ICON_AND_TITLE);
        }
    }

    protected void doSyncVisibilityAndUsability(boolean mutability) {
        if(autoCompleteField != null) {
            autoCompleteField.setEnabled(mutability);
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
        final ObjectAdapter pendingElseCurrentAdapter = getEntityModel().getPendingElseCurrentAdapter();
        return pendingElseCurrentAdapter != null? pendingElseCurrentAdapter.titleString(): "(no object)";
    }

    @Override
    protected void convertInput() {

        if(getEntityModel().isEditMode() && isEditableWithEitherAutoCompleteOrChoices()) {
            // flush changes to pending
            onSelected(autoCompleteField.getConvertedInput());
        }

        final ObjectAdapter pendingAdapter = getEntityModel().getPendingAdapter();
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
        
        final ChoiceProvider<ObjectAdapterMemento> provider;
        if (hasChoices()) {
            provider = providerForChoices(getEntityModel());
        } else if(hasParamOrPropertyAutoComplete()) {
            provider = providerForParamOrPropertyAutoComplete(getEntityModel());
        } else {
            provider = providerForObjectAutoComplete(getEntityModel());
        }

        final ModelAbstract<ObjectAdapterMemento> model = new ModelAbstract<ObjectAdapterMemento>(){
            private static final long serialVersionUID = 1L;
            
            @Override
            protected ObjectAdapterMemento load() {
                return ObjectAdapterMemento.createOrNull(getPendingElseCurrentAdapter());
            }
            
        };
        autoCompleteField = new Select2Choice<ObjectAdapterMemento>(ID_AUTO_COMPLETE, model, provider);
        final Settings settings = autoCompleteField.getSettings();
        
        ScalarModel scalarModel = (ScalarModel) getEntityModel();
        settings.setMinimumInputLength(scalarModel.getAutoCompleteMinLength());
        addOrReplace(autoCompleteField);
        
        // no need for link, since can see in drop-down
        permanentlyHide(ID_ENTITY_ICON_AND_TITLE);

        // no need for the 'null' title, since if there is no object yet
        // can represent this fact in the drop-down
        permanentlyHide(ID_ENTITY_TITLE_NULL);
    }

    abstract static class ObjectAdapterMementoProviderAbstract extends TextChoiceProvider<ObjectAdapterMemento> {
        private static final long serialVersionUID = 1L;

        @Override
        protected String getDisplayText(ObjectAdapterMemento choice) {
            return choice.getObjectAdapter(ConcurrencyChecking.NO_CHECK).titleString(null);
        }

        @Override
        protected Object getId(ObjectAdapterMemento choice) {
            return choice.toString();
        }

        @Override
        public void query(String term, int page, com.vaynberg.wicket.select2.Response<ObjectAdapterMemento> response) {
            
            List<ObjectAdapterMemento> mementos = obtainMementos(term);
            response.addAll(mementos);
        }

        protected abstract List<ObjectAdapterMemento> obtainMementos(String term);

        @Override
        public Collection<ObjectAdapterMemento> toChoices(Collection<String> ids) {
            Function<String, ObjectAdapterMemento> function = new Function<String, ObjectAdapterMemento>() {

                @Override
                public ObjectAdapterMemento apply(String input) {
                    final RootOid oid = RootOidDefault.deString(input, ObjectAdapterMemento.getOidMarshaller());
                    return ObjectAdapterMemento.createPersistent(oid);
                }
            };
            return Collections2.transform(ids, function);
        }
    }

    /**
     * @param entityModel - serializable, referenced by the AutoCompletionChoicesProvider below 
     */
    private static ChoiceProvider<ObjectAdapterMemento> providerForObjectAutoComplete(final EntityModel entityModel) {
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

    private static ChoiceProvider<ObjectAdapterMemento> providerForParamOrPropertyAutoComplete(final EntityModel entityModel) {
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
    
    /**
     * @param entityModel - serializable, referenced by the AutoCompletionChoicesProvider below 
     */
    private static ChoiceProvider<ObjectAdapterMemento> providerForChoices(final EntityModel entityModel) {
        return new ObjectAdapterMementoProviderAbstract() {

            private static final long serialVersionUID = 1L;

            @Override
            protected List<ObjectAdapterMemento> obtainMementos(String term) {
                final ScalarModel scalarModel = (ScalarModel) entityModel;
                final boolean hasChoices = scalarModel.hasChoices();
                if(!hasChoices) {
                    return Collections.emptyList();
                }
                final List<ObjectAdapter> choices = scalarModel.getChoices(null);
                if(choices.isEmpty()) {
                    return Collections.emptyList();
                }
                // take a copy otherwise is only lazily evaluated
                return Lists.newArrayList(Lists.transform(choices, Mementos.fromAdapter()));
            }
        };
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
        entityModelForLink.setContextAdapterIfAny(getEntityModel().getContextAdapterIfAny());
        entityModelForLink.setRenderingHint(getEntityModel().getRenderingHint());
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
        getEntityModel().setPending(selectedAdapterMemento);
        renderSamePage();
    }

    public void onNoResults() {
        renderSamePage();
    }

    @Override
    public void onCancel() {
        getEntityModel().clearPending();
    }

    private ObjectAdapter getPendingElseCurrentAdapter() {
        return getEntityModel().getPendingElseCurrentAdapter();
    }

    private void renderSamePage() {
        setResponsePage(getPage());
    }
    
    private boolean isEditableWithEitherAutoCompleteOrChoices() {
        // never doesn't apply in compact rendering contexts (ie tables)
        if(getEntityModel().getRenderingHint().isInTable()) {
            return false;
        }
        // doesn't apply if not editable, either
        if(getEntityModel().isViewMode()) {
            return false;
        }
        return hasChoices() || hasParamOrPropertyAutoComplete() || hasObjectAutoComplete();
    }

    private boolean hasChoices() {
        final EntityModel entityModel = getEntityModel();
        if (!(entityModel instanceof ScalarModel)) {
            return false;
        } 
        final ScalarModel scalarModel = (ScalarModel) entityModel;
        return scalarModel.hasChoices();
    }

    private boolean hasParamOrPropertyAutoComplete() {
        final EntityModel entityModel = getEntityModel();
        if (!(entityModel instanceof ScalarModel)) {
            return false;
        } 
        final ScalarModel scalarModel = (ScalarModel) entityModel;
        return scalarModel.hasAutoComplete();
    }

    private boolean hasObjectAutoComplete() {

        // on property/param
        final EntityModel entityModel = getEntityModel();
        if (!(entityModel instanceof ScalarModel)) {
            return false;
        } 
        final ScalarModel scalarModel = (ScalarModel) entityModel;
        boolean hasAutoComplete = scalarModel.hasAutoComplete();
        if(hasAutoComplete) {
            return true;
        }

        
        // else on underlying type
        final ObjectSpecification typeOfSpecification = getEntityModel().getTypeOfSpecification();
        final AutoCompleteFacet autoCompleteFacet = 
                (typeOfSpecification != null)? typeOfSpecification.getFacet(AutoCompleteFacet.class):null;
        return autoCompleteFacet != null;
    }
    
    
}
