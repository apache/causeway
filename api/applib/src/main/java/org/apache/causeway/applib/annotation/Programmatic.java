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

package org.apache.causeway.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an property, collection or action is to be called
 * programmatically and should be ignored from the metamodel.
 *
 * <p>
 * For example, it may be a helper method that needs to be <tt>public</tt> but
 * that doesn't conform to the requirements of an action (for example, invalid
 * parameter types).
 *
 * <p>
 * It can also be added to a type, meaning that the type is ignored from the metamodel.
 * This is intended as a &quot;get out of jail&quot; for any classes from unit tests, say,
 * that end up on the classpath of integration tests but should otherwise be ignored.
 * @apiNote synonym for {@link Domain.Exclude}
 * @see Domain.Exclude
 * @since 1.x {@index}
 */
@Inherited
@Target({
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.FIELD
})
@Retention(RetentionPolicy.RUNTIME)
@Domain.Exclude // meta annotation
public @interface Programmatic {
}
