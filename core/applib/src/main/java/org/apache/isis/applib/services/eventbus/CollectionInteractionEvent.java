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
package org.apache.isis.applib.services.eventbus;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.util.ObjectContracts;

public abstract class CollectionInteractionEvent<S,T> extends AbstractInteractionEvent<S> {

    private static final long serialVersionUID = 1L;

    //region > Default class
    /**
     * Propagated if no custom subclass was specified using
     * {@link org.apache.isis.applib.annotation.InteractionWithAction} annotation.
     */
    public static class Default extends CollectionInteractionEvent<Object, Object> {
        private static final long serialVersionUID = 1L;
        public Default(
                final Object source,
                final Identifier identifier,
                final Of of,
                final Object value) {
            super(source, identifier, of, value);
        }
    }
    //endregion

    //region > constructors
    public CollectionInteractionEvent(
            final S source,
            final Identifier identifier,
            final Of of) {
        super(source, identifier);
        this.of = of;
    }

    public CollectionInteractionEvent(
            final S source,
            final Identifier identifier,
            final Of of,
            final T value) {
        this(source, identifier, of);
        this.value = value;
    }
    //endregion

    //region > value
    private T value;
    public T getValue() {
        return value;
    }
    public void setValue(T value) {
        this.value = value;
    }
    //endregion

    //region > Of

    public static enum Of {
        /**
         * The collection is being added to.
         */
        ADD_TO,
        /**
         * The collection is being removed from.
         */
        REMOVE_FROM
    }

    private final Of of;

    public Of getOf() {
        return of;
    }
    //endregion

    //region > toString
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source,identifier,of,mode,value");
    }
    //endregion
}