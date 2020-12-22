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

package org.apache.isis.persistence.jdo.integration.objectadapter;

import java.util.Objects;
import java.util.Optional;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.Getter;
import lombok.val;

public final class PojoAdapter implements ObjectAdapter {

    private final SpecificationLoader specificationLoader;

    @Getter(onMethod = @__(@Override)) private final Object pojo;
    @Getter(onMethod = @__(@Override)) private final Oid oid;

    // -- FACTORIES

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
            return "B"; // bookmark-able
//            if(oid.isPersistent()) return "P";
//            if(oid.isTransient()) return "T";
//            if(oid.isViewModel()) return "V";
        }
        return "S"; // standalone adapter (value)
    }

    @Override
    public Optional<RootOid> getRootOid() {
        val oid = getOid();
        if (oid instanceof RootOid) {
            return Optional.of((RootOid)oid);
        }
        return Optional.empty();
    }

    @Override
    public boolean isRootOidMemoized() {
        return true; // oid is immutable
    }


}
