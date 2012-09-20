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

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuBuilder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.dropdownchoices.DropDownChoicesForObjectAdapterMementos;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link FormComponentPanel} representing a reference to an entity: a link and
 * a findUsing button.
 */
public class EntityLink extends EntityLinkAbstract {

    private static final long serialVersionUID = 1L;

    protected static final String ID_ENTITY_ICON_AND_TITLE = "entityIconAndTitle";
    private static final String ID_ENTITY_OID = "entityOid";
    protected static final String ID_ENTITY_TITLE_NULL = "entityTitleNull";
    private static final String ID_CHOICES = "choices";
    protected static final String ID_FIND_USING = "findUsing";
    protected static final String ID_ENTITY_CLEAR_LINK = "entityClearLink";
    protected static final String ID_ENTITY_DETAILS_LINK = "entityDetailsLink";
    protected static final String ID_ENTITY_DETAILS_LINK_LABEL = "entityDetailsLinkLabel";
    private static final String ID_ENTITY_DETAILS = "entityDetails";

    private static final String ID_FEEDBACK = "feedback";

    private final FindUsingLinkFactory linkFactory;

    
    private WebMarkupContainer findUsing;
    private Link<String> entityDetailsLink;
    private Link<String> entityClearLink;
    
    private PanelAbstract<?> actionFindUsingComponent;

    /**
     * Whether pending has been set (could have been set to null)
     */
    private boolean hasPending;
    /**
     * The new value (could be set to null; hasPending is used to distinguish).
     */
    private ObjectAdapterMemento pending;
    private TextField<ObjectAdapterMemento> pendingOid;


    public EntityLink(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        setType(ObjectAdapter.class);
        linkFactory = new FindUsingLinkFactory(this);
        buildGui();
    }

    public EntityModel getEntityModel() {
        return (EntityModel) getModel();
    }

    /**
     * Builds the parts of the GUI that are not dynamic.
     */
    private void buildGui() {
        addOrReplaceOidField();
        rebuildFindUsingMenu();
        addOrReplace(new ComponentFeedbackPanel(ID_FEEDBACK, this));

        syncWithInput();
    }

