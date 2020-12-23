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
package org.apache.isis.persistence.jdo.integration.oid;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.jdo.annotations.IdentityType;

import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.persistence.jdo.integration.oid._JdoObjectIdDecoder.JdoObjectIdDecodingRequest;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
class _JdoObjectIdDecoder implements ChainOfResponsibility.Handler<JdoObjectIdDecodingRequest, Object> {

    @Value(staticConstructor = "of")
    static class JdoObjectIdDecodingRequest {
        final ObjectSpecification spec;
        final String distinguisher;
        final String keyStr;
        
        boolean isApplicationIdentity() {
            val jdoPersistenceCapableFacet = spec.getFacet(JdoPersistenceCapableFacet.class);
            return jdoPersistenceCapableFacet != null 
                    && jdoPersistenceCapableFacet.getIdentityType() == IdentityType.APPLICATION;
        }

        public static JdoObjectIdDecodingRequest parse(ObjectSpecification spec, String idStr) {
            idStr = _Strings.nullToEmpty(idStr);
            final int separatorIdx = idStr.indexOf(JdoObjectIdSerializer.SEPARATOR);
            return separatorIdx != -1
                ? of(
                        spec,
                        idStr.substring(0, separatorIdx), 
                        idStr.substring(separatorIdx + 1))
                : of(spec, "", idStr);
        }
    }

    @NonNull private final Predicate<JdoObjectIdDecodingRequest> filter;
    @NonNull private final Function<JdoObjectIdDecodingRequest, Object> parser;
    
    @Override
    public boolean isHandling(JdoObjectIdDecodingRequest request) {
        return filter.test(request);
    }

    @Override
    public Object handle(JdoObjectIdDecodingRequest request) {
        return parser.apply(request);
    }

    public static Predicate<JdoObjectIdDecodingRequest> filter(@NonNull final String token) {
        return request->token.equals(request.getDistinguisher());
    }
    
    public static Function<JdoObjectIdDecodingRequest, Object> parser(
            @NonNull final Function<String, ? extends Object> appIdFactory,
            @NonNull final BiFunction<Class<?>, String, Object> jdoObjectIdFactory) {
        return request->request.isApplicationIdentity()
                ? appIdFactory.apply(request.getKeyStr())
                : jdoObjectIdFactory.apply(request.getSpec().getCorrespondingClass(), request.getKeyStr());
    }


}
