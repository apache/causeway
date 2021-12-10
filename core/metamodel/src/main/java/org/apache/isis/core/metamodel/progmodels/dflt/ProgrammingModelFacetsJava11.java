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

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.actions.action.ActionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.action.ActionAnnotationShouldEnforceConcreteTypeToBeIncludedWithMetamodelValidator;
import org.apache.isis.core.metamodel.facets.actions.action.ActionChoicesForCollectionParameterFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.action.ActionOverloadingValidator;
import org.apache.isis.core.metamodel.facets.actions.contributing.derived.ContributingFacetFromMixinFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.homepage.annotation.HomePageFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.derived.NotInServiceMenuFacetFromDomainServiceFacetFactory;
import org.apache.isis.core.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.collections.accessor.CollectionAccessorFacetViaAccessorFactory;
import org.apache.isis.core.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.javautilcollection.CollectionFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.parented.ParentedFacetSinceCollectionFactory;
import org.apache.isis.core.metamodel.facets.collections.sortedby.annotation.SortedByFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.jaxb.JaxbFacetFactory;
import org.apache.isis.core.metamodel.facets.members.cssclass.annotprop.CssClassFacetOnActionFromConfiguredRegexFactory;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop.CssClassFaFacetOnMemberPostProcessor;
import org.apache.isis.core.metamodel.facets.members.described.method.DescribedAsFacetForMemberViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.hidden.method.HideForContextFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.members.named.method.NamedFacetForMemberViaMethodFactory;
import org.apache.isis.core.metamodel.facets.object.ViewModelSemanticCheckingFacetFactory;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.bookmarkable.BookmarkPolicyFacetFallbackFactory;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacetFactory;
import org.apache.isis.core.metamodel.facets.object.choices.enums.ChoicesFacetFromEnumFactory;
import org.apache.isis.core.metamodel.facets.object.domainobject.DomainObjectAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainobject.logicaltype.LogicalTypeFacetForLogicalTypeNameAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainobjectlayout.DomainObjectLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.domainservice.annotation.DomainServiceFacetAnnotationFactory;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacetFactory;
import org.apache.isis.core.metamodel.facets.object.hidden.HiddenTypeFacetFromAuthorizationFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.annotation.RemoveAnnotatedMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.IteratorFilteringFacetFactory;
import org.apache.isis.core.metamodel.facets.object.ignore.javalang.RemoveMethodsFacetFactory;
import org.apache.isis.core.metamodel.facets.object.logicaltype.classname.LogicalTypeFacetFromClassNameFactory;
import org.apache.isis.core.metamodel.facets.object.navparent.annotation.NavigableParentAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.objectvalidprops.impl.ObjectValidPropertiesFacetImplFactory;
import org.apache.isis.core.metamodel.facets.object.recreatable.RecreatableObjectFacetFactory;
import org.apache.isis.core.metamodel.facets.object.support.ObjectSupportFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.annotation.TitleAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.object.value.annotcfg.ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory;
import org.apache.isis.core.metamodel.facets.param.autocomplete.method.ActionParameterAutoCompleteFacetViaMethodFactory;
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
import org.apache.isis.core.metamodel.facets.properties.choices.method.PropertyChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.method.PropertyDefaultFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.properties.disabled.inferred.DisabledFacetOnPropertyInferredFactory;
import org.apache.isis.core.metamodel.facets.properties.mandatory.dflt.MandatoryFacetOnProperyDefaultFactory;
import org.apache.isis.core.metamodel.facets.properties.property.PropertyAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.PropertyLayoutFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.update.PropertySetterFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.validating.dflt.PropertyValidateFacetDefaultFactory;
import org.apache.isis.core.metamodel.facets.properties.validating.method.PropertyValidateFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.value.semantics.ValueSemanticsAnnotationFacetFactory;
import org.apache.isis.core.metamodel.methods.DomainIncludeAnnotationEnforcesMetamodelContributionValidator;
import org.apache.isis.core.metamodel.methods.MethodByClassMap;
import org.apache.isis.core.metamodel.postprocessors.DeriveMixinMembersPostProcessor;
import org.apache.isis.core.metamodel.postprocessors.all.DeriveDescribedAsFromTypePostProcessor;
import org.apache.isis.core.metamodel.postprocessors.all.i18n.SynthesizeObjectNamingPostProcessor;
import org.apache.isis.core.metamodel.postprocessors.all.i18n.TranslationPostProcessor;
import org.apache.isis.core.metamodel.postprocessors.allbutparam.authorization.AuthorizationFacetPostProcessor;
import org.apache.isis.core.metamodel.postprocessors.collparam.DeriveCollectionParamDefaultsAndChoicesPostProcessor;
import org.apache.isis.core.metamodel.postprocessors.members.TweakDomainEventsForMixinPostProcessor;
import org.apache.isis.core.metamodel.postprocessors.members.navigation.DeriveNavigationFacetFromHiddenTypePostProcessor;
import org.apache.isis.core.metamodel.postprocessors.object.DeriveProjectionFacetsPostProcessor;
import org.apache.isis.core.metamodel.postprocessors.properties.DeriveDisabledFromImmutablePostProcessor;
import org.apache.isis.core.metamodel.postprocessors.propparam.DeriveChoicesFromExistingChoicesPostProcessor;
import org.apache.isis.core.metamodel.postprocessors.propparam.DeriveDefaultFromTypePostProcessor;
import org.apache.isis.core.metamodel.postprocessors.propparam.DeriveTypicalLengthFromTypePostProcessor;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.metamodel.services.classsubstitutor.ClassSubstitutorRegistry;
import org.apache.isis.core.metamodel.services.title.TitlesAndTranslationsValidator;

