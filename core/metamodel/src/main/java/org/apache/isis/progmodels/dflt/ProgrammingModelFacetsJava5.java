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

import org.apache.isis.core.metamodel.facets.object.audit.annotation.AuditableFromAuditedAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.audit.configuration.AuditableFromConfigurationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.audit.markerifc.AuditableMarkerInterfaceFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainservice.annotation.DomainServiceViaAnnotationFacetFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.progmodel.facets.actions.bulk.annotation.BulkAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.command.annotation.CommandAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.command.configuration.CommandFromConfigurationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.debug.annotation.DebugAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.defaults.method.ActionDefaultsFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.exploration.annotation.ExplorationAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.homepage.HomePageAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.invoke.ActionInvocationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.invoke.event.PostsActionInvokedEventFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.notcontributed.annotation.NotContributedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.notinservicemenu.annotation.NotInServiceMenuAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.notinservicemenu.method.NotInServiceMenuMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.prototype.annotation.PrototypeAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.publish.PublishedActionAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.semantics.ActionSemanticsAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.semantics.ActionSemanticsFallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.semantics.IdempotentAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.semantics.QueryOnlyAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.typeof.annotation.TypeOfAnnotationForActionsFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.validate.method.ActionValidationFacetViaValidateMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.accessor.CollectionAccessorFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.aggregated.ParentedSinceCollectionFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.clear.CollectionClearFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.collection.CollectionFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.disabled.fromimmutable.DisabledFacetForCollectionDerivedFromImmutableTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.event.PostsCollectionAddedToEventAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.event.PostsCollectionRemovedFromEventAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.modify.CollectionAddRemoveAndValidateFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.notpersisted.annotation.NotPersistedAnnotationForCollectionFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.sortedby.SortedByAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.typeof.TypeOfAnnotationForCollectionsFacetFactory;
import org.apache.isis.core.progmodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.members.cssclass.CssClassOnMemberFacetFactory;
import org.apache.isis.core.progmodel.facets.members.describedas.annotation.DescribedAsOnMemberFacetFactory;
import org.apache.isis.core.progmodel.facets.members.describedas.staticmethod.DescribedAsFacetViaDescriptionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disabled.annotation.DisabledFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disabled.forsession.DisabledFacetViaDisableForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disabled.method.DisabledFacetViaDisableMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disabled.staticmethod.DisabledFacetViaProtectMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hidden.annotation.HiddenForMemberFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hidden.forsession.HiddenFacetViaHideForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hidden.method.HiddenFacetViaHideMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hidden.staticmethod.HiddenFacetViaAlwaysHideMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.named.annotation.NamedOnMemberFacetFactory;
import org.apache.isis.core.progmodel.facets.members.named.staticmethod.NamedFacetViaNameMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.order.MemberOrderFacetFactory;
import org.apache.isis.core.progmodel.facets.members.resolve.RenderOrResolveFacetFactory;
import org.apache.isis.core.progmodel.facets.object.aggregated.annotation.AggregatedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.autocomplete.annotation.AutoCompleteAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.bookmarkable.annotation.BookmarkableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.bounded.annotation.BoundedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.bounded.markerifc.BoundedMarkerInterfaceFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.create.CreatedCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.load.LoadCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.persist.PersistCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.persist.PersistCallbackViaSaveMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.remove.RemoveCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.update.UpdateCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.choices.enums.EnumFacetFactory;
import org.apache.isis.core.progmodel.facets.object.cssclass.CssClassAnnotationForTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.defaults.annotation.DefaultedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.describedas.annotation.DescribedAsAnnotationOnTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.dirty.method.DirtyMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.object.disabled.method.DisabledObjectViaDisabledMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.encodeable.EncodableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.facets.annotation.FacetsAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.hidden.HiddenAnnotationForTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.hidden.method.HiddenObjectViaHiddenMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.icon.method.IconMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ignore.annotation.RemoveProgrammaticOrIgnoreAnnotationMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ignore.isis.RemoveSetDomainObjectContainerMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ignore.isis.RemoveStaticGettersAndSettersFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ignore.javalang.*;
import org.apache.isis.core.progmodel.facets.object.ignore.jdo.RemoveJdoEnhancementTypesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ignore.jdo.RemoveJdoPrefixedMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.object.immutable.annotation.ImmutableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.immutable.markerifc.ImmutableMarkerInterfaceFacetFactory;
import org.apache.isis.core.progmodel.facets.object.mask.annotation.MaskAnnotationForTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.maxlen.annotation.MaxLengthAnnotationForTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.membergroups.MemberGroupLayoutFacetFactory;
import org.apache.isis.core.progmodel.facets.object.multiline.annotation.MultiLineAnnotationOnTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.named.annotation.NamedAnnotationOnTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.named.staticmethod.NamedFacetViaSingularNameStaticMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.notpersistable.NotPersistableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.notpersistable.NotPersistableMarkerInterfacesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.objecttype.ObjectSpecIdAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.objecttype.ObjectTypeDerivedFromClassNameFacetFactory;
import org.apache.isis.core.progmodel.facets.object.orderactions.ActionOrderAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.orderfields.FieldOrderAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.parseable.ParseableFacetFactory;
import org.apache.isis.core.progmodel.facets.object.plural.annotation.PluralAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.plural.staticmethod.PluralMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.publish.PublishedObjectAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.regex.annotation.RegExFacetAnnotationForTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.title.TitleMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.typicallen.annotation.TypicalLengthAnnotationOnTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.validate.method.ValidateObjectViaValidateMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.validperspec.MustSatisfySpecificationOnTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.object.validprops.ObjectValidPropertiesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.value.annotation.ValueFacetFactory;
import org.apache.isis.core.progmodel.facets.object.viewmodel.iface.ViewModelInterfaceFacetFactory;
import org.apache.isis.core.progmodel.facets.object.wizard.iface.iface.WizardInterfaceFacetFactory;
import org.apache.isis.core.progmodel.facets.paged.PagedAnnotationOnTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.paged.PagedOnParentedCollectionFacetFactory;
import org.apache.isis.core.progmodel.facets.param.autocomplete.ActionParameterAutoCompleteFacetFactory;
import org.apache.isis.core.progmodel.facets.param.choices.enums.ParameterChoicesFacetDerivedFromChoicesFacetFacetFactory;
import org.apache.isis.core.progmodel.facets.param.choices.method.ActionChoicesFacetFactory;
import org.apache.isis.core.progmodel.facets.param.choices.methodnum.ActionParameterChoicesFacetFactory;
import org.apache.isis.core.progmodel.facets.param.defaults.fromtype.ParameterDefaultDerivedFromTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetFactory;
import org.apache.isis.core.progmodel.facets.param.describedas.annotation.DescribedAsAnnotationOnParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.javaxvaldigits.BigDecimalForParameterDerivedFromJavaxValidationAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.param.mandatory.annotation.OptionalAnnotationForParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.mandatory.dflt.MandatoryDefaultForParametersFacetFactory;
import org.apache.isis.core.progmodel.facets.param.multiline.annotation.MultiLineAnnotationOnParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.named.annotation.NamedAnnotationOnParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.renderedasdaybefore.annotation.RenderedAsDayBeforeAnnotationOnParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.typicallen.annotation.TypicalLengthAnnotationOnParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.typicallen.fromtype.TypicalLengthFacetForParameterDerivedFromTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.param.validate.maskannot.MaskAnnotationForParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.validate.maxlenannot.MaxLengthAnnotationForParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.validate.perspec.MustSatisfySpecificationOnParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.param.validate.regexannot.RegExFacetAnnotationForParameterFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.accessor.PropertyAccessorFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.autocomplete.PropertyAutoCompleteFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacetFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.choices.method.PropertyChoicesFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.defaults.fromtype.PropertyDefaultDerivedFromTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.defaults.method.PropertyDefaultFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.disabled.fromimmutable.DisabledFacetForPropertyDerivedFromImmutableTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.event.PostsPropertyChangedEventAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.javaxvaldigits.BigDecimalForPropertyDerivedFromJavaxValidationDigitsFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.mandatory.annotation.MandatoryAnnotationForPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.mandatory.annotation.OptionalAnnotationForPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.mandatory.dflt.MandatoryDefaultForPropertiesFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.mandatory.staticmethod.PropertyOptionalFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertyModifyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertySetAndClearFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.multiline.annotation.MultiLineOnPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.notpersisted.annotation.NotPersistedAnnotationForPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.renderedasdaybefore.annotation.RenderedAsDayBeforeAnnotationOnPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.typicallen.annotation.TypicalLengthOnPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.typicallen.fromtype.TypicalLengthFacetForPropertyDerivedFromTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateDefaultFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.maskannot.MaskAnnotationForPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.maxlenannot.MaxLengthAnnotationForPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.perspec.MustSatisfySpecificationOnPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.regexannot.RegExFacetAnnotationForPropertyFacetFactory;
import org.apache.isis.core.progmodel.facets.value.bigdecimal.BigDecimalValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.biginteger.BigIntegerValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.blobs.BlobValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.booleans.BooleanPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.booleans.BooleanWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.bytes.BytePrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.bytes.ByteWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.chars.CharPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.chars.CharWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.clobs.ClobValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.color.ColorValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.date.DateValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.datejodalocal.JodaLocalDateValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.datesql.JavaSqlDateValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.datetime.DateTimeValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.datetimejoda.JodaDateTimeValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.datetimejodalocal.JodaLocalDateTimeValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.dateutil.JavaUtilDateValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.floats.FloatPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.floats.FloatWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.image.ImageValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.imageawt.JavaAwtImageValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.integer.IntPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.integer.IntWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.longs.DoublePrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.longs.DoubleWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.longs.LongPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.longs.LongWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.money.MoneyValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.password.PasswordValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.percentage.PercentageValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.shortint.ShortPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.shortint.ShortWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.string.StringValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.time.TimeValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.timesql.JavaSqlTimeValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.timestamp.TimeStampValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.timestampsql.JavaSqlTimeStampValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.url.URLValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.uuid.UUIDValueTypeFacetFactory;

