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
package org.apache.isis.core.metamodel.services.command;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.background.ActionInvocationMemento;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.schema.cmd.v1.CommandMementoDto;

/**
 * Used to create mementos of a command, such that it can be persisted and then executed either immediately (ie invoke
 * in the foreground) or deferred (ie invoke in the background at some later time).
 */
public interface CommandMementoService {

    /**
     * Note that this method (more precisely, {@link ActionInvocationMemento}) does <i>not</i> support mixins.
     *
     * @deprecated - use {@link #asCommandMemento(List, ObjectAction, ObjectAdapter[])} instead.
     */
    @Deprecated
    @Programmatic
    ActionInvocationMemento asActionInvocationMemento(Method m, Object domainObject, Object[] args);

    /**
     * The default implementation returns a non-null value.  If a custom implementation returns <tt>null</tt>, then
     * the framework will fall back to using the deprecated {@link #asActionInvocationMemento(Method, Object, Object[])}.
     */
    @Programmatic
    CommandMementoDto asCommandMemento(
            final List<ObjectAdapter> targetAdapters,
            final ObjectAction objectAction,
            final ObjectAdapter[] argAdapters);

}
