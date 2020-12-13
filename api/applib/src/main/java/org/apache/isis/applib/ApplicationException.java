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

import org.apache.isis.applib.services.i18n.TranslatableString;

/**
 * Indicates that an exceptional condition/problem has occurred within the application's domain logic.
 *
 * <p>
 * Throwing this exception is equivalent to calling {@link DomainObjectContainer#raiseError(String)}.
 * The framework will trap the error and display the exception message as a warning.
 *
 * <p>
 * This exception should only be thrown for &quot;recoverable&quot; exceptions, that is, those which
 * could be anticipated by the application.  It should not be thrown for fatal, unanticipated exceptions.
 *
 * <p>
 * The framework attempts to apply some heuristics; if the underlying Isis transaction has been aborted
 * (for example as the result of a problem persisting some data) but then the application attempts to
 * throw this exception, the exception will be promoted to a fatal exception.
 *
 * <p>
 * Note that this exception has identical semantics to {@link RecoverableException}, and can be considered a
 * synonym.
 *
 * @see RecoverableException
 * @see NonRecoverableException
 * @see FatalException
 * @since ? {@index}
 */
public class ApplicationException extends RecoverableException {

    private static final long serialVersionUID = 1L;

    public ApplicationException(final String msg) {
        super(msg);
    }

    public ApplicationException(final TranslatableString translatableMessage, final Class<?> translationContextClass, final String translationContextMethod) {
        super(translatableMessage, translationContextClass, translationContextMethod);
    }

    public ApplicationException(final Throwable cause) {
        super(cause);
    }

    public ApplicationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public ApplicationException(final TranslatableString translatableMessage, final Class<?> translationContextClass, final String translationContextMethod, final Throwable cause) {
        super(translatableMessage, translationContextClass, translationContextMethod, cause);
    }
}
