/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.layout;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.layout.fixedcols.FCPage;

@Mixin
public class Object_viewLayout {

    private final Object object;

    public Object_viewLayout(final Object object) {
        this.object = object;
    }

    public static class ActionDomainEvent extends org.apache.isis.applib.IsisApplibModule.ActionDomainEvent<Object_viewLayout> {}



    @Programmatic // TODO ... excluded for now (getting an Isis framework exception in the view model rendering).
    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-th"
    )
    @MemberOrder(sequence = "550.2")
    public FCPage $$() {
        return getObjectLayoutMetadata();
    }

    @Programmatic // TODO ... excluded for now (getting an Isis framework exception in the view model rendering).
    public boolean hide$$() {
        return getObjectLayoutMetadata() == null;
    }

    protected FCPage getObjectLayoutMetadata() {
        return objectLayoutMetadataService.toMetadata(object);
    }



    @Inject
    ObjectLayoutMetadataService objectLayoutMetadataService;

}
