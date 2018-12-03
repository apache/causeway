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

package org.apache.isis.core.metamodel.spec.feature;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.action.associateWith.AssociatedWithFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaPosition;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.wizard.WizardFacet;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderFacetComparator;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.specimpl.MixedInMember;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

public interface ObjectAction extends ObjectMember {

    // -- getSemantics, getOnType
    /**
     * The semantics of this action.
     */
    SemanticsOf getSemantics();

    /**
     * Returns the specification for the type of object that this action can be
     * invoked upon.
     */
    ObjectSpecification getOnType();


    // -- getType, isPrototype

    ActionType getType();

    boolean isPrototype();



    // -- ReturnType
    /**
     * Returns the specifications for the return type.
     */
    ObjectSpecification getReturnType();

    /**
     * Returns <tt>true</tt> if the represented action returns a non-void object,
     * else returns false.
     */
    boolean hasReturn();



    // -- execute, executeWithRuleChecking

    /**
     * Invokes the action's method on the target object given the specified set
     * of parameters, checking the visibility, usability and validity first.
     *
     * @param mixedInAdapter - will be null for regular actions, and for mixin actions.  When a mixin action invokes its underlying mixedIn action, then will be populated (so that the ActionDomainEvent can correctly provide the underlying mixin)
     */
    ObjectAdapter executeWithRuleChecking(
            final ObjectAdapter target,
            final ObjectAdapter mixedInAdapter,
            final ObjectAdapter[] parameters,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) throws AuthorizationException;

    /**
     * Invokes the action's method on the target object given the specified set
     * of parameters.
     *
     * @param mixedInAdapter - will be null for regular actions, and for mixin actions.  When a mixin action invokes its underlying mixedIn action, then will be populated (so that the ActionDomainEvent can correctly provide the underlying mixin)
     */
    ObjectAdapter execute(
            ObjectAdapter targetAdapter,
            ObjectAdapter mixedInAdapter,
            ObjectAdapter[] parameters,
            final InteractionInitiatedBy interactionInitiatedBy);


    // -- isProposedArgumentSetValid, isEachIndividualArgumentValid, isArgumentSetValid

    /**
     * Whether the provided argument set is valid, represented as a {@link Consent}.
     *
     * <p>
     *     Basically just calls (the helper methods also called by) first
     *     {@link #isEachIndividualArgumentValid(ObjectAdapter, ObjectAdapter[], InteractionInitiatedBy)} and then
     *     {@link #isArgumentSetValid(ObjectAdapter, ObjectAdapter[], InteractionInitiatedBy)}.  Those methods are
     *     separated out so that viewers have more fine-grained control.
     * </p>
     */
    Consent isProposedArgumentSetValid(
            ObjectAdapter object,
            ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy);

    Consent isEachIndividualArgumentValid(
            ObjectAdapter objectAdapter,
            ObjectAdapter[] proposedArguments,
            InteractionInitiatedBy interactionInitiatedBy);

    Consent isArgumentSetValid(
            ObjectAdapter objectAdapter,
            ObjectAdapter[] proposedArguments,
            InteractionInitiatedBy interactionInitiatedBy);



    // -- Parameters (declarative)

    /**
     * Returns the number of parameters used by this method.
     */
    int getParameterCount();

    /**
     * Returns set of parameter information.
     *
     * <p>
     * Implementations may build this array lazily or eagerly as required.
     *
     * @return
     */
    List<ObjectActionParameter> getParameters();

    /**
     * Returns the {@link ObjectSpecification type} of each of the {@link #getParameters() parameters}.
     */
    List<ObjectSpecification> getParameterTypes();

    /**
     * Returns set of parameter information matching the supplied filter.
     *
     * @return
     */
    List<ObjectActionParameter> getParameters(
            Predicate<ObjectActionParameter> predicate);

    /**
     * Returns the parameter with provided id.
     */
    ObjectActionParameter getParameterById(String paramId);

    /**
     * Returns the parameter with provided name.
     */
    ObjectActionParameter getParameterByName(String paramName);



    // -- Parameters (per instance)

    /**
     * Returns the defaults references/values to be used for the action.
     */
    ObjectAdapter[] getDefaults(ObjectAdapter target);

    /**
     * Returns a list of possible references/values for each parameter, which
     * the user can choose from.
     */
    ObjectAdapter[][] getChoices(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy);


