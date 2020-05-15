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

import org.apache.wicket.model.Model;

import org.apache.isis.core.webapp.context.memento.ObjectMemento;

import lombok.extern.log4j.Log4j2;

/**
 * For widgets that use a <tt>org.wicketstuff.select2.Select2Choice</tt>;
 * synchronizes the {@link Model} of the <tt>Select2Choice</tt>
 * with the parent {@link ScalarModel}, allowing also for pending values.
 */
public interface ScalarModelWithPending extends Serializable {

    public ObjectMemento getPendingMemento();
    public void setPendingMemento(ObjectMemento pending);

    public ScalarModel getScalarModel();

    public static Model<ObjectMemento> create(ScalarModel scalarModel) {
        return Factory.createModel(Factory.asScalarModelWithPending(scalarModel));
    }
    
    @Log4j2
    static class Factory {
        
        private static ScalarModelWithPending asScalarModelWithPending(final ScalarModel scalarModel) {
            return new ScalarModelWithPending(){

                private static final long serialVersionUID = 1L;

                @Override
                public ObjectMemento getPendingMemento() {
                    return scalarModel.getPendingModel().getObject();
                }

                @Override
                public void setPendingMemento(ObjectMemento pending) {
                    scalarModel.getPendingModel().setObject(pending);
                }

                @Override
                public ScalarModel getScalarModel() {
                    return scalarModel;
                }
            };
        }

        private static Model<ObjectMemento> createModel(final ScalarModelWithPending owner) {
            return new Model<ObjectMemento>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ObjectMemento getObject() {
                    if (owner.getPendingMemento() != null) {
                        log.debug("pending not null: {}", owner.getPendingMemento().toString());
                        return owner.getPendingMemento();
                    }
                    log.debug("pending is null");

                    final ObjectMemento objectAdapterMemento = owner.getScalarModel().memento();
                    owner.setPendingMemento(objectAdapterMemento);

                    return objectAdapterMemento;
                }

                @Override
                public void setObject(final ObjectMemento adapterMemento) {
                    log.debug("setting to: {}", (adapterMemento!=null?adapterMemento.toString():null) );
                    owner.setPendingMemento(adapterMemento);
                    final ScalarModel ownerScalarModel = owner.getScalarModel();
                    if (ownerScalarModel != null) {
                        if(adapterMemento == null) {
                            ownerScalarModel.setObject(null);
                        } else {
                            final ObjectMemento ownerPending = owner.getPendingMemento();
                            if (ownerPending != null) {
                                log.debug("setting to pending: {}", ownerPending.toString());
                                ownerScalarModel.setObject(
                                        ownerScalarModel.getCommonContext().reconstructObject(ownerPending));
                            }
                        }
                    }
                }
            };
        }

    }


}
