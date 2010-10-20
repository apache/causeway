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


package org.apache.isis.progmodel.java5.reflect;

import org.apache.isis.noa.reflect.ObjectAction;
import org.apache.isis.noa.reflect.ObjectField;
import org.apache.isis.nof.reflect.peer.ActionPeer;
import org.apache.isis.nof.reflect.peer.OneToManyPeer;
import org.apache.isis.nof.reflect.peer.OneToOnePeer;
import org.apache.isis.nof.reflect.peer.ValuePeer;
import org.apache.isis.nof.reflect.remote.spec.DummyAction;
import org.apache.isis.nof.reflect.remote.spec.DummyOneToManyAssociation;
import org.apache.isis.nof.reflect.remote.spec.DummyOneToOneAssociation;
import org.apache.isis.nof.reflect.remote.spec.DummyValueAssociation;
import org.apache.isis.nof.reflect.spec.ReflectionPeerBuilder;



public class DummyBuilder extends ReflectionPeerBuilder {
    public ObjectAction createAction(final ActionPeer actionPeer) {
        return new DummyAction(actionPeer);
    }

    public ObjectField createField(final OneToManyPeer fieldPeer) {
        return new DummyOneToManyAssociation(fieldPeer);
    }

    public ObjectField createField(final OneToOnePeer fieldPeer) {
        return new DummyOneToOneAssociation(fieldPeer);
    }
    
    public ObjectField createField(ValuePeer fieldPeer) {
        return new DummyValueAssociation(fieldPeer);
    }
}
