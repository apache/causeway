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
package org.apache.isis.persistence.jdo.datanucleus.oid;

import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.isis.commons.handler.ChainOfResponsibility;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
class _JdoObjectIdEncoder implements ChainOfResponsibility.Handler<Object, String> {

    @NonNull private final Predicate<Object> filter;
    @NonNull private final Function<Object, String> keyStringifier;
    
    @Override
    public boolean isHandling(final Object jdoOid) {
        return filter.test(jdoOid);
    }
    
    @Override
    public String handle(final Object jdoOid) {
        return keyStringifier.apply(jdoOid);
    }
    
    public static Predicate<Object> filter(Class<?> jdoKeyType) {
        return jdoOid->jdoKeyType.isAssignableFrom(jdoOid.getClass());
    }
    
    public static Function<Object, String> stringifier(String token) {
        return jdoOid->token + JdoObjectIdSerializer.SEPARATOR + jdoOid;
    }

}
