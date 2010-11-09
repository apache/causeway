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

import org.apache.isis.core.metamodel.java5.RemoveGetClassMethodFacetFactory;
import org.apache.isis.core.metamodel.java5.RemoveInitMethodFacetFactory;
import org.apache.isis.core.metamodel.java5.RemoveJavaLangObjectMethodsFacetFactory;
import org.apache.isis.core.metamodel.java5.RemoveSetDomainObjectContainerMethodFacetFactory;
import org.apache.isis.core.metamodel.java5.RemoveStaticGettersAndSettersFacetFactory;
import org.apache.isis.core.metamodel.java5.RemoveSuperclassMethodsFacetFactory;
import org.apache.isis.core.metamodel.specloader.progmodelfacets.ProgrammingModelFacetsAbstract;
import org.apache.isis.core.progmodel.facets.actcoll.typeof.TypeOfAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.ActionMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.IteratorFilteringFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.SyntheticMethodFilteringFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.debug.DebugAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.executed.ExecutedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.executed.ExecutedViaNamingConventionFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.exploration.ExplorationAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.notcontributed.NotContributedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.notinrepositorymenu.NotInRepositoryMenuAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.actions.prototype.PrototypeAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.CollectionFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.CollectionFieldMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.collections.aggregated.AggregatedIfCollectionFacetFactory;
import org.apache.isis.core.progmodel.facets.disable.DisabledAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.hide.HiddenAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.ignore.RemoveIgnoreAnnotationMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.naming.describedas.DescribedAsAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.naming.named.NamedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.aggregated.AggregatedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.bounded.BoundedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.bounded.BoundedMarkerInterfaceFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.CreatedCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.LoadCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.PersistCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.RemoveCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.SaveCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.callbacks.UpdateCallbackFacetFactory;
import org.apache.isis.core.progmodel.facets.object.defaults.DefaultedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.dirty.DirtyMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.object.encodeable.EncodableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.facets.FacetsAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ident.icon.IconMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ident.plural.PluralAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ident.plural.PluralMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ident.singular.SingularMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.ident.title.TitleMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.immutable.ImmutableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.immutable.ImmutableMarkerInterfacesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.notpersistable.NotPersistableAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.object.notpersistable.NotPersistableMarkerInterfacesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.parseable.ParseableFacetFactory;
import org.apache.isis.core.progmodel.facets.object.validate.ValidateObjectViaValidateMethodFacetFactory;
import org.apache.isis.core.progmodel.facets.object.validprops.ObjectValidPropertiesFacetFactory;
import org.apache.isis.core.progmodel.facets.object.value.ValueFacetFactory;
import org.apache.isis.core.progmodel.facets.ordering.actionorder.ActionOrderAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.ordering.fieldorder.FieldOrderAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.ordering.memberorder.MemberOrderAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propcoll.notpersisted.NotPersistedAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.PropertyMethodsFacetFactory;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateDefaultFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.enums.EnumFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.enums.PropertyAndParameterChoicesFacetDerivedFromChoicesFacetFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.multiline.MultiLineAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.specification.MustSatisfySpecificationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.typicallength.TypicalLengthAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.typicallength.TypicalLengthDerivedFromTypeFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.mandatory.MandatoryDefaultFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.mandatory.OptionalAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.mask.MaskAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.maxlength.MaxLengthAnnotationFacetFactory;
import org.apache.isis.core.progmodel.facets.propparam.validate.regex.RegExAnnotationFacetFactory;
import org.apache.isis.core.progmodel.java5.FallbackFacetFactory;
import org.apache.isis.core.progmodel.value.BigDecimalValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.BigIntegerValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.BooleanPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.BooleanWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.BytePrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.ByteWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.CharPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.CharWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.ColorValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.DateTimeValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.DateValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.DoublePrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.DoubleWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.FloatPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.FloatWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.ImageValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.IntPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.IntWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.JavaAwtImageValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.JavaSqlDateValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.JavaSqlTimeStampValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.JavaSqlTimeValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.JavaUtilDateValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.LongPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.LongWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.MoneyValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.PasswordValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.PercentageValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.ShortPrimitiveValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.ShortWrapperValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.StringValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.TimeStampValueTypeFacetFactory;
import org.apache.isis.core.progmodel.value.TimeValueTypeFacetFactory;


public class ProgrammingModelFacetsJava5 extends ProgrammingModelFacetsAbstract {
	
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

        addFactory(ActionMethodsFacetFactory.class);
        addFactory(CollectionFieldMethodsFacetFactory.class);
        addFactory(PropertyMethodsFacetFactory.class);
        addFactory(IconMethodFacetFactory.class);
        
        addFactory(CreatedCallbackFacetFactory.class);
        addFactory(LoadCallbackFacetFactory.class);
        addFactory(SaveCallbackFacetFactory.class);
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
        addFactory(NotInRepositoryMenuAnnotationFacetFactory.class);
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
