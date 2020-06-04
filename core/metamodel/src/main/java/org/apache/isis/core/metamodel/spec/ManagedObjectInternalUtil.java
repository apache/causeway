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

package org.apache.isis.core.metamodel.spec;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0
 */
@UtilityClass
final class ManagedObjectInternalUtil {


    static final ManagedObject UNSPECIFIED = new ManagedObject() {

        @Override
        public ObjectSpecification getSpecification() {
            throw _Exceptions.unsupportedOperation();
        }

        @Override
        public Object getPojo() {
            return null;
        }

        @Override
        public Optional<RootOid> getRootOid() {
            return Optional.empty();
        }
        
    };

    static Optional<ObjectManager> objectManager(@Nullable ManagedObject adapter) {
        return ManagedObjects.spec(adapter)
        .map(ObjectSpecification::getMetaModelContext)
        .map(MetaModelContext::getObjectManager);
    }
    
    static Optional<RootOid> identify(@Nullable ManagedObject adapter) {
        return objectManager(adapter)
                .map(objectManager->objectManager.identifyObject(adapter)); 
    }
    
    // -- TITLE SUPPORT
    
    static String titleString(@Nullable ManagedObject managedObject, @Nullable ManagedObject contextAdapterIfAny) {
        
        if(!ManagedObjects.isSpecified(managedObject)) {
            return "unspecified object";
        }
        
        if (managedObject.getSpecification().isParentedOrFreeCollection()) {
            final CollectionFacet facet = managedObject.getSpecification().getFacet(CollectionFacet.class);
            return collectionTitleString(managedObject, facet);
        } else {
            return objectTitleString(managedObject, contextAdapterIfAny);
        }
    }

    private static String objectTitleString(ManagedObject managedObject, ManagedObject contextAdapterIfAny) {
        if (managedObject.getPojo() instanceof String) {
            return (String) managedObject.getPojo();
        }
        final ObjectSpecification specification = managedObject.getSpecification();
        String title = specification.getTitle(contextAdapterIfAny, managedObject);

        if (title == null) {
            title = getDefaultTitle(managedObject);
        }
        return title;
    }

    private static String collectionTitleString(ManagedObject managedObject, final CollectionFacet facet) {
        final int size = facet.size(managedObject);
        final ObjectSpecification elementSpecification = managedObject.getElementSpecification().orElse(null);
        if (elementSpecification == null || elementSpecification.getFullIdentifier().equals(Object.class.getName())) {
            switch (size) {
            case -1:
                return "Objects";
            case 0:
                return "No objects";
            case 1:
                return "1 object";
            default:
                return size + " objects";
            }
        } else {
            switch (size) {
            case -1:
                return elementSpecification.getPluralName();
            case 0:
                return "No " + elementSpecification.getPluralName();
            case 1:
                return "1 " + elementSpecification.getSingularName();
            default:
                return size + " " + elementSpecification.getPluralName();
            }
        }
    }

    private static String getDefaultTitle(ManagedObject managedObject) {
        return "A" + (" " + managedObject.getSpecification().getSingularName()).toLowerCase();
    }

}
