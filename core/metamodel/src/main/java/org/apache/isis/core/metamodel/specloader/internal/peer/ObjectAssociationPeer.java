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

import org.apache.isis.core.metamodel.spec.ObjectSpecification;


/**
 * Additional reflective details about field members.
 * 
 * @see org.apache.isis.core.metamodel.specloader.internal.peer.ObjectMemberPeer
 */
public interface ObjectAssociationPeer extends ObjectMemberPeer {

    /**
     * The {@link ObjectSpecification specification} of the associated object if {@link #isOneToOne()} is
     * <tt>true</tt>, or, the type of the associated object (rather than a <tt>Vector.class</tt>, say),
     * if {@link #isOneToMany()} is <tt>true</tt>.
     */
    ObjectSpecification getSpecification();

    /**
     * If this is a scalar association, representing (in old terminology) a reference to another entity or a
     * value.
     * 
     * <p>
     * Opposite of {@link #isOneToMany()}.
     */
    public boolean isOneToOne();

    /**
     * If this is a collection.
     * 
     * <p>
     * Opposite of {@link #isOneToOne()}.
     */
    public boolean isOneToMany();

}
