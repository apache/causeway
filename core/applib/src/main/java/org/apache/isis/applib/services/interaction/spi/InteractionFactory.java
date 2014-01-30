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
package org.apache.isis.applib.services.interaction.spi;

import java.util.UUID;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.interaction.Interaction;

/**
 * Intended for service to implement, providing a different implementation of
 * {@link Interaction}.
 * 
 * <p>
 * The default implementation (provided automatically by the framework) will
 * instantiate an in-memory implementation of {@link Interaction}.  However, other
 * services (eg as provided by the JDO objectstore) might provide a persistable 
 * {@link Interaction} object.
 */
public interface InteractionFactory {

    @Programmatic
    Interaction create();
    
    /**
     * Although the transactionId is also provided via {@link InteractionOutcome} in the
     * {@link #complete(Interaction)} callback, it is passed in here as well
     * so that an implementation can ensure that the {@link Interaction} is fully populated in order
     * to persist if required.
     * 
     * <p>
     * One case where this may be supported (for example, by the <tt>InteractionServiceJdo</tt> implementation)
     * is to flush still-running {@link Interaction}s to the database on-demand.
     */
    @Programmatic
    void startTransaction(final Interaction interaction, final UUID transactionId);
    
    @Programmatic
    void complete(final Interaction interaction);
    
}
