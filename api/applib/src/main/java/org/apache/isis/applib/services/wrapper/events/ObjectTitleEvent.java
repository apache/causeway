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

package org.apache.isis.applib.services.wrapper.events;

import org.apache.isis.applib.Identifier;

/**
 * <i>Supported only by {@link org.apache.isis.applib.services.wrapper.WrapperFactory} service, </i> represents an access (reading) of an object's title.
 *
 * <p>
 * The {@link #getReason()} will always be <tt>null</tt>; access is always
 * allowed.
 *
 * @since 1.x {@index}
 */
public class ObjectTitleEvent extends AccessEvent {

    private final String title;

    public ObjectTitleEvent(final Object source, final Identifier classIdentifier, final String title) {
        super(source, classIdentifier);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

}
