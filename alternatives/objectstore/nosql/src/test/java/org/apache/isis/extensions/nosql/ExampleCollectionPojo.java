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


package org.apache.isis.extensions.nosql;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.commons.exceptions.UnexpectedCallException;


public class ExampleCollectionPojo extends ExamplePojo {

    // {{ HomogenousCollection
    private List<ExampleValuePojo> homogenousCollection = new ArrayList<ExampleValuePojo>();

    public List<ExampleValuePojo> getHomogenousCollection() {
        return homogenousCollection;
    }

    public void setHomogenousCollection(final List<ExampleValuePojo> homogenousCollection) {
        this.homogenousCollection = homogenousCollection;
    }
    // }}

    // {{ HetrogenousCollection
    private List<ExamplePojo> hetrogenousCollection = new ArrayList<ExamplePojo>();

    public List<ExamplePojo> getHetrogenousCollection() {
        return hetrogenousCollection;
    }

    public void setHetrogenousCollection(final List<ExamplePojo> hetrogenousCollection) {
        this.hetrogenousCollection = hetrogenousCollection;
    }
    // }}

    // {{ NotPersisted
    @NotPersisted
    public List<ExampleValuePojo> getNotPersisted() {
        throw new UnexpectedCallException();
    }
    // }}


    

}


