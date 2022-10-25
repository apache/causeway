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
package org.apache.causeway.testing.archtestsupport.applib.entity.jdo.dom;

import java.util.Comparator;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Version;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;

@PersistenceCapable(schema = "jdo", identityType = IdentityType.DATASTORE)
@DatastoreIdentity
@Unique(name = "name", members = {"name"})
@Version
@DomainObject(nature = Nature.ENTITY)
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public abstract class JdoEntity2<X extends JdoEntity2<X>> implements Comparable<X> {

    protected final String name;

    // abstract classes do not need to have no-arg constructor
    public JdoEntity2(final String name) {
        this.name = name;
    }

    @Override public int compareTo(final JdoEntity2 o) {
        return Comparator.<JdoEntity2,String>comparing(x -> x.name).compare(this,o);
    }
}

