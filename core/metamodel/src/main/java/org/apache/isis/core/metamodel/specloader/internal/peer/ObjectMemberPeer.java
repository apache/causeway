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


package org.apache.isis.core.metamodel.specloader.internal.peer;

import org.apache.isis.core.commons.debug.Debuggable;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.MemberType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.identifier.Identified;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;


/**
 * Details about action and field members gained via reflection.
 * 
 * @see org.apache.isis.core.metamodel.specloader.internal.peer.ObjectActionPeer
 */
public interface ObjectMemberPeer extends Identified, Debuggable {


    /**
     * The type of a {@link MemberType#PROPERTY}, the return type of an 
     * {@link MemberType#ACTION}, the referenced
     * type of a {@link MemberType#COLLECTION}.
     */
    ObjectSpecification getSpecification(SpecificationLoader specificationLoader);


    boolean isProperty();
    boolean isCollection();
    boolean isAction();


}
