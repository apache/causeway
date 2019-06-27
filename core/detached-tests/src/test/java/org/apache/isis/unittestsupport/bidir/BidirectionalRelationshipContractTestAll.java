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
package org.apache.isis.unittestsupport.bidir;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.unittestsupport.bidir.BidirectionalRelationshipContractTestAbstract;
import org.apache.isis.unittestsupport.bidir.Instantiator;
import org.apache.isis.unittestsupport.bidir.InstantiatorSimple;

public class BidirectionalRelationshipContractTestAll extends BidirectionalRelationshipContractTestAbstract {

    public BidirectionalRelationshipContractTestAll() {
        super("org.apache.isis.core.unittestsupport.bidir",
                _Maps.<Class<?>,Instantiator>unmodifiable(
                        // no instantiator need be registered for ParentDomainObject.class;
                        // will default to using new InstantiatorSimple(AgreementForTesting.class),
                        ChildDomainObject.class, new InstantiatorForChildDomainObject(),
                        PeerDomainObject.class, new InstantiatorSimple(PeerDomainObjectForTesting.class)
                        ));
        withLoggingTo(System.out);
    }

}