public final class ProgrammingModelFacetsJava5 extends ProgrammingModelAbstract {

    public ProgrammingModelFacetsJava5() {
        
        // must be first, so any Facets created can be replaced by other
        // FacetFactorys later.
        addFactory(FallbackFacetFactory.class);

        addFactory(ObjectTypeDerivedFromClassNameFacetFactory.class);
        addFactory(DomainServiceViaAnnotationFacetFactory.class);
        addFactory(WizardInterfaceFacetFactory.class);

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

        // must be before any other FacetFactories that install
        // MandatoryFacet.class facets
        addFactory(MandatoryDefaultForPropertiesFacetFactory.class);
        addFactory(MandatoryDefaultForParametersFacetFactory.class);

        addFactory(PropertyValidateDefaultFacetFactory.class);

        // enum support
        addFactory(EnumFacetFactory.class);
        addFactory(ParameterChoicesFacetDerivedFromChoicesFacetFacetFactory.class);
        addFactory(PropertyChoicesFacetDerivedFromChoicesFacetFacetFactory.class);


        // properties
        addFactory(PropertyAccessorFacetFactory.class);
        addFactory(PropertySetAndClearFacetFactory.class);
        // must come after PropertySetAndClearFacetFactory
        addFactory(PropertyModifyFacetFactory.class);
        
        addFactory(PropertyValidateFacetFactory.class);
        addFactory(PropertyChoicesFacetFactory.class);
        addFactory(PropertyAutoCompleteFacetFactory.class);
        addFactory(PropertyDefaultFacetFactory.class);
        addFactory(PropertyOptionalFacetFactory.class);

        // collections
        addFactory(CollectionAccessorFacetFactory.class);
        addFactory(CollectionClearFacetFactory.class);
        addFactory(CollectionAddRemoveAndValidateFacetFactory.class);
        addFactory(SortedByAnnotationFacetFactory.class);

        // actions
        addFactory(ActionInvocationFacetFactory.class);
        addFactory(ActionValidationFacetViaValidateMethodFacetFactory.class);
        addFactory(ActionChoicesFacetFactory.class);
        addFactory(ActionParameterChoicesFacetFactory.class);
        addFactory(ActionParameterAutoCompleteFacetFactory.class);
        addFactory(ActionDefaultsFacetFactory.class);
        addFactory(ActionParameterDefaultsFacetFactory.class);
        addFactory(QueryOnlyAnnotationFacetFactory.class);
        addFactory(IdempotentAnnotationFacetFactory.class);
        addFactory(ActionSemanticsAnnotationFacetFactory.class);
        addFactory(ActionSemanticsFallbackFacetFactory.class);

        // members in general
        addFactory(NamedFacetViaNameMethodFacetFactory.class);
        addFactory(DescribedAsFacetViaDescriptionMethodFacetFactory.class);
        addFactory(DisabledFacetViaDisableForSessionMethodFacetFactory.class);
        addFactory(DisabledFacetViaDisableMethodFacetFactory.class);
        addFactory(DisabledFacetViaProtectMethodFacetFactory.class);
        addFactory(HiddenFacetViaHideForSessionMethodFacetFactory.class);
        addFactory(HiddenFacetViaAlwaysHideMethodFacetFactory.class);
        addFactory(HiddenFacetViaHideMethodFacetFactory.class);
        addFactory(RenderOrResolveFacetFactory.class);

        // objects
        addFactory(ObjectSpecIdAnnotationFacetFactory.class);
        addFactory(IconMethodFacetFactory.class);

        addFactory(CreatedCallbackFacetFactory.class);
        addFactory(LoadCallbackFacetFactory.class);
        addFactory(PersistCallbackViaSaveMethodFacetFactory.class);
        addFactory(PersistCallbackFacetFactory.class);
        addFactory(UpdateCallbackFacetFactory.class);
        addFactory(RemoveCallbackFacetFactory.class);

        addFactory(DirtyMethodsFacetFactory.class);
        addFactory(ValidateObjectViaValidateMethodFacetFactory.class);
        addFactory(ObjectValidPropertiesFacetFactory.class);
        addFactory(PluralMethodFacetFactory.class);
        addFactory(NamedFacetViaSingularNameStaticMethodFacetFactory.class);
        addFactory(TitleAnnotationFacetFactory.class);
        addFactory(TitleMethodFacetFactory.class);

        addFactory(MemberOrderFacetFactory.class);
        addFactory(ActionOrderAnnotationFacetFactory.class);
        addFactory(FieldOrderAnnotationFacetFactory.class);
        addFactory(MemberGroupLayoutFacetFactory.class);
        
        addFactory(AggregatedAnnotationFacetFactory.class);
        addFactory(BookmarkableAnnotationFacetFactory.class);
        addFactory(HomePageAnnotationFacetFactory.class);
        addFactory(BoundedAnnotationFacetFactory.class);
        addFactory(BoundedMarkerInterfaceFacetFactory.class);
        addFactory(DebugAnnotationFacetFactory.class);

        addFactory(DefaultedAnnotationFacetFactory.class);
        addFactory(PropertyDefaultDerivedFromTypeFacetFactory.class);
        addFactory(ParameterDefaultDerivedFromTypeFacetFactory.class);

        addFactory(DescribedAsAnnotationOnTypeFacetFactory.class);
        addFactory(DescribedAsOnMemberFacetFactory.class);
        addFactory(DescribedAsAnnotationOnParameterFacetFactory.class);
        
        addFactory(BigDecimalForParameterDerivedFromJavaxValidationAnnotationFacetFactory.class);
        addFactory(BigDecimalForPropertyDerivedFromJavaxValidationDigitsFacetFactory.class);

        addFactory(DisabledFacetFactory.class);
        addFactory(EncodableAnnotationFacetFactory.class);
        addFactory(ExplorationAnnotationFacetFactory.class);
        addFactory(PrototypeAnnotationFacetFactory.class);
        addFactory(NotContributedAnnotationFacetFactory.class);
        addFactory(NotInServiceMenuAnnotationFacetFactory.class);
        addFactory(NotInServiceMenuMethodFacetFactory.class);
        addFactory(BulkAnnotationFacetFactory.class);

        addFactory(HiddenAnnotationForTypeFacetFactory.class);
        // must come after the TitleAnnotationFacetFactory, because can act as an override
        addFactory(HiddenForMemberFacetFactory.class);
        addFactory(CssClassAnnotationForTypeFacetFactory.class);
        addFactory(CssClassOnMemberFacetFactory.class);

        addFactory(HiddenObjectViaHiddenMethodFacetFactory.class);
        addFactory(DisabledObjectViaDisabledMethodFacetFactory.class);

        addFactory(ImmutableAnnotationFacetFactory.class);
        addFactory(DisabledFacetForPropertyDerivedFromImmutableTypeFacetFactory.class);
        addFactory(DisabledFacetForCollectionDerivedFromImmutableTypeFacetFactory.class);

        // must come after the property/collection/action accessor+mutator facet factories
        addFactory(PostsPropertyChangedEventAnnotationFacetFactory.class);
        addFactory(PostsCollectionAddedToEventAnnotationFacetFactory.class);
        addFactory(PostsCollectionRemovedFromEventAnnotationFacetFactory.class);
        addFactory(PostsActionInvokedEventFacetFactory.class);

        
        addFactory(ImmutableMarkerInterfaceFacetFactory.class);

        addFactory(ViewModelInterfaceFacetFactory.class);

        addFactory(MaxLengthAnnotationForTypeFacetFactory.class);
        addFactory(MaxLengthAnnotationForPropertyFacetFactory.class);
        addFactory(MaxLengthAnnotationForParameterFacetFactory.class);

        addFactory(MustSatisfySpecificationOnTypeFacetFactory.class);
        addFactory(MustSatisfySpecificationOnPropertyFacetFactory.class);
        addFactory(MustSatisfySpecificationOnParameterFacetFactory.class);

        addFactory(MultiLineAnnotationOnTypeFacetFactory.class);
        addFactory(MultiLineOnPropertyFacetFactory.class);
        addFactory(MultiLineAnnotationOnParameterFacetFactory.class);

        addFactory(NamedAnnotationOnTypeFacetFactory.class);
        addFactory(NamedOnMemberFacetFactory.class);
        addFactory(NamedAnnotationOnParameterFacetFactory.class);

        addFactory(NotPersistableAnnotationFacetFactory.class);
        addFactory(NotPersistableMarkerInterfacesFacetFactory.class);

        addFactory(NotPersistedAnnotationForCollectionFacetFactory.class);
        addFactory(NotPersistedAnnotationForPropertyFacetFactory.class);

        addFactory(OptionalAnnotationForPropertyFacetFactory.class);
        addFactory(OptionalAnnotationForParameterFacetFactory.class);
        addFactory(MandatoryAnnotationForPropertyFacetFactory.class);

        addFactory(ParseableFacetFactory.class);
        addFactory(PluralAnnotationFacetFactory.class);
        addFactory(PagedAnnotationOnTypeFacetFactory.class);
        addFactory(PagedOnParentedCollectionFacetFactory.class);

        addFactory(AutoCompleteAnnotationFacetFactory.class);

        // must come after any facets that install titles
        addFactory(MaskAnnotationForTypeFacetFactory.class);
        addFactory(MaskAnnotationForPropertyFacetFactory.class);
        addFactory(MaskAnnotationForParameterFacetFactory.class);

        // must come after any facets that install titles, and after mask
        // if takes precedence over mask.
        addFactory(RegExFacetAnnotationForTypeFacetFactory.class);
        addFactory(RegExFacetAnnotationForPropertyFacetFactory.class);
        addFactory(RegExFacetAnnotationForParameterFacetFactory.class);

        addFactory(TypeOfAnnotationForCollectionsFacetFactory.class);
        addFactory(TypeOfAnnotationForActionsFacetFactory.class);

        addFactory(TypicalLengthFacetForPropertyDerivedFromTypeFacetFactory.class);
        addFactory(TypicalLengthFacetForParameterDerivedFromTypeFacetFactory.class);

        addFactory(TypicalLengthAnnotationOnTypeFacetFactory.class);
        addFactory(TypicalLengthOnPropertyFacetFactory.class);
        addFactory(TypicalLengthAnnotationOnParameterFacetFactory.class);
        addFactory(RenderedAsDayBeforeAnnotationOnPropertyFacetFactory.class);
        addFactory(RenderedAsDayBeforeAnnotationOnParameterFacetFactory.class);

        // built-in value types for Java language
        addFactory(BooleanPrimitiveValueTypeFacetFactory.class);
        addFactory(BooleanWrapperValueTypeFacetFactory.class);
        addFactory(BytePrimitiveValueTypeFacetFactory.class);
        addFactory(ByteWrapperValueTypeFacetFactory.class);
        addFactory(ShortPrimitiveValueTypeFacetFactory.class);
        addFactory(ShortWrapperValueTypeFacetFactory.class);
        addFactory(IntPrimitiveValueTypeFacetFactory.class);
        addFactory(IntWrapperValueTypeFacetFactory.class);
        addFactory(LongPrimitiveValueTypeFacetFactory.class);
        addFactory(LongWrapperValueTypeFacetFactory.class);
        addFactory(FloatPrimitiveValueTypeFacetFactory.class);
        addFactory(FloatWrapperValueTypeFacetFactory.class);
        addFactory(DoublePrimitiveValueTypeFacetFactory.class);
        addFactory(DoubleWrapperValueTypeFacetFactory.class);
        addFactory(CharPrimitiveValueTypeFacetFactory.class);
        addFactory(CharWrapperValueTypeFacetFactory.class);
        addFactory(BigIntegerValueTypeFacetFactory.class);
        addFactory(BigDecimalValueTypeFacetFactory.class);
        addFactory(JavaSqlDateValueTypeFacetFactory.class);
        addFactory(JavaSqlTimeValueTypeFacetFactory.class);
        addFactory(JavaUtilDateValueTypeFacetFactory.class);
        addFactory(JavaSqlTimeStampValueTypeFacetFactory.class);
        addFactory(StringValueTypeFacetFactory.class);
        addFactory(URLValueTypeFacetFactory.class);
        addFactory(UUIDValueTypeFacetFactory.class);

        addFactory(JavaAwtImageValueTypeFacetFactory.class);
        
        // applib values
        addFactory(BlobValueTypeFacetFactory.class);
        addFactory(ClobValueTypeFacetFactory.class);
        addFactory(DateValueTypeFacetFactory.class);
        addFactory(DateTimeValueTypeFacetFactory.class);
        addFactory(ColorValueTypeFacetFactory.class);
        addFactory(MoneyValueTypeFacetFactory.class);
        addFactory(PasswordValueTypeFacetFactory.class);
        addFactory(PercentageValueTypeFacetFactory.class);
        addFactory(TimeStampValueTypeFacetFactory.class);
        addFactory(TimeValueTypeFacetFactory.class);
        addFactory(ImageValueTypeFacetFactory.class);

        // jodatime values
        addFactory(JodaLocalDateValueTypeFacetFactory.class);
        addFactory(JodaLocalDateTimeValueTypeFacetFactory.class);
        addFactory(JodaDateTimeValueTypeFacetFactory.class);
        
        // written to not trample over TypeOf if already installed
        addFactory(CollectionFacetFactory.class);
        // must come after CollectionFacetFactory
        addFactory(ParentedSinceCollectionFacetFactory.class);

        // so we can dogfood the NO applib "value" types
        addFactory(ValueFacetFactory.class);


        //
        // services
        //
        
        addFactory(CommandAnnotationFacetFactory.class);
        // will not trample over CommandFacet if already installed
        // must be after ActionSemantics facet setup.
        addFactory(CommandFromConfigurationFacetFactory.class);

        addFactory(AuditableFromAuditedAnnotationFacetFactory.class);
        addFactory(AuditableMarkerInterfaceFacetFactory.class);
        // will not trample over AuditableFacet if already installed
        addFactory(AuditableFromConfigurationFacetFactory.class);

        addFactory(PublishedActionAnnotationFacetFactory.class);
        addFactory(PublishedObjectAnnotationFacetFactory.class);

        addFactory(FacetsAnnotationFacetFactory.class);
    }




}