    // -- Util
    public static final class Util {

        final static MemberOrderFacetComparator memberOrderFacetComparator = new MemberOrderFacetComparator(false);

        private Util() {
        }

        public static String nameFor(final ObjectAction objAction) {
            final String actionName = objAction.getName();
            if (actionName != null) {
                return actionName;
            }
            final NamedFacet namedFacet = objAction.getFacet(NamedFacet.class);
            if (namedFacet != null) {
                return namedFacet.value();
            }
            return "(no name)";
        }

        public static SemanticsOf semanticsOf(final ObjectAction objectAction) {
            return objectAction.getSemantics();
        }

        public static boolean isAreYouSureSemantics(final ObjectAction objectAction) {
            return semanticsOf(objectAction).isAreYouSure();
        }

        public static boolean isIdempotentOrCachable(ObjectAction objectAction) {
            final SemanticsOf semantics = semanticsOf(objectAction);
            return semantics.isIdempotentInNature() || semantics.isSafeAndRequestCacheable();
        }
        
        public static boolean isNoParameters(ObjectAction objectAction) {
            return objectAction.getParameterCount()==0;
        }

        public static boolean returnsBlobOrClob(final ObjectAction objectAction) {
            final ObjectSpecification returnType = objectAction.getReturnType();
            if (returnType != null) {
                Class<?> cls = returnType.getCorrespondingClass();
                if (Blob.class.isAssignableFrom(cls) || Clob.class.isAssignableFrom(cls)) {
                    return true;
                }
            }
            return false;
        }

        public static String actionIdentifierFor(final ObjectAction action) {
            @SuppressWarnings("unused")
            final Identifier identifier = action.getIdentifier();

            final String className = action.getOnType().getSpecId().asString().replace(".","-");
            final String actionId = action.getId();
            return className + "-" + actionId;
        }

        public static String descriptionOf(ObjectAction action) {
            return action.getDescription();
        }

        public static ActionLayout.Position actionLayoutPositionOf(ObjectAction action) {
            final ActionPositionFacet layoutFacet = action.getFacet(ActionPositionFacet.class);
            return layoutFacet != null ? layoutFacet.position() : ActionLayout.Position.BELOW;
        }

        public static String cssClassFaFor(final ObjectAction action) {
            final CssClassFaFacet cssClassFaFacet = action.getFacet(CssClassFaFacet.class);
            return cssClassFaFacet != null ? cssClassFaFacet.value() : null;
        }

        public static CssClassFaPosition cssClassFaPositionFor(final ObjectAction action) {
            CssClassFaFacet facet = action.getFacet(CssClassFaFacet.class);
            return facet != null ? facet.getPosition() : CssClassFaPosition.LEFT;
        }

        public static String cssClassFor(final ObjectAction action, final ObjectAdapter objectAdapter) {
            final CssClassFacet cssClassFacet = action.getFacet(CssClassFacet.class);
            return cssClassFacet != null ? cssClassFacet.cssClass(objectAdapter) : null;
        }


        public static List<ObjectAction> findTopLevel(
                final ObjectAdapter adapter) {
            final List<ObjectAction> topLevelActions = _Lists.newArrayList();

            addTopLevelActions(adapter, ActionType.USER, topLevelActions);
            if(_Context.isPrototyping()) {
                addTopLevelActions(adapter, ActionType.PROTOTYPE, topLevelActions);
            }
            return topLevelActions;
        }

        static void addTopLevelActions(
                final ObjectAdapter adapter,
                final ActionType actionType,
                final List<ObjectAction> topLevelActions) {

            final ObjectSpecification adapterSpec = adapter.getSpecification();

            Predicate<ObjectAction> predicate =
                    ObjectAction.Predicates.memberOrderNotAssociationOf(adapterSpec)
                    .and(ObjectAction.Predicates.dynamicallyVisible(adapter, 
                            InteractionInitiatedBy.USER, Where.ANYWHERE))
                    .and(ObjectAction.Predicates.excludeWizardActions(adapterSpec));

            final Stream<ObjectAction> userActions = 
                    adapterSpec.streamObjectActions(actionType, Contributed.INCLUDED)
                    .filter(predicate);
            userActions
            .forEach(topLevelActions::add);

        }


