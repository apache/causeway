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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;

import lombok.NonNull;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public abstract class ManagedObjectModel
extends ModelAbstract<ManagedObject> {

    private static final long serialVersionUID = 1L;

    private ObjectMemento memento;
    
    protected ManagedObjectModel(
            @NonNull IsisWebAppCommonContext commonContext) {
        this(commonContext, null);
    }
    
    protected ManagedObjectModel(
            @NonNull IsisWebAppCommonContext commonContext, 
            @Nullable ObjectMemento initialMemento) {

        super(commonContext);
        this.memento = initialMemento;
    }


    @Override
    protected ManagedObject load() {
        if (memento == null) {
            return null;
        }
        return super.getCommonContext().reconstructObject(memento);
    }

    @Override
    public void setObject(final ManagedObject adapter) {

        if(ManagedObject.isNullOrUnspecifiedOrEmpty(adapter)) {
            super.setObject(null);
            return;
        }

        super.setObject(adapter);

        if(adapter.getSpecification().isParentedOrFreeCollection()) {
            val pojo = adapter.getPojo();
            memento = super.getMementoService()
                    .mementoForPojos(_Casts.uncheckedCast(pojo), getTypeOfSpecificationId()
                            .orElseGet(()->adapter.getElementSpecification().get().getSpecId()));
        } else {

            memento = super.getMementoService().mementoForObject(adapter);
        }
    }
    
    public final Bookmark asHintingBookmarkIfSupported() {
        return memento!=null
                ? memento.asHintingBookmarkIfSupported()
                : null;
    }

    public final Bookmark asBookmarkIfSupported() {
        return memento!=null
                ? memento.asBookmarkIfSupported()
                : null;
    }
    
    public final String oidStringIfSupported() {
        return memento!=null
                ? memento.toString()
                : null;
    }
    
    /**
     * free of side-effects, used for serialization
     * @implNote overriding this must be consistent with {@link #getTypeOfSpecificationId()}
     */
    public Optional<ObjectSpecId> getTypeOfSpecificationId() {
        return Optional.ofNullable(memento)
                .map(ObjectMemento::getObjectSpecId);
    }
    
    private transient ObjectSpecification objectSpec;
    /**
     * @implNote can be overridden by sub-models (eg {@link ScalarModel}) that know the type of
     * the adapter without there being one. Overriding this must be consistent 
     * with {@link #getTypeOfSpecificationId()} 
     */
    public ObjectSpecification getTypeOfSpecification() {
        if(objectSpec==null) {
            val specId = getTypeOfSpecificationId().orElse(null);
            objectSpec = super.getSpecificationLoader().lookupBySpecIdElseLoad(specId); 
        }
        return objectSpec;
    }

    
    public boolean hasAsRootPolicy() {
        return hasBookmarkPolicy(BookmarkPolicy.AS_ROOT);
    }

    public boolean hasAsChildPolicy() {
        return hasBookmarkPolicy(BookmarkPolicy.AS_CHILD);
    }

    private boolean hasBookmarkPolicy(final BookmarkPolicy policy) {
        return lookupFacet(BookmarkPolicyFacet.class)
                .map(facet->facet.value() == policy)
                .orElse(false);
    }

    private <T extends Facet> Optional<T> lookupFacet(Class<T> facetClass) {
        return Optional.ofNullable(getTypeOfSpecification())
                .map(objectSpec->objectSpec.getFacet(facetClass));
    }
    
    // -- CONTRACT
    
    @Override
    public final int hashCode() {
        return Objects.hashCode(memento);
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj instanceof ManagedObjectModel) {
            val other = (ManagedObjectModel) obj;
            return Objects.equals(this.memento, other.memento);
        }
        return false;
    }
    
    // -- DEPRECATIONS
    
    @Deprecated //TODO do not expose this implementation detail
    public ObjectMemento memento() {
        return memento;
    }
    
    @Deprecated //TODO do not expose this implementation detail
    public void memento(ObjectMemento memento) {
        val manageObject = super.getCommonContext().reconstructObject(memento);
        super.setObject(manageObject);
        this.memento = memento;
        this.objectSpec = null; // invalidate
    }


}
