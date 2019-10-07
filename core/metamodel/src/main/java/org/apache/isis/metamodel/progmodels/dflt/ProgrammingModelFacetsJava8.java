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

package org.apache.isis.metamodel.progmodels.dflt;

import org.apache.isis.metamodel.facets.actions.action.ActionAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.actions.action.ActionChoicesForCollectionParameterFacetFactory;
import org.apache.isis.metamodel.facets.actions.defaults.method.ActionDefaultsFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.actions.homepage.annotation.HomePageFacetAnnotationFactory;
import org.apache.isis.metamodel.facets.actions.layout.ActionLayoutFacetFactory;
import org.apache.isis.metamodel.facets.actions.notcontributed.derived.NotContributedFacetDerivedFromDomainServiceFacetFactory;
import org.apache.isis.metamodel.facets.actions.notcontributed.derived.NotContributedFacetDerivedFromMixinFacetFactory;
import org.apache.isis.metamodel.facets.actions.notinservicemenu.derived.NotInServiceMenuFacetDerivedFromDomainServiceFacetFactory;
import org.apache.isis.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.all.i18n.TranslationFacetFactory;
import org.apache.isis.metamodel.facets.collections.accessor.CollectionAccessorFacetViaAccessorFactory;
import org.apache.isis.metamodel.facets.collections.clear.CollectionClearFacetFactory;
import org.apache.isis.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.collections.javautilcollection.CollectionFacetFactory;
import org.apache.isis.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.isis.metamodel.facets.collections.modify.CollectionAddToRemoveFromAndValidateFacetFactory;
import org.apache.isis.metamodel.facets.collections.parented.ParentedFacetSinceCollectionFactory;
import org.apache.isis.metamodel.facets.collections.sortedby.annotation.SortedByFacetAnnotationFactory;
import org.apache.isis.metamodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.metamodel.facets.jaxb.JaxbFacetFactory;
import org.apache.isis.metamodel.facets.members.cssclass.annotprop.CssClassFacetOnActionFromConfiguredRegexFactory;
import org.apache.isis.metamodel.facets.members.cssclassfa.annotprop.CssClassFaFacetOnMemberFactory;
import org.apache.isis.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberFactory;
import org.apache.isis.metamodel.facets.members.disabled.forsession.DisableForSessionFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.members.hidden.forsession.HideForSessionFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.members.hidden.method.HideForContextFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.members.order.annotprop.MemberOrderFacetFactory;
import org.apache.isis.metamodel.facets.object.ViewModelSemanticCheckingFacetFactory;
import org.apache.isis.metamodel.facets.object.bookmarkpolicy.bookmarkable.BookmarkPolicyFacetFallbackFactory;
import org.apache.isis.metamodel.facets.object.callbacks.CreatedCallbackFacetFactory;
import org.apache.isis.metamodel.facets.object.callbacks.LoadCallbackFacetFactory;
import org.apache.isis.metamodel.facets.object.callbacks.PersistCallbackFacetFactory;
import org.apache.isis.metamodel.facets.object.callbacks.PersistCallbackViaSaveMethodFacetFactory;
import org.apache.isis.metamodel.facets.object.callbacks.RemoveCallbackFacetFactory;
import org.apache.isis.metamodel.facets.object.callbacks.UpdateCallbackFacetFactory;
import org.apache.isis.metamodel.facets.object.choices.enums.EnumFacetUsingValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.object.cssclass.method.CssClassFacetMethodFactory;
import org.apache.isis.metamodel.facets.object.defaults.annotcfg.DefaultedFacetAnnotationElseConfigurationFactory;
import org.apache.isis.metamodel.facets.object.disabled.method.DisabledObjectFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.object.domainobjectlayout.DomainObjectLayoutFacetFactory;
import org.apache.isis.metamodel.facets.object.domainservice.annotation.DomainServiceFacetAnnotationFactory;
import org.apache.isis.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacetFactory;
import org.apache.isis.metamodel.facets.object.facets.annotation.FacetsFacetAnnotationFactory;
import org.apache.isis.metamodel.facets.object.grid.GridFacetFactory;
import org.apache.isis.metamodel.facets.object.hidden.method.HiddenObjectFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.object.icon.method.IconFacetMethodFactory;
import org.apache.isis.metamodel.facets.object.ignore.annotation.RemoveAnnotatedMethodsFacetFactory;
import org.apache.isis.metamodel.facets.object.ignore.isis.RemoveStaticGettersAndSettersFacetFactory;
import org.apache.isis.metamodel.facets.object.ignore.javalang.IteratorFilteringFacetFactory;
import org.apache.isis.metamodel.facets.object.ignore.javalang.RemoveMethodsFacetFactory;
import org.apache.isis.metamodel.facets.object.layout.LayoutFacetFactory;
import org.apache.isis.metamodel.facets.object.mixin.MixinFacetForMixinAnnotationFactory;
import org.apache.isis.metamodel.facets.object.navparent.annotation.NavigableParentAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetDerivedFromClassNameFactory;
import org.apache.isis.metamodel.facets.object.objectvalidprops.impl.ObjectValidPropertiesFacetImplFactory;
import org.apache.isis.metamodel.facets.object.recreatable.RecreatableObjectFacetFactory;
import org.apache.isis.metamodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.object.title.methods.TitleFacetViaMethodsFactory;
import org.apache.isis.metamodel.facets.object.validating.validateobject.method.ValidateObjectFacetMethodFactory;
import org.apache.isis.metamodel.facets.object.value.annotcfg.ValueFacetAnnotationOrConfigurationFactory;
import org.apache.isis.metamodel.facets.param.autocomplete.method.ActionParameterAutoCompleteFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.param.bigdecimal.javaxvaldigits.BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory;
import org.apache.isis.metamodel.facets.param.choices.method.ActionChoicesFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.param.disable.method.ActionParameterDisabledFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.param.hide.method.ActionParameterHiddenFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.param.layout.ParameterLayoutFacetFactory;
import org.apache.isis.metamodel.facets.param.mandatory.dflt.MandatoryFacetOnParametersDefaultFactory;
import org.apache.isis.metamodel.facets.param.name.ParameterNameFacetFactoryUsingReflection;
import org.apache.isis.metamodel.facets.param.parameter.ParameterAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.param.validate.method.ActionParameterValidationFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.properties.accessor.PropertyAccessorFacetViaAccessorFactory;
import org.apache.isis.metamodel.facets.properties.autocomplete.method.PropertyAutoCompleteFacetMethodFactory;
import org.apache.isis.metamodel.facets.properties.bigdecimal.javaxvaldigits.BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory;
import org.apache.isis.metamodel.facets.properties.choices.method.PropertyChoicesFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.properties.defaults.method.PropertyDefaultFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.properties.disabled.inferred.DisabledFacetOnPropertyInferredFactory;
import org.apache.isis.metamodel.facets.properties.mandatory.dflt.MandatoryFacetOnProperyDefaultFactory;
import org.apache.isis.metamodel.facets.properties.property.PropertyAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.properties.propertylayout.PropertyLayoutFacetFactory;
import org.apache.isis.metamodel.facets.properties.update.PropertyModifyFacetFactory;
import org.apache.isis.metamodel.facets.properties.update.PropertySetAndClearFacetFactory;
import org.apache.isis.metamodel.facets.properties.validating.dflt.PropertyValidateFacetDefaultFactory;
import org.apache.isis.metamodel.facets.properties.validating.method.PropertyValidateFacetViaMethodFactory;
import org.apache.isis.metamodel.facets.value.bigdecimal.BigDecimalValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.biginteger.BigIntegerValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.blobs.BlobValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.booleans.BooleanPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.booleans.BooleanWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.bytes.BytePrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.bytes.ByteWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.chars.CharPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.chars.CharWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.clobs.ClobValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.color.ColorValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.datejdk8local.Jdk8LocalDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.datejodalocal.JodaLocalDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.datesql.JavaSqlDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.datetimejdk8local.Jdk8LocalDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.datetimejdk8offset.Jdk8OffsetDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.datetimejoda.JodaDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.datetimejodalocal.JodaLocalDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.dateutil.JavaUtilDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.doubles.DoublePrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.doubles.DoubleWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.floats.FloatPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.floats.FloatWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.image.ImageValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.imageawt.JavaAwtImageValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.integer.IntPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.integer.IntWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.localrespath.LocalResourcePathValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.longs.LongPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.longs.LongWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.money.MoneyValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.password.PasswordValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.percentage.PercentageValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.shortint.ShortPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.shortint.ShortWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.string.StringValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.timejodalocal.JodaLocalTimeValueFacetSimpleFactory;
import org.apache.isis.metamodel.facets.value.timesql.JavaSqlTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.timestampsql.JavaSqlTimeStampValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.url.URLValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.facets.value.uuid.UUIDValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.metamodel.postprocessors.param.DeriveFacetsPostProcessor;
import org.apache.isis.metamodel.progmodel.ProgrammingModelAbstract;

