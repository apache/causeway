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
package org.apache.isis.applib.mixins.updates;

/**
 * Allows domain entities that reference the user that updated them to be called
 * by the (framework-provided) <code>TimestampService</code> whenever modified
 * in a transaction with the current user.
 *
 * <p>
 *     Note that this interface defines only a setter.  The
 *     {@link org.apache.isis.applib.mixins.security.HasUsername} can be used
 *     to expose this user name, allowing other modules to contribute behaviour
 *     to that mixee.
 * </p>
 *
 * @see org.apache.isis.applib.mixins.security.HasUsername
 *
 * @since 2.0 {@index}
 */
public interface OnUpdatedBy {

    void setUpdatedBy(String updatedBy);

}
