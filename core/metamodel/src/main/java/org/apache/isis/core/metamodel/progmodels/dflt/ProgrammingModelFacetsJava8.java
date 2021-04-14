/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */

package org.apache.isis.core.metamodel.progmodels.dflt;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.authorization.standard.AuthorizationFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.action.ActionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.action.ActionAnnotationShouldEnforceTypeToBeIncludedWithMetamodel;
import org.apache.isis.core.metamodel.facets.actions.action.ActionChoicesForCollectionParameterFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.contributing.derived.ContributingFacetDerivedFromMixinFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.defaults.method.ActionDefaultsFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.actions.homepage.annotation.HomePageFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.derived.NotInServiceMenuFacetDerivedFromDomainServiceFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.all.i18n.TranslationFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.accessor.CollectionAccessorFacetViaAccessorFactory;
import org.apache.isis.core.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.javautilcollection.CollectionFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.parented.ParentedFacetSinceCollectionFactory;
import org.apache.isis.core.metamodel.facets.collections.sortedby.annotation.SortedByFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.jaxb.JaxbFacetFactory;
import org.apache.isis.core.metamodel.facets.members.cssclass.annotprop.CssClassFacetOnActionFromConfiguredRegexFactory;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop.CssClassFaFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.hidden.method.HideForContextFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.object.ViewModelSemanticCheckingFacetFactory;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.bookmarkable.BookmarkPolicyFacetFallbackFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistCallbackViaSaveMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemoveCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdateCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.choices.enums.EnumFacetUsingValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.object.cssclass.method.CssClassFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.object.defaults.annotcfg.DefaultedFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.facets.object.disabled.method.DisabledObjectFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.DomainObjectLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainservice.annotation.DomainServiceFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.facets.annotation.FacetsFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacetFactory;
import org.apache.isis.core.metamodel.facets.object.hidden.method.HiddenObjectFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.object.icon.method.IconFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.annotation.RemoveAnnotatedMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.IteratorFilteringFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.navparent.annotation.NavigableParentAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetDerivedFromClassNameFactory;
import org.apache.isis.core.metamodel.facets.object.objectvalidprops.impl.ObjectValidPropertiesFacetImplFactory;
import org.apache.isis.core.metamodel.facets.object.recreatable.RecreatableObjectFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.methods.TitleFacetViaMethodsFactory;
import org.apache.isis.core.metamodel.facets.object.validating.validateobject.method.ValidateObjectFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.object.value.annotcfg.ValueFacetAnnotationOrConfigurationFactory;
import org.apache.isis.core.metamodel.facets.param.autocomplete.method.ActionParameterAutoCompleteFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.bigdecimal.javaxvaldigits.BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.choices.method.ActionChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.disable.method.ActionParameterDisabledFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.hide.method.ActionParameterHiddenFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.layout.ParameterLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.param.mandatory.dflt.MandatoryFacetOnParametersDefaultFactory;
import org.apache.isis.core.metamodel.facets.param.name.ParameterNameFacetFactoryUsingReflection;
import org.apache.isis.core.metamodel.facets.param.parameter.ParameterAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.param.validate.method.ActionParameterValidationFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.accessor.PropertyAccessorFacetViaAccessorFactory;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.method.PropertyAutoCompleteFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits.BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory;
import org.apache.isis.core.metamodel.facets.properties.choices.method.PropertyChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.method.PropertyDefaultFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.disabled.inferred.DisabledFacetOnPropertyInferredFactory;
import org.apache.isis.core.metamodel.facets.properties.mandatory.dflt.MandatoryFacetOnProperyDefaultFactory;
import org.apache.isis.core.metamodel.facets.properties.property.PropertyAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.PropertyLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.update.PropertySetterFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.validating.dflt.PropertyValidateFacetDefaultFactory;
import org.apache.isis.core.metamodel.facets.properties.validating.method.PropertyValidateFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.biginteger.BigIntegerValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.blobs.BlobValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.booleans.BooleanPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.booleans.BooleanWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.bytes.BytePrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.bytes.ByteWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.chars.CharPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.chars.CharWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.clobs.ClobValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datejodalocal.JodaLocalDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datesql.JavaSqlDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datetimejoda.JodaDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datetimejodalocal.JodaLocalDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.dateutil.JavaUtilDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.doubles.DoublePrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.doubles.DoubleWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.floats.FloatPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.floats.FloatWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.imageawt.JavaAwtImageValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.integer.IntPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.integer.IntWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.localrespath.LocalResourcePathValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.longs.LongPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.longs.LongWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.password.PasswordValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.shortint.ShortPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.shortint.ShortWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.string.StringValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.temporal.localdate.LocalDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.temporal.localdatetime.LocalDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.temporal.localtime.LocalTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.temporal.offsetdatetime.OffsetDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.temporal.offsettime.OffsetTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.temporal.zoneddatetime.ZonedDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.timejodalocal.JodaLocalTimeValueFacetSimpleFactory;
import org.apache.isis.core.metamodel.facets.value.timesql.JavaSqlTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.timestampsql.JavaSqlTimeStampValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.url.URLValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.uuid.UUIDValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.methods.OrphanedSupportingMethodValidator;
import org.apache.isis.core.metamodel.postprocessors.param.DeriveFacetsPostProcessor;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.metamodel.services.title.TitlesAndTranslationsValidator;

