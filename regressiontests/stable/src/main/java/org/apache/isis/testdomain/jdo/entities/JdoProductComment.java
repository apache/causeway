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
package org.apache.isis.testdomain.jdo.entities;

import java.sql.Timestamp;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.mixins.updates.OnUpdatedByAndAt;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable
@DatastoreIdentity(strategy=IdGeneratorStrategy.IDENTITY, column="id")
@DomainObject(
        objectType = "testdomain.jdo.ProductComment")
public class JdoProductComment implements OnUpdatedByAndAt {

    @Property @Column(allowsNull = "false")
    @Getter @Setter private JdoProduct product;

    @Property
    @Getter @Setter private String comment;

    // -- TIMESTAMPABLE

    @Property
    @Getter @Setter private String updatedBy;

    @Property
    @Getter @Setter private Timestamp updatedAt;


}
