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

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.yui.calendar.DateField;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.util.Generics;
import org.apache.isis.viewer.wicket.model.util.Mementos;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionInvokeHandler;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionPanel;
import org.apache.isis.viewer.wicket.ui.components.entity.combined.EntityCombinedPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuBuilder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.dropdownchoices.DropDownChoicesForObjectAdapterMementos;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.CancelHintRequired;
import org.apache.isis.viewer.wicket.ui.components.widgets.formcomponent.FormComponentPanelAbstract;
import org.apache.isis.viewer.wicket.ui.pages.PageType;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Links;

/**
 * {@link FormComponentPanel} representing a reference to an entity: a link and
 * a findUsing button.
 */
public class EntityLink extends FormComponentPanelAbstract<ObjectAdapter> implements CancelHintRequired, ActionInvokeHandler {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_LINK_WRAPPER = "entityLinkWrapper";
    private static final String ID_ENTITY_LINK = "entityLink";
    private static final String ID_ENTITY_OID = "entityOid";
    private static final String ID_ENTITY_TITLE = "entityTitle";
    private static final String ID_ENTITY_TITLE_NULL = "entityTitleNull";
    private static final String ID_ENTITY_IMAGE = "entityImage";
    private static final String ID_CHOICES = "choices";
    private static final String ID_FIND_USING = "findUsing";
    private static final String ID_ENTITY_DETAILS_LINK = "entityDetailsLink";
    private static final String ID_ENTITY_DETAILS_LINK_LABEL = "entityDetailsLinkLabel";
    private static final String ID_ENTITY_DETAILS = "entityDetails";

    private final FindUsingLinkFactory linkFactory;

    private TextField<ObjectAdapterMemento> entityOidField;
    private WebMarkupContainer findUsing;
    private PanelAbstract<?> actionFindUsingComponent;

