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
package org.apache.isis.applib.fixturescripts;

import org.apache.isis.applib.annotation.*;

@ViewModel
@Paged(500)
public class FixtureResult {


    private String fixtureScriptClassName;

    @Named("Fixture script")
    @Optional
    @MemberOrder(sequence="1")
    public String getFixtureScriptClassName() {
        return fixtureScriptClassName;
    }
    public void setFixtureScriptClassName(String fixtureScriptClassName) {
        this.fixtureScriptClassName = fixtureScriptClassName;
    }
    
    // //////////////////////////////////////

    private String key;

    @Named("Result key")
    @Title(sequence="1", append=": ")
    @MemberOrder(sequence="1")
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

    // //////////////////////////////////////

    private Object object;

    @Named("Result")
    @Title(sequence="2")
    @MemberOrder(sequence="1")
    public Object getObject() {
        return object;
    }
    public void setObject(Object object) {
        this.object = object;
    }
    
    // //////////////////////////////////////

    @Named("Result class")
    @MemberOrder(sequence="3")
    public String getClassName() {
        return object != null? object.getClass().getName(): null;
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    FixtureScripts fixtureScripts;

}