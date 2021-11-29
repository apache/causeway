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
package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

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

    public String targetClassNameFor(final ManagedObject targetAdapter) {
        return targetClassNameFor(targetAdapter.getSpecification());
    }

    public String targetClassNameFor(final ObjectSpecification spec) {
        return StringExtensions.asNaturalName2(spec.getSingularName());
    }

    public String memberIdentifierFor(final ObjectMember objectMember) {
        return objectMember.getFeatureIdentifier().getFullIdentityString();
    }

    public String logicalMemberIdentifierFor(final ObjectMember objectMember) {
        if(objectMember instanceof ObjectAction) {
            return logicalMemberIdentifierFor((ObjectAction)objectMember);
        }
        if(objectMember instanceof OneToOneAssociation) {
            return logicalMemberIdentifierFor((OneToOneAssociation)objectMember);
        }
        throw new IllegalArgumentException(objectMember.getClass() + " is not supported");
    }

    public String logicalMemberIdentifierFor(final ObjectAction objectAction) {
        return logicalMemberIdentifierFor(objectAction.getDeclaringType(), objectAction);
    }

    public String logicalMemberIdentifierFor(final OneToOneAssociation otoa) {
        return logicalMemberIdentifierFor(otoa.getDeclaringType(), otoa);
    }

    /**
     * Recovers an {@link Identifier} for given {@code logicalMemberIdentifier}.
     */
    @SneakyThrows
    public Identifier memberIdentifierFor(
            final @NonNull SpecificationLoader specLoader,
            final @NonNull Identifier.Type indentifierType,
            final @NonNull String logicalMemberIdentifier) {

        val ref = _Refs.stringRef(logicalMemberIdentifier);
        val logicalTypeName = ref.cutAtIndexOfAndDrop("#");
        val memberId = ref.getValue();
        val typeSpec = specLoader.specForLogicalTypeNameElseFail(logicalTypeName);
        val logicalType = LogicalType.eager(typeSpec.getCorrespondingClass(), logicalTypeName);

        if(indentifierType.isAction()) {
            return Identifier.actionIdentifier(logicalType, memberId);
        }

        if(indentifierType.isPropertyOrCollection()) {
            return Identifier.propertyOrCollectionIdentifier(logicalType, memberId);
        }

        throw _Exceptions.illegalArgument("unsupported identifier type %s (logicalMemberIdentifier=%s)",
                indentifierType, logicalMemberIdentifier);
    }

    /**
     * Whether given command corresponds to given objectMember.
     * <p>
     * Is related to {@link #logicalMemberIdentifierFor(ObjectMember)}.
     */
    public boolean isCommandForMember(
            final @Nullable Command command,
            final @Nullable ObjectMember objectMember) {
        return command!=null
                && objectMember!=null
                && logicalMemberIdentifierFor(objectMember)
                    .equals(command.getLogicalMemberIdentifier());
    }

    // -- HELPER

    private String logicalMemberIdentifierFor(
            final ObjectSpecification onType, final ObjectMember objectMember) {
        final String logicalTypeName = onType.getLogicalTypeName();
        final String localId = objectMember.getFeatureIdentifier().getMemberLogicalName();
        return logicalTypeName + "#" + localId;
    }

}
