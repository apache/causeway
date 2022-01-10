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
package org.apache.isis.applib.services.routing;

/**
 * Provides the ability to return (and therefore have rendered) an alternative
 * object from an action invocation.
 *
 * <p>
 * There are two primary use cases:
 * </p>
 *
 * <ul>
 *     <li>
 *          <p>
 *              if an action returns an aggregate leaf (that is, a child
 *              object which has an owning parent), then the parent object can
 *              be * returned instead.
 *          </p>
 *          <p>
 *              For example, an action returning `OrderItem` might instead
 *              render the owning `Order` object.  It is the responsibility
 *              of the implementation to figure out what the "owning" object
 *              might be.
 *          </p>
 *     </li>
 *     <li>
 *          <p>
 *              if an action returns `null` or is `void`, then return some
 *              other "useful" object.
 *          </p>
 *          <p>
 *              For example, return the home page (eg as defined by the
 *              {@link org.apache.isis.applib.annotation.HomePage} annotation).
 *          </p>
 *     </li>
 * </ul>
 *
 * <p>
 * Currently this service is used only by the Wicket viewer; it is ignored by
 * the Restful Objects viewer.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface RoutingService {

    /**
     * whether this implementation recognizes and can "route" the object.
     *
     * <p>
     *     The {@link #route(Object)} method is only called if this method
     *     returns <code>true</code>.
     * </p>
     *
     * @param original
     */
    boolean canRoute(Object original);

    /**
     * The object to route to instead; this may be the same as the original
     * object, some other object, or (indeed) `null`.
     *
     * @param original
     */
    Object route(Object original);

}
