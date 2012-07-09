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

package org.apache.isis.viewer.wicket.model.mementos;

import java.io.Serializable;

import org.apache.isis.core.commons.ensure.Ensure;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.memento.Memento;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.wicket.model.util.Oids;

public class ObjectAdapterMemento implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Factory method
     */
    public static ObjectAdapterMemento createOrNull(final ObjectAdapter adapter) {
        if (adapter == null) {
            return null;
        }
        return new ObjectAdapterMemento(adapter);
    }

    /**
     * Factory method
     */
    public static ObjectAdapterMemento createPersistent(final Oid oid, final SpecMemento specMemento) {
        return new ObjectAdapterMemento(oid, specMemento);
    }

    private final SpecMemento specMemento;
    private String titleHint;

    enum Type {
        /**
         * The {@link ObjectAdapter} that this is the memento for directly has
         * an {@link EncodableFacet} (it is almost certainly a value), and so is
         * stored directly.
         */
        ENCODEABLE {
            @Override
            ObjectAdapter recreateAdapter(final ObjectAdapterMemento nom) {
                final EncodableFacet encodableFacet = nom.specMemento.getSpecification().getFacet(EncodableFacet.class);
                return encodableFacet.fromEncodedString(nom.encodableValue);
            }

            @Override
            public String toString(final ObjectAdapterMemento nom) {
                return nom.encodableValue;
            }
        },
        /**
         * The {@link ObjectAdapter} that this is for is already known by its
         * (persistent) {@link Oid}.
         */
        PERSISTENT {
            @Override
            ObjectAdapter recreateAdapter(final ObjectAdapterMemento nom) {
                return getPersistenceSession().recreateAdapter(nom.getSpecMemento().getSpecification(), nom.persistentOid);
            }

            private PersistenceSession getPersistenceSession() {
                return IsisContext.getPersistenceSession();
            }

            @Override
            public String toString(final ObjectAdapterMemento nom) {
                return nom.persistentOid.toString();
            }
        },
        /**
         * Uses Isis' own {@link Memento}, to capture the state of a transient
         * object.
         */
        TRANSIENT {
            @Override
            ObjectAdapter recreateAdapter(final ObjectAdapterMemento nom) {
                final ObjectAdapter adapter = nom.transientMemento.recreateObject();
                return adapter;
            }

            @Override
            public String toString(final ObjectAdapterMemento nom) {
                return nom.transientMemento.toString();
            }
        };

        public synchronized ObjectAdapter getAdapter(final ObjectAdapterMemento nom) {
            return recreateAdapter(nom);
        }

        abstract ObjectAdapter recreateAdapter(ObjectAdapterMemento nom);

        public abstract String toString(ObjectAdapterMemento adapterMemento);
    }

    private Type type;

    /**
     * The current value, if {@link Type#ENCODEABLE}.
     * 
     * <p>
     * Will be <tt>null</tt> otherwise.
     */
    private String encodableValue;
    /**
     * The current value, if {@link Type#PERSISTENT}.
     * 
     * <p>
     * Will be <tt>null</tt> otherwise.
     */
    private Oid persistentOid;

    /**
     * The current value, if {@link Type#TRANSIENT}.
     * 
     * <p>
     * Will be <tt>null</tt> otherwise.
     */
    private Memento transientMemento;

    private ObjectAdapterMemento(final Oid oid, final SpecMemento specMemento) {
        Ensure.ensureThatArg(oid, Oids.isPersistent());
        this.persistentOid = oid;
        this.specMemento = specMemento;
        this.type = Type.PERSISTENT;
    }

    private ObjectAdapterMemento(final ObjectAdapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter cannot be null");
        }
        final ObjectSpecification specification = adapter.getSpecification();
        specMemento = new SpecMemento(specification);
        init(adapter);
        captureTitleHintIfPossible(adapter);
    }

    private void init(final ObjectAdapter adapter) {
        final ObjectSpecification specification = specMemento.getSpecification();
        final EncodableFacet encodableFacet = specification.getFacet(EncodableFacet.class);
        final boolean isEncodable = encodableFacet != null;
        if (isEncodable) {
            encodableValue = encodableFacet.toEncodedString(adapter);
            type = Type.ENCODEABLE;
        } else {
            final Oid oid = adapter.getOid();
            if (oid.isTransient()) {
                transientMemento = new Memento(adapter);
                type = Type.TRANSIENT;
            } else {
                persistentOid = oid;
                type = Type.PERSISTENT;
            }
        }
    }

    public void captureTitleHintIfPossible(final ObjectAdapter adapter) {
        if (adapter == null) {
            return;
        }
        if (adapter.isTitleAvailable()) {
            this.titleHint = adapter.titleString();
        }
    }

    public String getTitleHint() {
        return titleHint;
    }

    /**
     * Lazily looks up {@link ObjectAdapter} if required.
     * 
     * <p>
     * For transient objects, be aware that calling this method more than once
     * will cause the underlying {@link ObjectAdapter} to be recreated,
     * overwriting any changes that may have been made. In general then it's
     * best to call once and then hold onto the value thereafter. Alternatively,
     * can call {@link #setAdapter(ObjectAdapter)} to keep this memento in sync.
     */
    public ObjectAdapter getObjectAdapter() {
        return type.getAdapter(this);
    }

    /**
     * Updates the memento if the adapter's state has changed.
     * 
     * <p>
     * This is a no-op for
     * 
     * @param adapter
     */
    public void setAdapter(final ObjectAdapter adapter) {
        init(adapter);
    }

    public SpecMemento getSpecMemento() {
        return specMemento;
    }

    @Override
    public String toString() {
        return type.toString(this);
    }

}
