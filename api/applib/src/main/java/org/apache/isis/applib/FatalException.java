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
 * Indicates that an unexpected, non-recoverable (fatal) exception has occurred within
 * the application logic.
 *
 * <p>
 * Throwing this exception will (dependent on the viewer) result in some sort of an error page being displayed to the user.
 *
 * <p>
 * Note that this exception has identical semantics to {@link NonRecoverableException}, and can be considered a
 * synonym.
 *
 * @see RecoverableException
 * @see ApplicationException
 * @see NonRecoverableException
 * @since 1.x {@index}
 */
public class FatalException extends NonRecoverableException {

    private static final long serialVersionUID = 1L;

    public FatalException(final String msg) {
        super(msg);
    }

    public FatalException(final Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public FatalException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
