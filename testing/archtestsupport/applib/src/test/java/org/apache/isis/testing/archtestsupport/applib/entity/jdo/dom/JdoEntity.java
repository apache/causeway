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
package org.apache.isis.testing.archtestsupport.applib.entity.jdo.dom;

import java.util.Comparator;

import javax.inject.Inject;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;
import javax.jdo.annotations.Version;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.jaxb.PersistentEntityAdapter;

@PersistenceCapable(schema = "jdo", identityType = IdentityType.DATASTORE)
@DatastoreIdentity(strategy = IdGeneratorStrategy.NATIVE)
@Version
@Uniques(@Unique(name = "name", members = {"name"}))
@DomainObject(nature = Nature.ENTITY)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class JdoEntity implements Comparable<JdoEntity> {

    private String name;

    @Override public int compareTo(final JdoEntity o) {
        return Comparator.<JdoEntity,String>comparing(x -> x.name).compare(this,o);
    }

    @Inject @NotPersistent JdoService jdoService;
}
