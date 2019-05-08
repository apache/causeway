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

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.util.ComponentHintKey;

import lombok.val;

/**
 * Backing model to represent a {@link ObjectAdapter}.
 *
 * <p>
 * So that the model is {@link Serializable}, the {@link ObjectAdapter} is
 * stored as a {@link ObjectAdapterMemento}.
 */
public class EntityModel extends BookmarkableModel<ObjectAdapter> 
implements ObjectAdapterModel, UiHintContainer {

    private static final long serialVersionUID = 1L;

    // //////////////////////////////////////////////////////////
    // factory methods for PageParameters
    // //////////////////////////////////////////////////////////

    /**
     * Factory method for creating {@link PageParameters} to represent an
     * entity.
     */
    public static PageParameters createPageParameters(final ObjectAdapter adapter) {

        final PageParameters pageParameters = PageParametersUtils.newPageParameters();

        final Boolean persistent = adapter != null && adapter.isRepresentingPersistent();

        if (persistent) {
            final String oidStr = adapter.getOid().enStringNoVersion();
            PageParameterNames.OBJECT_OID.addStringTo(pageParameters, oidStr);
        } else {
            // don't do anything; instead the page should be redirected back to
            // an EntityPage so that the underlying EntityModel that contains
            // the memento for the transient ObjectAdapter can be accessed.
        }
        return pageParameters;
    }

    @Deprecated //TODO [2033] remove
    public void resetVersion() {
        if(getObjectAdapterMemento() == null) {
            return;
        }
        getObjectAdapterMemento().resetVersion();
    }

    public enum RenderingHint {
        REGULAR(Where.OBJECT_FORMS),
        PARENTED_PROPERTY_COLUMN(Where.PARENTED_TABLES),
        PARENTED_TITLE_COLUMN(Where.PARENTED_TABLES),
        STANDALONE_PROPERTY_COLUMN(Where.STANDALONE_TABLES),
        STANDALONE_TITLE_COLUMN(Where.STANDALONE_TABLES);

        private final Where where;

        RenderingHint(final Where where) {
            this.where = where;
        }

        public boolean isRegular() {
            return this == REGULAR;
        }

        public boolean isInParentedTable() {
            return this == PARENTED_PROPERTY_COLUMN;
        }
        public boolean isInStandaloneTable() {
            return this == STANDALONE_PROPERTY_COLUMN;
        }

        public boolean isInTable() {
            return isInParentedTable() || isInStandaloneTable() || isInTableTitleColumn();
        }

        public boolean isInTableTitleColumn() {
            return isInParentedTableTitleColumn() || isInStandaloneTableTitleColumn();
        }

        public boolean isInParentedTableTitleColumn() {
            return this == PARENTED_TITLE_COLUMN;
        }

        public boolean isInStandaloneTableTitleColumn() {
            return this == STANDALONE_TITLE_COLUMN;
        }

        public Where asWhere() {
            return this.where;
        }
    }

    public enum Mode {
        VIEW,EDIT
    }

    private final Map<PropertyMemento, ScalarModel> propertyScalarModels;
    private ObjectAdapterMemento adapterMemento;
    private ObjectAdapterMemento contextAdapterIfAny;

    private Mode mode;
    private RenderingHint renderingHint;
    private final PendingModel pendingModel;


    /**
     * {@link ConcurrencyException}, if any, that might have occurred previously
     */
    private ConcurrencyException concurrencyException;



    // //////////////////////////////////////////////////////////
    // constructors
    // //////////////////////////////////////////////////////////

    /**
     * As used by ScalarModel
     */
    public EntityModel(
            final Mode mode,
            final RenderingHint renderingHint) {
        this(_Maps.<PropertyMemento, ScalarModel>newHashMap(), null, mode, renderingHint);
    }

    public EntityModel(final PageParameters pageParameters) {
        this(ObjectAdapterMemento.ofRootOid(rootOidFrom(pageParameters)));
    }

    public EntityModel(final ObjectAdapter adapter) {
        this(ObjectAdapterMemento.ofAdapter(adapter));
        setObject(adapter);
    }

    public EntityModel(final ObjectAdapterMemento adapterMemento) {
        this(adapterMemento, _Maps.<PropertyMemento, ScalarModel>newHashMap());
    }

    public EntityModel(
            final ObjectAdapterMemento adapterMemento,
            final Map<PropertyMemento, ScalarModel> propertyScalarModels) {
        this(propertyScalarModels, adapterMemento);
    }

    // this constructor was introduced just so that, when debugging, could distinguish between EntityModel instantiated
    // in its own right, vs instantiated via the cloneWithLayoutMetadata.
    private EntityModel(
            final Map<PropertyMemento, ScalarModel> propertyScalarModels,
            final ObjectAdapterMemento adapterMemento) {
        this(propertyScalarModels, adapterMemento, Mode.VIEW, RenderingHint.REGULAR);
    }

    private EntityModel(
            final Map<PropertyMemento, ScalarModel> propertyScalarModels,
            final ObjectAdapterMemento adapterMemento,
            final Mode mode,
            final RenderingHint renderingHint) {
        this.adapterMemento = adapterMemento;
        this.pendingModel = new PendingModel(this);
        this.propertyScalarModels = propertyScalarModels;
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
        final ComponentHintKey componentHintKey = ComponentHintKey.create(component, keyName);
        if(componentHintKey != null) {
            return componentHintKey.get(getObjectAdapterMemento().asHintingBookmarkIfSupported());
        }
        return null;
    }

    @Override
    public void setHint(Component component, String keyName, String hintValue) {
        ComponentHintKey componentHintKey = ComponentHintKey.create(component, keyName);
        componentHintKey.set(this.getObjectAdapterMemento().asHintingBookmarkIfSupported(), hintValue);
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
    public boolean hasAsRootPolicy() {
        return hasBookmarkPolicy(BookmarkPolicy.AS_ROOT);
    }

    public boolean hasAsChildPolicy() {
        return hasBookmarkPolicy(BookmarkPolicy.AS_CHILD);
    }

    private boolean hasBookmarkPolicy(final BookmarkPolicy policy) {
        final BookmarkPolicyFacet facet = getBookmarkPolicyFacetIfAny();
        return facet != null && facet.value() == policy;
    }

    private BookmarkPolicyFacet getBookmarkPolicyFacetIfAny() {
        final ObjectSpecId specId = getObjectAdapterMemento().getObjectSpecId();
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(specId);
        return objectSpec.getFacet(BookmarkPolicyFacet.class);
    }



    // //////////////////////////////////////////////////////////
    // ObjectAdapterMemento, typeOfSpecification
    // //////////////////////////////////////////////////////////

    public ObjectAdapterMemento getObjectAdapterMemento() {
        return adapterMemento;
    }

    /**
     * Overridable for submodels (eg {@link ScalarModel}) that know the type of
     * the adapter without there being one.
     */
    @Override
    public ObjectSpecification getTypeOfSpecification() {
        if (adapterMemento == null) {
            return null;
        }
        return getSpecificationFor(adapterMemento.getObjectSpecId());
    }

    private ObjectSpecification getSpecificationFor(ObjectSpecId objectSpecId) {
        return getSpecificationLoader().lookupBySpecId(objectSpecId);
    }

    // //////////////////////////////////////////////////////////
    // loadObject, load, setObject
    // //////////////////////////////////////////////////////////

    private final static _Probe probe = _Probe.unlimited().label("EntityModel");
    
    /**
     * Not Wicket API, but used by <tt>EntityPage</tt> to do eager loading
     * when rendering after post-and-redirect.
     * @return
     */
    @Deprecated //TODO [2033] remove ?
    public ObjectAdapter load(ConcurrencyChecking concurrencyChecking) {
        
        if(concurrencyChecking==ConcurrencyChecking.CHECK && adapterMemento!=null) {
            val spec = IsisContext.getSpecificationLoader().lookupBySpecId(adapterMemento.getObjectSpecId());
            if(spec.isEntity()) {
                val info = "adapterMemento '"+adapterMemento+"'";
                probe.warnNotImplementedYet("[2033] ConcurrencyChecking no longer supported!? "+info);              
            }
        }
        
        return load();
    }

    /**
     * Callback from {@link #getObject()}, defaults to loading the object
     * using {@link ConcurrencyChecking#CHECK strict} checking.
     *
     * <p>
     * If non-strict checking is required, then just call {@link #load(ConcurrencyChecking)} with an
     * argument of {@link ConcurrencyChecking#NO_CHECK} first.
     */
    @Override
    public ObjectAdapter load() {
        if (adapterMemento == null) {
            return null;
        }
        final ObjectAdapter objectAdapter = adapterMemento.getObjectAdapter();
        return objectAdapter;
    }


    @Override
    public void setObject(final ObjectAdapter adapter) {
        super.setObject(adapter);
        adapterMemento = ObjectAdapterMemento.ofAdapter(adapter);
    }

    public void setObjectMemento(final ObjectAdapterMemento memento) {
        super.setObject(
                memento != null
                ? memento.getObjectAdapter()
                        : null);
        adapterMemento = memento;
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
            scalarModel = new ScalarModel(this, pm, mode, renderingHint);

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
        adapterMemento.resetVersion();
        for (final PropertyMemento pm : propertyScalarModels.keySet()) {
            OneToOneAssociation otoa = pm.getProperty(getSpecificationLoader());
            final ScalarModel scalarModel = propertyScalarModels.get(pm);
            final ObjectAdapter adapter = getObject();
            final ObjectAdapter associatedAdapter =
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
    public ObjectAdapterMemento getContextAdapterIfAny() {
        return contextAdapterIfAny;
    }

    /**
     * Used as a hint when the {@link #getRenderingHint()} is {@link RenderingHint#PARENTED_TITLE_COLUMN},
     * provides a context adapter to obtain the title.
     */
    @Override
    public void setContextAdapterIfAny(ObjectAdapterMemento contextAdapterIfAny) {
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
    // concurrency exceptions
    // //////////////////////////////////////////////////////////

    public void setException(ConcurrencyException ex) {
        this.concurrencyException = ex;
    }

    public String getAndClearConcurrencyExceptionIfAny() {
        if(concurrencyException == null) {
            return null;
        }
        final String message = concurrencyException.getMessage();
        concurrencyException = null;
        return message;
    }


    // //////////////////////////////////////////////////////////
    // Pending
    // //////////////////////////////////////////////////////////

    private static final class PendingModel extends Model<ObjectAdapterMemento> {
        private static final long serialVersionUID = 1L;

        private final EntityModel entityModel;

        /**
         * Whether pending has been set (could have been set to null)
         */
        private boolean hasPending;
        /**
         * The new value (could be set to null; hasPending is used to distinguish).
         */
        private ObjectAdapterMemento pending;


        public PendingModel(EntityModel entityModel) {
            this.entityModel = entityModel;
        }

        @Override
        public ObjectAdapterMemento getObject() {
            if (hasPending) {
                return pending;
            }
            final ObjectAdapter adapter = entityModel.getObject();
            return ObjectAdapterMemento.ofAdapter(adapter);
        }

        @Override
        public void setObject(final ObjectAdapterMemento adapterMemento) {
            pending = adapterMemento;
            hasPending = true;
        }

        public void clearPending() {
            this.hasPending = false;
            this.pending = null;
        }

        private ObjectAdapter getPendingAdapter() {
            final ObjectAdapterMemento memento = getObject();
            return memento != null
                    ? memento.getObjectAdapter()
                            : null;
        }

        public ObjectAdapter getPendingElseCurrentAdapter() {
            return hasPending ? getPendingAdapter() : entityModel.getObject();
        }

        public ObjectAdapterMemento getPending() {
            return pending;
        }

        public void setPending(ObjectAdapterMemento selectedAdapterMemento) {
            this.pending = selectedAdapterMemento;
            hasPending=true;
        }
    }


    public ObjectAdapter getPendingElseCurrentAdapter() {
        return pendingModel.getPendingElseCurrentAdapter();
    }

    public ObjectAdapter getPendingAdapter() {
        return pendingModel.getPendingAdapter();
    }

    public ObjectAdapterMemento getPending() {
        return pendingModel.getPending();
    }

    public void setPending(ObjectAdapterMemento selectedAdapterMemento) {
        pendingModel.setPending(selectedAdapterMemento);
    }

    public void clearPending() {
        pendingModel.clearPending();
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




    // //////////////////////////////////////////////////////////
    // equals, hashCode
    // //////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((adapterMemento == null) ? 0 : adapterMemento.hashCode());
        return result;
    }

    /**
     * In order that <tt>IsisAjaxFallbackDataTable</tt> can use a
     * <tt>ReuseIfModelsEqualStrategy</tt> to preserve any concurrency exception
     * information in original model.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityModel other = (EntityModel) obj;
        if (adapterMemento == null) {
            if (other.adapterMemento != null)
                return false;
        } else if (!adapterMemento.equals(other.adapterMemento))
            return false;
        return true;

    }



}
