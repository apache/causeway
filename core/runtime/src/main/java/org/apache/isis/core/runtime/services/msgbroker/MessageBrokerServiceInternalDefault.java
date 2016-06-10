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
package org.apache.isis.core.runtime.services.msgbroker;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.services.msgbroker.MessageBrokerServiceInternal;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + (Integer.MAX_VALUE - 1)  // ie before the Noop impl in metamodel
)
public class MessageBrokerServiceInternalDefault implements MessageBrokerServiceInternal {

    @Override
    public void informUser(final String message) {
        getMessageBroker().addMessage(message);
    }

    @Override
    public void warnUser(final String message) {
        getMessageBroker().addWarning(message);
    }

    @Override
    public void raiseError(final String message) {
        throw new RecoverableException(message);
    }


    private MessageBroker getMessageBroker() {
        return isisSessionFactory.getCurrentSession().getAuthenticationSession().getMessageBroker();
    }

    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;

}
