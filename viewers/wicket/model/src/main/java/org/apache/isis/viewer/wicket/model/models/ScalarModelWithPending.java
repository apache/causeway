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

import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.interactions.managed.ManagedValue;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;

import lombok.NonNull;

/**
 * For widgets that use a <tt>org.wicketstuff.select2.Select2Choice</tt>;
 * synchronizes the {@link Model} of the <tt>Select2Choice</tt>
 * with the parent {@link ScalarModel}.
 */
public interface ScalarModelWithPending
extends
    IModel<ObjectMemento>,
    HasCommonContext {

    ScalarModel scalarModel();

    default ManagedValue pendingValue() { return scalarModel().proposedValue(); }

    default ObjectMemento getPendingMemento() { return getObject(); }
    default void setPendingMemento(final ObjectMemento pending) { setObject(pending); }

    @Override
    default IsisAppCommonContext getCommonContext() {
        return scalarModel().getCommonContext();
    }

    // -- FACTORY

    public static IModel<ObjectMemento> create(final @NonNull ScalarModel scalarModel) {
        return new ScalarModelWithPendingImpl(scalarModel);
    }

    // -- IMPLEMENTATION

    //@Log4j2
    static class ScalarModelWithPendingImpl
    extends ChainingModel<ObjectMemento>
    implements ScalarModelWithPending {

        private static final long serialVersionUID = 1L;

        ScalarModelWithPendingImpl(final ScalarModel scalarModel) {
            super(scalarModel); // chaining to scalarModel
        }

        /**
         * chaining idiom: the {@link ScalarModel} we are chained to
         */
        @Override
        public ScalarModel scalarModel() {
            return (ScalarModel) super.getTarget();
        }

        @Override
        public ObjectMemento getObject() {
            return getCommonContext().mementoFor(
                    pendingValue().getValue().getValue());
        }

        @Override
        public void setObject(final ObjectMemento memento) {
            pendingValue().getValue().setValue(
                    getCommonContext().reconstructObject(memento));
        }

    }

}
