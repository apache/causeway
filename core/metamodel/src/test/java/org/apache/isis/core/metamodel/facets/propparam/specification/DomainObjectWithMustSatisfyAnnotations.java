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

package org.apache.isis.core.metamodel.facets.propparam.specification;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MustSatisfy;

public class DomainObjectWithMustSatisfyAnnotations extends AbstractDomainObject {

    private String firstName;

    @MustSatisfy(SpecificationAlwaysSatisfied.class)
    public String getFirstName() {
        resolve(firstName);
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
        objectChanged();
    }

    private String lastName;

    @MustSatisfy(SpecificationRequiresFirstLetterToBeUpperCase.class)
    public String getLastName() {
        resolve(lastName);
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
        objectChanged();
    }

    public void changeLastName(@MustSatisfy(SpecificationRequiresFirstLetterToBeUpperCase.class) final String lastName) {
        setLastName(lastName);
    }

}
