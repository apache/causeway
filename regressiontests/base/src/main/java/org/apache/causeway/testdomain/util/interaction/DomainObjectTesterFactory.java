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
package org.apache.causeway.testdomain.util.interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.function.ThrowingSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.ObjectSupport.IconSize;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.exceptions.unrecoverable.DomainModelException;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.facets.object.layout.LayoutPrefixFacet;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyNegotiationModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.testdomain.util.CollectionAssertions;
import org.apache.causeway.testing.integtestsupport.applib.validate.DomainModelValidator;

import lombok.Getter;
import lombok.SneakyThrows;

@Service
public class DomainObjectTesterFactory implements HasMetaModelContext {

    public <T> ObjectTester<T> objectTester(
            final T domainObject) {
        var tester = getServiceInjector().injectServicesInto(
                new ObjectTester<T>(domainObject));
        return tester;
    }

    public <T> ActionTester<T> actionTester(
            final T domainObject,
            final String actionName,
            final Where where) {
        var tester = getServiceInjector().injectServicesInto(
                new ActionTester<T>(domainObject, actionName, where));
        tester.init();
        return tester;
    }

    public <T> ActionTester<T> actionTesterForSpecificInteraction(
            final @NonNull T domainObject,
            final @NonNull ActionInteraction actionInteraction) {
        var managedAction = actionInteraction.getManagedActionElseFail();
        assertEquals(domainObject.getClass(),
                managedAction.getOwner().objSpec().getCorrespondingClass());
        var actionTester = getServiceInjector().injectServicesInto(
                new ActionTester<>(domainObject, actionInteraction, managedAction));
        actionTester.init();
        return actionTester;
    }

    public <T> PropertyTester<T> propertyTester(
            final T domainObject,
            final String propertyName,
            final Where where) {
        var tester = getServiceInjector().injectServicesInto(
                new PropertyTester<T>(domainObject, propertyName, where));
        tester.init();
        return tester;
    }

    public <T> CollectionTester<T> collectionTester(
            final T domainObject,
            final String collectionName,
            final Where where) {
        var tester = getServiceInjector().injectServicesInto(
                new CollectionTester<T>(domainObject, collectionName, where));
        tester.init();
        return tester;
    }

    // -- SHORTCUTS

    public <T> ObjectTester<T> objectTester(
            final Class<T> domainObjectType) {
        return objectTester(domainObject(domainObjectType));
    }

    public <T> ActionTester<T> actionTester(
            final Class<T> domainObjectType,
            final String actionName,
            final Where where) {
        return actionTester(domainObject(domainObjectType), actionName, where);
    }
    public <T> ActionTester<T> actionTester(
            final T domainObject,
            final String actionName) {
        return actionTester(domainObject, actionName, Where.ANYWHERE);
    }
    public <T> ActionTester<T> actionTester(
            final Class<T> domainObjectType,
            final String actionName) {
        return actionTester(domainObject(domainObjectType), actionName);
    }
    public <T> ActionTester<T> actionTesterForSpecificInteraction(
            final Class<T> domainObjectType,
            final ActionInteraction actionInteraction) {
        return actionTesterForSpecificInteraction(domainObject(domainObjectType), actionInteraction);
    }

    public <T> PropertyTester<T> propertyTester(
            final Class<T> domainObjectType,
            final String propertyName,
            final Where where) {
        return propertyTester(domainObject(domainObjectType), propertyName, where);
    }
    public <T> PropertyTester<T> propertyTester(
            final Class<T> domainObjectType,
            final String propertyName) {
        return propertyTester(domainObject(domainObjectType), propertyName);
    }
    public <T> PropertyTester<T> propertyTester(
            final T domainObject,
            final String propertyName) {
        return propertyTester(domainObject, propertyName, Where.ANYWHERE);
    }

    public <T> CollectionTester<T> collectionTester(
            final Class<T> domainObjectType,
            final String collectionName,
            final Where where) {
        return collectionTester(domainObject(domainObjectType), collectionName, where);
    }
    public <T> CollectionTester<T> collectionTester(
            final Class<T> domainObjectType,
            final String collectionName) {
        return collectionTester(domainObject(domainObjectType), collectionName);
    }
    public <T> CollectionTester<T> collectionTester(
            final T domainObject,
            final String collectionName) {
        return collectionTester(domainObject, collectionName, Where.ANYWHERE);
    }

