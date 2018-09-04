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
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

/**
 * Factoring out the commonality between <tt>ActionInvocationFacetViaMethod</tt> and <tt>BackgroundServiceDefault</tt>.
 */
public class CommandUtil {

    private CommandUtil(){}

    public static String targetMemberNameFor(final ObjectMember objectMember) {
        return objectMember.getName();
    }

    public static String targetClassNameFor(final ObjectAdapter targetAdapter) {
        return StringExtensions.asNaturalName2(targetAdapter.getSpecification().getSingularName());
    }

    public static String memberIdentifierFor(final ObjectMember objectMember) {
        return objectMember.getIdentifier().toClassAndNameIdentityString();
    }

    public static String argDescriptionFor(final ObjectAdapter valueAdapter) {
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
            final ObjectAdapter[] arguments) {
        final StringBuilder argsBuf = new StringBuilder();
        List<ObjectActionParameter> parameters = owningAction.getParameters();
        if(parameters.size() == arguments.length) {
            // should be the case
            int i=0;
            for (ObjectActionParameter param : parameters) {
                CommandUtil.appendParamArg(argsBuf, param, arguments[i++]);
            }
        }
        return argsBuf.toString();
    }

    public static Bookmark bookmarkFor(final ObjectAdapter adapter) {
        final Oid oid = adapter.getOid();
        if(!(oid instanceof RootOid)) {
            return null;
        }
        final RootOid rootOid = (RootOid) oid;
        return rootOid.asBookmark();
    }

    static void appendParamArg(
            final StringBuilder buf,
            final ObjectActionParameter param,
            final ObjectAdapter objectAdapter) {
        final String name = param.getName();
        appendArg(buf, name, objectAdapter);
    }

    private static void appendArg(
            final StringBuilder buf,
            final String name,
            final ObjectAdapter objectAdapter) {
        String titleOf = objectAdapter != null? objectAdapter.titleString(null): "null";
        buf.append(name).append(": ").append(titleOf).append("\n");
    }

    public static ObjectAdapter[] adaptersFor(final Object[] args, final ObjectAdapterProvider adapterProvider) {
        return _NullSafe.stream(args)
                .map(ObjectAdapter.Functions.adapterForUsing(adapterProvider))
                .collect(_Arrays.toArray(ObjectAdapter.class, _NullSafe.size(args)));
    }

}
