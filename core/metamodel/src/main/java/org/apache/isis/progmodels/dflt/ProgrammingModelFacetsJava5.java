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

package org.apache.isis.progmodels.dflt;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.actions.action.ActionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.action.ActionChoicesForCollectionParameterFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.contributing.maxlenannot.MaxLengthFacetOnActionAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.contributing.paged.PagedFacetOnActionFactory;
import org.apache.isis.core.metamodel.facets.actions.defaults.method.ActionDefaultsFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.actions.exploration.annotation.PrototypeFacetForExplorationAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.homepage.annotation.HomePageFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.annotation.NotContributedFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.derived.NotContributedFacetDerivedFromDomainServiceFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.derived.NotContributedFacetDerivedFromMixinFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.annotation.NotInServiceMenuFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.derived.NotInServiceMenuFacetDerivedFromDomainServiceFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.method.NotInServiceMenuFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.all.i18n.TranslationFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.accessor.CollectionAccessorFacetViaAccessorFactory;
import org.apache.isis.core.metamodel.facets.collections.clear.CollectionClearFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.javautilcollection.CollectionFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToRemoveFromAndValidateFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.paged.PagedFacetOnCollectionFactory;
import org.apache.isis.core.metamodel.facets.collections.parented.ParentedFacetSinceCollectionFactory;
import org.apache.isis.core.metamodel.facets.collections.sortedby.annotation.SortedByFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.jaxb.XmlJavaTypeAdapterFacetFactory;
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
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.bookmarkable.BookmarkPolicyFacetViaBookmarkableAnnotationElseFallbackFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.LoadCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.PersistCallbackViaSaveMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.RemoveCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.UpdateCallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.choices.boundedmarkerifc.ChoicesFacetFromBoundedMarkerInterfaceFactory;
import org.apache.isis.core.metamodel.facets.object.choices.enums.EnumFacetUsingValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.object.cssclass.annotation.CssClassFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.cssclass.method.CssClassFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.object.defaults.annotcfg.DefaultedFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.facets.object.describedas.annotation.DescribedAsFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.disabled.method.DisabledObjectFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.DomainObjectLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainservice.annotation.DomainServiceFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.encodeable.annotcfg.EncodableFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.facets.object.facets.annotation.FacetsFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.fieldorder.annotation.FieldOrderFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacetFactory;
import org.apache.isis.core.metamodel.facets.object.hidden.annotation.HiddenFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.hidden.method.HiddenObjectFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.object.icon.method.IconFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.annotation.RemoveAnnotatedMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.datanucleus.RemoveDatanucleusPersistableTypesFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.datanucleus.RemoveDnPrefixedMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.isis.RemoveStaticGettersAndSettersFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.IteratorFilteringFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.jdo.RemoveJdoEnhancementTypesFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.jdo.RemoveJdoPrefixedMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.immutable.immutablemarkerifc.ImmutableFacetMarkerInterfaceFactory;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.mask.annotation.MaskFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.maxlen.annotation.MaxLengthFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.membergroups.annotprop.MemberGroupLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacetForMixinAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.multiline.annotation.MultiLineFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.named.annotation.NamedFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.notpersistable.notpersistableannot.NotPersistableFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.notpersistable.notpersistablemarkerifc.NotPersistableFacetMarkerInterfaceFactory;
import org.apache.isis.core.metamodel.facets.object.objectspecid.classname.ObjectSpecIdFacetDerivedFromClassNameFactory;
import org.apache.isis.core.metamodel.facets.object.objectvalidprops.impl.ObjectValidPropertiesFacetImplFactory;
import org.apache.isis.core.metamodel.facets.object.paged.annotation.PagedFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.parented.aggregated.AggregatedAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.parseable.annotcfg.ParseableFacetAnnotationElseConfigurationFactory;
import org.apache.isis.core.metamodel.facets.object.plural.annotation.PluralAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.plural.staticmethod.PluralFacetStaticMethodFactory;
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
import org.apache.isis.core.metamodel.facets.param.choices.method.ActionChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.layout.ParameterLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.param.mandatory.dflt.MandatoryFacetOnParametersDefaultFactory;
import org.apache.isis.core.metamodel.facets.param.multiline.annotation.MultiLineFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.named.annotation.NamedFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.parameter.ParameterAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.param.renderedasdaybefore.annotation.RenderedAsDayBeforeFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.typicallen.annotation.TypicalLengthFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.validating.maskannot.MaskFacetOnParameterAnnotationFactory;
import org.apache.isis.core.metamodel.facets.properties.accessor.PropertyAccessorFacetViaAccessorFactory;
import org.apache.isis.core.metamodel.facets.properties.autocomplete.method.PropertyAutoCompleteFacetMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.bigdecimal.javaxvaldigits.BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory;
import org.apache.isis.core.metamodel.facets.properties.choices.method.PropertyChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.method.PropertyDefaultFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.disabled.inferred.DisabledFacetOnPropertyInferredFactory;
import org.apache.isis.core.metamodel.facets.properties.mandatory.dflt.MandatoryFacetOnProperyDefaultFactory;
import org.apache.isis.core.metamodel.facets.properties.mandatory.staticmethod.MandatoryFacetOnPropertyStaticMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.multiline.annotation.MultiLineFacetOnPropertyFactory;
import org.apache.isis.core.metamodel.facets.properties.property.PropertyAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.PropertyLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.renderedasdaybefore.annotation.RenderedAsDayBeforeAnnotationOnPropertyFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.typicallen.annotation.TypicalLengthOnPropertyFacetFactory;
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
import org.apache.isis.core.metamodel.facets.value.timejodalocal.JodaLocalTimeValueFacetSimpleFactory;
import org.apache.isis.core.metamodel.facets.value.timesql.JavaSqlTimeValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.timestamp.TimeStampValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.timestampsql.JavaSqlTimeStampValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.url.URLValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.value.uuid.UUIDValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.postprocessors.param.DeriveFacetsPostProcessor;
import org.apache.isis.core.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;

