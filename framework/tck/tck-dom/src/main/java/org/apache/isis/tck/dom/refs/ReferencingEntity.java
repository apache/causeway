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

package org.apache.isis.tck.dom.refs;

import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.ObjectType;

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

    // {{ AggregatedReference
    private AggregatedEntity aggregatedReference;

    public AggregatedEntity getAggregatedReference() {
        return aggregatedReference;
    }

    public void setAggregatedReference(final AggregatedEntity aggregatedReference) {
        this.aggregatedReference = aggregatedReference;
    }
    // }}

    // {{ NotPersisted
    @NotPersisted
    public SimpleEntity getNotPersisted() {
        throw new org.apache.isis.applib.ApplicationException("unexpected call");
    }
    // }}

}
