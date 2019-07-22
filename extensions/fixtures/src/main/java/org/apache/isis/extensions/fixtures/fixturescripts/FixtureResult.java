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
package org.apache.isis.extensions.fixtures.fixturescripts;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.ViewModelLayout;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        objectType = "isisApplib.FixtureResult"
        )
@ViewModelLayout(paged=500)
public class FixtureResult {


    // -- fixtureScriptClassName (property)

    private String fixtureScriptClassName;

    @PropertyLayout(named="Fixture script")
    @Property(optionality = Optionality.OPTIONAL)
    @MemberOrder(sequence="1")
    public String getFixtureScriptClassName() {
        return fixtureScriptClassName;
    }
    public void setFixtureScriptClassName(String fixtureScriptClassName) {
        this.fixtureScriptClassName = fixtureScriptClassName;
    }



    // -- fixtureScriptQualifiedName (programmatic)

    private String fixtureScriptQualifiedName;

    @Programmatic
    String getFixtureScriptQualifiedName() {
        return fixtureScriptQualifiedName;
    }

    void setFixtureScriptQualifiedName(String fixtureScriptQualifiedName) {
        this.fixtureScriptQualifiedName = fixtureScriptQualifiedName;
    }



    // -- key (property)

    private String key;

    @PropertyLayout(named="Result key")
    @Title(sequence="1", append=": ")
    @MemberOrder(sequence="1")
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }



    // -- object (property)

    private Object object;

    @PropertyLayout(named="Result")
    @Title(sequence="2")
    @MemberOrder(sequence="1")
    public Object getObject() {
        return object;
    }
    public void setObject(Object object) {
        this.object = object;
    }



    // -- className (derived property)


    @PropertyLayout(named="Result class")
    @MemberOrder(sequence="3")
    public String getClassName() {
        return object != null? object.getClass().getName(): null;
    }



    // -- injected services

    @Inject FixtureScripts fixtureScripts;



}