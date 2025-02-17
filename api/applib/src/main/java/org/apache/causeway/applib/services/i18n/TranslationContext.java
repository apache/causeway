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
package org.apache.causeway.applib.services.i18n;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;

/**
 * @since 2.x {@index}
 */
public record TranslationContext(String name)
implements Serializable {

    public static TranslationContext named(final String name) { return new TranslationContext(name); }

    public String getName() { return name; }

    //XXX no logical type name supported
    public static TranslationContext forClassName(
            final @Nullable Class<?> contextClass) {

        return contextClass!=null
                ? named(contextClass.getName())
                : EMPTY;
    }

    //XXX no logical type name supported
    public static TranslationContext forMethod(
            final @Nullable Class<?> contextClass,
            final @Nullable String contextMethodName) {

        var classContext = forClassName(contextClass);
        return _Strings.isNullOrEmpty(contextMethodName)
                ? classContext
                : named(classContext.getName() + "#" + contextMethodName + "()");
    }

    //XXX no logical type name supported
    public static TranslationContext forMethod(
            final @Nullable ResolvedMethod method) {

        return method!=null
                ? named(method.method().getDeclaringClass().getName() + "#" + method.name() + "()")
                : EMPTY;
    }

    //XXX no logical type name supported
    public static TranslationContext forEnum(
            final @Nullable Enum<?> objectAsEnum) {

        return objectAsEnum!=null
                ? named(objectAsEnum.getClass().getName() + "#" + objectAsEnum.name())
                : EMPTY;
    }

	// -- EMPTY

	private final static TranslationContext EMPTY = TranslationContext.named("default");

    public static TranslationContext empty() {
        return EMPTY;
    }

}
