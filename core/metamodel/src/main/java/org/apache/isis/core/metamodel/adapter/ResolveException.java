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

package org.apache.isis.core.metamodel.adapter;

import org.apache.isis.core.commons.exceptions.IsisException;

/**
 * Indicates that a request to resolve an object has failed. Unresolved objects
 * should never be used as they will cause further errors.
 */
public class ResolveException extends IsisException {
    private static final long serialVersionUID = 1L;

    public ResolveException() {
        super();
    }

    public ResolveException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public ResolveException(final String msg) {
        super(msg);
    }

    public ResolveException(final Throwable cause) {
        super(cause);
    }

}
