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

import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet.Intent;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;

public record ImperativeAspect(
    Can<MethodFacade> methods,
    Intent intent) {

    // -- FACTORIES

    public static ImperativeAspect singleRegularMethod(final ResolvedMethod method, final Intent intent) {
        return new ImperativeAspect(ImperativeFacet.singleRegularMethod(method), intent);
    }

    // --

    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("methods", methods().join(", "));
        methods().forEach(method->
            visitor.accept(
                    "intent." + method.getName(), intent));
    }

    public Object invokeSingleMethod(final ManagedObject domainObject) {
        var method = methods.getFirstElseFail().asMethodElseFail(); // expected regular, as the factories only creates regular
        final Object returnValue = MmInvokeUtils.invokeNoArg(method.method(), domainObject);
        return returnValue;
    }

    public Object invokeSingleMethod(final ManagedObject domainObject, final Object arg0) {
        var method = methods.getFirstElseFail().asMethodElseFail(); // expected regular, as the factories only creates regular
        final Object returnValue = MmInvokeUtils.invokeWithSingleArgPojo(method.method(), domainObject, arg0);
        return returnValue;
    }

    public <T> T eval(
            final ManagedObject domainObject,
            final T fallback) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(domainObject)) return fallback;

        try {
            return _Casts.uncheckedCast(invokeSingleMethod(domainObject));
        } catch (final RuntimeException ex) {
            return fallback;
        }
    }

    public <T> T eval(
            final ManagedObject domainObject,
            final T fallback,
            final Object arg0) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(domainObject)) return fallback;

        try {
            return _Casts.uncheckedCast(invokeSingleMethod(domainObject));
        } catch (final RuntimeException ex) {
            return fallback;
        }
    }

}
