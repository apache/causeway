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
package org.apache.isis.applib.services.bookmark;

import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;

/**
 * Domain service that contributes a property (named '<tt>Object</tt>') to
 * any class that implements {@link org.apache.isis.applib.services.bookmark.BookmarkHolder}.
 *
 *
 * <p>
 *     This service is automatically registered.  However, if not required then its contributions can be hidden using
 *     a subscribed on its domain events.
 * </p>
 *
 * @see org.apache.isis.applib.services.bookmark.BookmarkHolderActionContributions
 */
@DomainService(
        nature = NatureOfService.VIEW_CONTRIBUTIONS_ONLY
)
public class BookmarkHolderAssociationContributions {

    public static abstract class PropertyDomainEvent<T> extends IsisApplibModule.PropertyDomainEvent<BookmarkHolderAssociationContributions, T> {
        public PropertyDomainEvent(final BookmarkHolderAssociationContributions source, final Identifier identifier) {
            super(source, identifier);
        }

        public PropertyDomainEvent(final BookmarkHolderAssociationContributions source, final Identifier identifier, final T oldValue, final T newValue) {
            super(source, identifier, oldValue, newValue);
        }
    }

    // //////////////////////////////////////

    //region > init
    @Programmatic
    @PostConstruct
    public void init(Map<String,String> props) {
        ensureDependenciesInjected();
    }

    private void ensureDependenciesInjected() {
        if(this.bookmarkService == null){
            throw new IllegalStateException("BookmarkService domain service must be configured");
        }
    }
    //endregion

    // //////////////////////////////////////

    public static class ObjectDomainEvent extends PropertyDomainEvent<Object> {
        public ObjectDomainEvent(final BookmarkHolderAssociationContributions source, final Identifier identifier) {
            super(source, identifier);
        }

        public ObjectDomainEvent(final BookmarkHolderAssociationContributions source, final Identifier identifier, final Object oldValue, final Object newValue) {
            super(source, identifier, oldValue, newValue);
        }
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
        contributed = Contributed.AS_ASSOCIATION
    )
    @Property(
        domainEvent = ObjectDomainEvent.class
    )
    public Object object(final BookmarkHolder bookmarkHolder) {
        return bookmarkService.lookup(bookmarkHolder);
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    private BookmarkService bookmarkService;

}
