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

import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Title;

public class FixtureResult extends AbstractViewModel {

    @Override
    public String viewModelMemento() {
        return fixtureScripts.mementoFor(this);
    }
    
    @Override
    public void viewModelInit(String memento) {
        fixtureScripts.initOf(memento, this);
    }

    // //////////////////////////////////////

    private String key;
    
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
    
    @Title(sequence="2")
    @MemberOrder(sequence="1")
    public Object getObject() {
        return object;
    }
    public void setObject(Object object) {
        this.object = object;
    }
    
    // //////////////////////////////////////

    @javax.inject.Inject
    FixtureScripts fixtureScripts;

}