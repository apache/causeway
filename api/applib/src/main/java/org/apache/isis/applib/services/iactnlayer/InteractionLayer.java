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
package org.apache.isis.applib.services.iactnlayer;

import org.apache.isis.applib.services.iactn.Interaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Binds an {@link Interaction} (&quot;what&quot; is being executed) with
 * an {@link InteractionContext} (&quot;who&quot; is executing, &quot;when&quot; and &quot;where&quot;).
 *
 * <p>
 * {@link InteractionLayer}s are so called because they may be nested (held in a stack).  For example the
 * {@link org.apache.isis.applib.services.sudo.SudoService} creates a new temporary layer with a different
 * {@link InteractionContext#getUser() user}, while fixtures that mock the clock switch out the
 * {@link InteractionContext#getClock() clock}.
 * </p>
 *
 * <p>
 * The stack of layers is per-thread, managed by {@link InteractionService} as a thread-local).
 * </p>
 *
 * @since 2.0 {@index}
 */
@RequiredArgsConstructor
public class InteractionLayer {

	/**
	 * Current thread's {@link Interaction} : &quot;what&quot; is being executed
	 */
	@Getter private final Interaction interaction;

	/**
	 * &quot;who&quot; is performing this {@link #getInteraction()}, also
	 * &quot;when&quot; and &quot;where&quot;.
	 */
	@Getter private final InteractionContext interactionContext;

}
