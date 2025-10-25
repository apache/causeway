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
package org.apache.causeway.core.metamodel.util;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainServiceLayout.MenuBar;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.factory._InstanceUtil;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedType;
import org.apache.causeway.core.config.metamodel.facets.ParameterConfigOptions;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.autocomplete.AutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.causeway.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.facets.object.projection.ProjectionFacet;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.facets.object.tabledec.TableDecoratorFacet;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.facets.object.value.ValueSerializer;
import org.apache.causeway.core.metamodel.facets.objectvalue.daterenderedadjust.DateRenderAdjustFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MaxTotalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.digits.MinFractionalDigitsFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.fileaccept.FileAcceptFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.multiline.MultiLineFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.causeway.core.metamodel.facets.param.parameter.precpol.PrecedingParametersPolicyFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;

import lombok.experimental.UtilityClass;

/**
 * Facet utility.
 * <p>
 * Motivation: Viewers should not use {@link Facet}s directly.
 */
@UtilityClass
public final class Facets {

    public Can<ManagedObject> autoCompleteExecute(
            final ObjectSpecification objectSpec, final String term) {
        return objectSpec.lookupFacet(AutoCompleteFacet.class)
            .map(autoCompleteFacet->autoCompleteFacet.execute(term, InteractionInitiatedBy.USER))
            .orElseGet(Can::empty);
    }

    public boolean autoCompleteIsPresent(final ObjectSpecification objectSpec) {
        return objectSpec.containsFacet(AutoCompleteFacet.class);
    }

    public OptionalInt autoCompleteMinLength(final ObjectSpecification objectSpec) {
        return objectSpec.lookupFacet(AutoCompleteFacet.class)
            .map(AutoCompleteFacet::getMinLength)
            .map(OptionalInt::of)
            .orElseGet(OptionalInt::empty);
    }

    public Optional<BookmarkPolicy> bookmarkPolicy(final @Nullable FacetHolder facetHolder) {
        return Optional.ofNullable(facetHolder)
            .flatMap(spec->spec.lookupFacet(BookmarkPolicyFacet.class))
            .map(BookmarkPolicyFacet::value);
    }

    public BookmarkPolicy bookmarkPolicyOrElseNotSpecified(final @Nullable FacetHolder facetHolder) {
        return bookmarkPolicy(facetHolder).orElse(BookmarkPolicy.NOT_SPECIFIED);
    }

    public Optional<BSGrid> bootstrapGrid(
            final ObjectSpecification objectSpec, final @Nullable ManagedObject mo) {
        return objectSpec.lookupFacet(GridFacet.class)
            .filter(facet->facet.supports(BSGrid.class))
            .map(gridFacet->gridFacet.getGrid(mo))
            .flatMap(grid->_Casts.castTo(BSGrid.class, grid));
    }

    public Optional<BSGrid> bootstrapGrid(
            final ObjectSpecification objectSpec) {
        return bootstrapGrid(objectSpec, null);
    }

    public boolean collectionIsPresent(final ObjectSpecification objectSpec) {
        return objectSpec.containsFacet(CollectionFacet.class);
    }

    //XXX could be moved to ManagedObject directly, there be an utility already under a different name
    public Stream<ManagedObject> collectionStream(final @Nullable ManagedObject collection) {
        return CollectionFacet.streamAdapters(collection);
    }

    // used for non-scalar action return
    public Stream<ManagedObject> collectionStream(
            final ObjectSpecification objectSpec, final @Nullable ManagedObject collection) {
        return objectSpec.lookupFacet(CollectionFacet.class)
            .map(collectionFacet->collectionFacet.stream(collection))
            .orElseGet(Stream::empty);
    }

    public Optional<String> cssClass(
            final FacetHolder facetHolder, final ManagedObject objectAdapter) {
        return facetHolder.lookupFacet(CssClassFacet.class)
            .map(cssClassFacet->cssClassFacet.cssClass(objectAdapter));
    }

