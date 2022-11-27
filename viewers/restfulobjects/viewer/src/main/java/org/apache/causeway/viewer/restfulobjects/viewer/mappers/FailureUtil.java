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
package org.apache.causeway.viewer.restfulobjects.viewer.mappers;

import java.util.Optional;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.metamodel.methods.MethodFinder;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;

import lombok.experimental.UtilityClass;

@UtilityClass
final class FailureUtil {

    public static HttpStatusCode getFailureStatusCodeIfAny(final Throwable ex) {

        return MethodFinder
            .publicOnly(ex.getClass(), Can.ofSingleton("getErrorCode"))
            .withRequiredReturnType(int.class)
            .streamMethodsMatchingSignature(MethodFinder.NO_ARG)
            .findFirst()
            .map(errorCodeGetter->_Reflect.invokeMethodOn(errorCodeGetter, ex))
            .map(Try::getValue)
            .map(Optional::stream)
            .filter(Integer.class::isInstance)
            .map(Integer.class::cast)
            .map(RestfulResponse.HttpStatusCode::statusFor)
            .orElse(null);

    }

}
