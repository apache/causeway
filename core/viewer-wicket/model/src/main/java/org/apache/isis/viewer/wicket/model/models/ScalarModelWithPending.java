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

import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;

/**
 * For widgets that use a <tt>com.vaynberg.wicket.select2.Select2Choice</tt>; 
 * synchronizes the {@link Model} of the <tt>Select2Choice</tt>  
 * with the parent {@link ScalarModel}, allowing also for pending values.
 */
public interface ScalarModelWithPending extends Serializable {
    
    public ObjectAdapterMemento getPending();
    public void setPending(ObjectAdapterMemento pending);
    
    public ScalarModel getScalarModel();
    
    static class Util {
        
        private static final Logger LOG = LoggerFactory.getLogger(ScalarModelWithPending.Util.class);
        
        public static Model<ObjectAdapterMemento> createModel(final ScalarModelWithPending owner) {
            return new Model<ObjectAdapterMemento>() {

                private static final long serialVersionUID = 1L;

                @Override
                public ObjectAdapterMemento getObject() {
                    if (owner.getPending() != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("pending not null: " + owner.getPending().toString());
                        }
                        return owner.getPending();
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("pending is null");
                    }
                    
                    final ObjectAdapterMemento objectAdapterMemento = owner.getScalarModel().getObjectAdapterMemento();
                    owner.setPending(objectAdapterMemento);

                    return objectAdapterMemento;
                }

                @Override
                public void setObject(final ObjectAdapterMemento adapterMemento) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("setting to: " + adapterMemento!=null?adapterMemento.toString():null);
                    }
                    owner.setPending(adapterMemento);
                    if (owner.getScalarModel() != null) {
                        if(adapterMemento == null) {
                            owner.getScalarModel().setObject((ObjectAdapter)null);
                        } else {
                            if (owner.getPending() != null) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("setting to pending: " + owner.getPending().toString());
                                }
                                owner.getScalarModel().setObject(owner.getPending().getObjectAdapter(ConcurrencyChecking.NO_CHECK));
                            }
                        }
                    }
                }
            };
        }
    }
}