    public int dateRenderAdjustDays(final ObjectFeature feature) {
        return feature.lookupFacet(DateRenderAdjustFacet.class)
            .map(DateRenderAdjustFacet::getDateRenderAdjustDays)
            .orElse(0);
    }

    public boolean defaultViewIsPresent(final ObjectFeature feature) {
        return feature.containsFacet(DefaultViewFacet.class);
    }

    public boolean defaultViewIsTable(final ObjectFeature feature) {
        return defaultViewName(feature)
            .map(viewName->"table".equals(viewName))
            .orElse(false);
    }

    public Optional<String> defaultViewName(final ObjectFeature feature) {
        return feature.lookupFacet(DefaultViewFacet.class)
            .map(DefaultViewFacet::value);
    }

    public static ParameterConfigOptions.PrecedingParametersPolicy precedingParametersPolicy(
            final ObjectActionParameter parameter) {
        return parameter.lookupFacet(PrecedingParametersPolicyFacet.class)
            .map(PrecedingParametersPolicyFacet::value)
            .orElseGet(ParameterConfigOptions.PrecedingParametersPolicy::defaultsIfNotSpecifiedOtherwise);
    }

    public Optional<MenuBar> domainServiceLayoutMenuBar(final ObjectSpecification objectSpec) {
        return objectSpec.lookupFacet(DomainServiceLayoutFacet.class)
            .map(DomainServiceLayoutFacet::getMenuBar);
    }

    public Optional<String> fileAccept(final ObjectFeature feature) {
        return feature.lookupFacet(FileAcceptFacet.class)
            .map(FileAcceptFacet::value);
    }

    public void gridPreload(
            final ObjectSpecification objectSpec, final ManagedObject objectAdapter) {
        objectSpec.lookupFacet(GridFacet.class)
            .ifPresent(gridFacet->
                // the facet should always exist, in fact
                // just enough to ask for the metadata.
                // This will cause the current ObjectSpec to be updated as a side effect.
                gridFacet.getGrid(objectAdapter));
    }

    public boolean hasSafeSemantics(final FacetHolder holder) {
        return holder.lookupFacet(ActionSemanticsFacet.class)
            .map(ActionSemanticsFacet::value)
            .map(SemanticsOf::isSafeInNature)
            .orElse(false);
    }

    public Optional<Where> hiddenWhere(final ObjectFeature feature) {
        return feature.lookupFacet(HiddenFacet.class)
            .map(HiddenFacet::where);
    }

    public Predicate<ObjectFeature> hiddenWhereMatches(final Predicate<Where> matcher) {
        return feature->Facets.hiddenWhere(feature)
            .map(matcher::test)
            .orElse(false);
    }

    public boolean iconIsPresent(final ObjectSpecification objectSpec) {
        return objectSpec.containsFacet(IconFacet.class);
    }

    /**
     * Non-null, one of {LEFT, RIGHT, TOP or NONE}.
     */
    public LabelPosition labelAt(final ObjectFeature feature) {
        return feature.lookupFacet(LabelAtFacet.class)
            .map(LabelAtFacet::label)
            .map(labelPos->{
                switch (labelPos) {
                case LEFT:
                case RIGHT:
                case NONE:
                case TOP:
                    return labelPos;
                case DEFAULT:
                case NOT_SPECIFIED:
                default:
                    return LabelPosition.LEFT;
                }
            })
            .orElse(LabelPosition.LEFT);
    }

    public String labelAtCss(final ObjectFeature feature) {
        return "label-" + labelAt(feature).name().toLowerCase();
    }

    public OptionalInt minFractionalDigits(final FacetHolder facetHolder) {
        return facetHolder.lookupFacet(MinFractionalDigitsFacet.class)
            .map(MinFractionalDigitsFacet::getMinFractionalDigits)
            .filter(digits->digits>-1)
            .map(OptionalInt::of)
            .orElseGet(OptionalInt::empty);
    }

