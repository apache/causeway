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
 * <i>Supported only by {@link org.apache.isis.applib.services.wrapper.WrapperFactory} service, </i> represents an interaction with a collection object itself.
 *
 * @since 1.x {@index}
 */
public class CollectionMethodEvent extends AccessEvent {

    private final Object domainObject;
    private final String methodName;
    private final Object[] args;
    private final Object returnValue;

    public CollectionMethodEvent(final Object source, final FeatureIdentifier collectionIdentifier, final Object domainObject, final String methodName, final Object[] args, final Object returnValue) {
        super(source, collectionIdentifier);
        this.domainObject = domainObject;
        this.methodName = methodName;
        this.args = args;
        this.returnValue = returnValue;
    }

    /**
     * The collection object (an instance of a <tt>List</tt> or a <tt>Set</tt>
     * etc) that is the originator of this event.
     *
     * <p>
     * The owning domain object is available using {@link #getDomainObject()}.
     *
     * @see #getDomainObject()
     */
    @Override
    public Object getSource() {
        return super.getSource();
    }

    /**
     * The owner of the collection (an instance of
     * <tt>Customer/tt> or <tt>Order</tt>, say).
     *
     * @see #getSource()
     */
    public Object getDomainObject() {
        return domainObject;
    }

    /**
     * The name of the method invoked on this collection, for example
     * <tt>isEmpty</tt>.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * The arguments with which the collection's {@link #getMethodName() method}
     * was invoked.
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * The return value from the {@link #getMethodName() method} invocation.
     */
    public Object getReturnValue() {
        return returnValue;
    }

}
