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
package org.apache.isis.core.runtime.services.eventbus;

import java.util.List;
import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

/**
 * @deprecated - because <tt>EventBusServiceJdo</tt> is annotated as the default implementation.
 */
@Deprecated
public class EventBusServiceDefault extends EventBusService {

    @Override
    protected EventBus newEventBus() {
        return new EventBus(newEventBusSubscriberExceptionHandler());
    }

    protected SubscriberExceptionHandler newEventBusSubscriberExceptionHandler() {
        return new SubscriberExceptionHandler(){
            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                final List<Throwable> causalChain = Throwables.getCausalChain(exception);
                for (Throwable cause : causalChain) {
                    if(cause instanceof RecoverableException || cause instanceof NonRecoverableException) {
                        getTransactionManager().getTransaction().setAbortCause(new IsisApplicationException(exception));
                        return;
                    }
                }
                // otherwise simply ignore
            }
        };
    }

    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    //endregion


}

