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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Iterator;
import java.util.Optional;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Getter
@RequiredArgsConstructor(staticName = "of")
@Deprecated // ParameterNegotiationModel has all we need
public class ManagedParameterList implements Iterable<ManagedParameter2> {

    @NonNull private final ManagedAction owningAction;
    @NonNull private final Can<ManagedParameter2> parameters;
    
    public static ManagedParameterList ofValues(ManagedAction owningAction, Can<ManagedObject> paramValueList) {
        
        val paramValueIterator = paramValueList.iterator();
        val parameters = owningAction.getAction().getParameters()
        .map(param->{
            final ManagedObject paramValue = Optional
                    .ofNullable(paramValueIterator.next())
                    .orElse(ManagedObject.of(param.getSpecification(), null));
            return ManagedParameter2.of(owningAction, param, paramValue);
        });
        
        return of(owningAction, parameters);
    }

    @Override
    public Iterator<ManagedParameter2> iterator() {
        return getParameters().iterator();
    }
    
}