public final class ProgrammingModelFacetsJava5 extends ProgrammingModelAbstract {

    public ProgrammingModelFacetsJava5(final IsisConfiguration configuration) {
        this(DeprecatedPolicy.parse(configuration));
    }

    public ProgrammingModelFacetsJava5(final DeprecatedPolicy deprecatedPolicy) {
        super(deprecatedPolicy);

        // must be first, so any Facets created can be replaced by other
        // FacetFactorys later.
        addFactory(new FallbackFacetFactory());

        addFactory(new ObjectSpecIdFacetDerivedFromClassNameFactory());
        addFactory(new DomainServiceFacetAnnotationFactory());

        addFactory(new IteratorFilteringFacetFactory());

        addFactory(new RemoveMethodsFacetFactory());

        addFactory(new RemoveStaticGettersAndSettersFacetFactory());

        addFactory(new RemoveAnnotatedMethodsFacetFactory());

        // come what may, we have to ignore the PersistenceCapable supertype.
        addFactory(new RemoveJdoEnhancementTypesFacetFactory());
        // so we may as well also just ignore any 'jdo' prefixed methods here also.
        addFactory(new RemoveJdoPrefixedMethodsFacetFactory());
        // DN 4.x
        addFactory(new RemoveDatanucleusPersistableTypesFacetFactory());
        addFactory(new RemoveDnPrefixedMethodsFacetFactory());

        // must be before any other FacetFactories that install
        // MandatoryFacet.class facets
        addFactory(new MandatoryFacetOnProperyDefaultFactory());
        addFactory(new MandatoryFacetOnParametersDefaultFactory());

        addFactory(new PropertyValidateFacetDefaultFactory());

        // enum support
        addFactory(new EnumFacetUsingValueFacetUsingSemanticsProviderFactory());
        // addFactory(new ActionParameterChoicesFacetDerivedFromChoicesFacetFactory()); ... moved into post-processor, see below
        // addFactory(new PropertyChoicesFacetDerivedFromChoicesFacetFactory()); ... moved into post-processor, see below

        // properties
        addFactory(new PropertyAccessorFacetViaAccessorFactory());
        addFactory(new PropertySetAndClearFacetFactory());
        // must come after PropertySetAndClearFacetFactory (replaces setter facet with modify if need be)
        addFactory(new PropertyModifyFacetFactory());

        addFactory(new PropertyValidateFacetViaMethodFactory());
        addFactory(new PropertyChoicesFacetViaMethodFactory());
        addFactory(new PropertyAutoCompleteFacetMethodFactory());
        addFactory(new PropertyDefaultFacetViaMethodFactory());
        
        addFactory(new MandatoryFacetOnPropertyStaticMethodFactory());

        // collections
        addFactory(new CollectionAccessorFacetViaAccessorFactory());
        addFactory(new CollectionClearFacetFactory());
        addFactory(new CollectionAddToRemoveFromAndValidateFacetFactory());
        
        addFactory(new SortedByFacetAnnotationFactory());

        // actions
        
        addFactory(new ActionValidationFacetViaMethodFactory());
        addFactory(new ActionChoicesFacetViaMethodFactory());
        addFactory(new ActionParameterChoicesFacetViaMethodFactory());
        addFactory(new ActionParameterAutoCompleteFacetViaMethodFactory());
        addFactory(new ActionDefaultsFacetViaMethodFactory());
        addFactory(new ActionParameterDefaultsFacetViaMethodFactory());

        // members in general
        
        addFactory(new NamedFacetStaticMethodFactory());
        
        addFactory(new DescribedAsFacetStaticMethodFactory());
        addFactory(new DisableForSessionFacetViaMethodFactory());
        addFactory(new DisableForContextFacetViaMethodFactory());
        
        addFactory(new DisabledFacetStaticMethodFacetFactory());
        addFactory(new HideForSessionFacetViaMethodFactory());
        
        addFactory(new HiddenFacetStaticMethodFactory());
        addFactory(new HideForContextFacetViaMethodFactory());
        addFactory(new RenderFacetOrResolveFactory());

        addFactory(new CreatedCallbackFacetFactory());
        addFactory(new LoadCallbackFacetFactory());
        addFactory(new PersistCallbackViaSaveMethodFacetFactory());
        addFactory(new PersistCallbackFacetFactory());
        addFactory(new UpdateCallbackFacetFactory());
        addFactory(new RemoveCallbackFacetFactory());

        addFactory(new ValidateObjectFacetMethodFactory());
        addFactory(new ObjectValidPropertiesFacetImplFactory());
        
        addFactory(new PluralFacetStaticMethodFactory());
        
        addFactory(new org.apache.isis.core.metamodel.facets.object.named.staticmethod.NamedFacetStaticMethodFactory());

        addFactory(new MemberOrderFacetFactory());
        
        addFactory(new ActionOrderFacetAnnotationFactory());
        
        addFactory(new FieldOrderFacetAnnotationFactory());
        addFactory(new MemberGroupLayoutFacetFactory());

        addFactory(new AggregatedAnnotationFactory());
        
        addFactory(new BookmarkPolicyFacetViaBookmarkableAnnotationElseFallbackFactory());
        addFactory(new HomePageFacetAnnotationFactory());
        addFactory(new ChoicesFacetFromBoundedMarkerInterfaceFactory());

        addFactory(new DefaultedFacetAnnotationElseConfigurationFactory());
        //addFactory(new PropertyDefaultFacetDerivedFromTypeFactory()); ... logic moved to post-processor
        //addFactory(new ActionParameterDefaultFacetDerivedFromTypeFactory()); ... logic moved to post-processor

        
        addFactory(new DescribedAsFacetOnTypeAnnotationFactory());
        addFactory(new DescribedAsFacetOnMemberFactory());
        //addFactory(new DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory()); ... logic moved to post-processor

        addFactory(new BigDecimalFacetOnParameterFromJavaxValidationAnnotationFactory());
        addFactory(new BigDecimalFacetOnPropertyFromJavaxValidationDigitsAnnotationFactory());

        addFactory(new DisabledFacetOnMemberFromPropertiesFactory());
        addFactory(new EncodableFacetAnnotationElseConfigurationFactory());
        
        addFactory(new PrototypeFacetForExplorationAnnotationFactory());

        addFactory(new NotContributedFacetAnnotationFactory());

        addFactory(new NotInServiceMenuFacetAnnotationFactory());

        addFactory(new NotInServiceMenuFacetViaMethodFactory());

        addFactory(new NotContributedFacetDerivedFromDomainServiceFacetFactory());
        addFactory(new NotInServiceMenuFacetDerivedFromDomainServiceFacetFactory());

        
        addFactory(new HiddenFacetOnTypeAnnotationFactory());
        // must come after the TitleAnnotationFacetFactory, because can act as an override
        
        addFactory(new HiddenFacetOnMemberFromPropertiesFactory());

        
            addFactory(new CssClassFacetOnTypeAnnotationFactory());
        addFactory(new CssClassFacetOnMemberFactory());
        // must come after CssClassFacetOnMemberFactory
        addFactory(new CssClassFacetOnActionFromConfiguredRegexFactory());

        // addFactory(new CssClassFaFacetOnTypeAnnotationFactory());
        addFactory(new CssClassFaFacetOnMemberFactory());

        addFactory(new HiddenObjectFacetViaMethodFactory());
        addFactory(new DisabledObjectFacetViaMethodFactory());

        // addFactory(new CopyImmutableFacetOntoMembersFactory()); ... logic moved to post-processor
        addFactory(new ImmutableFacetMarkerInterfaceFactory());

        addFactory(new RecreatableObjectFacetFactory());
        addFactory(new XmlJavaTypeAdapterFacetFactory());
        addFactory(new MixinFacetForMixinAnnotationFactory());


        
        addFactory(new MaxLengthFacetOnTypeAnnotationFactory());

        addFactory(new MaxLengthFacetOnActionAnnotationFactory());


        addFactory(new MustSatisfySpecificationFromMustSatisfyAnnotationOnTypeFacetFactory());

        
        addFactory(new MultiLineFacetOnTypeAnnotationFactory());
        addFactory(new MultiLineFacetOnPropertyFactory());
        addFactory(new MultiLineFacetOnParameterAnnotationFactory());

        // must come after RecreatableObjectFacetFactory
        addFactory(new DomainObjectAnnotationFacetFactory());

        // must come after the property/collection accessor+mutator facet factories
        addFactory(new ActionAnnotationFacetFactory());
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        addFactory(new PropertyAnnotationFacetFactory());
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        addFactory(new CollectionAnnotationFacetFactory());

        addFactory(new ParameterAnnotationFacetFactory());

        // must come after DomainObjectAnnotationFacetFactory
        //addFactory(new DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory()); ... moved to post-processor
        //addFactory(new DisabledFacetOnCollectionDerivedFromViewModelFacetFactory()); ... moved to post-processor

        // must come after DomainObjectAnnotationFacetFactory & MixinFacetFactory
        addFactory(new NotContributedFacetDerivedFromMixinFacetFactory());

        addFactory(new GridFacetFactory());

        // must come before DomainObjectLayoutFacetFactory
        // (so subscribers on titleUi event etc can override)
        addFactory(new TitleAnnotationFacetFactory());
        addFactory(new TitleFacetViaMethodsFactory());
        addFactory(new IconFacetMethodFactory());
        addFactory(new CssClassFacetMethodFactory());
        addFactory(new LayoutFacetFactory());


        addFactory(new DomainServiceLayoutFacetFactory());
        addFactory(new DomainObjectLayoutFacetFactory());
        // must come after MultiLine
        addFactory(new PropertyLayoutFacetFactory());
        addFactory(new ParameterLayoutFacetFactory());
        addFactory(new ActionLayoutFacetFactory());
        addFactory(new CollectionLayoutFacetFactory());


        addFactory(new NamedFacetOnTypeAnnotationFactory());
        addFactory(new NamedFacetOnMemberFactory());
        
        addFactory(new NamedFacetOnParameterAnnotationFactory());

        addFactory(new NotPersistableFacetAnnotationFactory());
        addFactory(new NotPersistableFacetMarkerInterfaceFactory());

        addFactory(new ParseableFacetAnnotationElseConfigurationFactory());
        
        addFactory(new PluralAnnotationFacetFactory());

        addFactory(new PagedFacetOnTypeAnnotationFactory());

        addFactory(new PagedFacetOnCollectionFactory());

        addFactory(new PagedFacetOnActionFactory());

        // must come after any facets that install titles
        
        addFactory(new MaskFacetOnTypeAnnotationFactory());
        addFactory(new MaskFacetOnPropertyAnnotationFactory());
        
        addFactory(new MaskFacetOnParameterAnnotationFactory());

        // must come after any facets that install titles, and after mask
        // if takes precedence over mask.
        
        addFactory(new RegExFacetOnTypeAnnotationFactory());

        // addFactory(new TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory()); ... logic moved to post-processor
        // addFactory(new TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory()); ... logic moved to post-processor

        
        addFactory(new TypicalLengthFacetOnTypeAnnotationFactory());
        addFactory(new TypicalLengthOnPropertyFacetFactory());
        
        addFactory(new TypicalLengthFacetOnParameterAnnotationFactory());
        
        addFactory(new RenderedAsDayBeforeAnnotationOnPropertyFacetFactory());
        
        addFactory(new RenderedAsDayBeforeFacetOnParameterAnnotationFactory());

        // built-in value types for Java language
        addFactory(new BooleanPrimitiveValueFacetUsingSemanticsProviderFactory());
        addFactory(new BooleanWrapperValueFacetUsingSemanticsProviderFactory());
        addFactory(new BytePrimitiveValueFacetUsingSemanticsProviderFactory());
        addFactory(new ByteWrapperValueFacetUsingSemanticsProviderFactory());
        addFactory(new ShortPrimitiveValueFacetUsingSemanticsProviderFactory());
        addFactory(new ShortWrapperValueFacetUsingSemanticsProviderFactory());
        addFactory(new IntPrimitiveValueFacetUsingSemanticsProviderFactory());
        addFactory(new IntWrapperValueFacetUsingSemanticsProviderFactory());
        addFactory(new LongPrimitiveValueFacetUsingSemanticsProviderFactory());
        addFactory(new LongWrapperValueFacetUsingSemanticsProviderFactory());
        addFactory(new FloatPrimitiveValueFacetUsingSemanticsProviderFactory());
        addFactory(new FloatWrapperValueFacetUsingSemanticsProviderFactory());
        addFactory(new DoublePrimitiveValueFacetUsingSemanticsProviderFactory());
        addFactory(new DoubleWrapperValueFacetUsingSemanticsProviderFactory());
        addFactory(new CharPrimitiveValueFacetUsingSemanticsProviderFactory());
        addFactory(new CharWrapperValueFacetUsingSemanticsProviderFactory());
        addFactory(new BigIntegerValueFacetUsingSemanticsProviderFactory());
        addFactory(new BigDecimalValueFacetUsingSemanticsProviderFactory());
        addFactory(new JavaSqlDateValueFacetUsingSemanticsProviderFactory());
        addFactory(new JavaSqlTimeValueFacetUsingSemanticsProviderFactory());
        addFactory(new JavaUtilDateValueFacetUsingSemanticsProviderFactory());
        addFactory(new JavaSqlTimeStampValueFacetUsingSemanticsProviderFactory());
        addFactory(new StringValueFacetUsingSemanticsProviderFactory());
        addFactory(new URLValueFacetUsingSemanticsProviderFactory());
        addFactory(new UUIDValueFacetUsingSemanticsProviderFactory());

        addFactory(new JavaAwtImageValueFacetUsingSemanticsProviderFactory());

        // applib values
        addFactory(new BlobValueFacetUsingSemanticsProviderFactory());
        addFactory(new ClobValueFacetUsingSemanticsProviderFactory());
        addFactory(new DateValueFacetUsingSemanticsProviderFactory());
        addFactory(new DateTimeValueFacetUsingSemanticsProviderFactory());
        addFactory(new ColorValueFacetUsingSemanticsProviderFactory());
        addFactory(new MoneyValueFacetUsingSemanticsProviderFactory());
        addFactory(new PasswordValueFacetUsingSemanticsProviderFactory());
        addFactory(new PercentageValueFacetUsingSemanticsProviderFactory());
        addFactory(new TimeStampValueFacetUsingSemanticsProviderFactory());
        addFactory(new TimeValueFacetUsingSemanticsProviderFactory());
        addFactory(new ImageValueFacetUsingSemanticsProviderFactory());

        // jodatime values
        addFactory(new JodaLocalDateValueFacetUsingSemanticsProviderFactory());
        addFactory(new JodaLocalDateTimeValueFacetUsingSemanticsProviderFactory());
        addFactory(new JodaDateTimeValueFacetUsingSemanticsProviderFactory());
        addFactory(new JodaLocalTimeValueFacetSimpleFactory());

        // written to not trample over TypeOf if already installed
        addFactory(new CollectionFacetFactory());
        // must come after CollectionFacetFactory
        addFactory(new ParentedFacetSinceCollectionFactory());

        // so we can dogfood the applib "value" types
        addFactory(new ValueFacetAnnotationOrConfigurationFactory());

        // addFactory(new DisabledFacetOnPropertyDerivedFromImmutableFactory()); ... logic moved to post-processor
        // addFactory(new DisabledFacetOnCollectionDerivedFromImmutableFactory()); ... logic moved to post-processor

        // should come near the end, after any facets that install PropertySetterFacet have run.
        addFactory(new DisabledFacetOnPropertyInferredFactory());


        addFactory(new ActionChoicesForCollectionParameterFacetFactory());

        addFactory(new AuditableFacetMarkerInterfaceFactory());

        addFactory(new FacetsFacetAnnotationFactory());

        // must be after all named facets and description facets have been installed
        addFactory(new TranslationFacetFactory());

        addFactory(new ViewModelSemanticCheckingFacetFactory());
    }

    @Override
    public List<ObjectSpecificationPostProcessor> getPostProcessors() {
        return Lists.<ObjectSpecificationPostProcessor>newArrayList(
            new DeriveFacetsPostProcessor()
        );
    }
}
