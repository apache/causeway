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
package org.apache.isis.testdomain.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.Publishing;
import org.apache.isis.applib.annotations.Title;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@DomainObject(
        logicalTypeName = "testdomain.jpa.NonGeneratedStringId",
        entityChangePublishing = Publishing.DISABLED)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JpaEntityNonGeneratedStringId {

    @Id
    @Property @Title
    @Column(nullable = true)
    private @Getter @Setter String name;

}
