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

package org.apache.isis.progmodels.dflt;

import org.apache.isis.core.metamodel.facets.actions.action.ActionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.contributing.maxlenannot.MaxLengthFacetOnActionAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.contributing.paged.PagedFacetOnActionFactory;
import org.apache.isis.core.metamodel.facets.actions.debug.annotation.DebugFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.defaults.method.ActionDefaultsFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.actions.exploration.annotation.ExplorationFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.homepage.annotation.HomePageFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.interaction.ActionNamedDebugExplorationFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.annotation.NotContributedFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.derived.NotContributedFacetDerivedFromDomainServiceFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.annotation.NotInServiceMenuFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.derived.NotInServiceMenuFacetDerivedFromDomainServiceFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.method.NotInServiceMenuFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.all.i18n.TranslationFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.accessor.CollectionAccessorFacetViaAccessorFactory;
import org.apache.isis.core.metamodel.facets.collections.clear.CollectionClearFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.disabled.fromimmutable.DisabledFacetOnCollectionDerivedFromImmutableFactory;
import org.apache.isis.core.metamodel.facets.collections.javautilcollection.CollectionFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToRemoveFromAndValidateFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.paged.PagedFacetOnCollectionFactory;
import org.apache.isis.core.metamodel.facets.collections.parented.ParentedFacetSinceCollectionFactory;
import org.apache.isis.core.metamodel.facets.collections.sortedby.annotation.SortedByFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.members.cssclass.annotprop.CssClassFacetOnActionFromConfiguredRegexFactory;
import org.apache.isis.core.metamodel.facets.members.cssclass.annotprop.CssClassFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop.CssClassFaFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.members.describedas.staticmethod.DescribedAsFacetStaticMethodFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.forsession.DisableForSessionFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.layout.DisabledFacetOnMemberFromPropertiesFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.staticmethod.DisabledFacetStaticMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.members.hidden.forsession.HideForSessionFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.hidden.layout.HiddenFacetOnMemberFromPropertiesFactory;
import org.apache.isis.core.metamodel.facets.members.hidden.method.HideForContextFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.hidden.staticmethod.HiddenFacetStaticMethodFactory;
import org.apache.isis.core.metamodel.facets.members.named.annotprop.NamedFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.members.named.staticmethod.NamedFacetStaticMethodFactory;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetFactory;
import org.apache.isis.core.metamodel.facets.members.render.annotprop.RenderFacetOrResolveFactory;
import org.apache.isis.core.metamodel.facets.object.ViewModelSemanticCheckingFacetFactory;
import org.apache.isis.core.metamodel.facets.object.actionorder.annotation.ActionOrderFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.audit.markerifc.AuditableFacetMarkerInterfaceFactory;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.bookmarkable.BookmarkPolicyFacetViaBookmarkableAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.create.CreatedCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.load.LoadCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.persist.PersistCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.persist.PersistCallbackViaSaveMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.remove.RemoveCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.update.UpdateCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.choices.boundedmarkerifc.ChoicesFacetFromBoundedMarkerInterfaceFactory;
import org.apache.isis.core.metamodel.facets.object.choices.enums.EnumFacetUsingValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.object.cssclass.annotation.CssClassFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.cssclass.method.CssClassFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.object.cssclassfa.annotation.CssClassFaFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.defaults.annotcfg.DefaultedFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.facets.object.describedas.annotation.DescribedAsFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.dirty.method.DirtyMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.disabled.method.DisabledObjectFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.DomainObjectLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainservice.annotation.DomainServiceFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFactory;
import org.apache.isis.core.metamodel.facets.object.encodeable.annotcfg.EncodableFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.facets.object.facets.annotation.FacetsFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.fieldorder.annotation.FieldOrderFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.hidden.annotation.HiddenFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.hidden.method.HiddenObjectFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.object.icon.method.IconFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.annotation.RemoveProgrammaticOrIgnoreAnnotationMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.datanucleus.RemoveDatanucleusPersistableTypesFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.datanucleus.RemoveDnPrefixedMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.isis.RemoveSetDomainObjectContainerMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.isis.RemoveStaticGettersAndSettersFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.IteratorFilteringFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveGetClassMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveInitMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveInjectMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveJavaLangComparableMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveJavaLangObjectMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveSuperclassMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveSyntheticOrAbstractMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.jdo.RemoveJdoEnhancementTypesFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.jdo.RemoveJdoPrefixedMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.immutable.immutableannot.CopyImmutableFacetOntoMembersFactory;
import org.apache.isis.core.metamodel.facets.object.immutable.immutablemarkerifc.ImmutableFacetMarkerInterfaceFactory;
import org.apache.isis.core.metamodel.facets.object.mask.annotation.MaskFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.maxlen.annotation.MaxLengthFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.membergroups.annotprop.MemberGroupLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.multiline.annotation.MultiLineFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.named.annotation.NamedFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.notpersistable.notpersistableannot.NotPersistableFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.notpersistable.notpersistablemarkerifc.NotPersistableFacetMarkerInterfaceFactory;
import org.apache.isis.core.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetDerivedFromClassNameFactory;
import org.apache.isis.core.metamodel.facets.object.objectvalidprops.impl.ObjectValidPropertiesFacetImplFactory;
import org.apache.isis.core.metamodel.facets.object.paged.annotation.PagedFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.parented.aggregated.ParentedFacetSinceAggregatedAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.parseable.annotcfg.ParseableFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.facets.object.plural.annotation.PluralAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.plural.staticmethod.PluralFacetStaticMethodFactory;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnCollectionDerivedFromViewModelFacetFactory;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory;
import org.apache.isis.core.metamodel.facets.object.recreatable.RecreatableObjectFacetFactory;
import org.apache.isis.core.metamodel.facets.object.regex.annotation.RegExFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.methods.TitleFacetViaMethodsFactory;
import org.apache.isis.core.metamodel.facets.object.typicallen.annotation.TypicalLengthFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.validating.mustsatisfyspec.MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.object.validating.validateobject.method.ValidateObjectFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.object.value.annotcfg.ValueFacetAnnotationOrConfigurationFactory;
import org.apache.isis.core.metamodel.facets.param.autocomplete.method.ActionParameterAutoCompleteFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.bigdecimal.javaxvaldigits.BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.choices.enums.ActionParameterChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.core.metamodel.facets.param.choices.method.ActionChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.describedas.annotderived.DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.param.layout.ParameterLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.param.mandatory.dflt.MandatoryFacetOnParametersDefaultFactory;
import org.apache.isis.core.metamodel.facets.param.multiline.annotation.MultiLineFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.named.annotation.NamedFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.parameter.ParameterAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.param.renderedasdaybefore.annotation.RenderedAsDayBeforeFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.typicallen.annotation.TypicalLengthFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.param.validating.maskannot.MaskFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.properties.accessor.PropertyAccessorFacetViaAccessorFactory;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.method.PropertyAutoCompleteFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits.BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory;
import org.apache.isis.core.metamodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.choices.method.PropertyChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.method.PropertyDefaultFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutableFactory;
import org.apache.isis.core.metamodel.facets.properties.disabled.inferred.DisabledFacetOnPropertyInferredFactory;
import org.apache.isis.core.metamodel.facets.properties.mandatory.dflt.MandatoryFacetOnProperyDefaultFactory;
import org.apache.isis.core.metamodel.facets.properties.mandatory.staticmethod.MandatoryFacetOnPropertyStaticMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.multiline.annotation.MultiLineFacetOnPropertyFactory;
import org.apache.isis.core.metamodel.facets.properties.property.PropertyAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.PropertyLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.renderedasdaybefore.annotation.RenderedAsDayBeforeAnnotationOnPropertyFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.typicallen.annotation.TypicalLengthOnPropertyFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.update.PropertyModifyFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.update.PropertySetAndClearFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.validating.dflt.PropertyValidateFacetDefaultFactory;
import org.apache.isis.core.metamodel.facets.properties.validating.maskannot.MaskFacetOnPropertyAnnotationFactory;
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
import org.apache.isis.core.metamodel.facets.value.color.ColorValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.date.DateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datejodalocal.JodaLocalDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datesql.JavaSqlDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datetime.DateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datetimejoda.JodaDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.datetimejodalocal.JodaLocalDateTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.dateutil.JavaUtilDateValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.doubles.DoublePrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.doubles.DoubleWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.floats.FloatPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.floats.FloatWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.image.ImageValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.imageawt.JavaAwtImageValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.integer.IntPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.integer.IntWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.longs.LongPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.longs.LongWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.money.MoneyValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.password.PasswordValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.percentage.PercentageValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.shortint.ShortPrimitiveValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.shortint.ShortWrapperValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.string.StringValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.time.TimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.timesql.JavaSqlTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.timestamp.TimeStampValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.timestampsql.JavaSqlTimeStampValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.url.URLValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.uuid.UUIDValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;

