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
package org.apache.isis.jdo.datanucleus.service.eventbus;

import org.apache.isis.jdo.datanucleus.JDOStateManagerForIsis;
import org.apache.isis.jdo.datanucleus.JDOStateManagerForIsis.Hint;

/**
 * This domain service that enables both the framework and application code to publish events through either a
 * Guava {@link com.google.common.eventbus.EventBus} instance or through an Axon
 * {@link org.axonframework.eventhandling.SimpleEventBus} instance.

 *
 * <p>
 * In addition, this implementation is &quot;JDO-aware&quot; meaning that it allows events to be
 * {@link #post(Object) posted} from the setters of entities, automatically ignoring any calls to those setters that
 * occur as a side-effect of the JDO load/detach lifecycle.
 *
 * <p>
 * This implementation has no UI and there are no other implementations of the service API, and so it is annotated
 * with {@link org.apache.isis.applib.annotation.DomainService}.  Because it is implemented in the core, this means
 * that it is automatically registered and available for use; no further configuration is required.
 */
@Deprecated //FIXME[2112] find a replacement for that feature!?
public class EventBusServiceJdo /*extends EventBusServiceDefault*/ {

    /**
     * skip if called in any way by way of the {@link JDOStateManagerForIsis}.
     *
     * <p>
     * The {@link JDOStateManagerForIsis} sets a {@link JDOStateManagerForIsis#hint threadlocal}
     * if it has been called.
     */
    //@Override
    protected boolean skip(Object event) {
        return JDOStateManagerForIsis.hint.get() != Hint.NONE;
    }


}

