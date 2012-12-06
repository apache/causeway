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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.bookmarks.Bookmark;

/**
 * Specifies a repository action to use to support auto-complete.
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoComplete {

    /**
     * A candidate match for the auto-complete.
     * 
     * <p>
     * The auto-complete action is required to return a list of these instances.
     * The title can be used by the viewer in the drop down, while the
     * {@link #getBookmark()} identifies the object.
     */
    public static class Candidate {
        private final String title;
        private final Bookmark bookmark;
        public Candidate(String title, Bookmark bookmark) {
            this.title = title;
            this.bookmark = bookmark;
        }
        public String getTitle() {
            return title;
        }
        public Bookmark getBookmark() {
            return bookmark;
        }
    }
    
    /**
     * The class of the repository to use.
     * 
     * <p>
     * It is sufficient to specify an interface rather than a concrete type.
     */
    Class<?> repository();
    
    /**
     * The action to use in order to perform the auto-complete search
     * (defaults to &quot;autoComplete&quot;).
     * 
     * <p>
     * The action is required to accept a single string parameter, and must return
     * 
     */
    String action() default "autoComplete";
}
