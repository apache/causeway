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

package org.apache.isis.applib;

/**
 * Indicates that the persistence of an object failed.
 *
 * <p>
 * This exception is intended to represent an unexpected and non-recoverable condition (eg a unique/primary key/
 * foreign key constaint has been violated), and so is a subclass of {@link NonRecoverableException}.
 * Throwing this exception will therefore result in (some sort of) error page being displayed
 * to the user.
 *
 * @see NonRecoverableException
 * @see RecoverableException
 * @since 1.x {@index}
 */
public class PersistFailedException extends NonRecoverableException {

    private static final long serialVersionUID = 1L;

    public PersistFailedException(final String msg) {
        super(msg);
    }

    public PersistFailedException(final Throwable cause) {
        super(cause);
    }

    public PersistFailedException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