        public static List<ObjectAction> findForAssociation(
                final ObjectAdapter adapter,
                final ObjectAssociation association) {
            
            final List<ObjectAction> associatedActions = _Lists.newArrayList();

            addActions(adapter, ActionType.USER, association, associatedActions);
            if(_Context.isPrototyping()) {
                addActions(adapter, ActionType.PROTOTYPE, association, associatedActions);
            }

            Collections.sort(associatedActions, new Comparator<ObjectAction>() {

                @Override
                public int compare(ObjectAction o1, ObjectAction o2) {
                    final MemberOrderFacet m1 = o1.getFacet(MemberOrderFacet.class);
                    final MemberOrderFacet m2 = o2.getFacet(MemberOrderFacet.class);
                    return memberOrderFacetComparator.compare(m1, m2);
                }
            });
            return associatedActions;
        }

        static void addActions(
                final ObjectAdapter adapter,
                final ActionType type,
                final ObjectAssociation association, final List<ObjectAction> associatedActions) {
            final ObjectSpecification objectSpecification = adapter.getSpecification();

            Predicate<ObjectAction> predicate = 
                ObjectAction.Predicates.memberOrderOf(association)
                .and(ObjectAction.Predicates.excludeWizardActions(objectSpecification));

            final Stream<ObjectAction> userActions = 
                    objectSpecification.streamObjectActions(type, Contributed.INCLUDED)
                    .filter(predicate);
            userActions
            .forEach(associatedActions::add);
        }

        public static PromptStyle promptStyleFor(final ObjectAction objectAction) {
            PromptStyleFacet facet = objectAction.getFacet(PromptStyleFacet.class);
            if(facet == null) {
                // don't think this can occur, see PromptStyleFallback
                return PromptStyle.INLINE;
            }
            final PromptStyle promptStyle = facet.value();
            if(promptStyle == PromptStyle.AS_CONFIGURED) {
                // don't think this can occur, see PromptStyleConfiguration
                return PromptStyle.INLINE;
            }
            return promptStyle;
        }
        
        public static Optional<String> targetNameFor(
                final ObjectAction owningAction,
                final @Nullable ObjectAdapter mixedInAdapter) {
            
            if(mixedInAdapter != null) {
                final ObjectSpecification onType = owningAction.getOnType();
                final ObjectSpecification mixedInSpec = mixedInAdapter.getSpecification();
                final Optional<String> mixinName = mixedInSpec.getMixedInMember(onType)
                        .map(MixedInMember::getName);
                
                return mixinName;
            }
            return Optional.empty();
        }
    }


    // -- Predicates

    public static final class Predicates {

        private Predicates() {
        }

        public static Predicate<ObjectAction> associatedWith(final ObjectAssociation objectAssociation) {
            return new AssociatedWith(objectAssociation);
        }

        public static Predicate<ObjectAction> associatedWithAndWithCollectionParameterFor(
                final OneToManyAssociation collection) {

            final ObjectSpecification collectionTypeOfSpec = collection.getSpecification();

            return new AssociatedWith(collection)
                    .and(new HasParameterMatching(
                            new ObjectActionParameter.Predicates.CollectionParameter(collectionTypeOfSpec)
                            ));
        }

        public static class AssociatedWith implements Predicate<ObjectAction> {
            private final String memberId;
            private final String memberName;

            public AssociatedWith(final ObjectAssociation objectAssociation) {
                this.memberId = objectAssociation.getId();
                this.memberName = objectAssociation.getName();
            }

            @Override
            public boolean test(final ObjectAction objectAction) {
                final AssociatedWithFacet associatedWithFacet = objectAction.getFacet(AssociatedWithFacet.class);
                if(associatedWithFacet == null) {
                    return false;
                }
                final String associatedMemberName = associatedWithFacet.value();
                if (associatedMemberName == null) {
                    return false;
                }
                final String memberOrderNameLowerCase = associatedMemberName.toLowerCase();
                return memberName != null && Objects.equals(memberName.toLowerCase(), memberOrderNameLowerCase) ||
                        memberId   != null && Objects.equals(memberId.toLowerCase(), memberOrderNameLowerCase);
            }
        }

        public static class HasParameterMatching implements Predicate<ObjectAction> {
            private final Predicate<ObjectActionParameter> parameterPredicate;
            public HasParameterMatching(final Predicate<ObjectActionParameter> parameterPredicate) {
                this.parameterPredicate = parameterPredicate;
            }