    // -- HELPER

    @Inject protected FactoryService factoryService;

    private <T> T domainObject(final @NonNull Class<T> domainObjectType) {
        return factoryService.viewModel(domainObjectType);
    }

    // -- OBJECT TESTER

    public static class ObjectTester<T>
    extends Tester<T> {

        protected ObjectTester(final @NonNull T domainObject) {
            super(domainObject);
        }

        public void assertTitle(final @Nullable String expectedResult) {
            assertEquals(expectedResult,
                    super.objectSpecification.getTitleService().titleOf(vm.getPojo()));
            assertEquals(expectedResult,
                    vm.getTitle());
        }

        public void assertIcon(final @Nullable String expectedResult) {
            var expectedIcon = new ObjectSupport.ClassPathIconResource(expectedResult);
            assertEquals(expectedIcon,
                    super.objectSpecification.getTitleService().iconOf(vm.getPojo(), IconSize.LARGE));
            assertEquals(expectedIcon,
                    super.objectSpecification.lookupFacet(IconFacet.class)
                    .flatMap(iconFacet->iconFacet.icon(vm, IconSize.LARGE))
                    .orElse(null));
        }

        public void assertCssClass(final @Nullable String expectedResult) {
            assertEquals(expectedResult,
                    super.objectSpecification.lookupFacet(CssClassFacet.class)
                    .map(cssClassFacet->cssClassFacet.cssClass(vm))
                    .orElse(null));
        }

        public void assertLayout(final @Nullable String expectedResult) {
            assertEquals(expectedResult,
                    super.objectSpecification.lookupFacet(LayoutPrefixFacet.class)
                    .map(layoutPrefixFacet->layoutPrefixFacet.layoutPrefix(vm))
                    .orElse(null));
        }

        public void assertValidationFailureOnMember(
                final ProgrammingModelConstants.MessageTemplate violation,
                final String memberName) {

            var validateDomainModel =
                    new DomainModelValidator(specificationLoader, configuration, causewaySystemEnvironment);

            assertThrows(DomainModelException.class, validateDomainModel::throwIfInvalid);
            validateDomainModel.assertAnyFailuresContaining(
                    Identifier.classIdentifier(LogicalType.fqcn(getDomainObjectType())),
                    violation
                        .builder()
                        .addVariable("type", getDomainObjectType().getName())
                        .addVariable("member", memberName)
                        .buildMessage());
        }

    }

    // -- ACTION TESTER

