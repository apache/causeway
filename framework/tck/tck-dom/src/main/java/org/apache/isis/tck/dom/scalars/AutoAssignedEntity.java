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

package org.apache.isis.tck.dom.scalars;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;

@PersistenceCapable         // for jdo object store
@Discriminator("AUAS")      // for jdo object store
@javax.jdo.annotations.DatastoreIdentity(strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY) // for jdo objectstore
@Entity                     // for jpa object store
@DiscriminatorValue("AUAS") // for jpa object store
@ObjectType("AUAS")
public class AutoAssignedEntity extends AbstractDomainObject {

    // {{ Id (Integer, also used in title)
    private Integer id;

    @Title(sequence="1", append=": ")
    @Id         // for jpa object store
    @PrimaryKey // for jdo object store
    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }
    // }}

    
    // {{ StringProperty (also used in title)
    private String stringProperty;

    @Title(sequence="2")
    @Optional
    @MemberOrder(sequence = "1")
    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(final String description) {
        this.stringProperty = description;
    }

    // }}

}
