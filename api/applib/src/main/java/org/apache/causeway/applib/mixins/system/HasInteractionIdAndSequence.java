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
package org.apache.causeway.applib.mixins.system;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Property;

/**
 * Extends {@link HasInteractionId} to add a strictly monotonically increasing
 * sequence number.
 *
 * <p>
 *     There are two different use cases for this:
 * </p>
 * <ul>
 *     <li>
 *         <p>
 *             The first is to identify different transactions within the overall
 *             {@link org.apache.causeway.applib.services.iactn.Interaction}.
 *         </p>
 *         <p>
 *             In the vast majority of cases there will only be a single transaction
 *             per {@link org.apache.causeway.applib.services.iactn.Interaction}, but this
 *             isn't <i>always</i> the case as domain objects may on occasion need to
 *             explicitly manage transaction boundaries using
 *             {@link org.apache.causeway.applib.services.xactn.TransactionService}.
 *         </p>
 *     </li>
 *     <li>
 *         <p>
 *             The second is to identify different executions within the overall
 *             {@link org.apache.causeway.applib.services.iactn.Interaction}.
 *         </p>
 *         <p>
 *             In the vast majority of cases there will only be a single execution
 *             per {@link org.apache.causeway.applib.services.iactn.Interaction}, but this
 *             isn't <i>always</i> the case; sometimes a top-level execution will cause
 *             another execution to occur, using the {@link org.apache.causeway.applib.services.wrapper.WrapperFactory}.
 *         </p>
 *     </li>
 * </ul>
 * <p>
 *     These two different sequences are independent of each other.
 * </p>
 *
 *
 * @since 2.0 {@index}
 */
public interface HasInteractionIdAndSequence extends HasInteractionId {

    @Property(
            editing = Editing.DISABLED
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sequence {
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }

    /**
     * Holds the sequence number uniquely an &quot;event&quot; within the overall
     * {@link org.apache.causeway.applib.services.iactn.Interaction}.
     *
     * <p>
     *     The &quot;event&quot; could be a member execution (an action invocation or property edit),
     *     or could be a transaction.
     * </p>
     */
    @Sequence
    int getSequence();
}