public final class ProgrammingModelFacetsJava8 extends ProgrammingModelAbstract {

    public ProgrammingModelFacetsJava8(ServiceInjector serviceInjector) {
        super(serviceInjector);

        // must be first, so any Facets created can be replaced by other
        // FacetFactorys later.
        addFactory(FacetProcessingOrder.A1_FALLBACK_DEFAULTS, FallbackFacetFactory.class);

        addFactory(FacetProcessingOrder.B1_OBJECT_NAMING, ObjectSpecIdFacetDerivedFromClassNameFactory.class);
        addFactory(FacetProcessingOrder.B1_OBJECT_NAMING, DomainServiceFacetAnnotationFactory.class);

        addFactory(FacetProcessingOrder.C1_METHOD_REMOVING, IteratorFilteringFacetFactory.class);

        addFactory(FacetProcessingOrder.C1_METHOD_REMOVING, RemoveMethodsFacetFactory.class);

        addFactory(FacetProcessingOrder.C1_METHOD_REMOVING, RemoveAnnotatedMethodsFacetFactory.class);

        // must be before any other FacetFactories that install MandatoryFacet.class facets
        addFactory(FacetProcessingOrder.D1_MANDATORY_SUPPORT, MandatoryFacetOnProperyDefaultFactory.class);
        addFactory(FacetProcessingOrder.D1_MANDATORY_SUPPORT, MandatoryFacetOnParametersDefaultFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyValidateFacetDefaultFactory.class);

        // enum support
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, EnumFacetUsingValueFacetUsingSemanticsProviderFactory.class);