    public OptionalInt maxFractionalDigits(final FacetHolder facetHolder) {
        return facetHolder.lookupFacet(MaxFractionalDigitsFacet.class)
            .map(MaxFractionalDigitsFacet::getMaxFractionalDigits)
            .filter(digits->digits>-1)
            .map(OptionalInt::of)
            .orElseGet(OptionalInt::empty);
    }

    public OptionalInt maxFractionalDigits(final @Nullable Iterable<FacetHolder> facetHolders) {
        return _NullSafe.stream(facetHolders)
            .map(Facets::maxFractionalDigits)
            .findFirst()
            .orElseGet(OptionalInt::empty);
    }

    public OptionalInt maxLength(final FacetHolder facetHolder) {
        return facetHolder
            .lookupFacet(MaxLengthFacet.class)
            .map(MaxLengthFacet::value)
            .map(OptionalInt::of)
            .orElseGet(OptionalInt::empty);
    }

    public OptionalInt maxTotalDigits(final FacetHolder facetHolder) {
        return facetHolder.lookupFacet(MaxTotalDigitsFacet.class)
            .map(MaxTotalDigitsFacet::getMaxTotalDigits)
            .map(OptionalInt::of)
            .orElseGet(OptionalInt::empty);
    }

    public OptionalInt maxTotalDigits(final @Nullable Iterable<FacetHolder> facetHolders) {
        return _NullSafe.stream(facetHolders)
            .map(Facets::maxTotalDigits)
            .findFirst()
            .orElseGet(OptionalInt::empty);
    }

    public boolean mixinIsPresent(final ObjectSpecification objectSpec) {
        return objectSpec.containsFacet(MixinFacet.class);
    }

    public boolean multilineIsPresent(final ObjectFeature feature) {
        return feature.lookupNonFallbackFacet(MultiLineFacet.class)
            .isPresent();
    }

    public OptionalInt multilineNumberOfLines(final ObjectFeature feature) {
        return feature
            .lookupFacet(MultiLineFacet.class)
            .map(MultiLineFacet::numberOfLines)
            .map(OptionalInt::of)
            .orElseGet(OptionalInt::empty);
    }

    //XXX could be moved to ManagedObject directly
    public ManagedObject projected(final ManagedObject objectAdapter) {
        return objectAdapter.objSpec().lookupFacet(ProjectionFacet.class)
            .map(projectionFacet->projectionFacet.projected(objectAdapter))
            .orElse(objectAdapter);
    }

    public Optional<PromptStyle> promptStyle(final ObjectFeature feature) {
        return feature.lookupFacet(PromptStyleFacet.class)
            .map(PromptStyleFacet::value);
    }

    public PromptStyle promptStyleOrElse(final ObjectFeature feature, final PromptStyle fallback) {
        return Facets.promptStyle(feature)
            .map(promptStyle->
                promptStyle == PromptStyle.AS_CONFIGURED
                ? fallback
                : promptStyle)
            .orElse(fallback);
    }

    public Optional<ObjectSpecification> elementSpec(final FacetHolder facetHolder) {
        return facetHolder.lookupFacet(TypeOfFacet.class)
            .map(TypeOfFacet::elementSpec);
    }

    public Optional<ResolvedType> typeOfAnyCardinality(final FacetHolder facetHolder) {
        return facetHolder.lookupFacet(TypeOfFacet.class)
            .map(TypeOfFacet::value);
    }

    public OptionalInt typicalLength(
            final ObjectSpecification objectSpec, final OptionalInt maxLength) {
        var typicalLength = objectSpec
                .lookupFacet(TypicalLengthFacet.class)
                .map(TypicalLengthFacet::value)
                .orElse(null);
        // doesn't make sense for typical length to be > maxLength
        final Integer result = (typicalLength != null
                && maxLength.isPresent()
                && typicalLength > maxLength.getAsInt())
                ? (Integer)maxLength.getAsInt()
                : typicalLength;
        return Optional.ofNullable(result)
                .map(OptionalInt::of)
                .orElseGet(OptionalInt::empty);
    }

