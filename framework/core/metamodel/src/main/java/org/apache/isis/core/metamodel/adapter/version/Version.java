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

package org.apache.isis.core.metamodel.adapter.version;

import java.io.Serializable;
import java.util.Date;

import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

/**
 * An instance of this class is held by each {@link ObjectAdapter} and is used
 * to represent a particular version (at a point in time) of domain object
 * wrapped by that adapter.
 * 
 * <p>
 * This is normally done using some form of incrementing number or timestamp,
 * which would be held within the implementing class. The numbers, timestamps,
 * etc should change for each changed object, and the different() method should
 * indicate that the two Version objects are different.
 * 
 * <p>
 * The user's name and a timestamp should alos be kept so that when an message
 * is passed to the user it can be of the form "user has change object at time"
 */
public interface Version extends Serializable, Encodable {

    /**
     * Compares this version against the specified version and returns true if
     * they are different versions (by checking {@link #getSequence()}).
     * 
     * <p>
     * This is use for optimistic checking, where the existence of a different
     * version will normally cause a concurrency exception.
     */
    boolean different(Version version);

    /**
     * The internal, strictly monotonically increasing, version number.
     * 
     * <p>
     * This might be the timestamp of the change, or it might be simply a number incrementing 1,2,3...
     */
    long getSequence();
    
    /**
     * Returns the user who made the last change (used for display only)
     */
    String getUser();

    /**
     * Returns the time of the last change (used for display only, not comparison)
     */
    Date getTime();

    /**
     * Returns the sequence for printing/display
     */
    String sequence();

}
