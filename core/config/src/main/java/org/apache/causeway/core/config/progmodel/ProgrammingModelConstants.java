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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ObjectLifecycle;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.commons.semantics.AccessorSemantics;

import static org.apache.causeway.commons.internal.reflection._Reflect.predicates.paramAssignableFrom;
import static org.apache.causeway.commons.internal.reflection._Reflect.predicates.paramCount;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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

        public static boolean anyMatchOn(final ResolvedMethod method) {
            for(MethodIncludeMarker includeMarker : MethodIncludeMarker.values()) {
                if(_Annotations.synthesize(method.method(), includeMarker.getAnnotationType()).isPresent()) {
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

        public static boolean anyMatchOn(final ResolvedMethod method) {
            for(MethodExcludeMarker excludeMarker : MethodExcludeMarker.values()) {
                if(_Annotations.synthesize(method.method(), excludeMarker.getAnnotationType()).isPresent()) {
                    return true;
                }
            }
            return false;
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
            var mixinContructors = _Reflect
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
        TRANSLATABLE(String.class, TranslatableString.class),
        FONTAWESOME_LAYERS(FontAwesomeLayers.class);
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
        SINGULAR(Can::ofSingleton),
        PLURAL(ReturnTypeCategory::nonScalar),
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
        ICON_FA_LAYERS(ReturnTypeCategory.FONTAWESOME_LAYERS, "iconFaLayers"),
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
        DEFAULT(ReturnTypePattern.SINGULAR, "default"),
        CHOICES(ReturnTypePattern.PLURAL, "choices"),
        AUTO_COMPLETE(ReturnTypePattern.PLURAL, "autoComplete"),
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
            String nameFor(final MethodFacade actionMethod, final String prefix, final boolean isMixin) {
                return prefix + _Strings.capitalize(actionMethod.getName());
            }
        },
        /** eg. hide() */
        PREFIX_ONLY {
            @Override @Nullable
            String nameFor(final MethodFacade actionMethod, final String prefix, final boolean isMixin) {
                return isMixin
                        // prefix-only notation is restricted to mixins
                        ? prefix
                        : null;
            }
        };
        abstract @Nullable String nameFor(MethodFacade actionMethod, String prefix, boolean isMixin);
        public static Can<String> namesFor(final MethodFacade actionMethod, final String prefix, final boolean isMixin) {
            return Stream.of(ActionSupportNaming.values())
                    .map(naming->naming.nameFor(actionMethod, prefix, isMixin))
                    .collect(Can.toCan());
        }
    }

    public static enum ParameterSupportNaming {
        /** eg. hide2Act(..) */
        PREFIX_PARAM_INDEX_ACTION_NAME {
            @Override @Nullable
            String nameFor(final MethodFacade actionMethod, final String prefix, final boolean isMixin, final int paramNum) {
                return prefix + paramNum + _Strings.capitalize(actionMethod.getName());
            }
        },
        /** eg. hideEmail() .. where email is the referenced parameter's name */
        PREFIXED_PARAM_NAME {
            @Override @Nullable
            String nameFor(final MethodFacade actionMethod, final String prefix, final boolean isMixin, final int paramNum) {
                return isMixin
                        // no-action-name-reference notation is restricted to mixins
                        ? prefix + _Strings.capitalize(actionMethod.getParameterName(paramNum))
                        : null;
            }
        };
        abstract @Nullable String nameFor(MethodFacade actionMethod, String prefix, boolean isMixin, int paramNum);
        public static Can<IntFunction<String>> namesFor(final MethodFacade actionMethod, final String prefix, final boolean isMixin) {
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
            String nameFor(final MethodFacade member, final String prefix, final boolean isMixin) {
                return prefix + getCapitalizedMemberName(member.asMethodForIntrospection().method());
            }
        },
        /** eg. hide() */
        PREFIX_ONLY {
            @Override @Nullable
            String nameFor(final MethodFacade member, final String prefix, final boolean isMixin) {
                return isMixin
                        // prefix-only notation is restricted to mixins
                        ? prefix
                        : null;
            }
        };
        abstract @Nullable String nameFor(MethodFacade member, String prefix, boolean isMixin);
        public static Can<String> namesFor(final MethodFacade member, final String prefix, final boolean isMixin) {
            return Stream.of(MemberSupportNaming.values())
                    .map(naming->naming.nameFor(member, prefix, isMixin))
                    .collect(Can.toCan());
        }
    }

    //maybe gradually consolidate all MM validation raisers here
    @RequiredArgsConstructor
    public static enum MessageTemplate {
        NOT_AUTHORIZED_TO_EDIT_OR_USE("Not authorized to edit or use."),
        NOT_AUTHORIZED_TO_EDIT_OR_USE_MEMBER("Not authorized to edit or use ${member}."),
        NOT_AUTHORIZED_TO_EDIT_OR_USE_FEATURE("Not authorized to edit or use feature ${type}#${member}."),
        CONFLICTING_TITLE_STRATEGIES(
                "${type} has title() method with @Title annotation, which is not allowed; "
                + "consider either removing the @Title annotation or renaming the method"),
        CONFLICTING_OPTIONALITY(
                "${member} has conflicting optionality semantics; facets involved are:\n"
                + "${conflictingFacets}"),
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
        LOGICAL_TYPE_NAME_IS_NOT_EXPLICIT("The object type ${type} of sort ${beanSort} "
                + "must be specified explicitly "
                + "('${configProperty}' config property). "
                + "Defaulting the object type from the package/class/package name can lead "
                + "to data migration issues for apps deployed to production (if the class is "
                + "subsequently refactored). "
                + "Use @Discriminator, @Named or "
                + "@PersistenceCapable(schema=...) to specify explicitly."),
        NON_UNIQUE_LOGICAL_TYPE_NAME_OR_ALIAS("Logical type name (or alias) ${logicalTypeName} "
                + "mapped to multiple non-abstract classes:\n"
                + "${csv}"),
        PROXIED_SERVICE_BEAN_NOT_ALLOWED_TO_CONTRIBUTE("Logical type name (or alias) ${logicalTypeName} "
                + "mapped to at least one proxied class:\n"
                + "${csv}. Proxied service beans are not allowed to contribute actions to the UI or Web-API(s). "
                + "E.g. don't mix @Transactional with @DomainService"),
        UNKNONW_SORT_WITH_ACTION("${type}: is a (concrete) but UNKNOWN sort, yet has ${actionCount} actions: ${actions}"),
        ACTION_METHOD_OVERLOADING_NOT_ALLOWED("Action method overloading is not allowed, "
                + "yet ${type} has action(s) that have a the same member name: ${overloadedNames}"),
        ACTION_METHOD_RETURNING_TRANSIENT_ENTITY_NOT_ALLOWED("Action methods are not allowed to return transient entities, "
                + "yet ${type} has action ${memberId}, which did return a transient entity of type ${returnTypeSpec}. "
                + "To correct this issue either persist the entity within above method before returning it, "
                + "or don't have the method invocation managed by the framework, e.g. "
                + "mark the method @Programmatic."),
        ACTION_METHOD_RETURNING_NON_BOOKMARKABLE_OBJECT_NOT_ALLOWED("Action methods are not allowed to return objects, "
                + "for which no bookmark can be created, "
                + "yet ${type} has action ${memberId}, which did return such an object of type ${returnTypeSpec}."),
        PARAMETER_HAS_NO_CHOICES_NOR_AUTOCOMPLETE("${paramId} has no choices nor autoComplete, "
                + "yet represents a domain-object or is a plural."),
        PARAMETER_TUPLE_INVALID_USE_OF_ANNOTATION("${type}#${member}: "
                + "Can use @ParameterTuple only on parameter of a single arg action."),
        PARAMETER_TUPLE_TYPE_WITH_AMBIGUOUS_CONSTRUCTORS("${type}#${member}: "
                + "Tuple type ${patType} referenced by @ParameterTuple annotated parameter has no or more than one public constructor."),
        INVALID_MEMBER_ELEMENT_TYPE("${type}: has a member with vetoed, mixin or managed "
                + "element-type ${elementType}, which is not allowed; (allowed types are abstract, value, viewmodel and entity)"),
        MEMBER_ID_CLASH("${type}: has members using the same member-id "
                + "'${memberId}', which is not allowed; clashes:\n\t[1]${member1}\n\t[2]${member2}"),
        AMBIGUOUS_MIXIN_ANNOTATIONS("Annotation ${annot} on both method and type level is not allowed, "
                + "it must be one or the other. Found with mixin: ${mixinType}"),
        INVALID_MIXIN_TYPE("Mixin ${type} could not be identified as action, property or collection."),
        INVALID_MIXIN_MAIN("Mixin ${type} does declare method name '${expectedMethodName}' as"
                + " the mixin main method to use,"
                + " but introspection did pick up method '${actualMethodName}' instead."),
        INVALID_MIXIN_SORT("Mixin ${type} is declared as contributing '${expectedContributing}'"
                + " but introspection did pick it up as '${actualContributing}' instead."),
        INVALID_USE_OF_VALIDATION_SUPPORT_METHOD("Validation support method "
                + "for member '${memberName}' in class '${className}' "
                + "was returning an empty string, which is invalid use of Apache Causeway's programming model. "
                + "In case a successful validation was intended, "
                + "the method should return null instead of an empty string. "
                + "(Please inform your developers!)"),
        ;

        private final String template;

        public ViolationBuilder builder() {
            return new ViolationBuilder(this);
        }
        @RequiredArgsConstructor
        public static class ViolationBuilder {
            private final MessageTemplate violaton;
            private final Map<String, String> vars = new HashMap<>();
            public ViolationBuilder addVariable(final String name, final String value) {
                vars.put(name, value);
                return this;
            }
            public ViolationBuilder addVariable(final String name, final Number value) {
                vars.put(name, ""+value);
                return this;
            }
            /**
             * Populates 'type' and 'member' keys (for template variable resolution).
             */
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
            @Override public Stream<ResolvedConstructor> streamAll(final Class<?> cls) {
                return Try.call(()->
                    _ClassCache.getInstance()
                        .streamPublicConstructorsWithInjectSemantics(cls))
                        .getValue()
                        .orElse(Stream.empty());
            }
        },
        PUBLIC_ANY {
            @Override public Stream<ResolvedConstructor> streamAll(final Class<?> cls) {
                return Try.call(()->
                    _ClassCache.getInstance()
                        .streamPublicConstructors(cls))
                        .getValue()
                        .orElse(Stream.empty());
            }
        };
        public Can<ResolvedConstructor> getAll(final Class<?> cls) {
            return streamAll(cls).collect(Can.toCan());
        }
        public Optional<ResolvedConstructor> getFirst(final Class<?> cls) {
            return streamAll(cls).findFirst();
        }
        public abstract Stream<ResolvedConstructor> streamAll(Class<?> cls);

    }

    // -- HELPER

    private static String getCapitalizedMemberName(final Member member) {
        if(member instanceof Method) {
            var method = (Method)member;
            var methodName = method.getName();
            if(method.getParameterCount()>0
                    || method.getReturnType().equals(void.class)
                    || !AccessorSemantics.isCandidateGetterName(methodName)) {
                // definitely an action not a getter
                return _Strings.capitalize(methodName);
            }
            // must be a getter
            return _Strings.baseName(methodName);
        }
        // must be a field then
        return _Strings.capitalize(member.getName());
    }

    private static String processMessageTemplate(
            final String template,
            final Map<String, String> templateVars) {

        var templateRef = _Refs.stringRef(template);
        templateVars.forEach((k, v)->templateRef.update(str->str.replace("${" + k + "}", v)));
        return templateRef.getValue();
    }

}
