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

package org.apache.isis.core.commons.exceptions;

/**
 * Indicates an error raised by the application code.
 *
 * <p>
 * The viewer is expected to render the message within the application in a
 * user-friendly fashion, for example as a growl-like notification.
 *
 * <p>
 * <b>Note:</b> application code should not throw this exception directly; instead use {@link org.apache.isis.applib.ApplicationException},
 * {@link org.apache.isis.applib.RecoverableException}, {@link org.apache.isis.applib.NonRecoverableException} or {@link org.apache.isis.applib.FatalException}
 * </p>
 */
public class IsisApplicationException extends IsisException {

    private static final long serialVersionUID = 1L;

    public IsisApplicationException() {
        super();
    }

    public IsisApplicationException(final String msg) {
        super(msg);
    }

    public IsisApplicationException(final Throwable cause) {
        super(cause);
    }

    public IsisApplicationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
