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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.hint.HintStore;
import org.apache.causeway.applib.services.render.ObjectIcon;
import org.apache.causeway.applib.services.render.ObjectIconEmbedded;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.commons.model.object.UiObject;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModel;
import org.apache.causeway.viewer.wicket.model.models.interaction.BookmarkedObjectWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.HasBookmarkedOwnerAbstract;
import org.apache.causeway.viewer.wicket.model.models.interaction.prop.PropertyInteractionWkt;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

/**
 * Backing model to represent a domain object as {@link ManagedObject}.
 */
@Slf4j
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
            final PageParameters pageParameters) {
        var bookmark = PageParameterUtils.toBookmark(pageParameters).orElse(null);
        return ofBookmark(bookmark);
    }

    public static UiObjectWkt ofAdapter(
            final @Nullable ManagedObject adapter) {
        return new UiObjectWkt(BookmarkedObjectWkt.ofAdapter(adapter),
                ViewOrEditMode.VIEWING, RenderingHint.REGULAR);
    }

    public static UiObjectWkt ofAdapterForCollection(
            final ManagedObject adapter,
            final CollectionModel.@NonNull Variant variant) {
        return new UiObjectWkt(BookmarkedObjectWkt.ofAdapter(adapter),
                ViewOrEditMode.VIEWING, variant.getTitleColumnRenderingHint());
    }

    public static UiObjectWkt ofBookmark(
            final @Nullable Bookmark bookmark) {
        return new UiObjectWkt(BookmarkedObjectWkt.ofBookmark(bookmark),
                ViewOrEditMode.VIEWING, RenderingHint.REGULAR);
    }

    // -- CONSTRUCTORS

    /**
     * As used by TreeModel (same as {@link #ofAdapter(ManagedObject)}
     */
    protected UiObjectWkt(
            final ManagedObject adapter) {
        this(BookmarkedObjectWkt.ofAdapter(adapter),
                ViewOrEditMode.VIEWING, RenderingHint.REGULAR);
    }

    private UiObjectWkt(
            final @NonNull BookmarkedObjectWkt bookmarkedObject,
            final ViewOrEditMode viewOrEditMode,
            final RenderingHint renderingHint) {
        super(bookmarkedObject);
        this.viewOrEditMode = viewOrEditMode;
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
    private ViewOrEditMode viewOrEditMode;

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
    public ObjectIcon getIcon(IconSize iconSize) {
        return getManagedObject().getIcon(iconSize);
    }

    public void visitIconVariantOrElse(
            IconSize iconSize,
            Consumer<ResourceReference> a,
            Consumer<ObjectIconEmbedded> b,
            Consumer<FontAwesomeLayers> c,
            Runnable onNoMatch) {
        visitIconVariant(
            iconSize,
            urlBased->{
                var rref = imageResourceCache().resourceReferenceForObjectIcon(urlBased);
                if(rref!=null) {
                    a.accept(rref);
                } else {
                    onNoMatch.run();
                }
            },
            embedded->b.accept(embedded),
            fa->c.accept(fa.fontAwesomeLayers()));
    }

    @Override
    public ManagedObject getManagedObject() {
        return getObject();
    }

    // -- PROPERTY MODELS (CHILDREN)

    private transient Map<Identifier, PropertyModel> propertyModels;
    private Map<Identifier, PropertyModel> propertyModels() {
        if(propertyModels==null) {
            propertyModels = new HashMap<>();
        }
        return propertyModels;
    }

    /**
     * Lazily populates with the current value of each property.
     */
    public UiAttributeWkt getPropertyModel(
            final OneToOneAssociation property,
            final ViewOrEditMode viewOrEdit,
            final RenderingHint renderingHint) {

        var bookmarkedObjectModel = bookmarkedObjectModel();

        //[CAUSEWAY-3532] guard against (owner entity) object deleted/not-found
        //
        // due to the lazy nature of the underlying model,
        // (that is loading entities only if required),
        // this guard only triggers, once the first property model gets looked up;
        // in other words: this guard only works if every entity has at least a property
        var ownerPojo = bookmarkedObjectModel.managedObject()
                .getPojo();
        if(ownerPojo==null) {
            throw new ObjectNotFoundException(
                    bookmarkedObjectModel.bookmark().identifier());
        }

        var propIdentifier = property.getFeatureIdentifier();
        var propertyModels = propertyModels();
        final UiAttributeWkt existingPropertyModel = propertyModels.get(propIdentifier);
        if (existingPropertyModel != null) {
            return existingPropertyModel;
        }

        var propertyInteractionModel = new PropertyInteractionWkt(
                bookmarkedObjectModel,
                propIdentifier.memberLogicalName(),
                renderingHint.asWhere());

        final long modelsAdded = propertyInteractionModel.streamPropertyUiModels()
        .map(uiModel->PropertyModel.wrap(uiModel, viewOrEdit, renderingHint))
        .peek(propertyModel->log.debug("adding: {}", propertyModel))
        .filter(propertyModel->propertyModels.put(propIdentifier, propertyModel)==null)
        .count(); // consume the stream

        // future extensions might allow to add multiple UI models per single property model (typed tuple support)
        _Assert.assertEquals(1L, modelsAdded, ()->
            String.format("unexpected number of propertyModels added %d", modelsAdded));

        return propertyModels.get(propIdentifier);
    }

    @Override
    public Stream<Bookmark> streamPropertyBookmarks() {
        var candidateAdapter = this.getObject();

        return candidateAdapter.objSpec()
        .streamProperties(MixedIn.EXCLUDED)
        .map(prop->
            ManagedObjects.bookmark(prop.get(candidateAdapter, InteractionInitiatedBy.PASS_THROUGH))
            .orElse(null)
        )
        .filter(_NullSafe::isPresent);
    }

    // -- VIEW OR EDIT

    @Override
    public UiObjectWkt toEditingMode() {
        //noop for objects
        return this;
    }

    @Override
    public UiObjectWkt toViewingMode() {
        //noop for objects
        return this;
    }

    // -- DETACH

    @Override
    protected void onDetach() {
        propertyModels().values()
            .forEach(PropertyModel::detach);
        super.onDetach();
        propertyModels = null;
    }

    // -- TAB AND COLUMN (metadata if any)

    @Setter
    private @Nullable Bookmark contextBookmarkIfAny;

    @Override @Synchronized
    @Deprecated // this check should be made available with 'core' models - and not modeled here
    public boolean isContextAdapter(final ManagedObject other) {
        return contextBookmarkIfAny==null
                ? false
                : Objects.equals(contextBookmarkIfAny, other.getBookmark().orElse(null));
    }

    // -- HELPER

    private transient HintStore hintStore;
    private HintStore hintStore() {
        return hintStore = getMetaModelContext().loadServiceIfAbsent(HintStore.class, hintStore);
    }

    private transient ImageResourceCache imageResourceCache;
    private ImageResourceCache imageResourceCache() {
        return imageResourceCache = getMetaModelContext().loadServiceIfAbsent(ImageResourceCache.class, imageResourceCache);
    }

}
