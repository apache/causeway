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

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
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

/**
 * Factoring out the commonality between <tt>ActionInvocationFacetViaMethod</tt> 
 * and <tt>BackgroundServiceDefault</tt>.
 */
public class CommandUtil {

    private CommandUtil(){}

    public static String targetMemberNameFor(final ObjectMember objectMember) {
        return objectMember.getName();
    }

    public static String targetClassNameFor(final ManagedObject targetAdapter) {
        return targetClassNameFor(targetAdapter.getSpecification());
    }
    
    public static String targetClassNameFor(final ObjectSpecification spec) {
        return StringExtensions.asNaturalName2(spec.getSingularName());
    }

    public static String memberIdentifierFor(final ObjectMember objectMember) {
        return objectMember.getIdentifier().getFullIdentityString();
    }

    public static String logicalMemberIdentifierFor(final ObjectMember objectMember) {
        if(objectMember instanceof ObjectAction) {
            return logicalMemberIdentifierFor((ObjectAction)objectMember);
        }
        if(objectMember instanceof OneToOneAssociation) {
            return logicalMemberIdentifierFor((OneToOneAssociation)objectMember);
        }
        throw new IllegalArgumentException(objectMember.getClass() + " is not supported");
    }

    public static String logicalMemberIdentifierFor(final ObjectAction objectAction) {
        return logicalMemberIdentifierFor(objectAction.getOnType(), objectAction);
    }

    public static String logicalMemberIdentifierFor(final OneToOneAssociation otoa) {
        return logicalMemberIdentifierFor(otoa.getOnType(), otoa);
    }

    private static String logicalMemberIdentifierFor(
            final ObjectSpecification onType, final ObjectMember objectMember) {
        final String objectType = onType.getSpecId().asString();
        final String localId = objectMember.getIdentifier().getMemberName();
        return objectType + "#" + localId;
    }

    public static String argDescriptionFor(final ManagedObject valueAdapter) {
        final StringBuilder buf = new StringBuilder();
        if(valueAdapter != null) {
            appendArg(buf, "new value", valueAdapter);
        } else {
            buf.append("cleared");
        }
        return buf.toString();
    }
    
    public static String argDescriptionFor(
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

    public static Bookmark bookmarkFor(final ManagedObject adapter) {
        return ManagedObjects.bookmark(adapter)
                .orElse(null);
    }

    static void appendParamArg(
            final StringBuilder buf,
            final ObjectActionParameter param,
            final ManagedObject objectAdapter) {
        
        final String name = param.getName();
        appendArg(buf, name, objectAdapter);
    }

    private static void appendArg(
            final StringBuilder buf,
            final String name,
            final ManagedObject objectAdapter) {
        
        String titleOf = objectAdapter != null? objectAdapter.titleString(null): "null";
        buf.append(name).append(": ").append(titleOf).append("\n");
    }

    public static ManagedObject[] adaptersFor(
            final Object[] args, 
            final ObjectManager objectManager) {
        
        return _NullSafe.stream(args)
                .map(objectManager::adapt)
                .collect(_Arrays.toArray(ManagedObject.class, _NullSafe.size(args)));
    }

}
