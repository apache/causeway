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
package org.apache.causeway.viewer.wicket.model.models;

import java.util.Map;
import java.util.Objects;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.hint.HintStore;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.commons.ScalarRepresentation;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.spec.feature.memento.PropertyMemento;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.commons.model.object.UiObject;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;
import org.apache.causeway.viewer.wicket.model.models.interaction.prop.PropertyInteractionWkt;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Backing model to represent a domain object as {@link ManagedObject}.
 */
@Log4j2
public class UiObjectWkt
extends HasBookmarkedOwnerAbstract<ManagedObject>
implements
    UiObject,
    ObjectAdapterModel,
    UiHintContainer,
    BookmarkableModel {

    private static final long serialVersionUID = 1L;

    // -- FACTORIES

    public static UiObjectWkt ofPageParameters(
            final MetaModelContext commonContext,
            final PageParameters pageParameters) {
        val bookmark = PageParameterUtils.toBookmark(pageParameters).orElse(null);
        return ofBookmark(commonContext, bookmark);
    }

    public static UiObjectWkt ofAdapter(
            final @NonNull MetaModelContext commonContext,
            final @Nullable ManagedObject adapter) {
        return new UiObjectWkt(BookmarkedObjectWkt.ofAdapter(commonContext, adapter),
                ScalarRepresentation.VIEWING, RenderingHint.REGULAR);
    }

    public static UiObjectWkt ofAdapterForCollection(
            final MetaModelContext commonContext,
            final ManagedObject adapter,
            final @NonNull EntityCollectionModel.Variant variant) {
        return new UiObjectWkt(BookmarkedObjectWkt.ofAdapter(commonContext, adapter),
                ScalarRepresentation.VIEWING, variant.getTitleColumnRenderingHint());
    }


    public static UiObjectWkt ofBookmark(
            final @NonNull MetaModelContext commonContext,
            final @Nullable Bookmark bookmark) {
        return new UiObjectWkt(BookmarkedObjectWkt.ofBookmark(commonContext, bookmark),
                ScalarRepresentation.VIEWING, RenderingHint.REGULAR);
    }

    // -- CONSTRUCTORS

    /**
     * As used by TreeModel (same as {@link #ofAdapter(MetaModelContext, ManagedObject)}
     */
    protected UiObjectWkt(
            final MetaModelContext commonContext,
            final ManagedObject adapter) {
        this(BookmarkedObjectWkt.ofAdapter(commonContext, adapter),
                ScalarRepresentation.VIEWING, RenderingHint.REGULAR);
    }

    private UiObjectWkt(
            final @NonNull BookmarkedObjectWkt bookmarkedObject,
            final ScalarRepresentation mode,
            final RenderingHint renderingHint) {
        super(bookmarkedObject);
        this.mode = mode;
        this.renderingHint = renderingHint;
    }

    @Override
    protected ManagedObject load() {
        return super.getBookmarkedOwner();
    }

    // -- BOOKMARKABLE MODEL

    @Override
    public PageParameters getPageParameters() {
        return _HintPageParameterSerializer
                .hintStoreToPageParameters(
                        hintStore(),
                        getPageParametersWithoutUiHints(),
                        getOwnerBookmark());
    }

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        return PageParameterUtils.createPageParametersForObject(getBookmarkedOwner());
    }

    @Override
    public boolean isInlinePrompt() {
        return false;
    }

    // -- HINT SUPPORT

    @Getter(onMethod = @__(@Override))
    @Setter(onMethod = @__(@Override))
    private ScalarRepresentation mode;

    @Getter(onMethod = @__(@Override))
    private RenderingHint renderingHint;

    @Override
    public String getHint(final Component component, final String keyName) {
        final ComponentHintKey componentHintKey = ComponentHintKey.create(super.getMetaModelContext(), component, keyName);
        if(componentHintKey != null) {
            return componentHintKey.get(getOwnerBookmark());
        }
        return null;
    }

    @Override
    public void setHint(final Component component, final String keyName, final String hintValue) {
        ComponentHintKey componentHintKey = ComponentHintKey.create(super.getMetaModelContext(), component, keyName);
        componentHintKey.set(getOwnerBookmark(), hintValue);
    }

    @Override
    public void clearHint(final Component component, final String attributeName) {
        setHint(component, attributeName, null);
    }

    // -- OTHER OBJECT SPECIFIC

    @Override
    public String getTitle() {
        return getManagedObject().getTitle();
    }

    @Override
    public ManagedObject getManagedObject() {
        return getObject();
    }

    // -- PROPERTY MODELS (CHILDREN)

    private transient Map<PropertyMemento, ScalarPropertyModel> propertyScalarModels;
    private Map<PropertyMemento, ScalarPropertyModel> propertyScalarModels() {
        if(propertyScalarModels==null) {
            propertyScalarModels = _Maps.<PropertyMemento, ScalarPropertyModel>newHashMap();
        }
        return propertyScalarModels;
    }

    /**
     * Lazily populates with the current value of each property.
     */
    public ScalarModel getPropertyModel(
            final OneToOneAssociation property,
            final ScalarRepresentation viewOrEdit,
            final RenderingHint renderingHint) {

        val pm = property.getMemento();
        val propertyScalarModels = propertyScalarModels();
        final ScalarModel existingScalarModel = propertyScalarModels.get(pm);
        if (existingScalarModel == null) {

            val propertyInteractionModel = new PropertyInteractionWkt(
                    bookmarkedObjectModel(),
                    pm.getIdentifier().getMemberLogicalName(),
                    renderingHint.asWhere());

            final long modelsAdded = propertyInteractionModel.streamPropertyUiModels()
            .map(uiModel->ScalarPropertyModel.wrap(uiModel, viewOrEdit, renderingHint))
            .peek(scalarModel->log.debug("adding: {}", scalarModel))
            .filter(scalarModel->propertyScalarModels.put(pm, scalarModel)==null)
            .count();

            // future extensions might allow to add multiple UI models per single property model (typed tuple support)
            _Assert.assertEquals(1L, modelsAdded, ()->
                String.format("unexpected number of propertyScalarModels added %d", modelsAdded));

        }
        return propertyScalarModels.get(pm);

    }

    // -- VIEW OR EDIT

    @Override
    public UiObjectWkt toEditMode() {
        //noop for objects
        return this;
    }

    @Override
    public UiObjectWkt toViewMode() {
        //noop for objects
        return this;
    }

    // -- DETACH

    @Override
    protected void onDetach() {
        propertyScalarModels().values()
            .forEach(ScalarPropertyModel::detach);
        super.onDetach();
        propertyScalarModels = null;
    }

    // -- TAB AND COLUMN (metadata if any)

    @Getter @Setter
    private CollectionLayoutData collectionLayoutData;

    @Setter
    private @Nullable Bookmark contextBookmarkIfAny;

    @Override @Synchronized
    @Deprecated // this check should be made available with 'core' models - and not modeled here
    public boolean isContextAdapter(final ManagedObject other) {
        return contextBookmarkIfAny==null
                ? false
                : Objects.equals(contextBookmarkIfAny, other.getBookmark().orElse(null))
                ;
    }

    // -- HELPER

    private transient HintStore hintStore;
    private HintStore hintStore() {
        return hintStore = getMetaModelContext().loadServiceIfAbsent(HintStore.class, hintStore);
    }



}
