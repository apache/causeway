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
package org.apache.isis.applib.services.i18n;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Strings;

import lombok.Getter;
import lombok.Value;
import lombok.val;

/**
 * @since 2.x {@index}
 */
@Value(staticConstructor = "ofName")
public final class TranslationContext
implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final String name;

    //XXX this guard could be removed if non-null is guaranteed
    public static TranslationContext forTranslationContextHolder(
            final @Nullable HasTranslationContext hasTranslationContext) {

        return hasTranslationContext!=null
                ? hasTranslationContext.getTranslationContext()
                : EMPTY;
    }

    //XXX no logical type name supported
    public static TranslationContext forClassName(
            final @Nullable Class<?> contextClass) {

        return contextClass!=null
                ? ofName(contextClass.getName())
                : EMPTY;
    }

    //XXX no logical type name supported
    public static TranslationContext forMethod(
            final @Nullable Class<?> contextClass,
            final @Nullable String contextMethodName) {

        val classContext = forClassName(contextClass);
        return _Strings.isNullOrEmpty(contextMethodName)
                ? classContext
                : ofName(classContext.getName() + "#" + contextMethodName + "()");
    }

    //XXX no logical type name supported
    public static TranslationContext forMethod(
            final @Nullable Method method) {

        return method!=null
                ? ofName(method.getDeclaringClass().getName() + "#" + method.getName() + "()")
                : EMPTY;
    }

    //XXX no logical type name supported
    public static TranslationContext forEnum(
            final @Nullable Enum<?> objectAsEnum) {

        return objectAsEnum!=null
                ? ofName(objectAsEnum.getClass().getName() + "#" + objectAsEnum.name())
                : EMPTY;
    }

	// -- EMPTY

	private final static TranslationContext EMPTY = TranslationContext.ofName("default");

    public static TranslationContext empty() {
        return EMPTY;
    }

}
