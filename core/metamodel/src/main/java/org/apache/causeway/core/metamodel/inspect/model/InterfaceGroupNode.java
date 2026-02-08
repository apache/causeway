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

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
final class InterfaceGroupNode implements MMNode {
    
    private final Can<ObjectSpecification> interfaces;

    @Override
    public String title() {
        return "Interfaces";
    }

    @Override
    public String iconName() {
        return "";
    }
    
    @Override
    public void putDetails(Details details) {
    	interfaces.stream()
            .forEach(interfc->details.put(
            		interfc.getCorrespondingClass().getSimpleName(), 
            		interfc.getCorrespondingClass().getName()));
    }

    // -- TREE NODE STUFF

    @Getter @Setter
    private MMNode parentNode;

    @Override
    public Stream<MMNode> streamChildNodes() {
        return interfaces.stream().map(interfc->MMNodeFactory.superType(interfc, this));
    }

}
