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
package org.apache.causeway.core.metamodel.facets.actions.action.invocation;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.StringExtensions;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * Factoring out the commonality between <tt>ActionInvocationFacetViaMethod</tt>
 * and <tt>BackgroundServiceDefault</tt>.
 */
@UtilityClass
public class IdentifierUtil {

    public String targetClassNameFor(final ObjectSpecification spec) {
        return StringExtensions.asNaturalName2(spec.getSingularName());
    }

    /**
     * Recovers an {@link Identifier} for given {@code logicalMemberIdentifier}.
     */
    @SneakyThrows
    public Identifier memberIdentifierFor(
            final @NonNull SpecificationLoader specLoader,
            final @NonNull Identifier.Type identifierType,
            final @NonNull String logicalMemberIdentifier) {

        val ref = _Refs.stringRef(logicalMemberIdentifier);
        val logicalTypeName = ref.cutAtIndexOfAndDrop("#");
        val memberId = ref.getValue();
        val typeSpec = specLoader.specForLogicalTypeNameElseFail(logicalTypeName);
        val logicalType = LogicalType.eager(typeSpec.getCorrespondingClass(), logicalTypeName);

        if(identifierType.isAction()) {
            return Identifier.actionIdentifier(logicalType, memberId);
        }

        if(identifierType.isProperty()) {
            return Identifier.propertyIdentifier(logicalType, memberId);
        }

        if(identifierType.isCollection()) {
            return Identifier.collectionIdentifier(logicalType, memberId);
        }

        throw _Exceptions.illegalArgument("unsupported identifier type %s (logicalMemberIdentifier=%s)",
                identifierType, logicalMemberIdentifier);
    }

    /**
     * Whether given command corresponds to given objectMember.
     * <p>
     * Is related to {@link #logicalMemberIdentifierForDeclaredMember(ObjectMember)}.
     */
    public boolean isCommandForMember(
            final @Nullable Command command,
            final @NonNull InteractionHead interactionHead,
            final @Nullable ObjectMember objectMember) {
        return command!=null
                && objectMember!=null
                && logicalMemberIdentifierFor(interactionHead, objectMember)
                    .equals(command.getLogicalMemberIdentifier());
    }


    public String logicalMemberIdentifierFor(
            final @NonNull InteractionHead interactionHead,
            final ObjectMember objectMember) {
        if (objectMember instanceof ObjectAction) {
            ObjectAction objectAction = (ObjectAction) objectMember;
            if (objectAction.isDeclaredOnMixin()) {
                if (interactionHead instanceof ActionInteractionHead) {
                    ObjectAction objectActionOnMixee =
                            ((ActionInteractionHead) interactionHead).getMetaModel();
                    ObjectSpecification specificationOfMixee = interactionHead.getOwner().getSpecification();
                    return logicalMemberIdentifierFor(specificationOfMixee, objectActionOnMixee);
                }
            }
            // we fall through to the declared case; this should suffice because this method is only called by code
            // relating to commands, and contributed properties or collections don't emit commands.
        }

        return logicalMemberIdentifierForDeclaredMember(objectMember);
    }

    private String logicalMemberIdentifierFor(
            final ObjectSpecification onType, final ObjectMember objectMember) {
        final String logicalTypeName = onType.getLogicalTypeName();
        final String localId = objectMember.getFeatureIdentifier().getMemberLogicalName();
        return logicalTypeName + "#" + localId;
    }

    // -- HELPER

    /**
     * This assumes that the member is declared, ie is not a mixin.
     */
    private String logicalMemberIdentifierForDeclaredMember(final ObjectMember objectMember) {
        if(objectMember instanceof ObjectAction) {
            return logicalMemberIdentifierForDeclaredMember((ObjectAction)objectMember);
        }
        if(objectMember instanceof OneToOneAssociation) {
            return logicalMemberIdentifierForDeclaredMember((OneToOneAssociation)objectMember);
        }
        throw new IllegalArgumentException(objectMember.getClass() + " is not supported");
    }

    /**
     * This assumes that the member is declared, ie is not a mixin.
     */
    private String logicalMemberIdentifierForDeclaredMember(final ObjectAction objectAction) {
        return logicalMemberIdentifierFor(objectAction.getDeclaringType(), objectAction);
    }

    /**
     * This assumes that the member is declared, ie is not a mixin.
     */
    private String logicalMemberIdentifierForDeclaredMember(final OneToOneAssociation otoa) {
        return logicalMemberIdentifierFor(otoa.getDeclaringType(), otoa);
    }


}
