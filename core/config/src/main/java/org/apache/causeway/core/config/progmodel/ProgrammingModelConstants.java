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
package org.apache.causeway.core.config.progmodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ObjectLifecycle;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableCollection;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.collections._Collections;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._Reflect;

import static org.apache.causeway.commons.internal.reflection._Reflect.Filter.paramAssignableFrom;
import static org.apache.causeway.commons.internal.reflection._Reflect.Filter.paramCount;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.Accessors;

public final class ProgrammingModelConstants {

    // -- TYPE EXCLUDE MARKERS

    @Getter
    @RequiredArgsConstructor
    public enum TypeExcludeMarker {
        DOMAIN_EXCLUDE(Domain.Exclude.class)
        //,VETO(javax.enterprise.inject.Vetoed.class)
        ;
        private final Class<? extends Annotation> annotationType;

        public static boolean anyMatchOn(final Class<?> type) {
            for(TypeExcludeMarker excludeMarker : TypeExcludeMarker.values()) {
                if(_Annotations.synthesize(type, excludeMarker.getAnnotationType()).isPresent()) {
                    return true;
                }
            }
            return false;
        }
    }

    // -- METHOD INCLUDE MARKERS

    /**
     * Ensure included.
     */
    @Getter
    @RequiredArgsConstructor
    public enum MethodIncludeMarker {
        DOMAIN_INCLUDE(Domain.Include.class),
        ;
        private final Class<? extends Annotation> annotationType;

        public static boolean anyMatchOn(final Method method) {
            for(MethodIncludeMarker includeMarker : MethodIncludeMarker.values()) {
                if(_Annotations.synthesize(method, includeMarker.getAnnotationType()).isPresent()) {
                    return true;
                }
            }
            return false;
        }
    }


    // -- METHOD EXCLUDE MARKERS

    @Getter
    @RequiredArgsConstructor
    public enum MethodExcludeMarker {
        DOMAIN_EXCLUDE(Domain.Exclude.class),
        PRE_DESTROY_JAVAX(javax.annotation.PreDestroy.class),
        POST_CONSTRUCT_JAVAX(javax.annotation.PostConstruct.class),
        //PRE_DESTROY__JAKARTA(jakarta.annotation.PreDestroy.class),
        //POST_CONSTRUCT_JAKARTA(jakarta.annotation.PreDestroy.class)
        ;
        private final Class<? extends Annotation> annotationType;

        public static boolean anyMatchOn(final Method method) {
            for(MethodExcludeMarker excludeMarker : MethodExcludeMarker.values()) {
                if(_Annotations.synthesize(method, excludeMarker.getAnnotationType()).isPresent()) {
                    return true;
                }
            }
            return false;
        }
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

    // -- CANONICAL DATE-TIME PARSING/FORMATTING

    @RequiredArgsConstructor
    public enum DateTimeFormat {
        /**
         * Parser-format: {@literal "yyyy-MM-dd HH:mm:ss[.SSS][' '][XXX][x]"}<br>
         * Render-format: {@literal "yyyy-MM-dd HH:mm:ss.SSS XXX"}<br>
         * Examples:<br>
         * <ul>
         * <li>"2022-01-31 14:04:33.017 -03:30" (full form)</li>
         * <li>"2022-01-31 14:04:33 -03" (no millis, no offset minutes)</li>
         * <li>"2022-01-31 14:04:33 Z" (no millis, no offset = UTC)</li>
         * </ul>
         * <p>
         * Used eg. with {@code InteractAs(frozenDateTime=...)} annotation.
         */
        CANONICAL(
                new DateTimeFormatterBuilder()
                        .appendPattern("yyyy-MM-dd HH:mm:ss[.SSS][' '][XXX][x]")
                        .parseLenient()
                        .parseCaseInsensitive()
                        .toFormatter(Locale.ROOT),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS XXX", Locale.ROOT));

        public OffsetDateTime parseDateTime(final String dateTimeLiteral) {
            return OffsetDateTime.parse(dateTimeLiteral, parser);
        }

        public String formatDateTime(final OffsetDateTime dateTime) {
            return formatter.format(dateTime);
        }

        // -- HELPER

        private final DateTimeFormatter parser;
        private final DateTimeFormatter formatter;

    }


