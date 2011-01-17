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


package org.apache.isis.defaults.progmodel;

import org.apache.isis.core.metamodel.facets.object.isis.RemoveSetDomainObjectContainerMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.isis.RemoveStaticGettersAndSettersFacetFactory;
import org.apache.isis.core.metamodel.facets.object.java5.RemoveGetClassMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.java5.RemoveInitMethodFacetFactory;
import org.apache.isis.core.metamodel.facets.object.java5.RemoveJavaLangObjectMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.java5.RemoveSuperclassMethodsFacetFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.progmodel.facets.FallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.actcoll.typeof.TypeOfAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.debug.annotation.DebugAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.executed.annotation.ExecutedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.executed.prefix.ExecutedViaNamingConventionFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.exploration.annotation.ExplorationAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.invoke.ActionInvocationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.notcontributed.annotation.NotContributedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.notinservicemenu.annotation.NotInServiceMenuAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.prototype.annotation.PrototypeAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.validate.method.ActionValidationFacetViaValidateMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.accessor.CollectionAccessorFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.aggregated.AggregatedIfCollectionFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.clear.CollectionClearFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.collection.CollectionFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.modify.CollectionAddRemoveAndValidateFacetFactory;
import org.apache.isis.core.progmodel.facets.defaults.DefaultedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.describedas.annotation.DescribedAsAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.encodeable.EncodableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.ignore.annotation.RemoveIgnoreAnnotationMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.members.describedas.staticmethod.DescribedAsFacetViaDescriptionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disable.annotation.DisabledAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disable.forsession.DisabledFacetViaDisableForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disable.method.DisabledFacetViaDisableMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.disable.staticmethod.DisabledFacetViaProtectMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hide.annotation.HiddenAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hide.forsession.HiddenFacetViaHideForSessionMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hide.method.HiddenFacetViaHideMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.hide.staticmethod.HiddenFacetViaAlwaysHideMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.members.name.staticmethod.NamedFacetViaNameMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.named.annotation.NamedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.IteratorFilteringFacetFactory;
import org.apache.isis.core.progmodel.facets.object.SyntheticMethodFilteringFacetFactory;
import org.apache.isis.core.progmodel.facets.object.aggregated.annotation.AggregatedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.bounded.annotation.BoundedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.bounded.markerifc.BoundedMarkerInterfaceFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.create.CreatedCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.load.LoadCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.persist.PersistCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.persist.PersistCallbackViaSaveMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.remove.RemoveCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.update.UpdateCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.dirty.method.DirtyMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.object.facets.annotation.FacetsAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.icon.method.IconMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ident.singular.SingularMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ident.title.TitleMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.immutable.ImmutableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.immutable.ImmutableMarkerInterfacesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.notpersistable.NotPersistableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.notpersistable.NotPersistableMarkerInterfacesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.parseable.ParseableFacetFactory;
import org.apache.isis.core.progmodel.facets.object.plural.annotation.PluralAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.plural.staticmethod.PluralMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.validate.ValidateObjectViaValidateMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.validprops.ObjectValidPropertiesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactory;
import org.apache.isis.core.progmodel.facets.ordering.actionorder.ActionOrderAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.ordering.fieldorder.FieldOrderAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.ordering.memberorder.MemberOrderAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.param.choices.method.ActionChoicesFacetFactory;
import org.apache.isis.core.progmodel.facets.param.choices.methodnum.ActionParameterChoicesFacetFactory;
import org.apache.isis.core.progmodel.facets.param.defaults.method.ActionDefaultsFacetFactory;
import org.apache.isis.core.progmodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetFactory;
import org.apache.isis.core.progmodel.facets.propcoll.notpersisted.NotPersistedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.accessor.PropertyAccessorFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.choices.PropertyChoicesFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.defaults.PropertyDefaultFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.mandatory.PropertyOptionalFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertyModifyFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertySetAndClearFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateDefaultFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.enums.EnumFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.enums.PropertyAndParameterChoicesFacetDerivedFromChoicesFacetFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.multiline.annotation.MultiLineAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.specification.MustSatisfySpecificationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.typicallength.annotation.TypicalLengthAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.typicallength.derived.TypicalLengthDerivedFromTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.mandatory.annotation.OptionalAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.mandatory.dflt.MandatoryDefaultFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.mask.annotation.MaskAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.maxlength.annotation.MaxLengthAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.regex.annotation.RegExAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.value.bigdecimal.BigDecimalValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.biginteger.BigIntegerValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.booleans.BooleanPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.booleans.BooleanWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.bytes.BytePrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.bytes.ByteWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.chars.CharPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.chars.CharWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.color.ColorValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.date.DateValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.datesql.JavaSqlDateValueTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.value.datetime.DateTimeValueTypeFacetFactory;
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


public class ProgrammingModelFacetsJava5 extends ProgrammingModelAbstract {
	