            @Override
            public boolean test(final ObjectAction objectAction) {
                return stream(objectAction.getParameters())
                        .anyMatch(parameterPredicate);
            }
        }

        public static Predicate<ObjectAction> ofType(final ActionType type) {
            return (ObjectAction oa) -> oa.getType() == type;
        }

//        public static Predicate<ObjectAction> bulk() {
//            return new Predicate<ObjectAction>() {
//
//                @Override
//                public boolean test(ObjectAction oa) {
//
//                    final BulkFacet bulkFacet = oa.getFacet(BulkFacet.class);
//                    if(bulkFacet == null || bulkFacet.isNoop() || bulkFacet.value() == InvokeOn.OBJECT_ONLY) {
//                        return false;
//                    }
//                    if (oa.getParameterCount() != 0) {
//                        return false;
//                    }
//
//                    // currently don't support returning Blobs or Clobs
//                    // (because haven't figured out how to rerender the current page, but also to do a download)
//                    ObjectSpecification returnSpec = oa.getReturnType();
//                    if (returnSpec != null) {
//                        Class<?> returnType = returnSpec.getCorrespondingClass();
//                        if (returnType == Blob.class || returnType == Clob.class) {
//                            return false;
//                        }
//                    }
//                    return true;
//                }
//            };
//        }

        public static Predicate<ObjectAction> dynamicallyVisible(
                final ObjectAdapter target,
                final InteractionInitiatedBy interactionInitiatedBy,
                final Where where) {
            return (ObjectAction objectAction) -> {
                    final Consent visible = objectAction.isVisible(target, interactionInitiatedBy, where);
                    return visible.isAllowed();
            };
        }

//        public static Predicate<ObjectAction> notBulkOnly() {
//            return (ObjectAction t) -> {
//                    BulkFacet facet = t.getFacet(BulkFacet.class);
//                    return facet == null || facet.value() != InvokeOn.COLLECTION_ONLY;
//            };
//        }

        public static Predicate<ObjectAction> excludeWizardActions(final ObjectSpecification objectSpecification) {
            return wizardActions(objectSpecification).negate();
        }

        private static Predicate<ObjectAction> wizardActions(final ObjectSpecification objectSpecification) {
            return (ObjectAction input) -> {
                    if (objectSpecification == null) {
                        return false;
                    }
                    final WizardFacet wizardFacet = objectSpecification.getFacet(WizardFacet.class);
                    return wizardFacet != null && wizardFacet.isWizardAction(input);
            };
        }

        public static Predicate<ObjectAction> memberOrderOf(ObjectAssociation association) {
            final String assocName = association.getName();
            final String assocId = association.getId();
            return (ObjectAction t) -> {
                    final MemberOrderFacet memberOrderFacet = t.getFacet(MemberOrderFacet.class);
                    if (memberOrderFacet == null || _Strings.isNullOrEmpty(memberOrderFacet.name())) {
                        return false;
                    }
                    final String memberOrderName = memberOrderFacet.name().toLowerCase();
                    if (_Strings.isNullOrEmpty(memberOrderName)) {
                        return false;
                    }
                    return memberOrderName.equalsIgnoreCase(assocName) || memberOrderName.equalsIgnoreCase(assocId);
            };
        }

        public static Predicate<ObjectAction> memberOrderNotAssociationOf(final ObjectSpecification adapterSpec) {

            final Set<String> associationNamesAndIds = _Sets.newHashSet(); 
            
            adapterSpec.streamAssociations(Contributed.INCLUDED)
            .forEach(ass->{
                associationNamesAndIds.add(_Strings.lower(ass.getName()));
                associationNamesAndIds.add(_Strings.lower(ass.getId()));
            });

            return (ObjectAction t) -> {
                final MemberOrderFacet memberOrderFacet = t.getFacet(MemberOrderFacet.class);
                if (memberOrderFacet == null || _Strings.isNullOrEmpty(memberOrderFacet.name())) {
                    return true;
                }
                final String memberOrderName = memberOrderFacet.name().toLowerCase();
                if (_Strings.isNullOrEmpty(memberOrderName)) {
                    return false;
                }
                return !associationNamesAndIds.contains(memberOrderName);
            };
        }
    }



}
