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
import java.util.Set;

import com.google.common.collect.Maps;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.services.memento.MementoService.Memento;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.services.grid.fixedcols.applib.Hint;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.services.memento.MementoServiceDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.common.PageParametersUtils;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.util.ScopedSessionAttribute;

/**
 * Backing model to represent a {@link ObjectAdapter}.
 * 
 * <p>
 * So that the model is {@link Serializable}, the {@link ObjectAdapter} is
 * stored as a {@link ObjectAdapterMemento}.
 */
public class EntityModel extends BookmarkableModel<ObjectAdapter> {

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

        final Boolean persistent = adapter != null && adapter.representsPersistent();

        if (persistent) {
            final String oidStr = adapter.getOid().enStringNoVersion(getOidMarshaller());

            PageParameterNames.OBJECT_OID.addStringTo(pageParameters, oidStr);
        } else {
            // don't do anything; instead the page should be redirected back to
            // an EntityPage so that the underlying EntityModel that contains
            // the memento for the transient ObjectAdapter can be accessed.
        }
        return pageParameters;
    }

    public enum RenderingHint {
        REGULAR,
        PROPERTY_COLUMN,
        PARENTED_TITLE_COLUMN,
        STANDALONE_TITLE_COLUMN;

        public boolean isRegular() {
            return this == REGULAR;
        }

        public boolean isInTablePropertyColumn() {
            return this == PROPERTY_COLUMN;
        }

        public boolean isInTable() {
            return isInTablePropertyColumn() || isInTableTitleColumn();
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
    }

	public enum Mode {
        VIEW, EDIT;
    }

    private final Map<PropertyMemento, ScalarModel> propertyScalarModels;
    private ObjectAdapterMemento adapterMemento;
    private ObjectAdapterMemento contextAdapterIfAny;

    private Mode mode = Mode.VIEW;
    private RenderingHint renderingHint = RenderingHint.REGULAR;
    private final PendingModel pendingModel;

    /**
     * Toggled by 'entityDetailsButton'.
     */
    private boolean entityDetailsVisible;

    /**
     * {@link ConcurrencyException}, if any, that might have occurred previously
     */
    private ConcurrencyException concurrencyException;

    private final HintPageParameterSerializer hintPageParameterSerializer = new HintPageParameterSerializerDirect();

    // //////////////////////////////////////////////////////////
    // constructors
    // //////////////////////////////////////////////////////////

    public EntityModel() {
        this((ObjectAdapterMemento)null);
    }

    public EntityModel(final PageParameters pageParameters) {
        this(ObjectAdapterMemento.createPersistent(rootOidFrom(pageParameters)));
        hintPageParameterSerializer.pageParametersToHints(pageParameters, getHints());
    }
    public EntityModel(final ObjectAdapter adapter) {
        this(ObjectAdapterMemento.createOrNull(adapter));
        setObject(adapter);
    }

    public EntityModel(final ObjectAdapterMemento adapterMemento) {
        this(adapterMemento, Maps.<PropertyMemento, ScalarModel>newHashMap());
    }

    public EntityModel(final ObjectAdapterMemento adapterMemento, final Map<PropertyMemento, ScalarModel> propertyScalarModels) {
        this.adapterMemento = adapterMemento;
        this.pendingModel = new PendingModel(this);
        this.propertyScalarModels = propertyScalarModels;
    }

    public static String oidStr(final PageParameters pageParameters) {
        return PageParameterNames.OBJECT_OID.getStringFrom(pageParameters);
    }

    private static RootOid rootOidFrom(final PageParameters pageParameters) {
        return getOidMarshaller().unmarshal(oidStr(pageParameters), RootOid.class);
    }


    //////////////////////////////////////////////////
    // BookmarkableModel
    //////////////////////////////////////////////////

    
    @Override
    public PageParameters getPageParameters() {
        PageParameters pageParameters = createPageParameters(getObject());
        hintPageParameterSerializer.hintsToPageParameters(getHints(), pageParameters);
        return pageParameters;
    }

    public PageParameters getPageParametersWithoutUiHints() {
        return createPageParameters(getObject());
    }

    static interface HintPageParameterSerializer {
        public void hintsToPageParameters(Map<String,String> hints, PageParameters pageParameters);
        public void pageParametersToHints(final PageParameters pageParameters, Map<String,String> hints);
    }
    
    static class HintPageParameterSerializerDirect implements HintPageParameterSerializer, Serializable {

        private static final long serialVersionUID = 1L;

        public void hintsToPageParameters(Map<String,String> hints, PageParameters pageParameters) {
            Set<String> hintKeys = hints.keySet();
            for (String key : hintKeys) {
                String value = hints.get(key);
                pageParameters.add("hint-" + key, value);
            }
        }

        @Override
        public void pageParametersToHints(final PageParameters pageParameters, Map<String,String> hints) {
            Set<String> namedKeys = pageParameters.getNamedKeys();
            for (String namedKey : namedKeys) {
                if(namedKey.startsWith("hint-")) {
                    String value = pageParameters.get(namedKey).toString(null);
                    String key = namedKey.substring(5);
                    hints.put(key, value); // may replace
                }
            }
        }
    }
    
    static class HintPageParameterSerializerUsingViewModelSupport implements HintPageParameterSerializer, Serializable {
        private static final long serialVersionUID = 1L;

        public void hintsToPageParameters(Map<String,String> hints, PageParameters pageParameters) {
            if(hints.isEmpty()) {
                return;
            }
            MementoServiceDefault vms = new MementoServiceDefault();
            Memento memento = vms.create();
            Set<String> hintKeys = hints.keySet();
            for (String key : hintKeys) {
                String safeKey = key.replace(':', '_');
                Serializable value = hints.get(key);
                memento.set(safeKey, value);
            }
            String serializedHints = memento.asString();
            PageParameterNames.ANCHOR.addStringTo(pageParameters, serializedHints);
        }

        public void pageParametersToHints(final PageParameters pageParameters, Map<String,String> hints) {
            String hintsStr = PageParameterNames.ANCHOR.getStringFrom(pageParameters);
            if(hintsStr != null) {
                try {
                    Memento memento = new MementoServiceDefault().parse(hintsStr);
                    Set<String> keys = memento.keySet();
                    for (String safeKey : keys) {
                        String value = memento.get(safeKey, String.class);
                        String key = safeKey.replace('_', ':');
                        hints.put(key, value);
                    }
                } catch(RuntimeException ex) {
                    // fail gracefully, ie ignore.
                    System.err.println(ex);
                }
            }
        }
    }


    @Override
    public String getTitle() {
        return getObject().titleString(null);
    }

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

    /**
     * Not Wicket API, but used by <tt>EntityPage</tt> to do eager loading
     * when rendering after post-and-redirect.
     * @return 
     */
    public ObjectAdapter load(ConcurrencyChecking concurrencyChecking) {
        if (adapterMemento == null) {
            return null;
        }
        
        final ObjectAdapter objectAdapter = adapterMemento.getObjectAdapter(concurrencyChecking);
        return objectAdapter;
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
        return load(ConcurrencyChecking.CHECK);
    }


    @Override
    public void setObject(final ObjectAdapter adapter) {
        super.setObject(adapter);
        adapterMemento = ObjectAdapterMemento.createOrNull(adapter);
    }


    // //////////////////////////////////////////////////////////
    // PropertyModels
    // //////////////////////////////////////////////////////////

    /**
     * Lazily populates with the current value of each property.
     */
    public ScalarModel getPropertyModel(final PropertyMemento pm) {
        ScalarModel scalarModel = propertyScalarModels.get(pm);
        if (scalarModel == null) {
            scalarModel = new ScalarModel(getObjectAdapterMemento(), pm);
            if (isViewMode()) {
                scalarModel.toViewMode();
            } else {
                scalarModel.toEditMode();
            }
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
            final ScalarModel scalarModel = propertyScalarModels.get(pm);
            final ObjectAdapter adapter = getObject();
            final ObjectAdapter associatedAdapter =
                    pm.getProperty().get(adapter, InteractionInitiatedBy.USER);
            scalarModel.setObject(associatedAdapter);
        }
    }

    // //////////////////////////////////////////////////////////
    // RenderingHint, Mode, entityDetailsVisible
    // //////////////////////////////////////////////////////////


    public RenderingHint getRenderingHint() {
        return renderingHint;
    }
    public void setRenderingHint(RenderingHint renderingHint) {
        this.renderingHint = renderingHint;
    }

    public ObjectAdapterMemento getContextAdapterIfAny() {
        return contextAdapterIfAny;
    }
    
    /**
     * Used as a hint when the {@link #getRenderingHint()} is {@link RenderingHint#PARENTED_TITLE_COLUMN},
     * provides a context adapter to obtain the title.
     */
    public void setContextAdapterIfAny(ObjectAdapterMemento contextAdapterIfAny) {
        this.contextAdapterIfAny = contextAdapterIfAny;
    }
    
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

    public boolean isEntityDetailsVisible() {
        return entityDetailsVisible;
    }

    public void toggleDetails() {
        entityDetailsVisible = !entityDetailsVisible;
    }

    private Hint hint;

    private Hint getHint() {
        return hint;
    }
    private void setHint(Hint hint) {
        this.hint = hint;
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
            return ObjectAdapterMemento.createOrNull(adapter);
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
            return memento != null ? memento.getObjectAdapter(ConcurrencyChecking.NO_CHECK) : null;
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

    private Object layoutMetadata;

    public Object getLayoutMetadata() {
        return layoutMetadata;
    }

    /**
     * Returns a new copy that SHARES the property scalar models (for edit form).
     */
    public EntityModel cloneWithLayoutMetadata(final Object layoutMetadata) {
        final EntityModel entityModel = new EntityModel(this.adapterMemento, this.propertyScalarModels);
        entityModel.layoutMetadata = layoutMetadata;
        return entityModel;
    }



    @Override
    protected void doSetHint(final String scopeKey, final String attributeName, final String value) {
        ScopedSessionAttribute scopedSessionAttribute = scopedSessionAttributeByName.get(attributeName);
        if(scopedSessionAttribute == null) {
            scopedSessionAttribute = ScopedSessionAttribute.create(this, scopeKey, attributeName);
        }
        scopedSessionAttribute.set(value);
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
    

    // //////////////////////////////////////////////////////////
    // Dependencies (from context)
    // //////////////////////////////////////////////////////////

    protected static OidMarshaller getOidMarshaller() {
		return IsisContext.getOidMarshaller();
	}

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }





}
