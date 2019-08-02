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
package org.apache.isis.testdomain.ldap;

import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.runners.model.InitializationError;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service @Log4j2
public class LdapServerService {

    private CountDownLatch serverLatch;

    @PostConstruct
    public void start() throws InitializationError, InterruptedException {
        serverLatch = LdapEmbeddedServer.run();
        log.info("Embedded LDAP Server started at port {}.", LdapConstants.PORT);
    }

    @PreDestroy
    public void stop() {
        if(serverLatch!=null) {
            serverLatch.countDown();
            serverLatch = null;
            log.info("Embedded LDAP Server issued STOP.");
        }
    }

}
