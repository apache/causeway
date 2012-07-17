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
package org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.util;

import java.text.MessageFormat;
import java.util.List;

import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Version;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterFactory;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public final class JdoPropertyUtils {

    private JdoPropertyUtils() {
    }

    /**
     * Searches for the property annotated with {@link PrimaryKey}.
     * <p>
     * Returns the {@link OneToOneAssociation} if there is precisely one; else
     * <tt>null</tt>.
     * 
     * @see JdoPrimaryKeyPropertyFilter
     */
    public static OneToOneAssociation getPrimaryKeyPropertyFor(final ObjectSpecification objectSpec) {
        return getPropertyFor(objectSpec, "@PrimaryKey", new JdoPrimaryKeyPropertyFilter());
    }

    public static boolean hasPrimaryKeyProperty(final ObjectAdapter adapter) {
        return hasPrimaryKeyProperty(adapter.getSpecification());
    }

    public static boolean hasPrimaryKeyProperty(final ObjectSpecification objectSpec) {
        return getPrimaryKeyPropertyFor(objectSpec) != null;
    }
    
    /**
     * Searches for the property annotated with {@link Version}.
     * <p>
     * Returns the {@link OneToOneAssociation} if there is precisely one; else
     * <tt>null</tt>.
     * 
     * @see JdoVersionPropertyFilter
     */
    public static OneToOneAssociation getVersionPropertyFor(final ObjectSpecification objectSpec) {
        return getPropertyFor(objectSpec, "@Version", new JdoVersionPropertyFilter());
    }

    private static OneToOneAssociation getPropertyFor(final ObjectSpecification objSpec, final String annotationName, final Filter<ObjectAssociation> filter) {
        if (objSpec == null || !objSpec.containsFacet(JdoPersistenceCapableFacet.class)) {
            return null;
        }
        final List<? extends ObjectAssociation> propertyList = objSpec.getAssociations(filter);
        if (propertyList.size() == 0) {
            return JdoPropertyUtils.getPropertyFor(objSpec.superclass(), annotationName, filter);
        }
        if (propertyList.size() > 1) {
            throw new IllegalStateException(MessageFormat.format("Shouldn''t have more than one property annotated with {0} (''{1}'')", annotationName, objSpec.getFullIdentifier()));
        }
        return (OneToOneAssociation) propertyList.get(0);
    }

    public static void setPropertyIdFromOid(final ObjectAdapter adapter, final ObjectAdapterFactory adapterFactory) {

        final RootOid oid = (RootOid) adapter.getOid();
        final ObjectSpecification objectSpec = adapter.getSpecification();
        final Object idValue = idValueOf(oid, objectSpec);
        final ObjectAdapter jpaIdAdapter = adapterFactory.createAdapter(idValue, null);

        setId(adapter, jpaIdAdapter);
    }
    
    public static Object idValueOf(final RootOid oid, ObjectSpecification objectSpec) {
        final OneToOneAssociation idProperty = getPrimaryKeyPropertyFor(objectSpec);
        final EncodableFacet idPropEncodableFacet = idProperty.getFacet(EncodableFacet.class);
        final ObjectAdapter idPropValueAdapter = idPropEncodableFacet.fromEncodedString(oid.getIdentifier());
        return idPropValueAdapter.getObject();
    }


    private static void setId(final ObjectAdapter adapter, final ObjectAdapter idValueAdapter) {
        final ObjectSpecification objectSpec = adapter.getSpecification();
        final OneToOneAssociation idProperty = getPrimaryKeyPropertyFor(objectSpec);
        if (idProperty == null) {
            throw new IsisException("Specification {0} does not have a single property with IdFacet", objectSpec.getFullIdentifier());
        }
        idProperty.set(adapter, idValueAdapter);
    }

    public static Object getIdFor(final ObjectAdapter adapter) {
        final OneToOneAssociation idPropertyFor = getPrimaryKeyPropertyFor(adapter.getSpecification());
        if (idPropertyFor == null) {
            return null;
        }
        final PropertyOrCollectionAccessorFacet facet = idPropertyFor.getFacet(PropertyOrCollectionAccessorFacet.class);
        return facet.getProperty(adapter);
    }


}