        // properties
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyAccessorFacetViaAccessorFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertySetterFacetFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyValidateFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyChoicesFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyAutoCompleteFacetMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyDefaultFacetViaMethodFactory.class);

        // collections
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, CollectionAccessorFacetViaAccessorFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, SortedByFacetAnnotationFactory.class);

        // actions
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterHiddenFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterDisabledFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionValidationFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterValidationFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionChoicesFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterChoicesFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterAutoCompleteFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionDefaultsFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterDefaultsFacetViaMethodFactory.class);

        // members in general
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, DisableForContextFacetViaMethodFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, HideForContextFacetViaMethodFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, CreatedCallbackFacetFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, LoadCallbackFacetFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PersistCallbackViaSaveMethodFacetFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PersistCallbackFacetFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, UpdateCallbackFacetFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, RemoveCallbackFacetFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ValidateObjectFacetMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ObjectValidPropertiesFacetImplFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, BookmarkPolicyFacetFallbackFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, HomePageFacetAnnotationFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, DefaultedFacetAnnotationElseConfigurationFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, DescribedAsFacetOnMemberFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, NotInServiceMenuFacetDerivedFromDomainServiceFacetFactory.class);


        // must come after CssClassFacetOnMemberFactory
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, CssClassFacetOnActionFromConfiguredRegexFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, CssClassFaFacetOnMemberFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, HiddenObjectFacetViaMethodFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, DisabledObjectFacetViaMethodFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, RecreatableObjectFacetFactory.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, JaxbFacetFactory.class);

        // must come after RecreatableObjectFacetFactory
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, DomainObjectAnnotationFacetFactory.class);

        // must come after the property/collection accessor+mutator facet factories
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionAnnotationFacetFactory.class);
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyAnnotationFacetFactory.class);
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, CollectionAnnotationFacetFactory.class);

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ParameterNameFacetFactoryUsingReflection.class);
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ParameterAnnotationFacetFactory.class);

        // must come after DomainObjectAnnotationFacetFactory & MixinFacetFactory
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, ContributingFacetDerivedFromMixinFacetFactory.class);

        addFactory(FacetProcessingOrder.F1_LAYOUT, GridFacetFactory.class);

        // must come before DomainObjectLayoutFacetFactory
        // (so subscribers on titleUi event etc can override)
        addFactory(FacetProcessingOrder.F1_LAYOUT, TitleAnnotationFacetFactory.class);
        addFactory(FacetProcessingOrder.F1_LAYOUT, TitleFacetViaMethodsFactory.class);
        addFactory(FacetProcessingOrder.F1_LAYOUT, IconFacetMethodFactory.class);
        addFactory(FacetProcessingOrder.F1_LAYOUT, NavigableParentAnnotationFacetFactory.class);
        addFactory(FacetProcessingOrder.F1_LAYOUT, CssClassFacetMethodFactory.class);
        addFactory(FacetProcessingOrder.F1_LAYOUT, LayoutFacetFactory.class);

        addFactory(FacetProcessingOrder.F1_LAYOUT, DomainServiceLayoutFacetFactory.class);
        addFactory(FacetProcessingOrder.F1_LAYOUT, DomainObjectLayoutFacetFactory.class);

        // must come after MultiLine
        addFactory(FacetProcessingOrder.F1_LAYOUT, PropertyLayoutFacetFactory.class);
        addFactory(FacetProcessingOrder.F1_LAYOUT, ParameterLayoutFacetFactory.class);
        addFactory(FacetProcessingOrder.F1_LAYOUT, ActionLayoutFacetFactory.class);
        addFactory(FacetProcessingOrder.F1_LAYOUT, CollectionLayoutFacetFactory.class);


        // built-in value types for Java language
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, BooleanPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, BooleanWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, BytePrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, ByteWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, ShortPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, ShortWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, IntPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, IntWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, LongPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, LongWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, FloatPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, FloatWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, DoublePrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, DoubleWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, CharPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, CharWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, BigIntegerValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, BigDecimalValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, JavaSqlDateValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, JavaSqlTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, JavaSqlTimeStampValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, JavaUtilDateValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, StringValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, URLValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, LocalResourcePathValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, UUIDValueFacetUsingSemanticsProviderFactory.class);

        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, JavaAwtImageValueFacetUsingSemanticsProviderFactory.class);

        // applib values
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, BlobValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, ClobValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, PasswordValueFacetUsingSemanticsProviderFactory.class);

        // jodatime values
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, JodaLocalDateValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, JodaLocalDateTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, JodaDateTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, JodaLocalTimeValueFacetSimpleFactory.class);

        // java 8 time values
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, LocalTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, OffsetTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, LocalDateValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, LocalDateTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, OffsetDateTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FacetProcessingOrder.G1_VALUE_TYPES, ZonedDateTimeValueFacetUsingSemanticsProviderFactory.class);

        addFactory(FacetProcessingOrder.Z0_BEFORE_FINALLY, AuthorizationFacetFactory.class);

        // written to not trample over TypeOf if already installed
        addFactory(FacetProcessingOrder.Z1_FINALLY, CollectionFacetFactory.class);
        // must come after CollectionFacetFactory
        addFactory(FacetProcessingOrder.Z1_FINALLY, ParentedFacetSinceCollectionFactory.class);

        // so we can dogfood the applib "value" types
        addFactory(FacetProcessingOrder.Z1_FINALLY, ValueFacetAnnotationOrConfigurationFactory.class);


        // should come near the end, after any facets that install PropertySetterFacet have run.
        addFactory(FacetProcessingOrder.Z1_FINALLY, DisabledFacetOnPropertyInferredFactory.class);

        addFactory(FacetProcessingOrder.Z1_FINALLY, ActionChoicesForCollectionParameterFacetFactory.class);

        addFactory(FacetProcessingOrder.Z1_FINALLY, FacetsFacetAnnotationFactory.class);

        // must be after all named facets and description facets have been installed
        addFactory(FacetProcessingOrder.Z1_FINALLY, TranslationFacetFactory.class);

        addFactory(FacetProcessingOrder.Z1_FINALLY, ViewModelSemanticCheckingFacetFactory.class);

        addPostProcessor(PostProcessingOrder.A1_BUILTIN, DeriveFacetsPostProcessor.class);
        
        addValidator(new OrphanedSupportingMethodValidator());
        addValidator(new TitlesAndTranslationsValidator());
        addValidator(new ActionAnnotationShouldEnforceTypeToBeIncludedWithMetamodel());
        

    }


}