    // -- MIXIN CONSTRUCTION

    public enum MixinConstructor {
        /**
         * Assuming, mixins do have a public single argument constructor,
         * that receive an instance of the mixee's type.
         */
        PUBLIC_SINGLE_ARG_RECEIVING_MIXEE;

        // while this enum only has a single value, we just provide a (quasi) static method here
        public <T> Constructor<T> getConstructorElseFail(
                final @NonNull Class<T> mixinClass,
                final @NonNull Class<?> mixeeClass) {
            return _Casts.uncheckedCast(_Reflect
                        .getPublicConstructors(mixinClass)
                        .filter(paramCount(1).and(paramAssignableFrom(0, mixeeClass)))
                    .getSingleton()
                    .orElseThrow(()->_Exceptions.illegalArgument(
                            "Failed to locate constructor in '%s' to instantiate,"
                            + "when using type '%s' as first argument",
                            mixinClass.getName(), mixinClass.getName())));
        }

        // while this enum only has a single value, we just provide a (quasi) static method here
        public <T> Can<Constructor<T>> getConstructors(final Class<T> candidateMixinType) {
            val mixinContructors = _Reflect
                    .getPublicConstructors(candidateMixinType)
                    .filter(paramCount(1));
            return _Casts.uncheckedCast(mixinContructors);
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

        public static Can<Class<?>> nonScalar(final @NonNull Class<?> elementType) {
            if(VOID.returnTypes.contains(elementType)) {
                return Can.empty();
            }
            return Can.<Class<?>>of(
                Can.class,
                Collection.class,
                Array.newInstance(elementType, 0).getClass());
        }
    }

    // -- PARAMETER SUPPORT

    @RequiredArgsConstructor
    public static enum ReturnTypePattern {
        SCALAR(Can::ofSingleton),
        NON_SCALAR(ReturnTypeCategory::nonScalar),
        TEXT(__->ReturnTypeCategory.TRANSLATABLE.getReturnTypes()),
        BOOLEAN(__->ReturnTypeCategory.BOOLEAN.getReturnTypes());
        final Function<Class<?>, Can<Class<?>>> matchingTypesForElementType;
        public Can<Class<?>> matchingTypes(final @NonNull Class<?> elementType) {
            return matchingTypesForElementType.apply(elementType);
        }
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
        LAYOUT(ReturnTypeCategory.STRING, "layout"),

        /** as a fallback in the absence of other title providers */
        TO_STRING(ReturnTypeCategory.STRING, "toString"),
        ;
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
        DEFAULT(ReturnTypePattern.SCALAR, "default"),
        CHOICES(ReturnTypePattern.NON_SCALAR, "choices"),
        AUTO_COMPLETE(ReturnTypePattern.NON_SCALAR, "autoComplete"),
        HIDE(ReturnTypePattern.BOOLEAN, "hide"),
        DISABLE(ReturnTypePattern.TEXT, "disable"),
        VALIDATE(ReturnTypePattern.TEXT, "validate"),
        NAMED(ReturnTypePattern.TEXT, "named"), // imperative naming
        DESCRIBED(ReturnTypePattern.TEXT, "described"); // imperative naming
        MemberSupportPrefix(
                final ReturnTypePattern parameterSearchReturnType,
                final String ...methodNamePrefixes) {
            this.supportMethodReturnType = parameterSearchReturnType;
            this.methodNamePrefixes = Can.of(methodNamePrefixes);
        }
        private final ReturnTypePattern supportMethodReturnType;
        private final Can<String> methodNamePrefixes;
    }

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

