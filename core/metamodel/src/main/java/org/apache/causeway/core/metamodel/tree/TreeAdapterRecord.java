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
package org.apache.causeway.core.metamodel.tree;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

import lombok.NonNull;

/**
 * Wraps a non-serializable {@link TreeAdapter} using a serializable record.
 */
@Programmatic
public record TreeAdapterRecord<T>(
    TreeAdapter<T> treeAdapter) implements Serializable {

    // -- SERIALIZATION PROXY

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final @NonNull Class<? extends TreeAdapter<?>> treeAdapterClass;

        private SerializationProxy(final TreeAdapterRecord<?> treeAdapterRecord) {
            this.treeAdapterClass = _Casts.uncheckedCast(treeAdapterRecord.treeAdapter().getClass());
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private Object readResolve() {
            var mmc = MetaModelContext.instanceElseFail();
            try {
                return new TreeAdapterRecord(mmc.getFactoryService().getOrCreate(treeAdapterClass));
            } catch (Exception e) {
                throw _Exceptions.unrecoverable(e, "failed to instantiate tree adapter of type %s via FactoryService",
                    treeAdapterClass.getName());
            }
        }
    }

}
