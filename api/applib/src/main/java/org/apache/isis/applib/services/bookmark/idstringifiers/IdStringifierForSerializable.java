/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.isis.applib.services.bookmark.idstringifiers;

import java.io.Serializable;

import javax.annotation.Priority;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.resources._Serializables;

import lombok.NonNull;

@Component
@Priority(PriorityPrecedence.LAST) // goes last - is also enforced by IdStringifierLookupService programmatically
public class IdStringifierForSerializable extends IdStringifier.Abstract<Serializable> {

    private final UrlEncodingService codec;

    @Inject
    public IdStringifierForSerializable(
            final @NonNull UrlEncodingService codec) {
        super(Serializable.class);
        this.codec = codec;
    }

    @Override
    public boolean handles(final @NonNull Class<?> candidateValueClass) {
        return Serializable.class.isAssignableFrom(candidateValueClass);
    }

    @Override
    public String enstring(final @NonNull Serializable id) {
        // even though null case is guarded by lombok - keep null check for symmetry
        return id != null
                ? codec.encode(_Serializables.write(id))
                : null;
    }

    @Override
    public Serializable destring(
            final @NonNull String stringified,
            final @NonNull Class<?> targetEntityClass) {
        return destringAs(stringified, Serializable.class);
    }

    // -- HELPER

    private <T extends Serializable> T destringAs(
            final @NonNull String stringified,
            final @NonNull Class<T> requiredClass) {
        return _Strings.isNotEmpty(stringified)
                ? _Serializables.read(requiredClass, codec.decode(stringified))
                : null;
    }

}