    //maybe gradually consolidate all MM validation raisers here
    @RequiredArgsConstructor
    public static enum Violation {
        CONFLICTING_TITLE_STRATEGIES(
                "${type} has title() method with @Title annotation, which is not allowed; "
                + "consider either removing the @Title annotation or renaming the method"),
        ORPHANED_METHOD("${type}#${member}: is public, but orphaned (was not picked up by the framework); "
                + "reporting orphans, because the class is setup for member introspection, "
                + "without enforcing annotations"),
        UNSATISFIED_DOMAIN_INCLUDE_SEMANTICS("${type}#${member}: "
                + "has synthesized (effective) annotation @Domain.Include, "
                + "is assumed to represent or support a property, collection or action."),
        VIEWMODEL_CONFLICTING_SERIALIZATION_STRATEGIES(
                "${type}: has multiple incompatible annotations/interfaces indicating that "
                + "it is a recreatable object of some sort (${facetA} and ${facetB})"),
        VIEWMODEL_MULTIPLE_CONSTRUCTORS_WITH_INJECT_SEMANTICS(
                "${type}: ViewModel contract violation: there must be at most one public constructor that has inject semantics, "
                + "but found ${found}. "
                + "See " + org.apache.causeway.applib.ViewModel.class.getName() + " java-doc for details."),
        VIEWMODEL_MISSING_OR_MULTIPLE_PUBLIC_CONSTRUCTORS(
                "${type}: ViewModel contract violation: in absence of inject semantics there must be exactly one public constructor, "
                + "but found ${found}. "
                + "See " + org.apache.causeway.applib.ViewModel.class.getName() + " java-doc for details."),
        VIEWMODEL_MISSING_SERIALIZATION_STRATEGY(
                "${type}: Missing ViewModel serialization strategy encountered; "
                + "for ViewModels one of those must be true: "
                + "(1) implements the ViewModel interface, "
                + "(2) implements Serializable, "
                + "(3) uses JAXB semantics, "
                + "(4) has explicit VIEW_MODEL nature via DomainObject annotation."),
        DOMAIN_OBJECT_INVALID_NAVIGABLE_PARENT("${type}: the object's navigable parent must no be void, "
                + "plural, vetoed or a value-type; "
                + "yet the parent type '${parentType}' as discovered was ${parentTypeDeficiency}; "),
        DOMAIN_OBJECT_MISSING_A_NAMESPACE("${type}: the object type must declare a namespace, "
                + "yet there was none found in '${logicalTypeName}'; "
                + "eg. @Named(\"Customer\") is considered invalid, "
                + "whereas @Named(\"sales.Customer\") is valid."),
        DOMAIN_SERVICE_MISSING_A_NAMESPACE("${type}: the service type must declare a namespace, "
                + "yet there was none found in '${logicalTypeName}'; "
                + "Spring supports various naming strategies @Named(...) being one of them, "
                + "where eg. @Named(\"CustomerService\") is considered invalid, "
                + "whereas @Named(\"sales.CustomerService\") is valid."),
        TYPE_NOT_EAGERLY_DISCOVERED("The metamodel is configured for FULL introspection mode, "
                + "yet missed ${type} of sort ${beanSort} during application start. " +
                "This happens when type ${type} is not eagerly discovered by the metamodel introspection, "
                + "which (initially) only considers compile-time types via reflection. "
                + "Run-time types, "
                + "not explicitly referenced to be included with Spring's class discovery mechanism, "
                + "might slip this process. "
                + "Consider importing type ${type} with Spring's @Import annotation. "
                + "Types of sort VALUE should instead register a ValueSemanticsProvider with Spring, "
                + "to be properly understood by the framework."),
        NON_UNIQUE_LOGICAL_TYPE_NAME_OR_ALIAS("Logical type name (or alias) ${logicalTypeName} "
                + "mapped to multiple non-abstract classes:\n"
                + "${csv}"),
        UNKNONW_SORT_WITH_ACTION("${type}: is a (concrete) but UNKNOWN sort, yet has ${actionCount} actions: ${actions}"),
        ACTION_METHOD_OVERLOADING_NOT_ALLOWED("Action method overloading is not allowed, "
                + "yet ${type} has action(s) that have a the same member name: ${overloadedNames}"),
        ;

