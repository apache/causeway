/*
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
package org.apache.isis.applib.mixins.metamodel;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.metamodel.MetaModelServicesMenu;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.schema.metamodel.v1.DomainClassDto;
import org.apache.isis.schema.metamodel.v1.MetamodelDto;

@Mixin(method="prop")
public class Object_objectType {

    private final Object object;

    public Object_objectType(final Object object) {
        this.object = object;
    }

    public static class ActionDomainEvent extends org.apache.isis.applib.IsisApplibModule.ActionDomainEvent<Object_objectType> {
        private static final long serialVersionUID = 1L;
    }

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(contributed = Contributed.AS_ASSOCIATION)
    @Property()
    @MemberOrder(name = "metadata", sequence = "700.1")
    public String prop() {
        final Bookmark bookmark = bookmarkService.bookmarkFor(this.object);
        return bookmark.getObjectType();
    }


    @Inject
    BookmarkService bookmarkService;

}
