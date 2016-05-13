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
package org.apache.isis.applib.services.memento;

import java.util.Set;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.jaxb.JaxbService;

/**
 * This service provides a mechanism by which a serializable memento of arbitrary state can be created.  Most
 * commonly this is in support of implementing the {@link org.apache.isis.applib.ViewModel} interface.
 *
 * <p>
 * Because an implementation of this service (<tt>MementoServiceDefault</tt>) is annotated with
 * {@link org.apache.isis.applib.annotation.DomainService} and is implemented in the core runtime, it is automatically
 * registered and available for use; no configuration is required.
 * </p>
 *
 * @deprecated - for view models, use JAXB annotations and the {@link JaxbService}; for action invocations and such like, use {@link InteractionContext} and {@link CommandContext}.
 */
@Deprecated
public interface MementoService {

    /**
     * @deprecated - because {@link MementoService} is deprecated.
     */
    @Deprecated
    public static interface Memento {

        /**
         * @deprecated - because {@link MementoService} is deprecated.
         */
        @Deprecated
        @Programmatic
        public Memento set(String name, Object value);

        /**
         * @deprecated - because {@link MementoService} is deprecated.
         */
        @Deprecated
        @Programmatic
        public <T> T get(String name, Class<T> cls);

        /**
         * @deprecated - because {@link MementoService} is deprecated.
         */
        @Deprecated
        @Programmatic
        public String asString();

        /**
         * @deprecated - because {@link MementoService} is deprecated.
         */
        @Deprecated
        public Set<String> keySet();
    }
    
    /**
     * Creates an empty {@link Memento}.
     * 
     * <p>
     * Typically followed by {@link Memento#set(String, Object)} for each of the data values to
     * add to the {@link Memento}, then {@link Memento#asString()} to convert to a string format.
     *
     * @deprecated - because {@link MementoService} is deprecated.
     */
    @Deprecated
    @Programmatic
    public Memento create();

    /**
     * Parse string returned from {@link Memento#asString()}
     * 
     * <p>
     * Typically followed by {@link Memento#get(String, Class)} for each of the data values held
     * in the {@link Memento}. 
     *
     * @deprecated - because {@link MementoService} is deprecated.
     */
    @Deprecated
    @Programmatic
    public Memento parse(final String str);

    /**
     * Whether the value can be provided &quot;as-is&quot; as an argument to {@link Memento#set(String, Object)},
     * or whether it must be converted in some way.
     * 
     * <p>
     * The intention here is that a {@link Memento} implementation should be able to accept most/all common value types
     * (int, String, Date, BigDecimal etc), but will require entities to be converted into a serializable format,
     * specifically, as a {@link Bookmark}.
     *
     * @deprecated - because {@link MementoService} is deprecated.
     */
    @Deprecated
    @Programmatic
    public boolean canSet(Object input);


}
