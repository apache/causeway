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
package org.apache.causeway.testdomain.jdo.entities;

import java.util.List;

import javax.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@PersistenceCapable(identityType=IdentityType.DATASTORE, schema = "testdomain")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="Product")
@DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.INCREMENT,
        column="id")
@Named("testdomain.jdo.Product")
@DomainObject
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class JdoProduct implements Comparable<JdoProduct> {

    public String title() {
        return toString();
    }

    @Property(
            commandPublishing = Publishing.ENABLED, // used for publishing tests
            executionPublishing = Publishing.ENABLED, // used for publishing tests
            editing = Editing.DISABLED) // used for an async rule check test
    @Getter @Setter @Column(allowsNull = "true")
    private String name;
//    public void setName(String name) {
//        System.err.println("!!! setting name " + name);
//        this.name = name;
//    }

    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String description;

    @Property
    @Getter @Setter @Column(allowsNull = "false")
    private double price;

    @Collection
    @Persistent(mappedBy="product") @Column(allowsNull = "true")
    @Getter @Setter
    private List<JdoProductComment> comments;

    @Action(
            commandPublishing = Publishing.ENABLED,
            executionPublishing = Publishing.ENABLED)
    public void doubleThePrice() {
        this.setPrice(2.*getPrice());
    }

    @MemberSupport public String disableDoubleThePrice() {
        return "always disabled for testing purposes";
    }

    @Override
    public int compareTo(final JdoProduct other) {
        return _Strings.compareNullsFirst(this.getName(), other==null ? null : other.getName());
    }

}