public final class ProgrammingModelFacetsJava8 extends ProgrammingModelAbstract {

    public ProgrammingModelFacetsJava8() {
        
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, DeriveFacetsPostProcessor.class);

        // must be first, so any Facets created can be replaced by other
        // FacetFactorys later.
        add(FacetProcessingOrder.A1_FALLBACK_DEFAULTS, FallbackFacetFactory.class);
        
        add(FacetProcessingOrder.B1_OBJECT_NAMING, ObjectSpecIdFacetDerivedFromClassNameFactory.class);
        add(FacetProcessingOrder.B1_OBJECT_NAMING, DomainServiceFacetAnnotationFactory.class);

        add(FacetProcessingOrder.C1_METHOD_REMOVING, IteratorFilteringFacetFactory.class);

        add(FacetProcessingOrder.C1_METHOD_REMOVING, RemoveMethodsFacetFactory.class);

        add(FacetProcessingOrder.C1_METHOD_REMOVING, RemoveStaticGettersAndSettersFacetFactory.class, Marker.DEPRECATED);

        add(FacetProcessingOrder.C1_METHOD_REMOVING, RemoveAnnotatedMethodsFacetFactory.class);

        // must be before any other FacetFactories that install MandatoryFacet.class facets
        add(FacetProcessingOrder.D1_MANDATORY_SUPPORT, MandatoryFacetOnProperyDefaultFactory.class);
        add(FacetProcessingOrder.D1_MANDATORY_SUPPORT, MandatoryFacetOnParametersDefaultFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyValidateFacetDefaultFactory.class);

