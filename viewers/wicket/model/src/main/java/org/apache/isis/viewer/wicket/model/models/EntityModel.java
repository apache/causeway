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

package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.util.ComponentHintKey;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.val;

/**
 * Backing model to represent a {@link ManagedObject}.
 *
 * <p>
 * So that the model is {@link Serializable}, the {@link ManagedObject} is
 * stored as a {@link ObjectMemento}.
 */
//@Log4j2
public class EntityModel 
extends ManagedObjectModel 
implements ObjectAdapterModel, UiHintContainer, ObjectUiModel, BookmarkableModel {

    private static final long serialVersionUID = 1L;
    
    // -- FACTORY METHODS FOR PAGE PARAMETERS

    /**
     * Factory method for creating {@link PageParameters} to represent an
     * entity.
     */
    public static PageParameters createPageParameters(ManagedObject adapter) {

        val pageParameters = PageParametersUtils.newPageParameters();
        val isEntity = ManagedObject.isIdentifiable(adapter);

        if (isEntity) {
            ManagedObject.stringify(adapter)
            .ifPresent(oidStr->PageParameterNames.OBJECT_OID.addStringTo(pageParameters, oidStr));
        } else {
            // don't do anything; instead the page should be redirected back to
            // an EntityPage so that the underlying EntityModel that contains
            // the memento for the transient ObjectAdapter can be accessed.
        }
        return pageParameters;
    }

    private final Map<PropertyMemento, ScalarModel> propertyScalarModels;
    
    private ObjectMemento contextAdapterIfAny;

    private Mode mode;
    private RenderingHint renderingHint;

    // -- FACTORIES

    public static EntityModel ofParameters(
            IsisWebAppCommonContext commonContext, 
            PageParameters pageParameters) {

        return ofMemento(commonContext, commonContext.mementoFor(rootOidFrom(pageParameters)));
    }
    
    public static EntityModel ofAdapter(
            IsisWebAppCommonContext commonContext,
            ManagedObject adapter) {
        
        val adapterMemento = commonContext.mementoFor(adapter);
        return ofMemento(commonContext, adapterMemento);
    }

    public static EntityModel ofMemento(
            IsisWebAppCommonContext commonContext,
            ObjectMemento adapterMemento) {
        
        return ofMemento(commonContext, adapterMemento, /*propertyScalarModels*/null);
    }

    private static EntityModel ofMemento(
            IsisWebAppCommonContext commonContext,
            ObjectMemento adapterMemento,
            @Nullable Map<PropertyMemento, ScalarModel> propertyScalarModels) {
        
        return new EntityModel(commonContext, adapterMemento, propertyScalarModels, 
                Mode.VIEW, RenderingHint.REGULAR);
    }
    
    /**
     * As used by TreeModel (same as {@link #ofAdapter(IsisWebAppCommonContext, ManagedObject)}
     */
    protected EntityModel(
            IsisWebAppCommonContext commonContext,
            ManagedObject adapter) {
        
        this(commonContext, 
                commonContext.mementoFor(adapter), 
                /*propertyScalarModels*/null, 
                Mode.VIEW, RenderingHint.REGULAR);
    }
    
    /**
     * As used by ScalarModel
     */
    protected EntityModel(IsisWebAppCommonContext commonContext, Mode mode, RenderingHint renderingHint) {
        this(commonContext, null, _Maps.<PropertyMemento, ScalarModel>newHashMap(), 
                mode, renderingHint);
    }

    private EntityModel(
            IsisWebAppCommonContext commonContext,
            ObjectMemento adapterMemento,
            @Nullable Map<PropertyMemento, ScalarModel> propertyScalarModels,
            Mode mode,
            RenderingHint renderingHint) {
        
        super(requires(commonContext, "commonContext"), adapterMemento);
        
        this.propertyScalarModels = propertyScalarModels!=null ? propertyScalarModels : _Maps.<PropertyMemento, ScalarModel>newHashMap();
        this.mode = mode;
        this.renderingHint = renderingHint;
    }

    public static String oidStr(final PageParameters pageParameters) {
        return PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
    }

    private static RootOid rootOidFrom(final PageParameters pageParameters) {
        return Oid.unmarshaller().unmarshal(oidStr(pageParameters), RootOid.class);
    }


    //////////////////////////////////////////////////
    // BookmarkableModel
    //////////////////////////////////////////////////


    @Override
    public PageParameters getPageParameters() {
        PageParameters pageParameters = createPageParameters(getObject());
        HintPageParameterSerializer.hintStoreToPageParameters(pageParameters, this);
        return pageParameters;
    }

    @Override
    public boolean isInlinePrompt() {
        return false;
    }

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        return createPageParameters(getObject());
    }

    // //////////////////////////////////////////////////////////
    // Hint support
    // //////////////////////////////////////////////////////////

    @Override
    public String getHint(final Component component, final String keyName) {
        final ComponentHintKey componentHintKey = ComponentHintKey.create(super.getCommonContext(), component, keyName);
        if(componentHintKey != null) {
            return componentHintKey.get(super.asHintingBookmarkIfSupported());
        }
        return null;
    }

    @Override
    public void setHint(Component component, String keyName, String hintValue) {
        ComponentHintKey componentHintKey = ComponentHintKey.create(super.getCommonContext(), component, keyName);
        componentHintKey.set(super.asHintingBookmarkIfSupported(), hintValue);
    }

    @Override
    public void clearHint(Component component, String attributeName) {
        setHint(component, attributeName, null);
    }


    @Override
    public String getTitle() {
        return getObject().titleString(null);
    }

    @Override
    public ManagedObject getManagedObject() {
        return getObject();
    }

    // //////////////////////////////////////////////////////////
    // PropertyModels
    // //////////////////////////////////////////////////////////

    /**
     * Lazily populates with the current value of each property.
     */
    public ScalarModel getPropertyModel(
            final PropertyMemento pm,
            final Mode mode,
            final RenderingHint renderingHint) {
        ScalarModel scalarModel = propertyScalarModels.get(pm);
        if (scalarModel == null) {
            scalarModel = new ScalarPropertyModel(this, pm, mode, renderingHint);

            propertyScalarModels.put(pm, scalarModel);
        }
        return scalarModel;

    }

    /**
     * Resets the {@link #propertyScalarModels hash} of {@link ScalarModel}s for
     * each {@link PropertyMemento property} to the value held in the underlying
     * {@link #getObject() entity}.
     */
    public void resetPropertyModels() {
        //adapterMemento.resetVersion();
        for (final PropertyMemento pm : propertyScalarModels.keySet()) {
            OneToOneAssociation otoa = pm.getProperty(super.getSpecificationLoader());
            val scalarModel = propertyScalarModels.get(pm);
            val adapter = getObject();
            val associatedAdapter =
                    otoa.get(adapter, InteractionInitiatedBy.USER);
            scalarModel.setObject(associatedAdapter);
        }
    }

    // //////////////////////////////////////////////////////////
    // RenderingHint, Mode, entityDetailsVisible
    // //////////////////////////////////////////////////////////


    @Override
    public RenderingHint getRenderingHint() {
        return renderingHint;
    }
    @Override
    public void setRenderingHint(RenderingHint renderingHint) {
        this.renderingHint = renderingHint;
    }

    @Override
    public ObjectMemento getContextAdapterIfAny() {
        return contextAdapterIfAny;
    }

    /**
     * Used as a hint when the {@link #getRenderingHint()} is {@link RenderingHint#PARENTED_TITLE_COLUMN},
     * provides a context adapter to obtain the title.
     */
    @Override
    public void setContextAdapterIfAny(ObjectMemento contextAdapterIfAny) {
        this.contextAdapterIfAny = contextAdapterIfAny;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    protected void setMode(final Mode mode) {
        this.mode = mode;
    }

    public boolean isViewMode() {
        return mode == Mode.VIEW;
    }

    public boolean isEditMode() {
        return mode == Mode.EDIT;
    }

    public EntityModel toEditMode() {
        setMode(Mode.EDIT);
        for (final ScalarModel scalarModel : propertyScalarModels.values()) {
            scalarModel.toEditMode();
        }
        return this;
    }

    public EntityModel toViewMode() {
        setMode(Mode.VIEW);
        for (final ScalarModel scalarModel : propertyScalarModels.values()) {
            scalarModel.toViewMode();
        }
        return this;
    }

    // //////////////////////////////////////////////////////////
    // detach
    // //////////////////////////////////////////////////////////

    @Override
    protected void onDetach() {
        super.onDetach();
        for (PropertyMemento propertyMemento : propertyScalarModels.keySet()) {
            final ScalarModel scalarModel = propertyScalarModels.get(propertyMemento);
            scalarModel.detach();
        }
        // we no longer clear these, because we want to call resetPropertyModels(...) after an object has been updated.
        //propertyScalarModels.clear();
    }


    // //////////////////////////////////////////////////////////
    // tab and column metadata (if any)
    // //////////////////////////////////////////////////////////

    private CollectionLayoutData collectionLayoutData;

    public CollectionLayoutData getCollectionLayoutData() {
        return collectionLayoutData;
    }

    public void setCollectionLayoutData(final CollectionLayoutData collectionLayoutData) {
        this.collectionLayoutData = collectionLayoutData;
    }


}
