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
 * Note that this exception has identical semantics to {@link FatalException} (of which it is the immediate
 * superclass) and can be considered a synonym.
 * 
 * @see RecoverableException
 * @see ApplicationException
 * @see FatalException
 */
public class NonRecoverableException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public NonRecoverableException(final String msg) {
        super(msg);
    }

    public NonRecoverableException(final Throwable cause) {
        super(cause);
    }

    public NonRecoverableException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
