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
package org.apache.causeway.core.metamodel.services.objectlifecycle;

/**
 *
 * @since 2.0
 *
 */
public enum PropertyValuePlaceholder {

    /**
     * Indicates that the audit trail was unable to determine the value of the property, possibly because an
     * exception was thrown.
     *
     * <p>
     *     One way this can happen is if the property is derived but its implementation does not guard against
     *     null references to other related objects.  In particular, this could occur when deleting an aggregate root
     *     that surfaces the properties of its child entity items directly.  The child entity is deleted first, and
     *     then the aggregate root, but attempting to capture the value of this derived property the fails.
     * </p>
     */
    UNKNOWN,
    /**
     * Used as the <i>pre</i> value of a property for an object that is being created.
     */
    NEW,
    /**
     * Used as the <i>post</i> value of a property for an object that has been deleted.
     */
    DELETED
    ;

    @Override
    public String toString() {
        return "[" + name() + "]";
    }

}
