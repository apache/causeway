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

import java.util.function.Supplier;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;

/**
 * Represents an instance of some element of the meta-model managed by the framework.
 *
 */
public interface ManagedObject {

    /**
     * Returns the specification that details the structure (meta-model) of this object.
     */
    ObjectSpecification getSpecification();

    /**
     * Returns the adapted domain object, the 'plain old java' object this managed object 
     * represents with the framework.
     */
    Object getPojo();
    
    // -- TITLE
    
    public default String titleString() {
        return titleString(null);
    }
    
    default String titleString(ObjectAdapter contextAdapterIfAny) {
        return TitleUtil.titleString(this, contextAdapterIfAny);
    }
    
    
    public static class TitleUtil {
        
        public static String titleString(ManagedObject managedObject, ObjectAdapter contextAdapterIfAny) {
            if (managedObject.getSpecification().isParentedOrFreeCollection()) {
                final CollectionFacet facet = managedObject.getSpecification().getFacet(CollectionFacet.class);
                return collectionTitleString(managedObject, facet);
            } else {
                return objectTitleString(managedObject, contextAdapterIfAny);
            }
        }

        private static String objectTitleString(ManagedObject managedObject, ObjectAdapter contextAdapterIfAny) {
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
            final ObjectSpecification elementSpecification = managedObject.getElementSpecification();
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
    
    // -- SHORTCUT - ELEMENT SPECIFICATION
    
    /**
     * Used only for (standalone or parented) collections.
     * @deprecated use {@link ObjectSpecification#getElementSpecification()} instead, 
     * (proposed for removal, to keep the API slim)
     */
    @Deprecated
    default public ObjectSpecification getElementSpecification() {
        return getSpecification().getElementSpecification();
    }
    
    // -- SHORTCUT - ICON NAME

    /**
     * Returns the name of an icon to use if this object is to be displayed
     * graphically.
     * <p>
     * May return <code>null</code> if no icon is specified.
     * @deprecated use {@link ObjectSpecification#getIconName(ManagedObject))} instead, 
     * (proposed for removal, to keep the API slim)
     */
    @Deprecated
    default public String getIconName() {
        return getSpecification().getIconName(this);
    }
    
    // -- FACTORIES
    
    public static ManagedObject of(
            final ObjectSpecification specification, 
            final Object pojo) {
        
        return new ManagedObject() {
            @Override
            public ObjectSpecification getSpecification() {
                return specification;
            }
            @Override
            public Object getPojo() {
                return pojo;
            }
        };
    }
    
    public static ManagedObject of(
            final Supplier<ObjectSpecification> specificationSupplier, 
            final Object pojo) {
        
        return new ManagedObject() {
            private final _Lazy<ObjectSpecification> specification = _Lazy.of(specificationSupplier);
            
            @Override
            public ObjectSpecification getSpecification() {
                return specification.get();
            }
            @Override
            public Object getPojo() {
                return pojo;
            }
        };
    }
    

}
