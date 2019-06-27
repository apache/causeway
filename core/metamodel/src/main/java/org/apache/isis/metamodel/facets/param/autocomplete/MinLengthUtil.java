/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.metamodel.facets.param.autocomplete;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.MinLength;

public final class MinLengthUtil {

    private MinLengthUtil(){}

    /**
     * Finds the value of the minimum length, from the {@link MinLength} annotation
     * on the first parameter of the supplied method.
     */
    public static int determineMinLength(final Method method) {
        if(method == null) {
            return MIN_LENGTH_DEFAULT;
        }

        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if(parameterAnnotations.length == 1) {
            final Annotation[] searchArgAnnotations = parameterAnnotations[0];
            for(Annotation annotation: searchArgAnnotations) {
                if(annotation instanceof MinLength) {
                    MinLength minLength = (MinLength) annotation;
                    return minLength.value();
                }
            }
        }
        return MinLengthUtil.MIN_LENGTH_DEFAULT;
    }

    public static final int MIN_LENGTH_DEFAULT = 1;



}
