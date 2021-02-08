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
package org.apache.isis.applib.services.linking;

import java.net.URI;


/**
 * A service that returns a web link (`java.net.URI`) to any domain object in
 * one of the framework's viewers.
 *
 * <p>
 *     A typical use case is to generate a clickable link for rendering in an
 *     email, PDF, tweet or other communication.
 * </p>
 *
 * <p>
 *     Note that the implementation is specific to the viewer(s).  At the time
 *     of writing only one implementation is available, for the Wicket viewer.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface DeepLinkService {

    /**
     * Creates a URI that can be used to obtain a representation of the provided domain object in one of the
     * Isis viewers.
     */
    URI deepLinkFor(Object domainObject);

}
