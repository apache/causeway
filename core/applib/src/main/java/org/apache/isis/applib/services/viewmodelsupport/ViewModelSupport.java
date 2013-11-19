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
package org.apache.isis.applib.services.viewmodelsupport;

import org.apache.isis.applib.annotation.Programmatic;

public interface ViewModelSupport {

    public static interface Memento {

        @Programmatic
        public Memento set(String name, Object value);

        @Programmatic
        public <T> T get(String name, Class<T> cls);

        @Programmatic
        public String asString();
    }
    
    /**
     * Creates an empty {@link Memento}.
     * 
     * <p>
     * Typically followed by {@link Memento#set(String, Object)} for each of the data values to
     * add to the {@link Memento}, then {@link Memento#asString()} to convert to a string format.
     */
    @Programmatic
    public Memento create();

    /**
     * Parse string returned from {@link Memento#asString()}
     * 
     * <p>
     * Typically followed by {@link Memento#get(String, Class)} for each of the data values held
     * in the {@link Memento}. 
     */
    @Programmatic
    public Memento parse(final String str);

}
