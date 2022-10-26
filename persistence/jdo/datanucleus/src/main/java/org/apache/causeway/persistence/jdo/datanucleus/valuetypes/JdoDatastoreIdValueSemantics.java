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

import java.lang.reflect.Constructor;

import javax.annotation.Priority;

import org.datanucleus.identity.DatastoreId;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsBasedOnIdStringifier;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.factory._InstanceUtil;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

@Component
@Priority(PriorityPrecedence.LATE + 100) // after the implementations of DatastoreId; for a custom impl.
public class JdoDatastoreIdValueSemantics
extends ValueSemanticsBasedOnIdStringifier<DatastoreId> {

    public JdoDatastoreIdValueSemantics() {
        super(DatastoreId.class);
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final DatastoreId value) {
        return CommonDtoUtils.typedTupleBuilder(value)
                .addFundamentalType(ValueType.STRING, "targetClassName", DatastoreId::getTargetClassName)
                .addFundamentalType(ValueType.STRING, "key", this::enstring)
                .buildAsDecomposition();
    }

    @Override
    public DatastoreId compose(final ValueDecomposition decomposition) {
        val elementMap = CommonDtoUtils.typedTupleAsMap(decomposition.rightIfAny());
        final String targetClassName = (String)elementMap.get("targetClassName");
        final String key = (String)elementMap.get("key");
        return destring(_InstanceUtil.loadClass(targetClassName), key);
    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(final @NonNull DatastoreId value) {
        //
        // the JDO spec (5.4.3) requires that OIDs are serializable toString and
        // re-create-able through the constructor
        //
        // to do this, we also need to capture the class of the Id value class itself, followed by the value (as a string)
        return value.getClass().getName() + IdStringifier.SEPARATOR + value.toString();
    }

    @SneakyThrows
    @Override
    public DatastoreId destring(
            final @NonNull Class<?> targetEntityClass,
            final @NonNull String stringified) {
        int idx = stringified.indexOf(IdStringifier.SEPARATOR);
        String clsName = stringified.substring(0, idx);
        String keyStr = stringified.substring(idx + 1);
        final Class<?> cls = _Context.loadClass(clsName);
        final Constructor<?> cons = cls.getConstructor(String.class);
        final Object dnOid = cons.newInstance(keyStr);
        return _Casts.uncheckedCast(dnOid);
    }

}