    public static class ActionTester<T>
    extends MemberTester<T> {

        private ActionInteraction actionInteraction;
        private Optional<ActionInteraction> actionInteraction() {
            return Optional.ofNullable(actionInteraction);
        }

        private final @NonNull ThrowingSupplier<ParameterNegotiationModel> parameterNegotiationStarter;
        private List<Command> capturedCommands = new ArrayList<>();

        private ActionTester(
                final @NonNull T domainObject,
                final @NonNull ActionInteraction actionInteraction,
                final @NonNull ManagedAction managedAction) {
            super(domainObject,
                    managedAction.getId(),
                    "actionName",
                    managedAction.getWhere());
            this.actionInteraction = actionInteraction;
            this.parameterNegotiationStarter = ()->
                actionInteraction
                .startParameterNegotiation()
                .orElseThrow(()->_Exceptions
                        .illegalAccess("action not visible or usable: %s",
                                managedAction.getAction().getFeatureIdentifier()));
        }

        private ActionTester(
                final @NonNull T domainObject,
                final @NonNull String actionName,
                final @NonNull Where where) {
            super(domainObject, actionName, "actionName", where);
            this.parameterNegotiationStarter = null;
        }

        @Nullable
        public <X> X getActionOwnerAs(final Class<X> type) {
            return _Casts.uncheckedCast(getActionOwner().map(ManagedObject::getPojo).orElse(null));
        }

        public ManagedObject getActionOwnerElseFail() {
            return getActionOwner().orElseThrow();
        }

        public Optional<ManagedObject> getActionOwner() {
            return getManagedAction().map(ManagedAction::getOwner);
        }

        public Optional<ManagedAction> getManagedAction() {
            return actionInteraction().flatMap(ActionInteraction::getManagedAction);
        }

        public ManagedAction getManagedActionElseFail() {
            return getManagedAction().orElseThrow();
        }

        @Override
        public Optional<ObjectAction> getMetaModel() {
            return getManagedAction()
            .map(ManagedAction::getMetaModel)
            .map(ObjectAction.class::cast);
        }

        public ObjectAction getActionMetaModelElseFail() {
            return getMetaModel().orElseThrow();
        }

        @Override
        public Optional<ObjectSpecification> getElementType() {
            return getMetaModel()
                    .map(ObjectAction::getReturnType);
        }

        @Override
        protected Optional<ManagedAction> startInteractionOn(final ManagedObject viewModel) {
            if(parameterNegotiationStarter==null) {
                this.actionInteraction = ActionInteraction
                        .start(viewModel, getMemberName(), Where.NOT_SPECIFIED);
            }
            assertNotNull(actionInteraction);
            return getManagedAction();
        }

        public void assertInvocationResult(
                final @Nullable Object expectedResult,
                @SuppressWarnings("rawtypes") final @Nullable UnaryOperator ...pojoDefaultArgReplacers) {

            assertExists(true);

            var pojoReplacers = Can.ofArray(pojoDefaultArgReplacers);

            interactionService.runAnonymous(()->{

                var pendingArgs = startParameterNegotiation(true);

                pendingArgs.getParamModels()
                        .forEach(param->{
                            pojoReplacers
                                .get(param.paramIndex())
                                .ifPresent(replacer->updatePojo(param, replacer));
                        });

                //pendingArgs.validateParameterSetForParameters();

                var resultOrVeto = actionInteraction.invokeWith(pendingArgs);
                assertTrue(resultOrVeto.isSuccess()); // assert action did not throw

                var actionResultAsPojo = resultOrVeto.getSuccessElseFail().getPojo();
                assertEquals(expectedResult, actionResultAsPojo);

                captureCommand();
            });

        }

        public Object invokeWithPojos(final List<Object> pojoArgList) {

            assertExists(true);

            var pojoVector = Can.ofCollection(pojoArgList);

            return interactionService.callAnonymous(()->{

                var pendingArgs = startParameterNegotiation(true);

                pendingArgs.getParamModels()
                        .forEach(param->{
                            pojoVector
                                .get(param.paramIndex())
                                .ifPresent(pojo->updatePojo(param, __->pojo));
                        });

                //pendingArgs.validateParameterSetForParameters();

                var resultOrVeto = actionInteraction.invokeWith(pendingArgs);
                assertTrue(resultOrVeto.isSuccess()); // assert action did not throw

                captureCommand();

                return resultOrVeto.getSuccessElseFail().getPojo();
            });
        }

        /**
         * circumvents rule checking
         */
        public void assertInvocationResultNoRules(
                final @Nullable Object expectedResult,
                @SuppressWarnings("rawtypes") final @Nullable UnaryOperator ...pojoDefaultArgReplacers) {

            assertExists(true);

            var pojoReplacers = Can.ofArray(pojoDefaultArgReplacers);
            var managedAction = this.getManagedActionElseFail();

            interactionService.runAnonymous(()->{

                var pendingArgs = startParameterNegotiation(false); // no rule checking

                pendingArgs.getParamModels()
                .forEach(param->{
                    pojoReplacers
                        .get(param.paramIndex())
                        .ifPresent(replacer->updatePojo(param, replacer));
                });

                // spawns its own transactional boundary, or reuses an existing one if available
                var either = managedAction.invoke(pendingArgs.getParamValues());

                assertTrue(either.isSuccess()); // assert action did not throw

                var actionResultAsPojo = either.getSuccessElseFail().getPojo();

                assertEquals(expectedResult, actionResultAsPojo);

            });

        }

        @SuppressWarnings("unchecked")
        public void assertParameterValues(
                final boolean checkRules,
                @SuppressWarnings("rawtypes") final Consumer ...pojoDefaultArgTests) {

            assertExists(true);

            var pojoTests = Can.ofArray(pojoDefaultArgTests);

            interactionService.runAnonymous(()->{

                var pendingArgs = startParameterNegotiation(checkRules);

                pendingArgs.getParamModels()
                .forEach(param->{
                    pojoTests
                        .get(param.paramIndex())
                        .ifPresent(pojoTest->
                            pojoTest.accept(
                                    MmUnwrapUtils.single(param.getValue().getValue())
                                    ));
                });

                captureCommand();

            });

        }

        @SuppressWarnings("unchecked")
        public <X> void assertParameterChoices(
                final boolean checkRules,
                final Class<X> elementType,
                final Consumer<Iterable<X>> ...pojoArgChoiceTests) {

            assertExists(true);

            var pojoTests = Can.ofArray(pojoArgChoiceTests);

            interactionService.runAnonymous(()->{

                startParameterNegotiation(checkRules).getParamModels()
                .forEach(param->{
                    pojoTests
                        .get(param.paramIndex())
                        .ifPresent(pojoTest->
                            pojoTest.accept(
                                    (List<X>) choicesFor(param)
                                    ));
                });

                captureCommand();

            });

        }

        private static List<Object> choicesFor(final ManagedValue param) {
            return MmUnwrapUtils.multipleAsList(param.getChoices().getValue());
        }

        @SuppressWarnings("unchecked")
        public void assertParameterVisibility(
                final boolean checkRules,
                final Consumer<Boolean> ...argVisibleChecks) {

            assertParameterModel(checkRules,
                // when
                pendingArgs->{

                },
                // then
                pendingArgs->{

                    var visibilityTests = Can.ofArray(argVisibleChecks);

                    pendingArgs.getParamModels()
                    .forEach(param->{

                        var consent = pendingArgs.getVisibilityConsent(param.paramIndex());

                        visibilityTests
                            .get(param.paramIndex())
                            .ifPresent(visibilityTest->
                                visibilityTest.accept(consent.isAllowed()));
                    });

                });
        }

        public void assertParameterUsability(
                final boolean checkRules,
                @SuppressWarnings("unchecked") final Consumer<String> ...argUsableChecks) {

            assertParameterModel(checkRules,
                // when
                pendingArgs->{

                },
                // then
                pendingArgs->{

                    var usabilityTests = Can.ofArray(argUsableChecks);

                    pendingArgs.getParamModels()
                    .forEach(param->{

                        var consent = pendingArgs.getUsabilityConsent(param.paramIndex());

                        usabilityTests
                            .get(param.paramIndex())
                            .ifPresent(usabilityTest->
                                usabilityTest.accept(consent.getReasonAsString().orElse(null)));
                    });

                });

        }

        @SuppressWarnings("unchecked")
        public void assertValidationMessage(
                final String expectedMessage,
                final boolean checkRules,
                @SuppressWarnings("rawtypes") final UnaryOperator ...pojoDefaultArgMapper) {

            assertParameterModel(checkRules,
                // when
                pendingArgs->{

                    var pojoArgMappers = Can.ofArray(pojoDefaultArgMapper);

                    pendingArgs.getParamModels()
                    .forEach(param->{

                        var objManager = param.getMetaModel().getObjectManager();

                        pojoArgMappers
                            .get(param.paramIndex())
                            .ifPresent(argMapper->
                                param.getValue().setValue(
                                    objManager
                                    .adapt(
                                        argMapper
                                        .apply(MmUnwrapUtils.single(param.getValue().getValue())))));
                    });

                },
                // then
                pendingArgs->{

                    assertEquals(expectedMessage, pendingArgs.getObservableActionValidation().getValue());

                });
        }

        public void assertParameterModel(
                final boolean checkRules,
                final Consumer<ParameterNegotiationModel> when,
                final Consumer<ParameterNegotiationModel> then) {

            assertExists(true);

            interactionService.runAnonymous(()->{
                var pendingArgs = startParameterNegotiation(checkRules);
                when.accept(pendingArgs);
                captureCommand();
                pendingArgs.activateValidationFeedback();
                then.accept(pendingArgs);
            });

        }

        public Can<Command> getCapturedCommands() {
            return Can.ofCollection(capturedCommands);
        }

        /**
         * Use on non scalar results.
         */
        public DataTableTester tableTester(
                @SuppressWarnings("rawtypes") final @Nullable UnaryOperator ...pojoDefaultArgReplacers) {

            assertExists(true);

            var pojoReplacers = Can.ofArray(pojoDefaultArgReplacers);
            var managedAction = this.getManagedActionElseFail();

            return interactionService.callAnonymous(()->{

                var pendingArgs = startParameterNegotiation(true);

                pendingArgs.getParamModels()
                        .forEach(param->{
                            pojoReplacers
                                .get(param.paramIndex())
                                .ifPresent(replacer->updatePojo(param, replacer));
                        });

                //pendingArgs.validateParameterSetForParameters();

                var resultOrVeto = actionInteraction.invokeWith(pendingArgs);
                assertTrue(resultOrVeto.isSuccess()); // assert action did not throw

                var actionResult = resultOrVeto.getSuccessElseFail();

                var table = DataTableInteractive
                        .forAction(managedAction, actionResult);

                return DataTableTester.of(table);

            });

        }

        // -- HELPER

        @SuppressWarnings("unchecked")
        static void updatePojo(final ManagedValue managedValue, @SuppressWarnings("rawtypes") final UnaryOperator replacer) {
            managedValue.update(v->ManagedObject.adaptSingular(
                    v.objSpec(),
                    replacer.apply(v.getPojo())));
        }

        @SneakyThrows
        private ParameterNegotiationModel startParameterNegotiation(final boolean checkRules) {

            if(parameterNegotiationStarter!=null) {
                return parameterNegotiationStarter.get();
            }

            if(actionInteraction==null) {
                fail("action-interaction not initialized on action-tester");
            }

            if(checkRules) {
                actionInteraction
                    .checkVisibility()
                    .checkUsability();
            }

            return actionInteraction
                    .startParameterNegotiation().orElseThrow(()->_Exceptions
                            .illegalAccess("action not visible or usable: %s",
                                    getManagedAction()
                                    .map(ManagedAction::getAction)
                                    .map(ObjectAction::getFeatureIdentifier)
                                    .map(Identifier::toString)
                                    .orElse("no such action")));
        }

        private void captureCommand() {
            capturedCommands.add(
                    interactionService.currentInteraction().get().getCommand());
        }
    }

