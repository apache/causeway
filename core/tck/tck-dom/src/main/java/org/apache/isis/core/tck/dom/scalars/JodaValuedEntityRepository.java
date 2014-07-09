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

package org.apache.isis.core.tck.dom.scalars;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.core.tck.dom.AbstractEntityRepository;

@Named("JodaValuedEntities")
@ObjectType("JodaValuedEntities")
@DomainService
public class JodaValuedEntityRepository extends AbstractEntityRepository<JodaValuedEntity> {

    public JodaValuedEntityRepository() {
        super(JodaValuedEntity.class, "JodaValuedEntities");
    }

    /**
     * Required to discover the JodaValueEntity type.
     */
    @Override
    @MemberOrder(sequence = "2")
    public JodaValuedEntity newEntity() {
        return super.newEntity();
    }

}
