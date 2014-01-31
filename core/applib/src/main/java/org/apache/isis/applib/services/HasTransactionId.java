/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services;

import java.util.UUID;


/**
 * Mix-in interface for objects (usually created by service implementations) that are be persistable, 
 * and so can be associated with the the GUID of the Isis transaction in which they were
 * created.
 * 
 * <p>
 * Other services can then use this transaction Id as a &quot;foreign key&quot; to store other
 * persistent information.  They may then use contributed actions/collections to render such aggregated
 * information back to the user.
 */
public interface HasTransactionId {

    /**
     * The unique identifier (a GUID) of the transaction in which this interaction occurred.
     * 
     * <p>
     * Note that this is the same as the Isis transaction guid as found in the JDO applib's
     * <tt>PublishedEvent</tt>.
     */
    public UUID getTransactionId();

    public void setTransactionId(final UUID transactionId);

    

}    
