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

import java.util.List;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * Factoring out the commonality between <tt>ActionInvocationFacetViaMethod</tt>
 * and <tt>BackgroundServiceDefault</tt>.
 */
@UtilityClass
public class CommandUtil {

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
     * Whether given command corresponds to given objectMember.
     * <p>
     * Is related to {@link #logicalMemberIdentifierFor(ObjectMember)}.
     */
    public boolean matches(
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

    public String argDescriptionFor(final ManagedObject valueAdapter) {
        final StringBuilder buf = new StringBuilder();
        if(valueAdapter != null) {
            appendArg(buf, "new value", valueAdapter);
        } else {
            buf.append("cleared");
        }
        return buf.toString();
    }

    public String argDescriptionFor(
            final ObjectAction owningAction,
            final List<ManagedObject> arguments) {

        val argsBuf = new StringBuilder();
        val parameters = owningAction.getParameters();
        if(parameters.size() == arguments.size()) {
            // should be the case
            int i=0;
            for (ObjectActionParameter param : parameters) {
                CommandUtil.appendParamArg(argsBuf, param, arguments.get(i++));
            }
        }
        return argsBuf.toString();
    }

    public Bookmark bookmarkFor(final ManagedObject adapter) {
        return ManagedObjects.bookmark(adapter)
                .orElse(null);
    }

    void appendParamArg(
            final StringBuilder buf,
            final ObjectActionParameter param,
            final ManagedObject objectAdapter) {

        final String name = param.getStaticFriendlyName()
                .orElseThrow(_Exceptions::unexpectedCodeReach);
        appendArg(buf, name, objectAdapter);
    }

    private void appendArg(
            final StringBuilder buf,
            final String name,
            final ManagedObject objectAdapter) {

        String titleOf = objectAdapter != null? objectAdapter.titleString(): "null";
        buf.append(name).append(": ").append(titleOf).append("\n");
    }

    public ManagedObject[] adaptersFor(
            final Object[] args,
            final ObjectManager objectManager) {

        return _NullSafe.stream(args)
                .map(objectManager::adapt)
                .collect(_Arrays.toArray(ManagedObject.class, _NullSafe.size(args)));
    }

}
