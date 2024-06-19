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
package org.apache.causeway.applib.exceptions.unrecoverable;

import org.apache.causeway.applib.exceptions.UnrecoverableException;
import org.apache.causeway.applib.services.i18n.TranslatableString;

/**
 * Indicates that a bookmark cannot be found.
 *
 * @since 2.1, 3.1 {@index}
 *
 */
public class BookmarkNotFoundException extends UnrecoverableException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public BookmarkNotFoundException(final String msg) {
        super(msg);
    }

    public BookmarkNotFoundException(final TranslatableString translatableMessage,
            final Class<?> translationContextClass, final String translationContextMethod) {
        super(translatableMessage, translationContextClass, translationContextMethod);
    }

    public BookmarkNotFoundException(final Throwable cause) {
        super(cause);
    }

    public BookmarkNotFoundException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public BookmarkNotFoundException(final TranslatableString translatableMessage,
            final Class<?> translationContextClass, final String translationContextMethod, final Throwable cause) {
        super(translatableMessage, translationContextClass, translationContextMethod, cause);
    }
}