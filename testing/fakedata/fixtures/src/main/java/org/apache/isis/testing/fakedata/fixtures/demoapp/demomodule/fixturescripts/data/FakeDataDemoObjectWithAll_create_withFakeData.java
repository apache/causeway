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
package org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.fixturescripts.data;

import org.apache.isis.applib.annotations.Programmatic;
import org.apache.isis.testing.fakedata.applib.services.FakeDataService;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.dom.FakeDataDemoObjectWithAll;
import org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.dom.FakeDataDemoObjectWithAllMenu;

import lombok.Getter;
import lombok.Setter;

@lombok.experimental.Accessors(chain = true)
public class FakeDataDemoObjectWithAll_create_withFakeData extends FixtureScript {

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Boolean withFakeData;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private String name;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Boolean someBoolean;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Character someChar;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Byte someByte;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Short someShort;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Integer someInt;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Long someLong;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Float someFloat;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Double someDouble;

    @Getter
    private FakeDataDemoObjectWithAll fakeDataDemoObject;

    @Override
    protected void execute(final ExecutionContext executionContext) {

        // defaults
        this.defaultParam("withFakeData", executionContext, true);

        this.defaultParam("name", executionContext, fakeDataService.name().firstName());

        this.defaultParam("someBoolean", executionContext, withFakeData ? fakeDataService.booleans().any() : false);
        this.defaultParam("someChar", executionContext, withFakeData ? fakeDataService.chars().any() : (char)0);
        this.defaultParam("someByte", executionContext,   withFakeData ? fakeDataService.bytes().any(): (byte)0);
        this.defaultParam("someShort", executionContext,  withFakeData ? fakeDataService.shorts().any(): (short)0);
        this.defaultParam("someInt", executionContext,    withFakeData ? fakeDataService.ints().any(): 0);
        this.defaultParam("someLong", executionContext,   withFakeData ? fakeDataService.longs().any(): 0L);
        this.defaultParam("someFloat", executionContext,  withFakeData ? fakeDataService.floats().any(): 0.0f);
        this.defaultParam("someDouble", executionContext, withFakeData ? fakeDataService.doubles().any(): 0.0);

        // create
        this.fakeDataDemoObject =
                wrap(demoObjectWithAllMenu).createDemoObjectWithAll(getName(), getSomeBoolean(), getSomeChar(), getSomeByte(), getSomeShort(), getSomeInt(), getSomeLong(), getSomeFloat(), getSomeDouble());

        executionContext.addResult(this, fakeDataDemoObject);
    }

    @javax.inject.Inject
    FakeDataDemoObjectWithAllMenu demoObjectWithAllMenu;

    @javax.inject.Inject
    FakeDataService fakeDataService;
}
