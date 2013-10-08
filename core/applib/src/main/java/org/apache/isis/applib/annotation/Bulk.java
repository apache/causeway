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
import java.util.Arrays;
import java.util.List;

/**
 * Indicates the (entity) action should be used only against many objects
 * in a collection.
 * 
 * <p>
 * Bulk actions have a number of constraints:
 * <ul>
 * <li>It must take no arguments
 * <li>It cannot be hidden (any annotations or supporting methods to that effect will be
 *     ignored).
 * <li>It cannot be disabled (any annotations or supporting methods to that effect will be
 *     ignored).
 * </ul>
 * 
 * <p>
 * Has no meaning if annotated on an action of a domain service.
 */
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Bulk {
    
    public static class InteractionContext {

        /**
         * Intended only to be set only by the framework.
         * 
         * <p>
         * Will be populated while a bulk action is being invoked.
         * 
         * <p>
         * <b>Note</b>: the original design was for this field to be defined within {@link Bulk}.  However,
         * this <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6982543">javac bug</a> unfortunately
         * means that this field must live outside the annotation.
         */
        public static final ThreadLocal<InteractionContext> current = new ThreadLocal<InteractionContext>();

        private final List<Object> domainObjects;
        private final int size;
        
        private int index;

        public InteractionContext(Object... domainObjects) {
            this(Arrays.asList(domainObjects));
        }

        public InteractionContext(List<Object> domainObjects) {
            this.domainObjects = domainObjects;
            this.size = domainObjects.size();
        }

        public List<Object> getDomainObjects() {
            return domainObjects;
        }
        
        /**
         * Will be a value in range [0, {@link #getSize() size}).
         */
        public int getIndex() {
            return index;
        }
        
        /**
         * <b>NOT API</b>: intended to be called only by the framework.
         */
        public void setIndex(int index) {
            this.index = index;
        }
        
        public int getSize() {
            return size;
        }

        public boolean isFirst() {
            return this.index == 0;
        }
        
        public boolean isLast() {
            return this.index == (size-1);
        }

        /**
         * @param runnable
         */
        public static void with(Runnable runnable, Object... domainObjects) {
            try {
                Bulk.InteractionContext.current.set(new Bulk.InteractionContext(domainObjects));
                runnable.run();
            } finally {
                Bulk.InteractionContext.current.set(null);
            }
        }

    }
}
