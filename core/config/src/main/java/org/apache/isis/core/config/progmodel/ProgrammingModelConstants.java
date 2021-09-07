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
package org.apache.isis.core.config.progmodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.enterprise.inject.Vetoed;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.isis.applib.annotation.Domain;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.ObjectLifecycle;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

public final class ProgrammingModelConstants {

    // -- TYPE VETO MARKERS (EXLUDE FROM DOMAIN)

    @Getter
    @RequiredArgsConstructor
    public enum TypeVetoMarker {
        DOMAIN_EXLCUDE(Domain.Exclude.class),
        VETO(Vetoed.class);
        private final Class<? extends Annotation> annotationType;
    }

    // -- ACCESSORS

    @Getter
    @RequiredArgsConstructor
    public enum AccessorPrefix {
        GET("get"),
        IS("is"),
        SET("set");
        private final String prefix;

        public String prefix(final @Nullable String input) {
            return input!=null
                    ? prefix + input
                    : prefix;
        }

        public boolean isPrefixOf(final @Nullable String input) {
            return input!=null
                    ? input.startsWith(prefix)
                    : false;
        }

        public static boolean isCandidateGetterName(final @Nullable String name) {
            return GET.isPrefixOf(name)
                    || IS.isPrefixOf(name);
        }

        public static boolean isBooleanGetter(final Method method) {
            return IS.isPrefixOf(method.getName())
                    && method.getParameterCount() == 0
                    && !Modifier.isStatic(method.getModifiers())
                    && (method.getReturnType() == boolean.class
                        || method.getReturnType() == Boolean.class);
        }

        public static boolean isNonBooleanGetter(final Method method, final Predicate<Class<?>> typeFilter) {
            return GET.isPrefixOf(method.getName())
                    && method.getParameterCount() == 0
                    && !Modifier.isStatic(method.getModifiers())
                    && typeFilter.test(method.getReturnType());
        }

        public static boolean isNonBooleanGetter(final Method method, final Class<?> expectedType) {
            return isNonBooleanGetter(method, type->
                expectedType.isAssignableFrom(ClassUtils.resolvePrimitiveIfNecessary(type)));
        }

        public static boolean isGetter(final Method method) {
            return isBooleanGetter(method)
                    || isNonBooleanGetter(method, type->type != void.class);
        }
    }

    // -- LIFECYCLE CALLBACKS

    @Getter
    public enum CallbackMethod {
        CREATED("created"),
        LOADED("loaded"),
        PERSISTED("persisted", "saved"),
        PERSISTING("persisting", "saving"),
        REMOVING("removing", "deleting"),
        UPDATED("updated"),
        UPDATING("updating");
        CallbackMethod(final String ...methodNames) {
            this.methodNames = Can.of(methodNames);
        }
        private final Can<String> methodNames;
    }

    // -- TYPE CATEGORIES

    @Getter
    public enum ReturnTypeCategory {
        VOID(void.class),
        BOOLEAN(boolean.class),
        STRING(String.class),
        TRANSLATABLE(String.class, TranslatableString.class);
        ReturnTypeCategory(final Class<?> ...returnTypes) {
            this.returnTypes = Can.of(returnTypes);
        }
        private final Can<Class<?>> returnTypes;

        public static Can<Class<?>> nonScalar(final Class<?> elementReturnType) {
            return Can.<Class<?>>of(
                Can.class,
                Collection.class,
                Array.newInstance(elementReturnType, 0).getClass());
        }
    }

    // -- PARAMETER SUPPORT

    public static enum ReturnType {
        NON_SCALAR,
        TEXT,
        BOOLEAN,
        SAME_AS_PARAMETER_TYPE,
    }

    // -- OBJECT SUPPORT

    @Getter
    public enum ObjectSupportMethod {
        /** for batch disabling all members */
        DISABLED(ReturnTypeCategory.TRANSLATABLE, "disabled"),

        /** for batch hiding all members */
        HIDDEN(ReturnTypeCategory.BOOLEAN, "hidden"),

        TITLE(ReturnTypeCategory.TRANSLATABLE, "title"),
        CSS_CLASS(ReturnTypeCategory.STRING, "cssClass"),
        ICON_NAME(ReturnTypeCategory.STRING, "iconName"),
        LAYOUT(ReturnTypeCategory.STRING, "layout");
        ObjectSupportMethod(
                final ReturnTypeCategory returnTypeCategory,
                final String ...methodNames) {
            this.returnTypeCategory = returnTypeCategory;
            this.methodNames = Can.of(methodNames);
        }
        private final ReturnTypeCategory returnTypeCategory;
        private final Can<String> methodNames;
    }

    // -- MEMBER SUPPORT PREFIXES