        // enum support
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, EnumFacetUsingValueFacetUsingSemanticsProviderFactory.class);

        // properties
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyAccessorFacetViaAccessorFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertySetAndClearFacetFactory.class);
        // must come after PropertySetAndClearFacetFactory (replaces setter facet with modify if need be)
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyModifyFacetFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyValidateFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyChoicesFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyAutoCompleteFacetMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyDefaultFacetViaMethodFactory.class);

        // collections
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, CollectionAccessorFacetViaAccessorFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, CollectionClearFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, CollectionAddToRemoveFromAndValidateFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, SortedByFacetAnnotationFactory.class);

        // actions
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterHiddenFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterDisabledFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionValidationFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterValidationFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionChoicesFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterChoicesFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterAutoCompleteFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionDefaultsFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionParameterDefaultsFacetViaMethodFactory.class);

        // members in general
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, DisableForSessionFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, DisableForContextFacetViaMethodFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, HideForSessionFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, HideForContextFacetViaMethodFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, CreatedCallbackFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, LoadCallbackFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PersistCallbackViaSaveMethodFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PersistCallbackFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, UpdateCallbackFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, RemoveCallbackFacetFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ValidateObjectFacetMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ObjectValidPropertiesFacetImplFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, MemberOrderFacetFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, BookmarkPolicyFacetFallbackFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, HomePageFacetAnnotationFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, DefaultedFacetAnnotationElseConfigurationFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, DescribedAsFacetOnMemberFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, NotContributedFacetDerivedFromDomainServiceFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, NotInServiceMenuFacetDerivedFromDomainServiceFacetFactory.class);


        // must come after CssClassFacetOnMemberFactory
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, CssClassFacetOnActionFromConfiguredRegexFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, CssClassFaFacetOnMemberFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, HiddenObjectFacetViaMethodFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, DisabledObjectFacetViaMethodFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, RecreatableObjectFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, JaxbFacetFactory.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, MixinFacetForMixinAnnotationFactory.class);


        // must come after RecreatableObjectFacetFactory
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, DomainObjectAnnotationFacetFactory.class);

        // must come after the property/collection accessor+mutator facet factories
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ActionAnnotationFacetFactory.class);
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, PropertyAnnotationFacetFactory.class);
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, CollectionAnnotationFacetFactory.class);

        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ParameterNameFacetFactoryUsingReflection.class);
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, ParameterAnnotationFacetFactory.class);

        // must come after DomainObjectAnnotationFacetFactory & MixinFacetFactory
        add(FacetProcessingOrder.E1_MEMBER_MODELLING, NotContributedFacetDerivedFromMixinFacetFactory.class);

        add(FacetProcessingOrder.F1_LAYOUT, GridFacetFactory.class);

        // must come before DomainObjectLayoutFacetFactory
        // (so subscribers on titleUi event etc can override)
        add(FacetProcessingOrder.F1_LAYOUT, TitleAnnotationFacetFactory.class);
        add(FacetProcessingOrder.F1_LAYOUT, TitleFacetViaMethodsFactory.class);
        add(FacetProcessingOrder.F1_LAYOUT, IconFacetMethodFactory.class);
        add(FacetProcessingOrder.F1_LAYOUT, NavigableParentAnnotationFacetFactory.class);
        add(FacetProcessingOrder.F1_LAYOUT, CssClassFacetMethodFactory.class);
        add(FacetProcessingOrder.F1_LAYOUT, LayoutFacetFactory.class);

        add(FacetProcessingOrder.F1_LAYOUT, DomainServiceLayoutFacetFactory.class);
        add(FacetProcessingOrder.F1_LAYOUT, DomainObjectLayoutFacetFactory.class);

        // must come after MultiLine
        add(FacetProcessingOrder.F1_LAYOUT, PropertyLayoutFacetFactory.class);
        add(FacetProcessingOrder.F1_LAYOUT, ParameterLayoutFacetFactory.class);
        add(FacetProcessingOrder.F1_LAYOUT, ActionLayoutFacetFactory.class);
        add(FacetProcessingOrder.F1_LAYOUT, CollectionLayoutFacetFactory.class);


        // built-in value types for Java language
        add(FacetProcessingOrder.G1_VALUE_TYPES, BooleanPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, BooleanWrapperValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, BytePrimitiveValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, ByteWrapperValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, ShortPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, ShortWrapperValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, IntPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, IntWrapperValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, LongPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, LongWrapperValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, FloatPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, FloatWrapperValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, DoublePrimitiveValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, DoubleWrapperValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, CharPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, CharWrapperValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, BigIntegerValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, BigDecimalValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, JavaSqlDateValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, JavaSqlTimeValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, JavaSqlTimeStampValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, JavaUtilDateValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, StringValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, URLValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, LocalResourcePathValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, UUIDValueFacetUsingSemanticsProviderFactory.class);

        add(FacetProcessingOrder.G1_VALUE_TYPES, JavaAwtImageValueFacetUsingSemanticsProviderFactory.class);

        // applib values
        add(FacetProcessingOrder.G1_VALUE_TYPES, BlobValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, ClobValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, ColorValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, MoneyValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, PasswordValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, PercentageValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, ImageValueFacetUsingSemanticsProviderFactory.class);

        // jodatime values
        add(FacetProcessingOrder.G1_VALUE_TYPES, JodaLocalDateValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, JodaLocalDateTimeValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, JodaDateTimeValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, JodaLocalTimeValueFacetSimpleFactory.class);

        // java 8 time values
        add(FacetProcessingOrder.G1_VALUE_TYPES, Jdk8LocalDateValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, Jdk8OffsetDateTimeValueFacetUsingSemanticsProviderFactory.class);
        add(FacetProcessingOrder.G1_VALUE_TYPES, Jdk8LocalDateTimeValueFacetUsingSemanticsProviderFactory.class);

        // written to not trample over TypeOf if already installed
        add(FacetProcessingOrder.Z1_FINALLY, CollectionFacetFactory.class);
        // must come after CollectionFacetFactory
        add(FacetProcessingOrder.Z1_FINALLY, ParentedFacetSinceCollectionFactory.class);

        // so we can dogfood the applib "value" types
        add(FacetProcessingOrder.Z1_FINALLY, ValueFacetAnnotationOrConfigurationFactory.class);


        // should come near the end, after any facets that install PropertySetterFacet have run.
        add(FacetProcessingOrder.Z1_FINALLY, DisabledFacetOnPropertyInferredFactory.class);

        add(FacetProcessingOrder.Z1_FINALLY, ActionChoicesForCollectionParameterFacetFactory.class);

        add(FacetProcessingOrder.Z1_FINALLY, FacetsFacetAnnotationFactory.class);

        // must be after all named facets and description facets have been installed
        add(FacetProcessingOrder.Z1_FINALLY, TranslationFacetFactory.class);

        add(FacetProcessingOrder.Z1_FINALLY, ViewModelSemanticCheckingFacetFactory.class);
       
        
    }


}
