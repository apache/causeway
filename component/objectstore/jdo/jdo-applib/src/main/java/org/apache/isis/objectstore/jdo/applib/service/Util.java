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
package org.apache.isis.objectstore.jdo.applib.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.Command;

public class Util {

    private Util() {}

    public static Bookmark bookmarkFor(final String str) {
        return str != null? new Bookmark(str): null;
    }

    public static String asString(Bookmark bookmark) {
        return bookmark != null? bookmark.toString(): null;
    }

    public static Object lookupBookmark(Bookmark bookmark, final BookmarkService bookmarkService, DomainObjectContainer container) {
        try {
            return bookmarkService != null
                    ? bookmarkService.lookup(bookmark)
                    : null;
        } catch(RuntimeException ex) {
            if(ex.getClass().getName().contains("ObjectNotFoundException")) {
                container.warnUser("Object not found - has it since been deleted?");
                return null;
            } 
            throw ex;
        }
    }

    
    public static String abbreviated(final String str, final int maxLength) {
        return str != null? (str.length() < maxLength ? str : str.substring(0, maxLength - 3) + "..."): null;
    }

    public static BigDecimal durationBetween(Timestamp startedAt, Timestamp completedAt) {
        if(completedAt == null) {
            return null;
        }
        long millis = completedAt.getTime() - startedAt.getTime();
        return new BigDecimal(millis).divide(new BigDecimal(1000)).setScale(3, RoundingMode.HALF_EVEN);
    }

}