public final class ProgrammingModelFacetsJava5 extends ProgrammingModelAbstract {

    public ProgrammingModelFacetsJava5() {
        
        // must be first, so any Facets created can be replaced by other
        // FacetFactorys later.
        addFactory(FallbackFacetFactory.class);

        addFactory(ObjectSpecIdFacetDerivedFromClassNameFactory.class);
        addFactory(DomainServiceFacetAnnotationFactory.class);

        addFactory(IteratorFilteringFacetFactory.class);
        addFactory(RemoveSyntheticOrAbstractMethodsFacetFactory.class);
        addFactory(RemoveSuperclassMethodsFacetFactory.class);
        addFactory(RemoveJavaLangObjectMethodsFacetFactory.class);
        addFactory(RemoveJavaLangComparableMethodsFacetFactory.class);
        addFactory(RemoveSetDomainObjectContainerMethodFacetFactory.class);
        addFactory(RemoveInitMethodFacetFactory.class);
        addFactory(RemoveInjectMethodsFacetFactory.class);
        addFactory(RemoveStaticGettersAndSettersFacetFactory.class);
        addFactory(RemoveGetClassMethodFacetFactory.class);
        addFactory(RemoveProgrammaticOrIgnoreAnnotationMethodsFacetFactory.class);
        
        // come what may, we have to ignore the PersistenceCapable supertype.
        addFactory(RemoveJdoEnhancementTypesFacetFactory.class);
        // so we may as well also just ignore any 'jdo' prefixed methods here also.
        addFactory(RemoveJdoPrefixedMethodsFacetFactory.class);
        // DN 4.x
        addFactory(RemoveDatanucleusPersistableTypesFacetFactory.class);
        addFactory(RemoveDnPrefixedMethodsFacetFactory.class);

        // must be before any other FacetFactories that install
        // MandatoryFacet.class facets
        addFactory(MandatoryFacetOnProperyDefaultFactory.class);
        addFactory(MandatoryFacetOnParametersDefaultFactory.class);

        addFactory(PropertyValidateFacetDefaultFactory.class);

        // enum support
        addFactory(EnumFacetUsingValueFacetUsingSemanticsProviderFactory.class);
        addFactory(ActionParameterChoicesFacetDerivedFromChoicesFacetFactory.class);
        addFactory(PropertyChoicesFacetDerivedFromChoicesFacetFactory.class);


        // properties
        addFactory(PropertyAccessorFacetViaAccessorFactory.class);
        addFactory(PropertySetAndClearFacetFactory.class);
        // must come after PropertySetAndClearFacetFactory (replaces setter facet with modify if need be)
        addFactory(PropertyModifyFacetFactory.class);

        addFactory(PropertyValidateFacetViaMethodFactory.class);
        addFactory(PropertyChoicesFacetViaMethodFactory.class);
        addFactory(PropertyAutoCompleteFacetMethodFactory.class);
        addFactory(PropertyDefaultFacetViaMethodFactory.class);
        addFactory(MandatoryFacetOnPropertyStaticMethodFactory.class);

        // collections
        addFactory(CollectionAccessorFacetViaAccessorFactory.class);
        addFactory(CollectionClearFacetFactory.class);
        addFactory(CollectionAddToRemoveFromAndValidateFacetFactory.class);
        addFactory(SortedByFacetAnnotationFactory.class);

        // actions
        addFactory(ActionNamedDebugExplorationFacetFactory.class);
        addFactory(ActionValidationFacetViaMethodFactory.class);
        addFactory(ActionChoicesFacetViaMethodFactory.class);
        addFactory(ActionParameterChoicesFacetViaMethodFactory.class);
        addFactory(ActionParameterAutoCompleteFacetViaMethodFactory.class);
        addFactory(ActionDefaultsFacetViaMethodFactory.class);
        addFactory(ActionParameterDefaultsFacetViaMethodFactory.class);

        // members in general
        addFactory(NamedFacetStaticMethodFactory.class);
        addFactory(DescribedAsFacetStaticMethodFactory.class);
        addFactory(DisableForSessionFacetViaMethodFactory.class);
        addFactory(DisableForContextFacetViaMethodFactory.class);
        addFactory(DisabledFacetStaticMethodFacetFactory.class);
        addFactory(HideForSessionFacetViaMethodFactory.class);
        addFactory(HiddenFacetStaticMethodFactory.class);
        addFactory(HideForContextFacetViaMethodFactory.class);
        addFactory(RenderFacetOrResolveFactory.class);

        // objects
        addFactory(IconFacetMethodFactory.class);
        addFactory(CssClassFacetMethodFactory.class);

        addFactory(CreatedCallbackFacetFactory.class);
        addFactory(LoadCallbackFacetFactory.class);
        addFactory(PersistCallbackViaSaveMethodFacetFactory.class);
        addFactory(PersistCallbackFacetFactory.class);
        addFactory(UpdateCallbackFacetFactory.class);
        addFactory(RemoveCallbackFacetFactory.class);

        addFactory(DirtyMethodsFacetFactory.class);
        addFactory(ValidateObjectFacetMethodFactory.class);
        addFactory(ObjectValidPropertiesFacetImplFactory.class);
        addFactory(PluralFacetStaticMethodFactory.class);
        addFactory(org.apache.isis.core.metamodel.facets.object.named.staticmethod.NamedFacetStaticMethodFactory.class);
        addFactory(TitleAnnotationFacetFactory.class);
        addFactory(TitleFacetViaMethodsFactory.class);

        addFactory(MemberOrderFacetFactory.class);
        addFactory(ActionOrderFacetAnnotationFactory.class);
        addFactory(FieldOrderFacetAnnotationFactory.class);
        addFactory(MemberGroupLayoutFacetFactory.class);
        
        addFactory(ParentedFacetSinceAggregatedAnnotationFactory.class);
        addFactory(BookmarkPolicyFacetViaBookmarkableAnnotationFactory.class);
        addFactory(HomePageFacetAnnotationFactory.class);
        addFactory(ChoicesFacetFromBoundedMarkerInterfaceFactory.class);
        addFactory(DebugFacetAnnotationFactory.class);

        addFactory(DefaultedFacetAnnotationElseConfigurationFactory.class);
        addFactory(PropertyDefaultFacetDerivedFromTypeFactory.class);
        addFactory(ActionParameterDefaultFacetDerivedFromTypeFactory.class);

        addFactory(DescribedAsFacetOnTypeAnnotationFactory.class);
        addFactory(DescribedAsFacetOnMemberFactory.class);
        addFactory(DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory.class);
        
        addFactory(BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory.class);
        addFactory(BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory.class);

        addFactory(DisabledFacetOnMemberFromPropertiesFactory.class);
        addFactory(EncodableFacetAnnotationElseConfigurationFactory.class);
        addFactory(ExplorationFacetAnnotationFactory.class);
        addFactory(NotContributedFacetAnnotationFactory.class);
        addFactory(NotInServiceMenuFacetAnnotationFactory.class);
        addFactory(NotInServiceMenuFacetViaMethodFactory.class);

        addFactory(NotContributedFacetDerivedFromDomainServiceFacetFactory.class);
        addFactory(NotInServiceMenuFacetDerivedFromDomainServiceFacetFactory.class);


        addFactory(HiddenFacetOnTypeAnnotationFactory.class);
        // must come after the TitleAnnotationFacetFactory, because can act as an override
        addFactory(HiddenFacetOnMemberFromPropertiesFactory.class);

        addFactory(CssClassFacetOnTypeAnnotationFactory.class);
        addFactory(CssClassFacetOnMemberFactory.class);
        // must come after CssClassFacetOnMemberFactory
        addFactory(CssClassFacetOnActionFromConfiguredRegexFactory.class);

        addFactory(CssClassFaFacetOnTypeAnnotationFactory.class);
        addFactory(CssClassFaFacetOnMemberFactory.class);

        addFactory(HiddenObjectFacetViaMethodFactory.class);
        addFactory(DisabledObjectFacetViaMethodFactory.class);

        addFactory(CopyImmutableFacetOntoMembersFactory.class);
        addFactory(ImmutableFacetMarkerInterfaceFactory.class);

        addFactory(RecreatableObjectFacetFactory.class);

        addFactory(MaxLengthFacetOnTypeAnnotationFactory.class);
        addFactory(MaxLengthFacetOnActionAnnotationFactory.class);

        addFactory(MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacetFactory.class);

        addFactory(MultiLineFacetOnTypeAnnotationFactory.class);
        addFactory(MultiLineFacetOnPropertyFactory.class);
        addFactory(MultiLineFacetOnParameterAnnotationFactory.class);



        // must come after RecreatableObjectFacetFactory
        addFactory(DomainObjectAnnotationFacetFactory.class);


        // must come after the property/collection accessor+mutator facet factories
        addFactory(ActionAnnotationFacetFactory.class);
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        addFactory(PropertyAnnotationFacetFactory.class);
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        addFactory(CollectionAnnotationFacetFactory.class);


        addFactory(ParameterAnnotationFacetFactory.class);

        // must come after DomainObjectAnnotationFacetFactory
        addFactory(DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory.class);
        addFactory(DisabledFacetOnCollectionDerivedFromViewModelFacetFactory.class);

        addFactory(DomainServiceLayoutFactory.class);
        addFactory(DomainObjectLayoutFacetFactory.class);
        // must come after MultiLine
        addFactory(PropertyLayoutFacetFactory.class);
        addFactory(ParameterLayoutFacetFactory.class);
        addFactory(ActionLayoutFacetFactory.class);
        addFactory(CollectionLayoutFacetFactory.class);

        addFactory(NamedFacetOnTypeAnnotationFactory.class);
        addFactory(NamedFacetOnMemberFactory.class);
        addFactory(NamedFacetOnParameterAnnotationFactory.class);

        addFactory(NotPersistableFacetAnnotationFactory.class);
        addFactory(NotPersistableFacetMarkerInterfaceFactory.class);

        addFactory(ParseableFacetAnnotationElseConfigurationFactory.class);
        addFactory(PluralAnnotationFacetFactory.class);
        addFactory(PagedFacetOnTypeAnnotationFactory.class);
        addFactory(PagedFacetOnCollectionFactory.class);
        addFactory(PagedFacetOnActionFactory.class);


        // must come after any facets that install titles
        addFactory(MaskFacetOnTypeAnnotationFactory.class);
        addFactory(MaskFacetOnPropertyAnnotationFactory.class);
        addFactory(MaskFacetOnParameterAnnotationFactory.class);

        // must come after any facets that install titles, and after mask
        // if takes precedence over mask.
        addFactory(RegExFacetOnTypeAnnotationFactory.class);

        addFactory(TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory.class);
        addFactory(TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory.class);

        addFactory(TypicalLengthFacetOnTypeAnnotationFactory.class);
        addFactory(TypicalLengthOnPropertyFacetFactory.class);
        addFactory(TypicalLengthFacetOnParameterAnnotationFactory.class);
        addFactory(RenderedAsDayBeforeAnnotationOnPropertyFacetFactory.class);
        addFactory(RenderedAsDayBeforeFacetOnParameterAnnotationFactory.class);

        // built-in value types for Java language
        addFactory(BooleanPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(BooleanWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(BytePrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(ByteWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(ShortPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(ShortWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(IntPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(IntWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(LongPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(LongWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FloatPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(FloatWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(DoublePrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(DoubleWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(CharPrimitiveValueFacetUsingSemanticsProviderFactory.class);
        addFactory(CharWrapperValueFacetUsingSemanticsProviderFactory.class);
        addFactory(BigIntegerValueFacetUsingSemanticsProviderFactory.class);
        addFactory(BigDecimalValueFacetUsingSemanticsProviderFactory.class);
        addFactory(JavaSqlDateValueFacetUsingSemanticsProviderFactory.class);
        addFactory(JavaSqlTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(JavaUtilDateValueFacetUsingSemanticsProviderFactory.class);
        addFactory(JavaSqlTimeStampValueFacetUsingSemanticsProviderFactory.class);
        addFactory(StringValueFacetUsingSemanticsProviderFactory.class);
        addFactory(URLValueFacetUsingSemanticsProviderFactory.class);
        addFactory(UUIDValueFacetUsingSemanticsProviderFactory.class);

        addFactory(JavaAwtImageValueFacetUsingSemanticsProviderFactory.class);
        
        // applib values
        addFactory(BlobValueFacetUsingSemanticsProviderFactory.class);
        addFactory(ClobValueFacetUsingSemanticsProviderFactory.class);
        addFactory(DateValueFacetUsingSemanticsProviderFactory.class);
        addFactory(DateTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(ColorValueFacetUsingSemanticsProviderFactory.class);
        addFactory(MoneyValueFacetUsingSemanticsProviderFactory.class);
        addFactory(PasswordValueFacetUsingSemanticsProviderFactory.class);
        addFactory(PercentageValueFacetUsingSemanticsProviderFactory.class);
        addFactory(TimeStampValueFacetUsingSemanticsProviderFactory.class);
        addFactory(TimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(ImageValueFacetUsingSemanticsProviderFactory.class);

        // jodatime values
        addFactory(JodaLocalDateValueFacetUsingSemanticsProviderFactory.class);
        addFactory(JodaLocalDateTimeValueFacetUsingSemanticsProviderFactory.class);
        addFactory(JodaDateTimeValueFacetUsingSemanticsProviderFactory.class);
        
        // written to not trample over TypeOf if already installed
        addFactory(CollectionFacetFactory.class);
        // must come after CollectionFacetFactory
        addFactory(ParentedFacetSinceCollectionFactory.class);

        // so we can dogfood the applib "value" types
        addFactory(ValueFacetAnnotationOrConfigurationFactory.class);


        addFactory(DisabledFacetOnPropertyDerivedFromImmutableFactory.class);
        addFactory(DisabledFacetOnCollectionDerivedFromImmutableFactory.class);

        // should come near the end, after any facets that install PropertySetterFacet have run.
        addFactory(DisabledFacetOnPropertyInferredFactory.class);

        addFactory(AuditableFacetMarkerInterfaceFactory.class);

        addFactory(FacetsFacetAnnotationFactory.class);

        // must be after all named facets and description facets have been installed
        addFactory(TranslationFacetFactory.class);

        addFactory(ViewModelSemanticCheckingFacetFactory.class);
    }
}
