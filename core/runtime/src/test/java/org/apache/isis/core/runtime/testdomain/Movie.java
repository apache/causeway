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


package org.apache.isis.core.runtime.testdomain;

import java.util.Vector;


public class Movie {
    private Person director;
    private String name;
    private final Vector roles = new Vector();

    public void addToRoles(final Role role) {
        roles.addElement(role);
    }

    public Person getDirector() {
        return director;
    }

    public String getName() {
        return name;
    }

    public Vector getRoles() {
        return roles;
    }

    public void removeFromRoles(final Role role) {
        roles.removeElement(role);
    }

    public void setDirector(final Person director) {
        this.director = director;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String title() {
        return name;
    }

    /*
     * public static void aboutActionFindMovie(ActionAbout about, String name, Person director, Person actor) {
     * about.setParameter(0, "Name"); about.setParameter(1, "Director"); about.setParameter(2, "Actor"); }
     * 
     */
}
