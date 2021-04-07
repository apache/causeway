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
package demoapp.dom.services.core.wrapperFactory;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;

import lombok.RequiredArgsConstructor;
import lombok.val;

//tag::class[]
@Action(
    semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "propertyAsync"
)
@ActionLayout(
    named = "Mixin Update Property"
    , sequence = "2"
)
@RequiredArgsConstructor
public class WrapperFactoryJdo_mixinUpdatePropertyAsync {

    @Inject WrapperFactory wrapperFactory;

    // ...
//end::class[]

    private final WrapperFactoryJdo wrapperFactoryJdo;

//tag::class[]
    public WrapperFactoryJdo act(final String value) {
        val control = AsyncControl.returningVoid().withSkipRules();
        val wrapped = this.wrapperFactory.asyncWrap(this.wrapperFactoryJdo, control);
        wrapped.setPropertyAsync(value);
        return this.wrapperFactoryJdo;
    }
    public String default0Act() {
        return wrapperFactoryJdo.getPropertyAsync();
    }
}
//end::class[]
