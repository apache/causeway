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
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.annotDomain._commands.ExposePersistedCommands;

//tag::class[]
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject(
        nature=Nature.ENTITY
        , objectType = "demo.WrapperFactoryJdo"
        , editing = Editing.DISABLED
)
public class WrapperFactoryJdo
        implements HasAsciiDocDescription, ExposePersistedCommands {

    @Inject WrapperFactory wrapperFactory;
    @Inject FactoryService factoryService;

    // ...
//end::class[]

    public WrapperFactoryJdo(String initialValue) {
        this.propertyAsync = initialValue;
        this.propertyAsyncMixin = initialValue;
    }

    public String title() {
        return "WrapperFactory";
    }

//tag::property[]
    @Property()
    @MemberOrder(name = "async", sequence = "1")
    @Getter @Setter
    private String propertyAsync;

    @Property()
    @MemberOrder(name = "async", sequence = "2")
    @Getter @Setter
    private String propertyAsyncMixin;
//end::property[]

//tag::async[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "propertyAsync"
        , associateWithSequence = "1"
    )
    @ActionLayout(
        describedAs = "@Action()"
    )
    public WrapperFactoryJdo updatePropertyAsync(final String value) {
        val control = AsyncControl.returningVoid().withSkipRules();
        val wrapperFactoryJdo = this.wrapperFactory.asyncWrap(this, control);
        wrapperFactoryJdo.setPropertyAsync(value);
        return this;
    }
    public String default0UpdatePropertyAsync() {
        return getPropertyAsync();
    }
//end::async[]

//tag::async[]
    @Action(
        semantics = SemanticsOf.IDEMPOTENT
        , associateWith = "propertyAsyncMixin"
        , associateWithSequence = "1"
    )
    @ActionLayout(
        describedAs = "Calls the 'updatePropertyAsync' (mixin) action asynchronously"
    )
    public WrapperFactoryJdo updatePropertyUsingAsyncWrapMixin(final String value) {
        val control = AsyncControl.returning(WrapperFactoryJdo.class).withSkipRules();
        val mixin = this.wrapperFactory.asyncWrapMixin(WrapperFactoryJdo_updatePropertyAsyncMixin.class, this, control);
        WrapperFactoryJdo act = mixin.act(value);
        return this;
    }
    public String default0UpdatePropertyUsingAsyncWrapMixin() {
        return new WrapperFactoryJdo_updatePropertyAsyncMixin(this).default0Act();
    }
//end::async[]

//tag::class[]

}
//end::class[]
