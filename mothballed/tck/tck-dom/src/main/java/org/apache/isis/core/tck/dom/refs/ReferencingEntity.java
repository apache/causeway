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

package org.apache.isis.core.tck.dom.refs;

import java.util.List;

import javax.jdo.annotations.IdentityType;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.ObjectType;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.Discriminator("RFCG")
@javax.jdo.annotations.DatastoreIdentity(strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY)
@ObjectType("RFCG")
public class ReferencingEntity extends BaseEntity {
    
    
    // {{ Reference
    private SimpleEntity reference;

    public SimpleEntity getReference() {
        return reference;
    }

    public void setReference(final SimpleEntity reference) {
        this.reference = reference;
    }

    // }}

    // {{ AggregatedEntity
    private AggregatedEntity aggregatedReference;

    public AggregatedEntity getAggregatedReference() {
        return aggregatedReference;
    }

    public void setAggregatedReference(final AggregatedEntity aggregatedReference) {
        this.aggregatedReference = aggregatedReference;
    }

    public AggregatedEntity addAggregatedReference() {
        final AggregatedEntity aggregatedEntity = newAggregatedInstance(AggregatedEntity.class);
        setAggregatedReference(aggregatedEntity);
        return aggregatedEntity;
    }
    // }}

    // {{ AggregatedEntities
    private List<AggregatedEntity> aggregatedEntities = Lists.newArrayList();

    public List<AggregatedEntity> getAggregatedEntities() {
        return aggregatedEntities;
    }

    public void setAggregatedEntities(List<AggregatedEntity> aggregatedEntities) {
        this.aggregatedEntities = aggregatedEntities;
    }
    
    public AggregatedEntity addAggregatedEntityToCollection() {
        final AggregatedEntity aggregatedEntity = newAggregatedInstance(AggregatedEntity.class);
        getAggregatedEntities().add(aggregatedEntity);
        return aggregatedEntity;
    }
    // }}

    // {{ NotPersisted
    @NotPersisted
    public SimpleEntity getNotPersisted() {
        throw new org.apache.isis.applib.NonRecoverableException("unexpected call");
    }
    // }}


}
