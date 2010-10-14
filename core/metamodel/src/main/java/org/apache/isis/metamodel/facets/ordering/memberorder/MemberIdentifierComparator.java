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


package org.apache.isis.metamodel.facets.ordering.memberorder;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.isis.applib.Identifier;
import org.apache.isis.metamodel.specloader.internal.peer.ObjectMemberPeer;


/**
 * Compares {@link ObjectMemberPeer}) by {@link ObjectMemberPeer#getIdentifier()}
 *
 */
public class MemberIdentifierComparator implements Comparator<ObjectMemberPeer>, Serializable {

    private static final long serialVersionUID = 1L;

    public int compare(final ObjectMemberPeer o1, final ObjectMemberPeer o2) {
        final Identifier identifier1 = o1.getIdentifier();
        final Identifier identifier2 = o2.getIdentifier();
        return identifier1.compareTo(identifier2);
    }

}
