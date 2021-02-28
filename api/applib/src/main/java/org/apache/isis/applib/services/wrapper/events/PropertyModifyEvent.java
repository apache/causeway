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

import org.apache.isis.applib.id.FeatureIdentifier;

/**
 * <i>Supported only by {@link org.apache.isis.applib.services.wrapper.WrapperFactory} service, </i> represents a check as to whether a particular value for a property is valid
 * or not.
 *
 * <p>
 * If {@link #getReason()} is not <tt>null</tt> then provides the reason why the
 * value is invalid; otherwise the value is valid.
 * @since 1.x {@index}
 */
public class PropertyModifyEvent extends ValidityEvent {

    private final Object proposed;

    public PropertyModifyEvent(final Object source, final FeatureIdentifier propertyIdentifier, final Object proposed) {
        super(source, propertyIdentifier);
        this.proposed = proposed;
    }

    /**
     * If <tt>null</tt>, then the property was cleared.
     */
    @Override
    public Object getProposed() {
        return proposed;
    }

}