    // -- PROPERTY TESTER

    public static class PropertyTester<T>
    extends MemberTester<T> {

        @Getter
        private Optional<ManagedProperty> managedPropertyIfAny;

        private PropertyTester(
                final @NonNull T domainObject,
                final @NonNull String propertyName,
                final @NonNull Where where) {
            super(domainObject, propertyName, "property", where);
        }

        @Override
        public Optional<OneToOneAssociation> getMetaModel() {
            return managedPropertyIfAny
            .map(ManagedProperty::getMetaModel)
            .map(OneToOneAssociation.class::cast);
        }

        public OneToOneAssociation getPropertyMetaModelElseFail() {
            return getMetaModel().orElseThrow();
        }

        @Override
        protected Optional<ObjectSpecification> getElementType() {
            return getMetaModel()
                    .map(OneToOneAssociation::getElementType);
        }

        @Override
        protected Optional<ManagedProperty> startInteractionOn(final ManagedObject viewModel) {
            return this.managedPropertyIfAny = PropertyInteraction
                    .start(viewModel, getMemberName(), where)
                    .getManagedProperty();
        }

        /**
         * circumvents rule checking
         */
        public void assertValue(final Object expectedPropertyValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{
                    assertEquals(expectedPropertyValue, managedProperty.getPropertyValue().getPojo());
                });
            });
        }

        /**
         * circumvents rule checking
         */
        public void assertValueUpdate(final Object proposedNewPropertyValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{

                    var newPropertyValue = managedProperty.getMetaModel().getMetaModelContext()
                          .getObjectManager().adapt(proposedNewPropertyValue);

                    managedProperty.modifyProperty(newPropertyValue);
                    assertEquals(proposedNewPropertyValue, managedProperty.getPropertyValue().getPojo());
                });
            });

        }

        public void assertValueUpdateUsingNegotiation(final Object proposedNewPropertyValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{

                    var propNeg = managedProperty.startNegotiation();
                    var initialValue = managedProperty.getPropertyValue();

                    assertEquals(initialValue, propNeg.getValue().getValue());

                    var newPropertyValue = managedProperty.getMetaModel().getMetaModelContext()
                            .getObjectManager().adapt(proposedNewPropertyValue);
                    propNeg.getValue().setValue(newPropertyValue);

                    // yet just pending
                    assertEquals(initialValue, managedProperty.getPropertyValue());
                    assertEquals(newPropertyValue, propNeg.getValue().getValue());

                    propNeg.submit();

                    // after submission
                    assertEquals(newPropertyValue, managedProperty.getPropertyValue());
                    assertEquals(newPropertyValue, propNeg.getValue().getValue());

                });
            });

        }

        public void assertValueNegotiation(
                final Consumer<PropertyNegotiationModel> when,
                final Consumer<PropertyNegotiationModel> then) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{
                    var propNeg = managedProperty.startNegotiation();
                    when.accept(propNeg);
                    propNeg.activateValidationFeedback();
                    propNeg.submit();
                    then.accept(propNeg);
                });
            });
        }

        /**
         * Supported by all properties that reflect a value type.
         * Uses value-semantics under the hood to do the conversion.
         */
        public void assertValueUpdateUsingNegotiationTextual(
                final String parsableProposedValue) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{

                    var propNeg = managedProperty.startNegotiation();
                    var initialValue = managedProperty.getPropertyValue();

                    assertEquals(initialValue, propNeg.getValue().getValue());

                    propNeg.getValueAsParsableText().setValue(parsableProposedValue);

                    // yet just pending
                    assertEquals(initialValue, managedProperty.getPropertyValue());
                    assertEquals(parsableProposedValue, propNeg.getValueAsParsableText().getValue());

                    propNeg.submit();

                    // after submission
                    assertEquals(parsableProposedValue, asParsebleText());
                    assertEquals(parsableProposedValue, propNeg.getValueAsParsableText().getValue());

                });
            });
        }

        /**
         * Supported by all properties that reflect a value type.
         * Uses value-semantics under the hood to do the conversion.
         */
        public void assertValueUpdateUsingNegotiationTextual(
                final String parsableProposedValue,
                final @NonNull String expectedValidationMessage) {

            assertExists(true);

            managedPropertyIfAny
            .ifPresent(managedProperty->{
                interactionService.runAnonymous(()->{

                    var propNeg = managedProperty.startNegotiation();
                    var initialValue = managedProperty.getPropertyValue();

                    assertEquals(initialValue, propNeg.getValue().getValue());

                    propNeg.getValueAsParsableText().setValue(parsableProposedValue);

                    // yet just pending
                    assertEquals(initialValue, managedProperty.getPropertyValue());
                    assertEquals(parsableProposedValue, propNeg.getValueAsParsableText().getValue());

                    // check expected validation message
                    propNeg.activateValidationFeedback();
                    assertEquals(expectedValidationMessage, propNeg.getValidationMessage().getValue());
                });
            });
        }

        @SuppressWarnings("unchecked")
        public String asParsebleText() {
            assertExists(true);
            var valueFacet = getFacetOnElementTypeElseFail(ValueFacet.class);
            var prop = this.getMetaModel().get();

            var context = valueFacet
                    .createValueSemanticsContext(prop);

            return valueFacet.selectParserForAttributeOrElseFallback(prop)
                    .parseableTextRepresentation(context,
                            MmUnwrapUtils.single(managedPropertyIfAny.get().getPropertyValue()));
        }

    }

    // -- COLLECTION TESTER

    public static class CollectionTester<T>
    extends MemberTester<T> {

        @Getter
        private Optional<ManagedCollection> managedCollectionIfAny;

        private CollectionTester(
                final @NonNull T domainObject,
                final @NonNull String collectionName,
                final @NonNull Where where) {
            super(domainObject, collectionName, "collection", where);
        }

        @Override
        public Optional<OneToManyAssociation> getMetaModel() {
            return managedCollectionIfAny
            .map(ManagedCollection::getMetaModel)
            .map(OneToManyAssociation.class::cast);
        }

        public OneToManyAssociation getCollectionMetaModelElseFail() {
            return getMetaModel().orElseThrow();
        }

        @Override
        protected Optional<ObjectSpecification> getElementType() {
            return getMetaModel()
                    .map(OneToManyAssociation::getElementType);
        }

        @Override
        protected Optional<ManagedCollection> startInteractionOn(final ManagedObject viewModel) {
            return this.managedCollectionIfAny = CollectionInteraction
                    .start(viewModel, getMemberName(), Where.NOT_SPECIFIED)
                    .getManagedCollection();
        }

        /**
         * circumvents rule checking
         */
        public Stream<ManagedObject> streamCollectionElements() {

            assertExists(true);

            return managedCollectionIfAny
            .map(managedCollection->
                interactionService.callAnonymous(managedCollection::streamElements))
            .orElseGet(Stream::empty);
        }

        /**
         * circumvents rule checking
         */
        public void assertCollectionElements(final Iterable<?> expectedCollectionElements) {

            assertExists(true);

            managedCollectionIfAny
            .ifPresent(managedCollection->{
                interactionService.runAnonymous(()->{
                    CollectionAssertions
                    .assertComponentWiseEquals(expectedCollectionElements, managedCollection.getCollectionValue().getPojo());
                });
            });
        }

        public DataTableTester tableTester() {
            return DataTableTester.of(getManagedCollectionIfAny()
                    .orElseThrow()
                    .createDataTableModel());
        }

    }

    // -- COMMON ABSTRACT MEMBER TESTER

    private static abstract class MemberTester<T>
    extends Tester<T>{

        @Getter private final String memberName;
        private final String memberSort;
        protected final Where where;

        private Optional<? extends ManagedMember> managedMemberIfAny;

        protected MemberTester(
                final @NonNull T domainObject,
                final @NonNull String memberName,
                final @NonNull String memberSort,
                final @NonNull Where where) {
            super(domainObject);
            this.memberName = memberName;
            this.memberSort = memberSort;
            this.where = where;
        }

        protected final MemberTester<T> init() {
            this.managedMemberIfAny = startInteractionOn(vm);
            return this;
        }

        protected abstract Optional<? extends ObjectMember> getMetaModel();

        protected abstract Optional<ObjectSpecification> getElementType();

        public ObjectSpecification getElementTypeElseFail() {
            return getElementType().orElseThrow();
        }

        protected abstract Optional<? extends ManagedMember> startInteractionOn(ManagedObject viewModel);

        public <F extends Facet> F getFacetOnMemberElseFail(final Class<F> facetType) {
            return getMetaModel()
            .map(m->m.getFacet(facetType))
            .orElseThrow(()->_Exceptions.noSuchElement(
                    String.format("%s has no %s", memberName, facetType.getSimpleName())
                    ));
        }

        public <F extends Facet> F getFacetOnElementTypeElseFail(final Class<F> facetType) {
            return getElementType()
            .map(m->m.getFacet(facetType))
            .orElseThrow(()->_Exceptions.noSuchElement(
                    String.format("'%s: %s' has no %s on its element type",
                            memberName,
                            getObjectSpecification().getCorrespondingClass().getSimpleName(),
                            facetType.getSimpleName())
                    ));
        }

        public final void assertExists(final boolean isExpectedToExist) {

            if(isExpectedToExist
                    && managedMemberIfAny.isEmpty()) {
                fail(String.format("%s %s does not exist", memberSort, memberName));
            }

            if(!isExpectedToExist
                    && managedMemberIfAny.isPresent()) {
                fail(String.format("%s %s does exist", memberSort, memberName));
            }
        }

        public void assertMemberId(final String expectedMemberId) {
            assertEquals(expectedMemberId,
                    getMetaModel()
                    .orElseThrow(_Exceptions::noSuchElement).getId());
        }

        public final void assertFriendlyName(final String expectedFriendlyName) {
            assertTrue(managedMemberIfAny.isPresent());
            assertEquals(expectedFriendlyName,
                    managedMemberIfAny.get()
                    .getFriendlyName());
        }

        public final void assertDescription(final String expectedDescription) {
            assertTrue(managedMemberIfAny.isPresent());
            assertEquals(_Strings.nullToEmpty(expectedDescription),
                    managedMemberIfAny.get()
                    .getDescription().orElse(""));
        }

        private final void assertVisibility(final boolean isExpectedVisible) {
            assertVisibilityIsVetoedWith(isExpectedVisible ? null : "Hidden");
        }

        public final void assertVisibilityIsVetoed() {
            assertVisibility(false);
        }

        public final void assertVisibilityIsNotVetoed() {
            assertVisibility(true);
        }

        private final void assertVisibilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedVisible = expectedVetoReason == null;

            if(isExpectedVisible) {
                assertExists(true);
            }

            managedMemberIfAny
            .ifPresent(managedCollection->{
                interactionService.runAnonymous(()->{

                    final String actualVetoResaon = managedCollection
                        .checkVisibility()
                        .flatMap(veto->veto.getReasonAsString())
                        .orElse(null);

                    assertEquals(expectedVetoReason, actualVetoResaon);
                });
            });
        }

        public final void assertUsabilityIsNotVetoed() {
            assertUsabilityIsVetoedWith(null);
        }

        public final void assertUsabilityIsVetoedWithAll(final Can<String> expectedVetoReasons) {

            assertExists(true);

            managedMemberIfAny
            .ifPresent(managedMember->{
                interactionService.runAnonymous(()->{
                    final String actualVetoReason = managedMember
                            .checkUsability()
                            .flatMap(veto->veto.getReasonAsString())
                            .orElse(null);

                    if(!expectedVetoReasons.isEmpty()
                            && actualVetoReason==null) {
                        fail("usability not vetoed, while expecting all of: " + expectedVetoReasons.toList());
                    }

                    expectedVetoReasons
                    .forEach(expectedVetoReason->{
                        assertTrue(actualVetoReason.contains(expectedVetoReason),
                               ()->String.format("usability veto %s is not containing %s",
                                       actualVetoReason, expectedVetoReason));
                    });
                });
            });
        }

        public final void assertUsabilityIsVetoedWith(final @Nullable String expectedVetoReason) {

            final boolean isExpectedUsable = expectedVetoReason == null;

            if(isExpectedUsable) {
                assertExists(true);
            }

            managedMemberIfAny
            .ifPresent(managedMember->{
                interactionService.runAnonymous(()->{
                    final String actualVetoReason = managedMember
                            .checkUsability()
                            .flatMap(veto->veto.getReasonAsString())
                            .orElse(null);

                        assertEquals(expectedVetoReason, actualVetoReason);
                });
            });
        }

        public final void assertIsExplicitlyAnnotated(final boolean isExpectedExplicitlyAnnotated) {
            if(isExpectedExplicitlyAnnotated
                    && !getMetaModel().get().isExplicitlyAnnotated()) {
                fail(String.format("%s %s is not explicitly annotated", memberSort, memberName));
            }

            if(!isExpectedExplicitlyAnnotated
                    && getMetaModel().get().isExplicitlyAnnotated()) {
                fail(String.format("%s %s is explicitly annotated", memberSort, memberName));
            }
        }

    }

    private static abstract class Tester<T> {

        @Inject protected CausewayConfiguration configuration;
        @Inject protected CausewaySystemEnvironment causewaySystemEnvironment;
        @Inject protected SpecificationLoader specificationLoader;
        @Inject protected InteractionService interactionService;

        @Getter private final Class<T> domainObjectType;
        @Getter private final ObjectSpecification objectSpecification;
        protected final ManagedObject vm;

        protected Tester(
                final @NonNull T domainObject) {
            this.domainObjectType = _Casts.uncheckedCast(domainObject.getClass());
            this.vm = MetaModelContext.instanceElseFail().getObjectManager().adapt(domainObject);
            this.objectSpecification = vm.objSpec();
        }

    }

}
