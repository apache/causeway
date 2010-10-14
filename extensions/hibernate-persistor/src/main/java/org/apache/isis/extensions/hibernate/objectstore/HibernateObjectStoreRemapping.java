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


package org.apache.isis.extensions.hibernate.objectstore;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;
import org.apache.isis.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.runtime.persistence.objectstore.IsisStoreDelegating;
import org.apache.isis.runtime.persistence.objectstore.ObjectStore;
import org.apache.isis.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;
import org.apache.isis.runtime.transaction.ObjectPersistenceException;


/**
 * Wraps a {@link HibernateObjectStore} to ensure objects are mapped, even if they aren't specified within the
 * properties file or picked up from related objects.
 * 
 * <p>
 * This class is useful when initially setting up a project, as it will tell you which entity classes 
 * aren't listed in the properties and would cause a failure using the standard {@link HibernateObjectStore}.
 */
public class HibernateObjectStoreRemapping extends IsisStoreDelegating {

    public HibernateObjectStoreRemapping(final ObjectStore decorated) {
        super(decorated, "RemappingHibernateObjectStore");
    }
    
    //////////////////////////////////////////////////
    // createXxxCommand
    //////////////////////////////////////////////////

    @Override
    public CreateObjectCommand createCreateObjectCommand(final ObjectAdapter object) {
        ensureMapped(object);
        return super.createCreateObjectCommand(object);
    }

    @Override
    public SaveObjectCommand createSaveObjectCommand(final ObjectAdapter object) {
        ensureMapped(object);
        return super.createSaveObjectCommand(object);
    }

    @Override
    public DestroyObjectCommand createDestroyObjectCommand(final ObjectAdapter object) {
        ensureMapped(object);
        return super.createDestroyObjectCommand(object);
    }

    //////////////////////////////////////////////////
    // getObject, resolveField, resolveImmediately
    //////////////////////////////////////////////////

    @Override
    public ObjectAdapter getObject(final Oid oid, final ObjectSpecification hint) throws ObjectNotFoundException,
            ObjectPersistenceException {
        ensureMapped(hint);
        return super.getObject(oid, hint);
    }

    @Override
    public void resolveField(final ObjectAdapter object, final ObjectAssociation field) {
        ensureMapped(object);
        if (field.isOneToOneAssociation() || field.isOneToManyAssociation()) {
            ensureMapped(field.getSpecification());
        }
        super.resolveField(object, field);
    }

    public void resolveImmediately(final ObjectAdapter object) {
        ensureMapped(object);
        super.resolveImmediately(object);
    }

    //////////////////////////////////////////////////
    // getInstances, hasInstances
    //////////////////////////////////////////////////

    public ObjectAdapter[] getInstances(final PersistenceQuery criteria) {
        ensureMapped(criteria.getSpecification());
        return super.getInstances(criteria);
    }

    @Override
    public boolean hasInstances(final ObjectSpecification specification) {
        ensureMapped(specification);
        return super.hasInstances(specification);
    }


    //////////////////////////////////////////////////
    // Helpers: ensureMapped
    //////////////////////////////////////////////////

    private void ensureMapped(final ObjectAdapter object) {
        ensureMapped(object.getSpecification());
    }

    private void ensureMapped(final ObjectSpecification specification) {
        HibernateUtil.ensureMapped(specification);
    }


}
