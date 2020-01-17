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

package org.apache.isis.core.metamodel.adapter.oid;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

import static org.apache.isis.core.metamodel.adapter.oid.Oid.unmarshaller;

/**
 * Used as the {@link Oid} for {@link OneToManyAssociation} (collections).
 */
public interface ParentedOid extends Oid {

    /**
     * object identifier of the domain object that is holding the {@link OneToManyAssociation}
     * this instance is representing
     */
    RootOid getParentOid();

    /**
     * id of the {@link OneToManyAssociation} this instance is representing, that is 
     * the member name, that is annotated with {@link Collection}
     */
    String getName();

    // -- DECODE FROM STRING

    public static ParentedOid deString(String enString) {
        return unmarshaller().unmarshal(enString, ParentedOid.class);
    }

}
