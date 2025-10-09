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
package org.apache.causeway.core.metamodel.inspect.model;

import java.util.stream.Stream;

import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class ActionNode extends MemberNode {

    private final ObjectAction action;

    @Override
    public String title() {
        return switch(action.getParameterCount()) {
            case 0 -> "%s()".formatted(action.getId());
            case 1 -> "%s(1 arg)".formatted(action.getId());
            default -> "%s(%d args)".formatted(action.getId(), action.getParameterCount());
        };
    }
    
    @Override
    public void putDetails(Details details) {
        details.put("Id", action.getId());
        details.put("Friendly Name", action.getCanonicalFriendlyName());
        details.put("Mixed In", "" + isMixedIn());
        details.put("Return Element Type", action.getReturnType().logicalTypeName());
    }

    @Override
    public Stream<MMNode> streamChildNodes() {
        return Stream.<MMNode>concat(
                super.streamChildNodes(),
    
                action.streamParameters()
                    .map(param->MMNodeFactory.param(param, this))
            );
    }

    @Override
    protected ObjectMember member() {
        return action;
    }
    
}
