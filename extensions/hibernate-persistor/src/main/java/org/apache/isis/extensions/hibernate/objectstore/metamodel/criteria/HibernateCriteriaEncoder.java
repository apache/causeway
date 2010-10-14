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


package org.apache.isis.extensions.hibernate.objectstore.metamodel.criteria;

import org.apache.isis.metamodel.commons.exceptions.NotYetImplementedException;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.remoting.data.query.PersistenceQueryData;
import org.apache.isis.remoting.protocol.encoding.internal.PersistenceQueryEncoderAbstract;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;


// REVIEW this creates a dependency between hibernate and remote command - should it live 
// else where ?
public class HibernateCriteriaEncoder extends PersistenceQueryEncoderAbstract {

    public PersistenceQueryData encode(final PersistenceQuery criteria) {

        // HibernateCriteriaCriteria hibernateCriteria = (HibernateCriteriaCriteria) criteria;
        // SerializedObjectData objectData = new SerializedObjectData();
        // objectData.setData(encode(hibernateCriteria.getCriteria()));
        // objectData.setType(hibernateCriteria.getCriteria().getClass().getName());
        // return new HibernateCriteriaData((HibernateCriteriaCriteria) criteria, objectData);

        throw new NotYetImplementedException();
    }

    @Override
    protected PersistenceQuery doDecode(
            final ObjectSpecification specification,
            final PersistenceQueryData criteriaData) {
        // SerializedObjectData patternData = ((HibernateCriteriaData) criteriaData).getData();
        // Criteria criteria = (Criteria) decode(patternData);
        // return new HibernateCriteriaCriteria(((HibernateCriteriaData) criteriaData).getClass(), criteria,
        // ((HibernateCriteriaData) criteriaData).getResultType());
        throw new NotYetImplementedException();
    }

    public Class<HibernateCriteriaCriteria> getPersistenceQueryClass() {
        return HibernateCriteriaCriteria.class;
    }
}
