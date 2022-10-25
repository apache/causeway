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
package org.apache.causeway.applib.services.wrapper.callable;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.schema.cmd.v2.CommandDto;

/**
 * Provides access to the details of the asynchronous callable (representing a child command to be executed
 * asynchronously) when using
 * {@link org.apache.causeway.applib.services.wrapper.WrapperFactory#asyncWrap(Object, AsyncControl)} and its brethren.
 *
 * <p>
 *     To explain in a little more depth; we can execute commands (actions etc) asynchronously using
 *     {@link org.apache.causeway.applib.services.wrapper.WrapperFactory#asyncWrap(Object, AsyncControl)} or similar.
 *     The {@link AsyncControl} parameter allows various aspects of this to be controlled, one such being the
 *     implementation of the {@link java.util.concurrent.ExecutorService} (using
 *     {@link AsyncControl#with(ExecutorService)}).
 * </p>
 *
 * <p>
 *     The default {@link ExecutorService} is just {@link java.util.concurrent.ForkJoinPool}, and this and similar
 *     implementations will hold the provided callable in memory and execute it in due course.  For these out-of-the-box
 *     implementations, the {@link java.util.concurrent.Callable} is a black box and they have no need to look inside
 *     it.  So long as the implementation of the Callable is not serialized then deserialized (ie is only ever held in
 *     memory), then all will work fine.
 * </p>
 *
 * <p>
 *     This interface, though, is intended to expose the details of the passed {@link java.util.concurrent.Callable},
 *     most notably the {@link CommandDto} to be executed.  The main use case this supports is to allow a custom
 *     implementation of {@link ExecutorService} to be provided that could do more sophisticated things, for example
 *     persisting the callable somewhere, either exploiting the fact that the object is serializable, or perhaps by
 *     unpacking the parts and persisting (for example, as a <code>CommandLogEntry</code> courtesy of the
 *     commandlog extension).
 * </p>
 *
 * <p>
 *     These custom implementations of {@link ExecutorService} must however reinitialize the state of the callable,
 *     either by injecting in services using {@link org.apache.causeway.applib.services.inject.ServiceInjector} and then
 *     just <code>call()</code>ing it, or alternatively and more straightforwardly simply executing it using
 *     {@link org.apache.causeway.applib.services.wrapper.WrapperFactory#execute(AsyncCallable)}.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface AsyncCallable<R> extends Serializable {

    /**
     * The requested {@link InteractionContext} to execute the command, as inferred from the {@link AsyncControl}
     * that was used to call
     * {@link org.apache.causeway.applib.services.wrapper.WrapperFactory#asyncWrap(Object, AsyncControl)} and its ilk.
     */
    InteractionContext getInteractionContext();

    /**
     * The transaction propagation to use when creating a new {@link org.apache.causeway.applib.services.iactn.Interaction}
     * in which to execute the child command.
     */
    Propagation getPropagation();

    /**
     * Details of the actual child command (action or property edit) to be performed.
     *
     * <p>
     *     (Ultimately this is handed onto the {@link org.apache.causeway.applib.services.command.CommandExecutorService}).
     * </p>
     */
    CommandDto getCommandDto();

    /**
     * The type of the object returned by the child command once finally executed.
     */
    Class<R> getReturnType();

    /**
     * The unique {@link Command#getInteractionId() interactionId} of the parent {@link Command}, which is to say the
     * {@link Command} that was active in the original interaction where
     * {@link org.apache.causeway.applib.services.wrapper.WrapperFactory#asyncWrap(Object, AsyncControl)} (or its brethren)
     * was called.
     *
     * <p>
     *     This can be useful for custom implementations of {@link ExecutorService} that use the commandlog
     *     extension's <code>CommandLogEntry</code>, to link parent and child commands together.
     * </p>
     */
    UUID getParentInteractionId();

}