    private void addOrReplaceOidField() {
        pendingOid = new TextField<ObjectAdapterMemento>(ID_ENTITY_OID, new Model<ObjectAdapterMemento>() {

            private static final long serialVersionUID = 1L;

            
            @Override
            public ObjectAdapterMemento getObject() {
                if (hasPending) {
                    return pending;
                }
                final ObjectAdapter adapter = EntityLink.this.getModelObject();
                return ObjectAdapterMemento.createOrNull(adapter);
            }

            @Override
            public void setObject(final ObjectAdapterMemento adapterMemento) {
                pending = adapterMemento;
                hasPending = true;
            }

        }) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onModelChanged() {
                super.onModelChanged();
                syncWithInput();
            }
        };
        pendingOid.setType(ObjectAdapterMemento.class);
        addOrReplace(pendingOid);
        pendingOid.setVisible(false);
    }

    void rebuildFindUsingMenu() {
        final EntityModel entityModel = getEntityModel();
        final List<ObjectAction> actions = findServiceActionsFor(entityModel.getTypeOfSpecification());
        findUsing = new WebMarkupContainer(ID_FIND_USING);
        switch (actions.size()) {
        case 0:
            permanentlyHide(findUsing, ComponentType.ACTION);
            break;
        default:
            // TODO: i18n

            final CssMenuBuilder cssMenuBuilder = new CssMenuBuilder(null, getServiceAdapters(), actions, linkFactory);
            final CssMenuPanel cssMenuPanel = cssMenuBuilder.buildPanel(ComponentType.ACTION.getWicketId(), "find using...");

            findUsing.addOrReplace(cssMenuPanel);
            actionFindUsingComponent = cssMenuPanel;
            break;
        }
        addOrReplace(findUsing);
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
        findUsing.setVisible(mutability);
        

        if(entityClearLink != null) {
            entityClearLink.setVisible(mutability);
        }

        if(entityDetailsLink != null) {
            entityDetailsLink.setVisible(getEntityModel().getRenderingHint() == RenderingHint.REGULAR);
        }
        
        doSyncVisibilityAndUsability(mutability);
    }

    /**
     * Optional hook
     */
    protected void doSyncVisibilityAndUsability(boolean mutability) {
    }

    /**
     * Since we override {@link #convertInput()}, it is (apparently) enough to
     * just return a value that is suitable for error reporting.
     * 
     * @see DateField#getInput() for reference
     */
    @Override
    public String getInput() {
        return pendingOid.getInput();
    }

    @Override
    protected void convertInput() {

        doConvertInput();

        final ObjectAdapter pendingAdapter = getPendingAdapter();
        setConvertedInput(pendingAdapter);
    }

    /**
     * Optional hook
     */
    protected void doConvertInput() {
    }

    private ObjectAdapter getPendingAdapter() {
        final ObjectAdapterMemento memento = pendingOid.getModelObject();
        return memento != null ? memento.getObjectAdapter(ConcurrencyChecking.NO_CHECK) : null;
    }

    @Override
    protected void onBeforeRender() {
        syncWithInput();
        super.onBeforeRender();
    }

    private void syncWithInput() {
        final ObjectAdapter adapter = getPendingElseCurrentAdapter();

        final IModel<List<? extends ObjectAdapterMemento>> choicesMementos = getChoicesModel();
        if (choicesMementos != null) {

            // choices drop-down
            final IModel<ObjectAdapterMemento> modelObject = pendingOid.getModel();
            final DropDownChoicesForObjectAdapterMementos dropDownChoices = new DropDownChoicesForObjectAdapterMementos(ID_CHOICES, modelObject, choicesMementos);
            addOrReplace(dropDownChoices);
            dropDownChoices.setEnabled(getEntityModel().isEditMode());

            // no need for link, since can see in drop-down
            permanentlyHide(ID_ENTITY_ICON_AND_TITLE);

            // no need for the 'null' title, since if there is no object yet
            // can represent this fact in the drop-down
            permanentlyHide(ID_ENTITY_TITLE_NULL);
            
            // hide links
            permanentlyHide(ID_FIND_USING, ID_ENTITY_CLEAR_LINK);
            doSyncWithInputWhenChoices();
        } else {

            // choices drop-down
            permanentlyHide(ID_CHOICES);

            // show link if have value
            syncLinkWithInput(adapter);

            // represent no object by a simple label displaying '(null)'
            syncEntityTitleNullWithInput(adapter);

            // link
            syncEntityClearLinksWithInput(adapter);
        }


        doSyncWithInputOther();
        
        syncEntityDetailsLinksWithInput(adapter);
        syncEntityDetailsWithInput(adapter);


        syncVisibilityAndUsability();
    }


    /**
     * Optional hook
     */
    protected void doSyncWithInputWhenChoices() {
    }

    /**
     * Optional hook
     */
    protected void doSyncWithInputOther() {
    }

    protected ObjectAdapter getPendingElseCurrentAdapter() {
        return hasPending ? getPendingAdapter() : getEntityModel().getObject();
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
            addOrReplace(new Label(ID_ENTITY_TITLE_NULL, "(null)"));
        }
    }

    private void syncEntityDetailsLinksWithInput(final ObjectAdapter adapter) {
        if (adapter == null) {
            permanentlyHide(ID_ENTITY_DETAILS_LINK);
            return;
        } 
        entityDetailsLink = new Link<String>(ID_ENTITY_DETAILS_LINK) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                getEntityModel().toggleDetails();
                doSyncEntityDetailsOnClick();
            }

        };
        addOrReplace(entityDetailsLink);
        entityDetailsLink.add(new Label(ID_ENTITY_DETAILS_LINK_LABEL, buildEntityDetailsModel()));
    }

    /**
     * Optional hook
     */
    protected void doSyncEntityDetailsOnClick() {
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

    private Model<String> buildEntityDetailsModel() {
        final String label = getEntityModel().isEntityDetailsVisible() ? "-" : "+";
        return Model.of(label);
    }

    private void syncEntityDetailsWithInput(final ObjectAdapter adapter) {
        if (adapter != null && getEntityModel().isEntityDetailsVisible()) {
            final EntityModel entityModel = new EntityModel(adapter);
            
            final ComponentFactory componentFactory = getComponentFactoryRegistry().findComponentFactory(ComponentType.ENTITY_PROPERTIES, entityModel);
            final Component entityPanel = componentFactory.createComponent(ID_ENTITY_DETAILS, entityModel);
            
            addOrReplace(entityPanel);
        } else {
            permanentlyHide(ID_ENTITY_DETAILS);
        }
    }

    private IModel<List<? extends ObjectAdapterMemento>> getChoicesModel() {
        final EntityModel entityModel = getEntityModel();
        if (entityModel instanceof ScalarModel) {
            final ScalarModel scalarModel = (ScalarModel) entityModel;
            final List<ObjectAdapter> choices = scalarModel.getChoices();
            if (choices.size() == 0) {
                return null;
            }
            // take a copy otherwise is only lazily evaluated
            final List<ObjectAdapterMemento> choicesMementos = Lists.newArrayList(Lists.transform(choices, Mementos.fromAdapter()));
            return Model.ofList(choicesMementos);
        }
        return null;
    }

    private void addOrReplaceIconAndTitle(ObjectAdapter pendingOrCurrentAdapter) {
        final EntityModel entityModelForLink = new EntityModel(pendingOrCurrentAdapter);
        final ComponentFactory componentFactory = getComponentFactoryRegistry().findComponentFactory(ComponentType.ENTITY_ICON_AND_TITLE, entityModelForLink);
        final Component component = componentFactory.createComponent(entityModelForLink);
        addOrReplace(component);
    }


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
        final ActionPanel actionPanel = new ActionPanel(actionFindUsingComponent.getComponentType().toString(), actionModel);
        actionFindUsingComponent.replaceWith(actionPanel);
    }

    public void onSelected(final ObjectAdapter selectedAdapter) {
        final ObjectAdapterMemento selectedAdapterMemento = ObjectAdapterMemento.createOrNull(selectedAdapter);
        onSelected(selectedAdapterMemento);
    }

    private void onSelected(final ObjectAdapterMemento selectedAdapterMemento) {
        pendingOid.setDefaultModelObject(selectedAdapterMemento);
        rebuildFindUsingMenu();
        renderSamePage();
    }

    public void onNoResults() {
        rebuildFindUsingMenu();
        renderSamePage();
    }

    @Override
    public void onCancel() {
        pendingOid.clearInput();
        this.hasPending = false;
        this.pending = null;
    }

    private void renderSamePage() {
        setResponsePage(getPage());
    }
}
