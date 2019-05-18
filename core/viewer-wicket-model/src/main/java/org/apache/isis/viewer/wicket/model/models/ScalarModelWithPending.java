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

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.runtime.memento.ObjectAdapterMemento;

/**
 * For widgets that use a <tt>org.wicketstuff.select2.Select2Choice</tt>;
 * synchronizes the {@link Model} of the <tt>Select2Choice</tt>
 * with the parent {@link ScalarModel}, allowing also for pending values.
 */
public interface ScalarModelWithPending extends Serializable {

    public ObjectAdapterMemento getPending();
    public void setPending(ObjectAdapterMemento pending);

    public ScalarModel getScalarModel();

    static class Util {

        private static final Logger LOG = LoggerFactory.getLogger(ScalarModelWithPending.Util.class);

        public static IModel<ObjectAdapterMemento> createModel(final ScalarModel model) {
            return createModel(model.asScalarModelWithPending());
        }

        public static Model<ObjectAdapterMemento> createModel(final ScalarModelWithPending owner) {
            return new Model<ObjectAdapterMemento>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ObjectAdapterMemento getObject() {
                    if (owner.getPending() != null) {
                        LOG.debug("pending not null: {}", owner.getPending().toString());
                        return owner.getPending();
                    }
                    LOG.debug("pending is null");

                    final ObjectAdapterMemento objectAdapterMemento = owner.getScalarModel().getObjectAdapterMemento();
                    owner.setPending(objectAdapterMemento);

                    return objectAdapterMemento;
                }

                @Override
                public void setObject(final ObjectAdapterMemento adapterMemento) {
                    LOG.debug("setting to: {}", (adapterMemento!=null?adapterMemento.toString():null) );
                    owner.setPending(adapterMemento);
                    final ScalarModel ownerScalarModel = owner.getScalarModel();
                    if (ownerScalarModel != null) {
                        if(adapterMemento == null) {
                            ownerScalarModel.setObject(null);
                        } else {
                            final ObjectAdapterMemento ownerPending = owner.getPending();
                            if (ownerPending != null) {
                                LOG.debug("setting to pending: {}", ownerPending.toString());
                                ownerScalarModel.setObject(
                                        ownerPending.getObjectAdapter());
                            }
                        }
                    }
                }
            };
        }

    }
}