import lombok.val;

public final class ProgrammingModelFacetsJava11
extends ProgrammingModelAbstract {

    public ProgrammingModelFacetsJava11(final MetaModelContext mmc) {
        super(mmc);

        // act on the peer objects (FacetedMethod etc), rather than ObjectMembers etc
        addFacetFactories();

        // only during the post processors will the mixin members been resolved
        // and are available on the ObjectSpecification.
        addPostProcessors();

        addValidators();
    }

    private void addFacetFactories() {

        val mmc = getMetaModelContext();
        val classSubstitutorRegistry = mmc.getServiceRegistry().lookupServiceElseFail(ClassSubstitutorRegistry.class);

        // must be first, so any Facets created can be replaced by other
        // FacetFactorys later.
        addFactory(FacetProcessingOrder.A1_FALLBACK_DEFAULTS, new FallbackFacetFactory(mmc));

        addFactory(FacetProcessingOrder.B1_OBJECT_NAMING, new LogicalTypeFacetFromClassNameFactory(mmc, classSubstitutorRegistry));
        addFactory(FacetProcessingOrder.B1_OBJECT_NAMING, new DomainServiceFacetAnnotationFactory(mmc));
        addFactory(FacetProcessingOrder.B1_OBJECT_NAMING, new ValueFacetForValueAnnotationOrAnyMatchingValueSemanticsFacetFactory(mmc));

        addFactory(FacetProcessingOrder.C1_METHOD_REMOVING, new IteratorFilteringFacetFactory(mmc));

        addFactory(FacetProcessingOrder.C1_METHOD_REMOVING, new RemoveMethodsFacetFactory(mmc));

        addFactory(FacetProcessingOrder.C1_METHOD_REMOVING, new RemoveAnnotatedMethodsFacetFactory(mmc));

        // must be before any other FacetFactories that install MandatoryFacet.class facets
        addFactory(FacetProcessingOrder.D1_MANDATORY_SUPPORT, new MandatoryFacetOnProperyDefaultFactory(mmc));
        addFactory(FacetProcessingOrder.D1_MANDATORY_SUPPORT, new MandatoryFacetOnParametersDefaultFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new PropertyValidateFacetDefaultFactory(mmc));

        // enum support
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ChoicesFacetFromEnumFactory(mmc));

        // properties
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new PropertyAccessorFacetViaAccessorFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new PropertySetterFacetFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new PropertyValidateFacetViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new PropertyChoicesFacetViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new PropertyAutoCompleteFacetMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new PropertyDefaultFacetViaMethodFactory(mmc));

        // collections
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new CollectionAccessorFacetViaAccessorFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new SortedByFacetAnnotationFactory(mmc));

        // actions
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ActionParameterHiddenFacetViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ActionParameterDisabledFacetViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ActionValidationFacetViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ActionParameterValidationFacetViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ActionParameterChoicesFacetViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ActionParameterAutoCompleteFacetViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ActionParameterDefaultsFacetViaMethodFactory(mmc));

        // members in general
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new NamedFacetForMemberViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new DescribedAsFacetForMemberViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new DisableForContextFacetViaMethodFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new HideForContextFacetViaMethodFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new CallbackFacetFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ObjectValidPropertiesFacetImplFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new BookmarkPolicyFacetFallbackFactory(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new HomePageFacetAnnotationFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new NotInServiceMenuFacetFromDomainServiceFacetFactory(mmc));

        // must come after CssClassFacetOnMemberFactory
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new CssClassFacetOnActionFromConfiguredRegexFactory(mmc));

        val postConstructMethodsCache = new MethodByClassMap();

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new RecreatableObjectFacetFactory(mmc, postConstructMethodsCache));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new JaxbFacetFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new LogicalTypeFacetForLogicalTypeNameAnnotationFacetFactory(mmc, postConstructMethodsCache));

        // must come after RecreatableObjectFacetFactory
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new DomainObjectAnnotationFacetFactory(mmc, postConstructMethodsCache));

        // must come after the property/collection accessor+mutator facet factories
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ActionAnnotationFacetFactory(mmc));
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new PropertyAnnotationFacetFactory(mmc));
        // after the ActionAnnotationFacetFactory so that takes precedent for contributed associations
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new CollectionAnnotationFacetFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ParameterNameFacetFactoryUsingReflection(mmc));
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ParameterAnnotationFacetFactory(mmc));

        // must come after DomainObjectAnnotationFacetFactory & MixinFacetFactory
        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ContributingFacetFromMixinFacetFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new HiddenTypeFacetFromAuthorizationFactory(mmc));

        addFactory(FacetProcessingOrder.E1_MEMBER_MODELLING, new ValueSemanticsAnnotationFacetFactory(mmc));

        addFactory(FacetProcessingOrder.F1_LAYOUT, new GridFacetFactory(mmc));

        // must come before DomainObjectLayoutFacetFactory
        // (so subscribers on titleUi event etc can override) ... no longer true since we have event rank that overrules anyway
        addFactory(FacetProcessingOrder.F1_LAYOUT, new TitleAnnotationFacetFactory(mmc));

        addFactory(FacetProcessingOrder.F1_LAYOUT, new ObjectSupportFacetFactory(mmc));
        addFactory(FacetProcessingOrder.F1_LAYOUT, new NavigableParentAnnotationFacetFactory(mmc));

        addFactory(FacetProcessingOrder.F1_LAYOUT, new DomainServiceLayoutFacetFactory(mmc));
        addFactory(FacetProcessingOrder.F1_LAYOUT, new DomainObjectLayoutFacetFactory(mmc));

        // must come after MultiLine
        addFactory(FacetProcessingOrder.F1_LAYOUT, new PropertyLayoutFacetFactory(mmc));
        addFactory(FacetProcessingOrder.F1_LAYOUT, new ParameterLayoutFacetFactory(mmc));
        addFactory(FacetProcessingOrder.F1_LAYOUT, new ActionLayoutFacetFactory(mmc));
        addFactory(FacetProcessingOrder.F1_LAYOUT, new CollectionLayoutFacetFactory(mmc));

        // written to not trample over TypeOf if already installed
        addFactory(FacetProcessingOrder.Z1_FINALLY, new CollectionFacetFactory(mmc));
        // must come after CollectionFacetFactory
        addFactory(FacetProcessingOrder.Z1_FINALLY, new ParentedFacetSinceCollectionFactory(mmc));

        // should come near the end, after any facets that install PropertySetterFacet have run.
        addFactory(FacetProcessingOrder.Z1_FINALLY, new DisabledFacetOnPropertyInferredFactory(mmc));

        addFactory(FacetProcessingOrder.Z1_FINALLY, new ActionChoicesForCollectionParameterFacetFactory(mmc));

        addFactory(FacetProcessingOrder.Z1_FINALLY, new ViewModelSemanticCheckingFacetFactory(mmc));
    }

    private void addPostProcessors() {

        val mmc = getMetaModelContext();

        addPostProcessor(PostProcessingOrder.A0_BEFORE_BUILTIN, new DeriveMixinMembersPostProcessor(mmc));

        // only after this point have any mixin members been resolved and are available on the ObjectSpecification.

        // must run before Object nouns are used
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new SynthesizeObjectNamingPostProcessor(mmc));

        // requires member names to have settled
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new CssClassFaFacetOnMemberPostProcessor(mmc));

        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new DeriveDescribedAsFromTypePostProcessor(mmc));
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new DeriveTypicalLengthFromTypePostProcessor(mmc));
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new DeriveDefaultFromTypePostProcessor(mmc));
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new DeriveChoicesFromExistingChoicesPostProcessor(mmc));
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new DeriveDisabledFromImmutablePostProcessor(mmc));
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new DeriveCollectionParamDefaultsAndChoicesPostProcessor(mmc));
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new TweakDomainEventsForMixinPostProcessor(mmc));
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new DeriveProjectionFacetsPostProcessor(mmc));
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new DeriveNavigationFacetFromHiddenTypePostProcessor(mmc));

        // must be after all named facets and description facets have been installed
        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new TranslationPostProcessor(mmc));

        addPostProcessor(PostProcessingOrder.A1_BUILTIN, new AuthorizationFacetPostProcessor(mmc));
    }

    private void addValidators() {

        val mmc = getMetaModelContext();

        addValidator(new DomainIncludeAnnotationEnforcesMetamodelContributionValidator(mmc));
        addValidator(new TitlesAndTranslationsValidator(mmc));  // should this instead be a post processor, alongside TranslationPostProcessor ?
        addValidator(new ActionAnnotationShouldEnforceConcreteTypeToBeIncludedWithMetamodelValidator(mmc));
        addValidator(new ActionOverloadingValidator(mmc));
    }

}
