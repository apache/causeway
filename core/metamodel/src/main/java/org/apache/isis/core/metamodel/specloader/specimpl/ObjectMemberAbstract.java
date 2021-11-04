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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.Facet.Precedence;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.HasFacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.all.described.MemberDescribedFacet;
import org.apache.isis.core.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.interactions.AccessContext;
import org.apache.isis.core.metamodel.interactions.DisablingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.HidingInteractionAdvisor;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.command.CommandDtoFactory;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class ObjectMemberAbstract
implements
    ObjectMember,
    HasMetaModelContext,
    HasFacetHolder {

    @Getter(onMethod_ = {@Override}) private final @NonNull Identifier featureIdentifier;
    @Getter(onMethod_ = {@Override}) private final @NonNull FeatureType featureType;
    @Getter private final @NonNull FacetedMethod facetedMethod;

    // -- CONSTRUCTOR

    protected ObjectMemberAbstract(
            final Identifier featureIdentifier,
            final FacetedMethod facetedMethod,
            final FeatureType featureType) {

        this.featureIdentifier = featureIdentifier;
        this.facetedMethod = facetedMethod;
        this.featureType = featureType;
        if (getId() == null) {
            throw new IllegalArgumentException("Id must always be set");
        }
    }

    // -- IDENTIFIERS

    @Override
    public final String getId() {
        return getFeatureIdentifier().getMemberLogicalName();
    }

    // -- INTERACTION HEAD

    /**
     * To be overridden (only) by mixed in members!
     * @see MixedInMember
     */
    protected InteractionHead headFor(final ManagedObject ownerAdapter) {
        return InteractionHead.regular(ownerAdapter);
    }

    // -- Name, Description, Help (convenience for facets)

    @Override
    public final String getFriendlyName(final Supplier<ManagedObject> domainObjectProvider) {

        val namedFacet = getFacet(MemberNamedFacet.class);

        if(namedFacet==null) {
            throw _Exceptions.unrecoverableFormatted("no MemberNamedFacet preset on %s", getFeatureIdentifier());
        }

        return namedFacet
            .getSpecialization()
            .fold(  textFacet->textFacet.translated(),
                    textFacet->textFacet.textElseNull(headFor(domainObjectProvider.get()).getTarget()));
    }

    @Override
    public final Optional<String> getStaticFriendlyName() {
        return lookupFacet(MemberNamedFacet.class)
        .map(MemberNamedFacet::getSpecialization)
        .flatMap(specialization->specialization
                .fold(
                        textFacet->Optional.of(textFacet.translated()),
                        textFacet->Optional.empty()));
    }


    @Override
    public final Optional<String> getDescription(final Supplier<ManagedObject> domainObjectProvider) {
        return lookupFacet(MemberDescribedFacet.class)
        .map(MemberDescribedFacet::getSpecialization)
        .map(specialization->specialization
                .fold(textFacet->textFacet.translated(),
                      textFacet->textFacet.textElseNull(headFor(domainObjectProvider.get()).getTarget())));
    }

    @Override
    public final Optional<String> getStaticDescription() {
        return lookupFacet(MemberDescribedFacet.class)
                .map(MemberDescribedFacet::getSpecialization)
                .flatMap(specialization->specialization
                        .fold(
                                textFacet->Optional.of(textFacet.translated()),
                                textFacet->Optional.empty()));
    }


    @Override
    public final String getHelp() {
        final HelpFacet facet = getFacet(HelpFacet.class);
        return facet.value();
    }

    // -- CANONICAL NAMING

    @Override
    public final String getCanonicalFriendlyName() {
        return lookupFacet(MemberNamedFacet.class)
        .flatMap(MemberNamedFacet::getSharedFacetRanking)
        .flatMap(facetRanking->facetRanking.getWinnerNonEventLowerOrEqualTo(MemberNamedFacet.class, Precedence.HIGH))
        .map(MemberNamedFacet::getSpecialization)
        .flatMap(specialization->specialization.left())
        .map(HasStaticText::translated)
        //we have a facet-post-processor to ensure following code path is unreachable,
        //however, we keep it in support of JUnit testing
        .orElseGet(()->getFeatureIdentifier().getMemberNaturalName());
    }

    @Override
    public final Optional<String> getCanonicalDescription() {
        return lookupFacet(MemberDescribedFacet.class)
        .flatMap(MemberDescribedFacet::getSharedFacetRanking)
        .flatMap(facetRanking->facetRanking.getWinnerNonEventLowerOrEqualTo(MemberDescribedFacet.class, Precedence.HIGH))
        .map(MemberDescribedFacet::getSpecialization)
        .flatMap(specialization->specialization.left())
        .map(HasStaticText::translated);
    }


    // -- Hidden (or visible)
    /**
     * Create an {@link InteractionContext} to represent an attempt to view this
     * member (that is, to check if it is visible or not).
     *
     * <p>
     * Typically it is easier to just call
     * {@link ObjectMember#isVisible(ManagedObject, InteractionInitiatedBy, Where)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
    protected abstract VisibilityContext createVisibleInteractionContext(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where);



    @Override
    public boolean isAlwaysHidden() {
        return HiddenFacet.isAlwaysHidden(getFacetHolder());
    }

    /**
     * Loops over all {@link HidingInteractionAdvisor} {@link Facet}s and
     * returns <tt>true</tt> only if none hide the member.
     */
    @Override
    public Consent isVisible(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        val visibilityContext = createVisibleInteractionContext(target, interactionInitiatedBy, where);
        return InteractionUtils.isVisibleResult(this, visibilityContext).createConsent();
    }

    // -- Disabled (or enabled)
    /**
     * Create an {@link InteractionContext} to represent an attempt to
     * use this member (that is, to check if it is usable or not).
     *
     * <p>
     * Typically it is easier to just call
     * {@link ObjectMember#isUsable(ManagedObject, InteractionInitiatedBy, Where)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
    protected abstract UsabilityContext createUsableInteractionContext(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where);

    /**
     * Loops over all {@link DisablingInteractionAdvisor} {@link Facet}s and
     * returns <tt>true</tt> only if none disables the member.
     */
    @Override
    public Consent isUsable(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        val usabilityContext = createUsableInteractionContext(target, interactionInitiatedBy, where);
        return InteractionUtils.isUsableResult(this, usabilityContext).createConsent();
    }

    // -- PREDICATES

    @Override
    public final boolean isAction() {
        return featureType.isAction();
    }

    @Override
    public final boolean isPropertyOrCollection() {
        return featureType.isPropertyOrCollection();
    }

    @Override
    public final boolean isOneToManyAssociation() {
        return featureType.isCollection();
    }

    @Override
    public final boolean isOneToOneAssociation() {
        return featureType.isProperty();
    }

    // -- MIXIN ADAPTER FACTORY

    protected ManagedObject mixinAdapterFor(
            final @NonNull Class<?> mixinType,
            final @NonNull ManagedObject mixee) {

        val spec = getSpecificationLoader().loadSpecification(mixinType);
        val mixinFacet = spec.getFacet(MixinFacet.class);
        val mixinPojo = mixinFacet.instantiate(mixee.getPojo() /*nullable for action parameter mixins*/);
        return ManagedObject.of(spec, Objects.requireNonNull(mixinPojo));
    }

    // -- OBJECT CONTRACT

    @Override
    public String toString() {
        return getStaticFriendlyName()
                .map(name->String.format("id=%s,name='%s'", getId(), name))
                .orElseGet(()->String.format("id=%s,name=imperative", getId()));
    }

    // -- COMMAND SETUP

    protected void setupCommand(
            final InteractionHead head,
            final Function<UUID, CommandDto> commandDtoFactory) {

        val command = getInteractionContext().currentInteractionElseFail().getCommand();

        _Assert.assertNotNull(command,
            "No command available with current thread, "
                + "are we missing an interaction context?");

        if (command.getCommandDto() != null) {
            // guard here to prevent subsequent mixin actions from
            // trampling over the command's DTO
        } else {
            val dto = commandDtoFactory.apply(command.getInteractionId());
            command.updater().setCommandDto(dto);
        }

    }

    // -- DEPENDENCIES

    protected InteractionProvider getInteractionContext() {
        return getServiceRegistry().lookupServiceElseFail(InteractionProvider.class);
    }

    protected CommandDtoFactory getCommandDtoFactory() {
        return getServiceRegistry().lookupServiceElseFail(CommandDtoFactory.class);
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return facetedMethod.getMetaModelContext();
    }

}
