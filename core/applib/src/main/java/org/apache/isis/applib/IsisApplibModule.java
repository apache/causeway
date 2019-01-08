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
package org.apache.isis.applib;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "module")
public class IsisApplibModule extends ModuleAbstract {

    //region > ui event classes
    public abstract static class TitleUiEvent<S>
            extends org.apache.isis.applib.services.eventbus.TitleUiEvent<S> { }
    public abstract static class IconUiEvent<S>
            extends org.apache.isis.applib.services.eventbus.IconUiEvent<S> { }
    public abstract static class CssClassUiEvent<S>
            extends org.apache.isis.applib.services.eventbus.CssClassUiEvent<S> { }
    public abstract static class LayoutUiEvent<S>
            extends org.apache.isis.applib.services.eventbus.LayoutUiEvent<S> { }
    //endregion

    //region > domain event classes
    public abstract static class ActionDomainEvent<S> extends org.apache.isis.applib.services.eventbus.ActionDomainEvent<S> {
        public ActionDomainEvent() {}

        /**
         * @deprecated - use the {@link #ActionDomainEvent() no-arg constructor} instead to avoid boilerplate.
         */
        @Deprecated
        public ActionDomainEvent(final S source, final Identifier identifier) {
            super(source, identifier);
        }
        /**
         * @deprecated - use the {@link #ActionDomainEvent() no-arg constructor} instead to avoid boilerplate.
         */
        @Deprecated
        public ActionDomainEvent(final S source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }
        /**
         * @deprecated - use the {@link #ActionDomainEvent() no-arg constructor} instead to avoid boilerplate.
         */
        @Deprecated
        public ActionDomainEvent(final S source, final Identifier identifier, final List<Object> arguments) {
            super(source, identifier, arguments);
        }
    }

    public abstract static class CollectionDomainEvent<S,T> extends org.apache.isis.applib.services.eventbus.CollectionDomainEvent<S,T> {
        public CollectionDomainEvent() { }
        /**
         * @deprecated - use the {@link #CollectionDomainEvent() no-arg constructor} instead to avoid boilerplate.
         */
        @Deprecated
        public CollectionDomainEvent(final S source, final Identifier identifier, final Of of) {
            super(source, identifier, of);
        }
        /**
         * @deprecated - use the {@link #CollectionDomainEvent() no-arg constructor} instead to avoid boilerplate.
         */
        @Deprecated
        public CollectionDomainEvent(final S source, final Identifier identifier, final Of of, final T value) {
            super(source, identifier, of, value);
        }
    }

    public abstract static class PropertyDomainEvent<S,T> extends org.apache.isis.applib.services.eventbus.PropertyDomainEvent<S,T> {
        public PropertyDomainEvent() { }
        /**
         * @deprecated - use the {@link #PropertyDomainEvent() no-arg constructor} instead to avoid boilerplate.
         */
        @Deprecated
        public PropertyDomainEvent(final S source, final Identifier identifier) {
            super(source, identifier);
        }
        /**
         * @deprecated - use the {@link #PropertyDomainEvent() no-arg constructor} instead to avoid boilerplate.
         */
        @Deprecated
        public PropertyDomainEvent(final S source, final Identifier identifier, final T oldValue, final T newValue) {
            super(source, identifier, oldValue, newValue);
        }
    }

    //endregion

}
