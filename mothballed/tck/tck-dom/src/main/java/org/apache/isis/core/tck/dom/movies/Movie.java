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

package org.apache.isis.core.tck.dom.movies;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.ObjectType;

@ObjectType("movies.MOVIE")
public class Movie {

    public String title() {
        return name;
    }

    // {{ name: String 
    private String name;
    public String getName() {
        return name;
    }
    public void setName(final String name) {
        this.name = name;
    }
    // }}

    // {{ director: Person 
    private Person director;
    public Person getDirector() {
        return director;
    }
    public void setDirector(final Person director) {
        this.director = director;
    }
    
    // }}
    
    // {{ roles: List 
    private final List<Role> roles = Lists.newArrayList();
    public List<Role> getRoles() {
        return roles;
    }
    public void addToRoles(final Role role) {
        roles.add(role);
    }
    public void removeFromRoles(final Role role) {
        roles.remove(role);
    }
    // }}
}