	public ProgrammingModelFacetsJava5() {
		
        // must be first, so any Facets created can be replaced by other FacetFactorys later.
        addFactory(FallbackFacetFactory.class);

        addFactory(IteratorFilteringFacetFactory.class);
        addFactory(SyntheticMethodFilteringFacetFactory.class);
        addFactory(RemoveSuperclassMethodsFacetFactory.class);
        addFactory(RemoveJavaLangObjectMethodsFacetFactory.class);
        addFactory(RemoveSetDomainObjectContainerMethodFacetFactory.class);
        addFactory(RemoveInitMethodFacetFactory.class);
        addFactory(RemoveStaticGettersAndSettersFacetFactory.class);
        addFactory(RemoveGetClassMethodFacetFactory.class);
        addFactory(RemoveIgnoreAnnotationMethodsFacetFactory.class);

        // must be before any other FacetFactories that install MandatoryFacet.class facets
        addFactory(MandatoryDefaultFacetFactory.class);
        addFactory(PropertyValidateDefaultFacetFactory.class);

        // properties
        addFactory(PropertyAccessorFacetFactory.class);
        addFactory(PropertySetAndClearFacetFactory.class);
        addFactory(PropertyModifyFacetFactory.class); // must come after PropertySetAndClearFacetFactory
        addFactory(PropertyValidateFacetFactory.class);
        addFactory(PropertyChoicesFacetFactory.class);
        addFactory(PropertyDefaultFacetFactory.class);
        addFactory(PropertyOptionalFacetFactory.class);

        // collections
        addFactory(CollectionAccessorFacetFactory.class);
        addFactory(CollectionClearFacetFactory.class);
        addFactory(CollectionAddRemoveAndValidateFacetFactory.class);

        // actions
        addFactory(ActionInvocationFacetFactory.class);
        addFactory(ActionValidationFacetViaValidateMethodFacetFactory.class);
        addFactory(ActionChoicesFacetFactory.class);
        addFactory(ActionParameterChoicesFacetFactory.class);
        addFactory(ActionDefaultsFacetFactory.class);
        addFactory(ActionParameterDefaultsFacetFactory.class);
        
        // members in general
        addFactory(NamedFacetViaNameMethodFacetFactory.class);
        addFactory(DescribedAsFacetViaDescriptionMethodFacetFactory.class);
        addFactory(DisabledFacetViaDisableForSessionMethodFacetFactory.class);
        addFactory(DisabledFacetViaDisableMethodFacetFactory.class);
        addFactory(DisabledFacetViaProtectMethodFacetFactory.class);
        addFactory(HiddenFacetViaHideForSessionMethodFacetFactory.class);
        addFactory(HiddenFacetViaAlwaysHideMethodFacetFactory.class);
        addFactory(HiddenFacetViaHideMethodFacetFactory.class);
        
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
        addFactory(SingularMethodFacetFactory.class);
        addFactory(TitleMethodFacetFactory.class);

        addFactory(ActionOrderAnnotationFacetFactory.class);
        addFactory(AggregatedAnnotationFacetFactory.class);
        addFactory(BoundedAnnotationFacetFactory.class);
        addFactory(BoundedMarkerInterfaceFacetFactory.class);
        addFactory(DebugAnnotationFacetFactory.class);
        addFactory(DefaultedAnnotationFacetFactory.class);
        addFactory(DescribedAsAnnotationFacetFactory.class);
        addFactory(DisabledAnnotationFacetFactory.class);
        addFactory(EncodableAnnotationFacetFactory.class);
        addFactory(ExecutedAnnotationFacetFactory.class);
        addFactory(ExecutedViaNamingConventionFacetFactory.class);
        addFactory(ExplorationAnnotationFacetFactory.class);
        addFactory(PrototypeAnnotationFacetFactory.class);
        addFactory(NotContributedAnnotationFacetFactory.class);
        addFactory(NotInServiceMenuAnnotationFacetFactory.class);
        addFactory(FieldOrderAnnotationFacetFactory.class);
        addFactory(HiddenAnnotationFacetFactory.class);
        addFactory(ImmutableAnnotationFacetFactory.class);
        addFactory(ImmutableMarkerInterfacesFacetFactory.class);
        addFactory(MaxLengthAnnotationFacetFactory.class);
        addFactory(MemberOrderAnnotationFacetFactory.class);
        addFactory(MustSatisfySpecificationFacetFactory.class);
        addFactory(MultiLineAnnotationFacetFactory.class);
        addFactory(NamedAnnotationFacetFactory.class);
        addFactory(NotPersistableAnnotationFacetFactory.class);
        addFactory(NotPersistableMarkerInterfacesFacetFactory.class);
        addFactory(NotPersistedAnnotationFacetFactory.class);
        addFactory(OptionalAnnotationFacetFactory.class);
        addFactory(ParseableFacetFactory.class);
        addFactory(PluralAnnotationFacetFactory.class);
        // must come after any facets that install titles
        addFactory(MaskAnnotationFacetFactory.class);
        // must come after any facets that install titles, and after mask
        // if takes precedence over mask.
        addFactory(RegExAnnotationFacetFactory.class);
        addFactory(TypeOfAnnotationFacetFactory.class);
        addFactory(TypicalLengthAnnotationFacetFactory.class);
        addFactory(TypicalLengthDerivedFromTypeFacetFactory.class);

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
        
        // applib values
        addFactory(DateValueTypeFacetFactory.class);
        addFactory(DateTimeValueTypeFacetFactory.class);
        addFactory(ColorValueTypeFacetFactory.class);
        addFactory(MoneyValueTypeFacetFactory.class);
        addFactory(PasswordValueTypeFacetFactory.class);
        addFactory(PercentageValueTypeFacetFactory.class);
        addFactory(TimeStampValueTypeFacetFactory.class);
        addFactory(TimeValueTypeFacetFactory.class);
        addFactory(ImageValueTypeFacetFactory.class);        
        addFactory(JavaAwtImageValueTypeFacetFactory.class);

        // enum support
        addFactory(EnumFacetFactory.class);
        addFactory(PropertyAndParameterChoicesFacetDerivedFromChoicesFacetFacetFactory.class);

        // written to not trample over TypeOf if already installed
        addFactory(CollectionFacetFactory.class);
        // must come after CollectionFacetFactory
        addFactory(AggregatedIfCollectionFacetFactory.class);
        
        // so we can dogfood the NO applib "value" types
        addFactory(ValueFacetFactory.class);

        addFactory(FacetsAnnotationFacetFactory.class);
	}


}