    @Getter
    public static enum MemberSupportPrefix {
        DEFAULT(ReturnType.SAME_AS_PARAMETER_TYPE, "default"),
        CHOICES(ReturnType.NON_SCALAR, "choices"),
        AUTO_COMPLETE(ReturnType.NON_SCALAR, "autoComplete"),
        HIDE(ReturnType.BOOLEAN, "hide"),
        DISABLE(ReturnType.TEXT, "disable"),
        VALIDATE(ReturnType.TEXT, "validate"),
        NAMED(ReturnType.TEXT, "named"), // imperative naming
        DESCRIBED(ReturnType.TEXT, "described"); // imperative naming
        MemberSupportPrefix(
                final ReturnType parameterSearchReturnType,
                final String ...methodNamePrefixes) {
            this.parameterSearchReturnType = parameterSearchReturnType;
            this.methodNamePrefixes = Can.of(methodNamePrefixes);
        }
        private final ReturnType parameterSearchReturnType;
        private final Can<String> methodNamePrefixes;
    }

    // -- OTHER LITERALS

    public static final String TO_STRING = "toString";

    // -- CONFLICTING MARKER ANNOTATIONS

    @Getter
    @RequiredArgsConstructor
    public static enum ConflictingAnnotations {
        OBJECT_SUPPORT(Can.of(ObjectLifecycle.class, MemberSupport.class)),
        OBJECT_LIFECYCLE(Can.of(ObjectSupport.class, MemberSupport.class)),
        MEMBER_SUPPORT(Can.of(ObjectSupport.class, ObjectLifecycle.class));
        final Can<Class<? extends Annotation>> prohibits;
    }

    // -- SUPPORTING METHOD NAMING CONVENTION

    public static enum ActionSupportNaming {
        /** eg. hideAct() */
        PREFIXED_ACTION_NAME {
            @Override @Nullable
            String nameFor(final Method actionMethod, final String prefix, final boolean isMixin) {
                return prefix + _Strings.capitalize(actionMethod.getName());
            }
        },
        /** eg. hide() */
        PREFIX_ONLY {
            @Override @Nullable
            String nameFor(final Method actionMethod, final String prefix, final boolean isMixin) {
                return isMixin
                        // prefix-only notation is restricted to mixins
                        ? prefix
                        : null;
            }
        };
        abstract @Nullable String nameFor(Method actionMethod, String prefix, boolean isMixin);
        public static Can<String> namesFor(final Method actionMethod, final String prefix, final boolean isMixin) {
            return Stream.of(ActionSupportNaming.values())
                    .map(naming->naming.nameFor(actionMethod, prefix, isMixin))
                    .collect(Can.toCan());
        }
    }

    public static enum ParameterSupportNaming {
        /** eg. hide2Act(..) */
        PREFIX_PARAM_INDEX_ACTION_NAME {
            @Override @Nullable
            String nameFor(final Method actionMethod, final String prefix, final boolean isMixin, final int paramNum) {
                return prefix + paramNum + _Strings.capitalize(actionMethod.getName());
            }
        },
        /** eg. hideEmail() .. where email is the referenced parameter's name */
        PREFIXED_PARAM_NAME {
            @Override @Nullable
            String nameFor(final Method actionMethod, final String prefix, final boolean isMixin, final int paramNum) {
                return isMixin
                        // no-action-name-reference notation is restricted to mixins
                        ? prefix + _Strings.capitalize(actionMethod.getParameters()[paramNum].getName())
                        : null;
            }
        };
        abstract @Nullable String nameFor(Method actionMethod, String prefix, boolean isMixin, int paramNum);
        public static Can<IntFunction<String>> namesFor(final Method actionMethod, final String prefix, final boolean isMixin) {
            return Stream.of(ParameterSupportNaming.values())
                    .<IntFunction<String>>map(naming->(paramNum->naming.nameFor(actionMethod, prefix, isMixin, paramNum)))
                    .collect(Can.toCan());
        }
    }

    /** deals with <i>fields</i>, <i>getters</i> and <i>actions</i> */
    public static enum MemberSupportNaming {
        /** eg. hideProp() */
        PREFIXED_MEMBER_NAME {
            @Override @Nullable
            String nameFor(final Member member, final String prefix, final boolean isMixin) {
                return prefix + getCapitalizedMemberName(member);
            }
        },
        /** eg. hide() */
        PREFIX_ONLY {
            @Override @Nullable
            String nameFor(final Member member, final String prefix, final boolean isMixin) {
                return isMixin
                        // prefix-only notation is restricted to mixins
                        ? prefix
                        : null;
            }
        };
        abstract @Nullable String nameFor(Member member, String prefix, boolean isMixin);
        public static Can<String> namesFor(final Member member, final String prefix, final boolean isMixin) {
            return Stream.of(MemberSupportNaming.values())
                    .map(naming->naming.nameFor(member, prefix, isMixin))
                    .collect(Can.toCan());
        }
    }

    // -- HELPER

    private static String getCapitalizedMemberName(final Member member) {
        if(member instanceof Method) {
            val method = (Method)member;
            val methodName = method.getName();
            if(method.getParameterCount()>0
                    || method.getReturnType().equals(void.class)
                    || !AccessorPrefix.isCandidateGetterName(methodName)) {
                // definitely an action not a getter
                return _Strings.capitalize(methodName);
            }
            // must be a getter
            return _Strings.capitalize(_Strings.asPrefixDropped(methodName));
        }
        // must be a field then
        return _Strings.capitalize(member.getName());
    }


}
