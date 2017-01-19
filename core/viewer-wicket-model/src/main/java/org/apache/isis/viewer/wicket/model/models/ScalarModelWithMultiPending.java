/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

/**
 * For widgets that use a <tt>org.wicketstuff.select2.Select2MultiChoice</tt>;
 * synchronizes the {@link Model} of the <tt>Select2MultiChoice</tt>
 * with the parent {@link ScalarModel}, allowing also for pending values.
 */
public interface ScalarModelWithMultiPending extends Serializable {

    public ArrayList<ObjectAdapterMemento> getPending();
    public void setPending(ArrayList<ObjectAdapterMemento> pending);

    public ScalarModel getScalarModel();

    static class Util {

        private static final Logger LOG = LoggerFactory.getLogger(ScalarModelWithMultiPending.Util.class);
        
        public static Model<ArrayList<ObjectAdapterMemento>> createModel(final ScalarModelWithMultiPending owner) {
            return new Model<ArrayList<ObjectAdapterMemento>>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ArrayList<ObjectAdapterMemento> getObject() {
                    final ArrayList<ObjectAdapterMemento> pending = owner.getPending();
                    if (pending != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("pending not null: " + pending.toString());
                        }
                        return pending;
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("pending is null");
                    }

                    final ScalarModel scalarModel = owner.getScalarModel();
                    final ObjectAdapterMemento objectAdapterMemento = scalarModel.getObjectAdapterMemento();
                    return objectAdapterMemento != null? objectAdapterMemento.getList(): null;
                }

                @Override
                public void setObject(final ArrayList<ObjectAdapterMemento> adapterMemento) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("setting to: %s", adapterMemento != null ? adapterMemento.toString() : null));
                    }
                    owner.setPending(adapterMemento);

                    final ScalarModel ownerScalarModel = owner.getScalarModel();
                    final PersistenceSession persistenceSession = ownerScalarModel.getPersistenceSession();
                    final SpecificationLoader specificationLoader = ownerScalarModel.getSpecificationLoader();

                    if(adapterMemento == null) {
                        ownerScalarModel.setObject(null);
                    } else {
                        final ArrayList<ObjectAdapterMemento> ownerPending = owner.getPending();
                        if (ownerPending != null) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(String.format("setting to pending: %s", ownerPending.toString()));
                            }
                            ownerScalarModel.setObjectMemento(
                                    ObjectAdapterMemento.createForList(adapterMemento), persistenceSession, specificationLoader);
                        }
                    }
                }
            };
        }

        public static ObjectAdapter toAdapter(
                final ArrayList<ObjectAdapterMemento> ownerPending,
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader) {
            final ArrayList<Object> listOfPojos = Lists
                    .newArrayList(FluentIterable.from(ownerPending).transform(
                            ObjectAdapterMemento.Functions
                                    .toPojo(persistenceSession, specificationLoader))
                            .toList());
            return persistenceSession.adapterFor(listOfPojos);
        }

        public static ArrayList<ObjectAdapterMemento> asMementoList(
                final ObjectAdapterMemento objectAdapterMemento,
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader) {

            if(objectAdapterMemento == null) {
                return Lists.newArrayList();
            }

            final ObjectAdapter objectAdapter = objectAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK,
                    persistenceSession, specificationLoader);
            if(objectAdapter == null) {
                return Lists.newArrayList();
            }

            final List<ObjectAdapter> objectAdapters = CollectionFacet.Utils.convertToAdapterList(objectAdapter);

            return Lists.newArrayList(
                FluentIterable.from(objectAdapters)
                              .transform(ObjectAdapterMemento.Functions.fromAdapter())
                              .toList());
        }

        public static ObjectAdapterMemento toAdapterMemento(
                final Collection<ObjectAdapterMemento> modelObject,
                final PersistenceSession persistenceSession,
                final SpecificationLoader specificationLoader) {

            return ObjectAdapterMemento.createForList(modelObject);
        }
    }
}