        private final String template;

        public ViolationBuilder builder() {
            return new ViolationBuilder(this);
        }
        @RequiredArgsConstructor
        public static class ViolationBuilder {
            private final Violation violaton;
            private final Map<String, String> vars = new HashMap<>();
            public ViolationBuilder addVariable(final String name, final String value) {
                vars.put(name, value);
                return this;
            }
            public ViolationBuilder addVariable(final String name, final Number value) {
                vars.put(name, ""+value);
                return this;
            }
            public ViolationBuilder addVariablesFor(final Identifier featureIdentifier) {
                addVariable("type", featureIdentifier.getLogicalType().getClassName());
                addVariable("member", featureIdentifier.getMemberLogicalName());
                return this;
            }
            public String buildMessage() {
                return processMessageTemplate(violaton.template, vars);
            }
        }
    }

    /**
     * violation of view-model contract should be covered by meta-model validation
     */
    public static enum ViewmodelConstructor {
        PUBLIC_WITH_INJECT_SEMANTICS {
            @Override public <T> Stream<Constructor<T>> streamAll(final Class<T> cls) {
                return Try.call(()->
                    _ClassCache.getInstance()
                        .streamPublicConstructorsWithInjectSemantics(cls))
                        .getValue()
                        .orElse(Stream.empty());
            }
        },
        PUBLIC_ANY {
            @Override public <T> Stream<Constructor<T>> streamAll(final Class<T> cls) {
                return Try.call(()->
                    _ClassCache.getInstance()
                        .streamPublicConstructors(cls))
                        .getValue()
                        .orElse(Stream.empty());
            }
        };
        public <T> Can<Constructor<T>> getAll(final Class<T> cls) {
            return streamAll(cls).collect(Can.toCan());
        }
        public <T> Optional<Constructor<T>> getFirst(final Class<T> cls) {
            return streamAll(cls).findFirst();
        }
        public abstract <T> Stream<Constructor<T>> streamAll(Class<T> cls);

    }

    /**
     * Supported collection types, including arrays.
     * Order matters, as class substitution is processed on first matching type.
     * <p>
     * Non scalar <i>Action Parameter</i> types cannot be more special than what we offer here.
     */
    @RequiredArgsConstructor
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static enum CollectionSemantics {
        ARRAY(Array.class){
            @Override public Object asContainerType(
                    final Class<?> elementType, final @NonNull List<?> nonScalar) {
                return _Arrays.toArray(_Casts.uncheckedCast(nonScalar), elementType);
            }
        },
        @Deprecated
        VECTOR(Vector.class){
            @Override public Object asContainerType(
                    final Class<?> elementType, final @NonNull List<?> nonScalar) {
                return new Vector(nonScalar);
            }
        },
        LIST(List.class){
            @Override public Object asContainerType(
                    final Class<?> elementType, final @NonNull List<?> nonScalar) {
                return Collections.unmodifiableList(nonScalar);
            }
        },
        SORTED_SET(SortedSet.class){
            @Override public Object asContainerType(
                    final Class<?> elementType, final @NonNull List<?> nonScalar) {
                return _Collections.asUnmodifiableSortedSet(nonScalar);
            }
        },
        SET(Set.class){
            @Override public Object asContainerType(
                    final Class<?> elementType, final @NonNull List<?> nonScalar) {
                return _Collections.asUnmodifiableSet(nonScalar);
            }
        },
        COLLECTION(Collection.class){
            @Override public Object asContainerType(
                    final Class<?> elementType, final @NonNull List<?> nonScalar) {
                return Collections.unmodifiableCollection(nonScalar);
            }
        },
        CAN(Can.class){
            @Override public Object asContainerType(
                    final Class<?> elementType, final @NonNull List<?> nonScalar) {
                return Can.ofCollection(nonScalar);
            }
        },
        IMMUTABLE_COLLECTION(ImmutableCollection.class){
            @Override public Object asContainerType(
                    final Class<?> elementType, final @NonNull List<?> nonScalar) {
                return CAN.asContainerType(elementType, nonScalar);
            }
        }
        ;
        public boolean isArray() {return this == ARRAY;}
        public boolean isVector() {return this == VECTOR;}
        public boolean isList() {return this == LIST;}
        public boolean isSortedSet() {return this == SORTED_SET;}
        public boolean isSet() {return this == SET;}
        public boolean isCollection() {return this == COLLECTION;}
        public boolean isCan() {return this == CAN;}
        public boolean isImmutableCollection() {return this == IMMUTABLE_COLLECTION;}
        //
        public boolean isSetAny() {return isSet() || isSortedSet(); }
        @Getter private final Class<?> containerType;
        private static final ImmutableEnumSet<CollectionSemantics> all =
                ImmutableEnumSet.allOf(CollectionSemantics.class);
        @Getter @Accessors(fluent = true)
        private static final ImmutableEnumSet<CollectionSemantics> typeSubstitutors = all.remove(ARRAY);
        public static Optional<CollectionSemantics> valueOf(final @Nullable Class<?> type) {
            if(type==null) return Optional.empty();
            return type.isArray()
                    ? Optional.of(CollectionSemantics.ARRAY)
                    : all.stream()
                        .filter(collType->collType.getContainerType().isAssignableFrom(type))
                        .findFirst();
        }
        public Object unmodifiableCopyOf(
                final Class<?> elementType, final @NonNull Iterable<?> nonScalar) {
            // defensive copy
            return asContainerType(elementType,
                    _NullSafe.stream(nonScalar).collect(Collectors.toList()));
        }
        protected abstract Object asContainerType(
                final Class<?> elementType, final @NonNull List<?> nonScalar);
    }

