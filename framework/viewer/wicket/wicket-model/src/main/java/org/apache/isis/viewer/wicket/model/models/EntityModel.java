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

import com.google.common.collect.Maps;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResource;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

/**
 * Backing model to represent a {@link ObjectAdapter}.
 * 
 * <p>
 * So that the model is {@link Serializable}, the {@link ObjectAdapter} is
 * stored as a {@link ObjectAdapterMemento}.
 */
public class EntityModel extends ModelAbstract<ObjectAdapter> {

    private static final long serialVersionUID = 1L;
    

    // //////////////////////////////////////////////////////////
    // factory methods for PageParameters
    // //////////////////////////////////////////////////////////

    /**
     * Factory method for creating {@link PageParameters} to represent an
     * entity.
     */
    public static PageParameters createPageParameters(final ObjectAdapter adapter) {

        final PageParameters pageParameters = new PageParameters();

        final Boolean persistent = adapter.representsPersistent();

        if (persistent) {
            final String oidStr = adapter.getOid().enString(getOidMarshaller());

            PageParameterNames.OBJECT_OID.addTo(pageParameters, oidStr);
        } else {
            // don't do anything; instead the page should be redirected back to
            // an EntityPage so that the underlying EntityModel that contains
            // the
            // memento for the transient ObjectAdapter can be accessed.
        }

        return pageParameters;
    }

    public enum RenderingHint {
        REGULAR,
        COMPACT
    }

	public enum Mode {
        VIEW, EDIT;
    }

    private ObjectAdapterMemento adapterMemento;
    private Mode mode = Mode.VIEW;
    private RenderingHint renderingHint = RenderingHint.REGULAR;
    private final Map<PropertyMemento, ScalarModel> propertyScalarModels = Maps.newHashMap();

    /**
     * Toggled by 'entityDetailsButton'.
     */
    private boolean entityDetailsVisible;
    
    /**
     * {@link ConcurrencyException}, if any, that might have occurred previously
     */
    private ConcurrencyException concurrencyException;

    // //////////////////////////////////////////////////////////
    // constructors
    // //////////////////////////////////////////////////////////

    public EntityModel() {
    }

    public EntityModel(final PageParameters pageParameters) {
        this(ObjectAdapterMemento.createPersistent(rootOidFrom(pageParameters)));
    }

    public EntityModel(final ObjectAdapter adapter) {
        this(ObjectAdapterMemento.createOrNull(adapter));
        setObject(adapter);
    }

    public EntityModel(final ObjectAdapterMemento adapterMemento) {
        this.adapterMemento = adapterMemento;
    }

    private static String oidStr(final PageParameters pageParameters) {
        return PageParameterNames.OBJECT_OID.getFrom(pageParameters);
    }

    private static RootOid rootOidFrom(final PageParameters pageParameters) {
        return getOidMarshaller().unmarshal(oidStr(pageParameters), RootOid.class);
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
    // load, setObject
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
        if(concurrencyChecking == ConcurrencyChecking.NO_CHECK) {
            this.resetPropertyModels();
        }
        return objectAdapter;
    }

    @Override
    public ObjectAdapter load() {
        return load(ConcurrencyChecking.CHECK);
    }

    @Override
    public void setObject(final ObjectAdapter adapter) {
        super.setObject(adapter);
        adapterMemento = ObjectAdapterMemento.createOrNull(adapter);
    }

    @Override
    public void detach() {
        if (isAttached()) {
            if (adapterMemento != null) {
                adapterMemento.captureTitleHintIfPossible();
            }
        }
        super.detach();
    }

    // hmmm... doesn't seem to be used; get rid of if come back.
    // I'm guessing the property models stuff below superceded this.

    // ////////////////////////////////////////////////////////////
    // // child (property) model objects;
    // ////////////////////////////////////////////////////////////
    //
    // public void setChildModelObject(String propertyIdentifier,
    // ObjectAdapter associatedAdapter) {
    // ObjectAdapter adapter = getObject();
    // if (adapter == null) {
    // // let's fail fast, because this presumably ought not to happen
    // throw new IllegalStateException(
    // "no adapter set for the EntityModel");
    // }
    // ObjectSpecification noSpec = adapter.getSpecification();
    // ObjectAssociation association =
    // noSpec.getAssociation(propertyIdentifier);
    // if (association == null) {
    // throw new IllegalArgumentException(String.format(
    // "Id '%s' does not represent an association in spec '%s'",
    // propertyIdentifier, noSpec.getFullName()));
    // }
    // if (association.isOneToManyAssociation()) {
    // throw new IllegalArgumentException(String.format(
    // "Association '%s' is not a property in spec '%s'",
    // propertyIdentifier, noSpec.getFullName()));
    // }
    // OneToOneAssociation property = (OneToOneAssociation) association;
    //
    // // TODO: need to add in validation here.
    // // Also, not sure if should be copying into a pending value rather than
    // // apply directly.
    //
    // if (associatedAdapter != null) {
    // property.set(adapter, associatedAdapter);
    // } else {
    // property.clearAssociation(adapter);
    // }
    // }

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
            final ObjectAdapter associatedAdapter = pm.getProperty().get(getObject());
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
    // validation
    // //////////////////////////////////////////////////////////

    public String getReasonInvalidIfAny() {
        final ObjectAdapter adapter = getObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.CHECK);
        final Consent validity = adapter.getSpecification().isValid(adapter);
        return validity.isAllowed() ? null : validity.getReason();
    }

    public void apply() {
        final ObjectAdapter adapter = getObjectAdapterMemento().getObjectAdapter(ConcurrencyChecking.CHECK);
        for (final ScalarModel scalarModel : propertyScalarModels.values()) {
            final OneToOneAssociation property = scalarModel.getPropertyMemento().getProperty();
            final ObjectAdapter associate = scalarModel.getObject();
            property.set(adapter, associate);
        }
        getObjectAdapterMemento().setAdapter(adapter);
        toViewMode();
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
