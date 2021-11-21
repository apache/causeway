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
package org.apache.isis.applib.value.semantics;

/**
 * Indicates that parsing has failed, ie the entry is illegal (rather than
 * invalid).
 *
 * @since 1.x {@index}
 */
public class ParsingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ParsingException() {
        super();
    }

    public ParsingException(final String msg) {
        super(msg);
    }

    public ParsingException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public ParsingException(final Throwable cause) {
        super(cause);
    }

}