    // -- TABLES

    public Optional<TableDecorator> tableDecorator(final FacetHolder ... facetHolders) {
        return FacetUtil.lookupFacetInButExcluding(TableDecoratorFacet.class, o -> o == TableDecorator.Default.class, facetHolders)
                .stream()
                .map(TableDecoratorFacet::value)
                .map(decoratorClass -> {
                    var decorator = _InstanceUtil.createInstance(decoratorClass, decoratorClass);
                    return facetHolders[0].injectServicesInto(decorator);
                })
                .findFirst();
    }

    // -- VALUE FACET

    public static Predicate<ObjectSpecification> valueTypeMatches(final Predicate<Class<?>> typeMatcher) {
        return spec->
            spec.valueFacet()
            .map(ValueFacet::getLogicalType)
            .map(LogicalType::correspondingClass)
            .map(typeMatcher::test)
            .orElse(false);
    }

    public Optional<ObjectAction> valueCompositeMixinForParameter(
            final ObjectFeature param,
            final ParameterNegotiationModel parameterNegotiationModel,
            final int paramIndex) {
        var objectSpec = param.getElementType();
        return objectSpec.valueFacet()
            .<ObjectAction>flatMap(valueFacet->
                valueFacet.selectCompositeValueMixinForParameter(
                        parameterNegotiationModel,paramIndex));
    }

    public Optional<ObjectAction> valueCompositeMixinForProperty(
            final ObjectFeature prop,
            final ManagedProperty managedProperty) {
        var objectSpec = prop.getElementType();
        return objectSpec.valueFacet()
            .<ObjectAction>flatMap(valueFacet->
                valueFacet.selectCompositeValueMixinForProperty(managedProperty));
    }

    public <X> Stream<X> valueStreamSemantics(
            final ObjectSpecification objectSpec, final Class<X> requiredType) {
        return objectSpec.valueFacet()
            .map(valueFacet->valueFacet.streamValueSemantics(requiredType))
            .orElseGet(Stream::empty);
    }

    public <X> boolean valueHasSemantics(
            final ObjectSpecification objectSpec, final Class<X> requiredType) {
        return objectSpec.valueFacet()
            .map(valueFacet->valueFacet.streamValueSemantics(requiredType)
                    .findFirst()
                    .isPresent())
            .orElse(false);
    }

    public <X> Optional<ValueSemanticsProvider<X>> valueDefaultSemantics(
            final ObjectSpecification objectSpec,
            final Class<X> requiredType) {
        return objectSpec.valueFacet()
            .filter(typeGuard(requiredType))
            .flatMap(ValueFacet::selectDefaultSemantics)
            .map(_Casts::uncheckedCast);
    }

    @SuppressWarnings("unchecked")
    public <X> Optional<ValueSerializer<X>> valueSerializer(
            final ObjectSpecification objectSpec,
            final Class<X> requiredType) {
        return objectSpec.valueFacet()
            .filter(typeGuard(requiredType))
            .map(valueFacet->(ValueSerializer<X>)valueFacet);
    }

    public <X> ValueSerializer<X> valueSerializerElseFail(
            final ObjectSpecification objectSpec,
            final Class<X> requiredType) {
        return valueSerializer(objectSpec, requiredType)
            .orElseThrow(()->_Exceptions.illegalArgument(
                    "ObjectSpec is expected to have a ValueFacet<%s>",
                    objectSpec.getCorrespondingClass().getName()));
    }

    // -- HELPER

    @SuppressWarnings("rawtypes")
    private Predicate<? super ValueFacet> typeGuard(
            final Class<?> requiredType) {
        return valueFacet->
            ClassUtils.resolvePrimitiveIfNecessary(requiredType)
            .isAssignableFrom(
                    ClassUtils.resolvePrimitiveIfNecessary(valueFacet.getValueClass()));
    }

}