    private Image image;
    private Label label;
    private ObjectAdapterMemento pending;

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
        syncWithInput();
    }

    private void addOrReplaceOidField() {
        entityOidField = new TextField<ObjectAdapterMemento>(ID_ENTITY_OID, new Model<ObjectAdapterMemento>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ObjectAdapterMemento getObject() {
                if (pending != null) {
                    return pending;
                }
                final ObjectAdapter adapter = EntityLink.this.getModelObject();
                return ObjectAdapterMemento.createOrNull(adapter);
            }

            @Override
            public void setObject(final ObjectAdapterMemento adapterMemento) {
                pending = adapterMemento;
            }

        }) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onModelChanged() {
                super.onModelChanged();
                syncWithInput();
            }

        };
        entityOidField.setType(ObjectAdapterMemento.class);
        addOrReplace(entityOidField);
        entityOidField.setVisible(false);
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
     * <tt>findUsing</tt> button is shown/not shown as required.
     * 
     * <p>
     * REVIEW: there ought to be a better way to do this. I'd hoped to override
     * {@link #setEnabled(boolean)}, but it is <tt>final</tt>, and there doesn't
     * seem to be anyway to install a listener. One option might be to move it
     * to {@link #onBeforeRender()} ?
     */
    public void syncFindUsingVisibility() {
        findUsing.setVisible(isEnabled() && !getEntityModel().isViewMode());
    }

    /**
     * Since we override {@link #convertInput()}, it is (apparently) enough to
     * just return a value that is suitable for error reporting.
     * 
     * @see DateField#getInput() for reference
     */
    @Override
    public String getInput() {
        return entityOidField.getInput();
    }

    /**
     * Ensures that the link is always enabled and traversable, even if (in the
     * context of an entity property form) the entity model is in view mode.
     * 
     * <p>
     * A slight hack, but works...
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    protected void convertInput() {
        final ObjectAdapter pendingAdapter = getPendingAdapter();
        setConvertedInput(pendingAdapter);
    }

    private ObjectAdapter getPendingAdapter() {
        final ObjectAdapterMemento memento = entityOidField.getModelObject();
        return memento != null ? memento.getObjectAdapter(ConcurrencyChecking.NO_CHECK) : null;
    }

    @Override
    protected void onBeforeRender() {
        syncWithInput();
        super.onBeforeRender();
    }

    private void syncWithInput() {
        final EntityModel entityModel = getEntityModel();

        final ObjectAdapter adapter = Generics.coalesce(getPendingAdapter(), entityModel.getObject());

        syncImageWithInput(adapter);

        final IModel<List<? extends ObjectAdapterMemento>> choicesMementos = getChoicesModel();
        if (choicesMementos != null) {

            // choices drop-down
            final IModel<ObjectAdapterMemento> modelObject = entityOidField.getModel();
            addOrReplace(new DropDownChoicesForObjectAdapterMementos(ID_CHOICES, modelObject, choicesMementos));

            // no need for link, since can see in drop-down
            permanentlyHide(ID_ENTITY_LINK_WRAPPER);

            // no need for the 'null' title, since if there is no object yet
            // can represent this fact in the drop-down
            permanentlyHide(ID_ENTITY_TITLE_NULL);
        } else {

            // choices drop-down
            permanentlyHide(ID_CHOICES);

            // show link if have value
            syncLinkWithInput(adapter);

            // represent no object by a simple label displaying '(null)'
            syncEntityTitleNullWithInput(adapter);
        }

        // link
        syncEntityDetailsButtonWithInput(adapter);

        syncEntityDetailsWithInput(adapter);
        syncFindUsingVisibility();
    }

    private void syncImageWithInput(final ObjectAdapter adapter) {
        if (adapter != null) {
            addOrReplaceImage(adapter);
        } else {
            permanentlyHide(ID_ENTITY_IMAGE);
        }
    }

    private void syncEntityDetailsButtonWithInput(final ObjectAdapter adapter) {
        if (adapter != null && getEntityModel().isEditMode()) {
            final Link<String> link = new Link<String>(ID_ENTITY_DETAILS_LINK) {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    getEntityModel().toggleDetails();
                }
            };
            addOrReplace(link);
            link.add(new Label(ID_ENTITY_DETAILS_LINK_LABEL, buildEntityDetailsModel()));
        } else {
            permanentlyHide(ID_ENTITY_DETAILS_LINK);
        }
    }

    private Model<String> buildEntityDetailsModel() {
        final String label = getEntityModel().isEntityDetailsVisible() ? "hide" : "show";
        return Model.of(label);
    }

    private void syncLinkWithInput(final ObjectAdapter adapter) {
        if (adapter != null) {
            addOrReplaceLink(adapter);
        } else {
            permanentlyHide(ID_ENTITY_LINK_WRAPPER);
        }
    }

    private void syncEntityTitleNullWithInput(final ObjectAdapter adapter) {
        if (adapter != null) {
            permanentlyHide(ID_ENTITY_TITLE_NULL);
        } else {
            addOrReplace(new Label(ID_ENTITY_TITLE_NULL, "(null)"));
        }
    }

    private void syncEntityDetailsWithInput(final ObjectAdapter adapter) {
        if (adapter != null && getEntityModel().isEntityDetailsVisible()) {
            final EntityModel entityModel = new EntityModel(adapter);
            addOrReplace(new EntityCombinedPanel(ID_ENTITY_DETAILS, entityModel));
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

    private void addOrReplaceImage(final ObjectAdapter adapter) {
        final PackageResource imageResource = determineImageResource(adapter);

        if (imageResource != null) {
            image = new Image(ID_ENTITY_IMAGE, imageResource);
            addOrReplace(image);
        } else {
            permanentlyHide(ID_ENTITY_IMAGE);
        }
    }

    private PackageResource determineImageResource(final ObjectAdapter adapter) {
        ObjectSpecification typeOfSpec;
        PackageResource imageResource = null;
        if (adapter != null) {
            typeOfSpec = adapter.getSpecification();
            final IconFacet iconFacet = typeOfSpec.getFacet(IconFacet.class);
            if (iconFacet != null) {
                final String iconName = iconFacet.iconName(adapter);
                imageResource = getImageCache().findImage(iconName);
            }
        }
        if (imageResource == null) {
            typeOfSpec = getEntityModel().getTypeOfSpecification();
            imageResource = getImageCache().findImage(typeOfSpec);
        }
        return imageResource;
    }

    private void addOrReplaceLink(final ObjectAdapter adapter) {
        final PageParameters pageParameters = EntityModel.createPageParameters(adapter);
        final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ENTITY);
        final String linkId = ID_ENTITY_LINK;
        final AbstractLink link = newLink(linkId, pageClass, pageParameters);
        label = new Label(ID_ENTITY_TITLE, adapter.titleString());
        link.add(label);
        final WebMarkupContainer entityLinkWrapper = new WebMarkupContainer(ID_ENTITY_LINK_WRAPPER);
        entityLinkWrapper.addOrReplace(link);

        entityLinkWrapper.setEnabled(true);

        addOrReplace(entityLinkWrapper);
    }

    protected AbstractLink newLink(final String linkId, final Class<? extends Page> pageClass, final PageParameters pageParameters) {
        return Links.newBookmarkablePageLink(linkId, pageParameters, pageClass);
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
        entityOidField.setDefaultModelObject(selectedAdapterMemento);
        rebuildFindUsingMenu();
        renderSamePage();
    }

    public void onNoResults() {
        rebuildFindUsingMenu();
        renderSamePage();
    }

    @Override
    public void onCancel() {
        entityOidField.clearInput();
        this.pending = null;
    }

    private void renderSamePage() {
        setResponsePage(getPage());
    }
}
