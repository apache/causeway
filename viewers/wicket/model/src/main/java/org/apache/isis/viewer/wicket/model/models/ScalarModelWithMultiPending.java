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
import java.util.ArrayList;

import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * For widgets that use a <tt>org.wicketstuff.select2.Select2MultiChoice</tt>;
 * synchronizes the {@link Model} of the <tt>Select2MultiChoice</tt>
 * with the parent {@link ScalarModel}, allowing also for pending values.
 */
public interface ScalarModelWithMultiPending extends Serializable {

    public ArrayList<ObjectMemento> getMultiPending();
    public void setMultiPending(ArrayList<ObjectMemento> pending);

    public ScalarModel getScalarModel();

    public static Model<ArrayList<ObjectMemento>> create(final ScalarModel scalarModel) {
        return Factory.createModel(Factory.asScalarModelWithMultiPending(scalarModel));
    }


    @Log4j2
    static class Factory {

        private static ScalarModelWithMultiPending asScalarModelWithMultiPending(
                final ScalarModel scalarModel) {
            return new ScalarModelWithMultiPending(){

                private static final long serialVersionUID = 1L;

                @Override
                public ArrayList<ObjectMemento> getMultiPending() {
                    ObjectMemento pendingMemento = scalarModel.getPendingModel().getObject();
                    return ObjectMemento.unwrapList(pendingMemento)
                            .orElse(null);
                }

                @Override
                public void setMultiPending(final ArrayList<ObjectMemento> pending) {
                    val logicalType = getScalarModel().getScalarTypeSpec().getLogicalType();
                    ObjectMemento adapterMemento = ObjectMemento.wrapMementoList(pending, logicalType);
                    scalarModel.getPendingModel().setObject(adapterMemento);
                }

                @Override
                public ScalarModel getScalarModel() {
                    return scalarModel;
                }
            };
        }

        private static Model<ArrayList<ObjectMemento>> createModel(
                final ScalarModelWithMultiPending owner) {
            return new Model<ArrayList<ObjectMemento>>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ArrayList<ObjectMemento> getObject() {
                    final ArrayList<ObjectMemento> pending = owner.getMultiPending();
                    if (pending != null) {
                        log.debug("pending not null: {}", pending.toString());
                        return pending;
                    }
                    log.debug("pending is null");

                    val ownerScalarModel = owner.getScalarModel();
                    val commonContext = ownerScalarModel.getCommonContext();
                    val objectAdapterMemento =
                            commonContext.mementoFor(ownerScalarModel.getObject());
                    return ObjectMemento.unwrapList(objectAdapterMemento)
                            .orElse(null);
                }

                @Override
                public void setObject(final ArrayList<ObjectMemento> adapterMemento) {
                    log.debug("setting to: {}", (adapterMemento != null ? adapterMemento.toString() : null));
                    owner.setMultiPending(adapterMemento);

                    val ownerScalarModel = owner.getScalarModel();
                    val commonContext = ownerScalarModel.getCommonContext();

                    if(adapterMemento == null) {
                        ownerScalarModel.setObject(null);
                    } else {
                        final ArrayList<ObjectMemento> ownerPending = owner.getMultiPending();
                        if (ownerPending != null) {
                            log.debug("setting to pending: {}", ownerPending.toString());
                            val logicalType = ownerScalarModel.getScalarTypeSpec().getLogicalType();
                            val multiPending = ObjectMemento.wrapMementoList(adapterMemento, logicalType);
                            ownerScalarModel.setObject(
                                    commonContext.reconstructObject(multiPending));
                        }
                    }
                }
            };
        }

    }
}