    //TODO perhaps needs an update to reflect Java 7->11 Language changes
    @RequiredArgsConstructor
    public static enum WrapperFactoryProxy {
        COLLECTION(
                // intercepted ...
                List.of(
                        getMethod(Collection.class, "contains", Object.class),
                        getMethod(Collection.class, "size"),
                        getMethod(Collection.class, "isEmpty")
                ),
                // vetoed ...
                List.of(
                        getMethod(Collection.class, "add", Object.class),
                        getMethod(Collection.class, "remove", Object.class),
                        getMethod(Collection.class, "addAll", Collection.class),
                        getMethod(Collection.class, "removeAll", Collection.class),
                        getMethod(Collection.class, "retainAll", Collection.class),
                        getMethod(Collection.class, "clear")
                )),
        LIST(
                // intercepted ...
                _Lists.concat(
                        COLLECTION.intercepted,
                        List.of(
                                getMethod(List.class, "get", int.class)
                        )
                ),
                // vetoed ...
                _Lists.concat(
                        COLLECTION.vetoed,
                        List.of(
                        )
                )),
        MAP(
                // intercepted ...
                List.of(
                        getMethod(Map.class, "containsKey", Object.class),
                        getMethod(Map.class, "containsValue", Object.class),
                        getMethod(Map.class, "size"),
                        getMethod(Map.class, "isEmpty")
                ),
                // vetoed ...
                List.of(
                        getMethod(Map.class, "put", Object.class, Object.class),
                        getMethod(Map.class, "remove", Object.class),
                        getMethod(Map.class, "putAll", Map.class),
                        getMethod(Map.class, "clear")
                ))
        ;
        @Getter private final List<Method> intercepted;
        @Getter private final List<Method> vetoed;
        // -- HELPER
        @SneakyThrows
        private static Method getMethod(
                final Class<?> cls,
                final String methodName,
                final Class<?>... parameterClass) {
            return cls.getMethod(methodName, parameterClass);
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

    private static String processMessageTemplate(
            final String template,
            final Map<String, String> templateVars) {

        val templateRef = _Refs.stringRef(template);
        templateVars.forEach((k, v)->templateRef.update(str->str.replace("${" + k + "}", v)));
        return templateRef.getValue();
    }


}
