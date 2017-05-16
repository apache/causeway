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

import org.apache.wicket.Component;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

/**
 * This is a wrapper around {@link EntityModel} that adds in layout hints only but otherwise has the same identity
 * as the underlying EntityModel.
 *
 * <p>
 *     This is to avoid concurrency check exceptions ... we only need to update the single EntityModel, and it is
 *     shared everywhere.
 * </p>
 */
public class EntityModelWithLayoutHints extends EntityModel {

    private static final long serialVersionUID = 1L;

    private final EntityModel underlying;

    private final Object layoutMetadata;

    public EntityModelWithLayoutHints(final EntityModel underlying, final Object layoutMetadata) {
        this.underlying = underlying;
        this.layoutMetadata = layoutMetadata;
    }

    @Override
    public Object getLayoutMetadata() {
        return layoutMetadata;
    }

    @Override public void resetVersion() {
        underlying.resetVersion();
    }

    @Override public PageParameters getPageParameters() {
        return underlying.getPageParameters();
    }

    @Override public PageParameters getPageParametersWithoutUiHints() {
        return underlying.getPageParametersWithoutUiHints();
    }

    @Override public String getHint(final Component component, final String keyName) {
        return underlying.getHint(component, keyName);
    }

    @Override public void setHint(final Component component, final String keyName, final String hintValue) {
        underlying.setHint(component, keyName, hintValue);
    }

    @Override public void clearHint(final Component component, final String attributeName) {
        underlying.clearHint(component, attributeName);
    }

    @Override public String getTitle() {
        return underlying.getTitle();
    }

    @Override public boolean hasAsRootPolicy() {
        return underlying.hasAsRootPolicy();
    }

    @Override public boolean hasAsChildPolicy() {
        return underlying.hasAsChildPolicy();
    }

    @Override public ObjectAdapterMemento getObjectAdapterMemento() {
        return underlying.getObjectAdapterMemento();
    }

    @Override public ObjectSpecification getTypeOfSpecification() {
        return underlying.getTypeOfSpecification();
    }

    @Override public ObjectAdapter load(final AdapterManager.ConcurrencyChecking concurrencyChecking) {
        return underlying.load(concurrencyChecking);
    }

    @Override public ObjectAdapter load() {
        return underlying.load();
    }

    @Override public void setObject(final ObjectAdapter adapter) {
        underlying.setObject(adapter);
    }

    @Override public void setObjectMemento(
            final ObjectAdapterMemento memento,
            final PersistenceSession persistenceSession,
            final SpecificationLoader specificationLoader) {
        underlying.setObjectMemento(memento, persistenceSession, specificationLoader);
    }

    @Override public ScalarModel getPropertyModel(final PropertyMemento pm) {
        return underlying.getPropertyModel(pm);
    }

    @Override public void resetPropertyModels() {
        underlying.resetPropertyModels();
    }

    @Override public RenderingHint getRenderingHint() {
        return underlying.getRenderingHint();
    }

    @Override public void setRenderingHint(final RenderingHint renderingHint) {
        underlying.setRenderingHint(renderingHint);
    }

    @Override public ObjectAdapterMemento getContextAdapterIfAny() {
        return underlying.getContextAdapterIfAny();
    }

    @Override public void setContextAdapterIfAny(final ObjectAdapterMemento contextAdapterIfAny) {
        underlying.setContextAdapterIfAny(contextAdapterIfAny);
    }

    @Override public Mode getMode() {
        return underlying.getMode();
    }

    @Override protected void setMode(final Mode mode) {
        underlying.setMode(mode);
    }

    @Override public boolean isViewMode() {
        return underlying.isViewMode();
    }

    @Override public boolean isEditMode() {
        return underlying.isEditMode();
    }

    @Override public EntityModel toEditMode() {
        return underlying.toEditMode();
    }

    @Override public EntityModel toViewMode() {
        return underlying.toViewMode();
    }

    @Override protected void onDetach() {
        underlying.onDetach();
    }

    @Override public void setException(final ConcurrencyException ex) {
        underlying.setException(ex);
    }

    @Override public String getAndClearConcurrencyExceptionIfAny() {
        return underlying.getAndClearConcurrencyExceptionIfAny();
    }

    @Override public ObjectAdapter getPendingElseCurrentAdapter() {
        return underlying.getPendingElseCurrentAdapter();
    }

    @Override public ObjectAdapter getPendingAdapter() {
        return underlying.getPendingAdapter();
    }

    @Override public ObjectAdapterMemento getPending() {
        return underlying.getPending();
    }

    @Override public void setPending(final ObjectAdapterMemento selectedAdapterMemento) {
        underlying.setPending(selectedAdapterMemento);
    }

    @Override public void clearPending() {
        underlying.clearPending();
    }

    @Override public EntityModel cloneWithLayoutMetadata(final Object layoutMetadata) {
        return underlying.cloneWithLayoutMetadata(layoutMetadata);
    }

    @Override public int hashCode() {
        return underlying.hashCode();
    }

    @Override public boolean equals(final Object obj) {
        return underlying.equals(obj);
    }
}
