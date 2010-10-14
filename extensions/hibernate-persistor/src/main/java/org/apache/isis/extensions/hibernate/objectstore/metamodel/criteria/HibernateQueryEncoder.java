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


package org.apache.isis.extensions.hibernate.objectstore.metamodel.criteria;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.query.Parameter;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.query.QueryPlaceholder;
import org.apache.isis.remoting.data.Data;
import org.apache.isis.remoting.data.query.PersistenceQueryData;
import org.apache.isis.remoting.exchange.KnownObjectsRequest;
import org.apache.isis.remoting.protocol.encoding.internal.PersistenceQueryEncoderAbstract;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;


public class HibernateQueryEncoder extends PersistenceQueryEncoderAbstract {

    public Class<HibernateQueryCriteria> getPersistenceQueryClass() {
        return HibernateQueryCriteria.class;
    }

    public PersistenceQueryData encode(final PersistenceQuery criteria) {
        final HibernateQueryCriteria hibernateCriteria = downcast(criteria);
        final List<Parameter> parms = hibernateCriteria.getQuery().getParameters();
        final ObjectSpecification[] specs = new ObjectSpecification[parms.size()];
        final ObjectAdapter[] objects = new ObjectAdapter[parms.size()];
        for (int i = 0; i < parms.size(); i++) {
            final Parameter parm = parms.get(i);
            final ObjectAdapter adapter = getAdapterManager().getAdapterFor(parm.getValue());
            specs[i] = adapter.getSpecification();
            objects[i] = adapter;
        }
        final Data[] objectData = getObjectEncoder().encodeActionParameters(specs, objects, new KnownObjectsRequest());
        final byte[] serialisedQuery = serialise(hibernateCriteria.getQuery());
        return new HibernateQueryData(downcast(criteria), serialisedQuery, objectData);
    }

    // REVIEW serialise query inline here as part of strategy rather than in Marshaller - correct ?
    private byte[] serialise(final Object toSerialise) {
        try {
            final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(toSerialise);
            objectStream.flush();
            return byteStream.toByteArray();
        } catch (final Exception e) {
            throw new IsisException(e);
        }
    }

    private Object deSerialise(final byte[] bytes) {
        try {
            final ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            final ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            return objectStream.readObject();
        } catch (final Exception e) {
            throw new IsisException(e);
        }
    }

    @Override
    protected PersistenceQuery doDecode(
            final ObjectSpecification specification,
            final PersistenceQueryData persistenceQueryData) {

        final HibernateQueryData queryData = downcast(persistenceQueryData);
        final QueryPlaceholder query = (QueryPlaceholder) deSerialise(queryData.getQueryAsBytes());

        for (int i = 0; i < queryData.getData().length; i++) {
            final Parameter parm = query.getParameters().get(i);
            final Data data = queryData.getData()[i];
            final ObjectAdapter adapter = getObjectEncoder().decode(data);
            parm.setValue(adapter.getObject());
        }

        return new HibernateQueryCriteria(persistenceQueryData.getPersistenceQueryClass(), query, (downcast(persistenceQueryData))
                .getResultType());
    }

	private HibernateQueryCriteria downcast(final PersistenceQuery criteria) {
		return (HibernateQueryCriteria) criteria;
	}

	private HibernateQueryData downcast(
			final PersistenceQueryData persistenceQueryData) {
		return (HibernateQueryData) persistenceQueryData;
	}
    
    
    //////////////////////////////////////////////////////////
    // Dependencies (from context)
    //////////////////////////////////////////////////////////
    
    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private static AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }
    

}
