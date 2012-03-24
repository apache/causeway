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

package org.apache.isis.applib.profiles;

import org.apache.isis.applib.fixtures.UserProfileFixture;

/**
 * Domain object representing a particular perspective on the services or
 * objects available to a logged-in user.
 * 
 * <p>
 * The word "perspective" is used here in a similar sense to the Eclipse IDE,
 * meaning a configuration of objects. You might also think of it as the user's
 * desktop (though that metaphor only makes sense with the DnD viewer).
 * 
 * <p>
 * Note that this type is an interface, not a class. The actual implementation
 * is provided by the framework itself.
 * 
 * <p>
 * {@link Perspective}s go together with {@link Profile}s: a {@link Profile} is
 * a container of multiple {@link Perspective}s. As such, {@link Perspective}s
 * can be created from {@link Profile}s; {@link Profile}s themselves are created
 * using the {@link UserProfileFixture} can be used. Thereafter the @{link
 * Profile} and its {@link Perspective}s are stored in a <tt>profilestore</tt>
 * (analogous to an object store).
 */
public interface Perspective {

    Object addToServices(Class<?> serviceClass);

    void removeFromServices(Class<?> serviceClass);

    void addToServices(Class<?>... serviceClasses);

    void removeFromServices(Class<?>... serviceClasses);

    void addGenericRepository(Class<?>... domainClasses);

    void addToObjects(Object... object);

}
