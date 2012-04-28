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

package org.apache.isis.tck.dom.eg;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.ObjectType;

@ObjectType("EPC")
public class ExamplePojoWithCollections extends ExamplePojo {

    // {{ HomogeneousCollection
    private List<ExamplePojoWithValues> homogeneousCollection = new ArrayList<ExamplePojoWithValues>();

    public List<ExamplePojoWithValues> getHomogeneousCollection() {
        return homogeneousCollection;
    }

    public void setHomogeneousCollection(final List<ExamplePojoWithValues> homogenousCollection) {
        this.homogeneousCollection = homogenousCollection;
    }

    // }}

    // {{ HetrogeneousCollection
    private List<ExamplePojo> heterogeneousCollection = new ArrayList<ExamplePojo>();

    public List<ExamplePojo> getHeterogeneousCollection() {
        return heterogeneousCollection;
    }

    public void setHeterogeneousCollection(final List<ExamplePojo> hetrogenousCollection) {
        this.heterogeneousCollection = hetrogenousCollection;
    }

    // }}

    // {{ NotPersisted
    @NotPersisted
    public List<ExamplePojoWithValues> getNotPersisted() {
        throw new org.apache.isis.applib.ApplicationException("unexpected call");
    }
    // }}

}
