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
package org.apache.causeway.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet.Intent;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmInvokeUtil;

import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
public class ImperativeAspect {

    private final Can<Method> methods;
    private final Intent intent;

    public Intent getIntent(final Method method) {
        return intent;
    }

    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("methods",
                getMethods().stream()
                .map(Method::toString)
                .collect(Collectors.joining(", ")));
        getMethods().forEach(method->
            visitor.accept(
                    "intent." + method.getName(), getIntent(method)));
    }

    public static ImperativeAspect singleMethod(final Method method, final Intent checkIfDisabled) {
        return of(ImperativeFacet.singleMethod(method), checkIfDisabled);
    }

    public Object invokeSingleMethod(final ManagedObject domainObject) {
        val method = methods.getFirstOrFail();
        final Object returnValue = MmInvokeUtil.invoke(method, domainObject);
        return returnValue;
    }

    public <T> T eval(
            final ManagedObject domainObject,
            final T fallback) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(domainObject)) {
            return fallback;
        }
        try {
            return _Casts.uncheckedCast(invokeSingleMethod(domainObject));
        } catch (final RuntimeException ex) {

            return fallback;
        }

    }

}
