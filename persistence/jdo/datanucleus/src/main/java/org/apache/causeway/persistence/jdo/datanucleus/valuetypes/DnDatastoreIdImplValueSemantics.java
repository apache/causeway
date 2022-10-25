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
package org.apache.causeway.persistence.jdo.datanucleus.valuetypes;

import javax.annotation.Priority;

import org.datanucleus.identity.DatastoreIdImpl;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsBasedOnIdStringifier;
import org.apache.causeway.commons.internal.factory._InstanceUtil;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

@Component
@Priority(PriorityPrecedence.LATE)
public class DnDatastoreIdImplValueSemantics
extends ValueSemanticsBasedOnIdStringifier<DatastoreIdImpl> {

    public static final String STRING_DELIMITER = "[OID]"; // as

    public DnDatastoreIdImplValueSemantics() {
        super(DatastoreIdImpl.class);
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final DatastoreIdImpl value) {
        return CommonDtoUtils.typedTupleBuilder(value)
                .addFundamentalType(ValueType.STRING, "targetClassName", DatastoreIdImpl::getTargetClassName)
                .addFundamentalType(ValueType.STRING, "key", this::enstring)
                .buildAsDecomposition();
    }

    @Override
    public DatastoreIdImpl compose(final ValueDecomposition decomposition) {
        val elementMap = CommonDtoUtils.typedTupleAsMap(decomposition.rightIfAny());
        final String targetClassName = (String)elementMap.get("targetClassName");
        final String key = (String)elementMap.get("key");
        return destring(_InstanceUtil.loadClass(targetClassName), key);
    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(final @NonNull DatastoreIdImpl value) {
        return value.getKeyAsObject().toString();
    }

    @SneakyThrows
    @Override
    public DatastoreIdImpl destring(
            final @NonNull Class<?> targetEntityClass,
            final @NonNull String stringified) {
        // enString invoked toString() on the original key; invoking toString() on its stringified form does not change it
        val proto = new DatastoreIdImpl(targetEntityClass.getName(), stringified);
        // now render in the form that the DataStoreImpl constructor expects; it will take it apart itself.
        val str = proto.toString();
        return new DatastoreIdImpl(str);
    }

}
