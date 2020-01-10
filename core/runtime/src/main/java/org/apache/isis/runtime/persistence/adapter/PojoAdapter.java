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

package org.apache.isis.runtime.persistence.adapter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.commons.ToString;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.session.IsisSession;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.Getter;
import lombok.val;

public final class PojoAdapter implements ObjectAdapter {

    private final SpecificationLoader specificationLoader;

    @Getter(onMethod = @__(@Override)) private final Object pojo;
    @Getter(onMethod = @__(@Override)) private final Oid oid;

    // -- FACTORIES

    public static PojoAdapter of(
            final Object pojo,
            final Oid oid) {

        return of(pojo, oid, IsisSession.currentOrElseNull());
    }

    public static PojoAdapter ofValue(Serializable value) {
        val oid = Oid.Factory.value();
        return PojoAdapter.of(value, oid);
    }

    public static ObjectAdapter ofTransient(Object pojo, ObjectSpecId specId) {
        val identifier = UUID.randomUUID().toString();
        return PojoAdapter.of(pojo, Oid.Factory.transientOf(specId, identifier));
    }

    public static PojoAdapter of(
            final Object pojo,
            final Oid oid,
            final IsisSession isisSession) {

        val specificationLoader = isisSession.getSpecificationLoader();

        return new PojoAdapter(pojo, oid, specificationLoader);
    }

    public static PojoAdapter of(
            final Object pojo,
            final Oid oid,
            final SpecificationLoader specificationLoader) {
        return new PojoAdapter(pojo, oid, specificationLoader);
    }

    private PojoAdapter(
            final Object pojo,
            final Oid oid,
            final SpecificationLoader specificationLoader) {

        Objects.requireNonNull(pojo);

        this.specificationLoader = specificationLoader;

        if (pojo instanceof ObjectAdapter) {
            throw new IsisException("ObjectAdapter can't be used to wrap an ObjectAdapter: " + pojo);
        }
        if (pojo instanceof Oid) {
            throw new IsisException("ObjectAdapter can't be used to wrap an Oid: " + pojo);
        }

        this.pojo = pojo;
        this.oid = requires(oid, "oid");
    }

    // -- getSpecification

    final _Lazy<ObjectSpecification> objectSpecification = _Lazy.of(this::loadSpecification);

    @Override
    public ObjectSpecification getSpecification() {
        return objectSpecification.get();
    }

    private ObjectSpecification loadSpecification() {
        final Class<?> aClass = getPojo().getClass();
        final ObjectSpecification specification = specificationLoader.loadSpecification(aClass);
        return specification;
    }

    // -- getAggregateRoot
    
//    @Override
//    public ObjectAdapter getAggregateRoot() {
//        if(!isParentedCollection()) {
//            return this;
//        }
//        val collectionOid = (ParentedOid) oid;
//        val rootOid = collectionOid.getParentOid();
//        val rootAdapter = persistenceSession.adapterFor(rootOid);
//        return rootAdapter;
//    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        toString(str);

        // don't do title of any entities. For persistence entities, might
        // forces an unwanted resolve
        // of the object. For transient objects, may not be fully initialized.

        str.append("pojo-toString", pojo.toString());
        str.appendAsHex("pojo-hash", pojo.hashCode());
        return str.toString();
    }

    protected void toString(final ToString str) {
        str.append(aggregateResolveStateCode());
        final Oid oid = getOid();
        if (oid != null) {
            str.append(":");
            str.append(oid.toString());
        } else {
            str.append(":-");
        }
        str.setAddComma();
        if (!objectSpecification.isMemoized()) {
            str.append("class", getPojo().getClass().getName());
        } else {
            str.append("specification", getSpecification().getShortIdentifier());
        }
    }

    private String aggregateResolveStateCode() {

        // this is an approximate re-implementation...
        final Oid oid = getOid();
        if(oid != null) {
            if(oid.isPersistent()) return "P";
            if(oid.isTransient()) return "T";
            if(oid.isViewModel()) return "V";
        }
        return "S"; // standalone adapter (value)
    }


}
