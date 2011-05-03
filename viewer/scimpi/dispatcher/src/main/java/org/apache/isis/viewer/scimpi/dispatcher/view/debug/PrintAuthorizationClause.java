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
package org.apache.isis.viewer.scimpi.dispatcher.view.debug;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class PrintAuthorizationClause extends AbstractElementProcessor {

    @Override
    public String getName() {
        return "print-authorization-clause";
    }

    @Override
    public void process(final Request request) {
        final RequestContext context = request.getContext();
        if (context.isDebugDisabled()) {
            return;
        }

        final Identifier identifier = (Identifier) context.getVariable("_security-identifier");
        final List<String> roles = (List<String>) context.getVariable("_security-roles");
        final StringBuffer roleList = new StringBuffer();
        for (final String role : roles) {
            if (roleList.length() > 0) {
                roleList.append("|");
            }
            roleList.append(role);
        }

        request.appendHtml("<pre>");
        request.appendHtml(identifier.toClassIdentityString() + ":" + roleList + "\n");
        request.appendHtml(identifier.toClassAndNameIdentityString() + ":" + roleList + "\n");
        request.appendHtml(identifier.toFullIdentityString() + ":" + roleList + "\n");
        request.appendHtml("</pre>");
    }

}
