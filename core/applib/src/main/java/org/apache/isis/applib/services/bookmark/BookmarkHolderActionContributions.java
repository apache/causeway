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
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * Domain service that contributes an action (named '<tt>lookup</tt>') to
 * any class that implements {@link org.apache.isis.applib.services.bookmark.BookmarkHolder}.
 *
 * <p>
 * Alternatively, can use {@link BookmarkHolderAssociationContributions}
 * to contribute an property.
 *
 * <p>
 * The service must be explicitly registered, typically in <tt>isis.properties</tt>.
 */
public class BookmarkHolderActionContributions  {


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


    @NotInServiceMenu
    @NotContributed(NotContributed.As.ASSOCIATION)
    public Object lookup(final BookmarkHolder bookmarkHolder) {
        return bookmarkService.lookup(bookmarkHolder);
    }


    @javax.inject.Inject
    private BookmarkService bookmarkService;

}
