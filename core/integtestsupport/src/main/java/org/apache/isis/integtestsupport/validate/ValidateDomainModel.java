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
package org.apache.isis.integtestsupport.validate;

import java.util.Collection;

import org.apache.isis.metamodel.spec.DomainModelException;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.system.context.IsisContext;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0
 *
 */
@Log4j2
public class ValidateDomainModel implements Runnable {

    public void run() {

        val specificationLoader = IsisContext.getSpecificationLoader();
        val validationFailures = specificationLoader.validate();

        val objectSpecifications = specificationLoader.currentSpecifications();
        
        if(log.isDebugEnabled()) {
            for (ObjectSpecification objectSpecification : objectSpecifications) {
                log.debug("loaded: " + objectSpecification.getFullIdentifier());
            }
        }

        if (validationFailures.occurred()) {
            throwFailureException(
                    validationFailures.getNumberOfMessages() + " problems found.", 
                    validationFailures.getMessages());
        }
    }
    
    // -- HELPER
    
    private void throwFailureException(String errorMessage, Collection<String> logMessages) {
        logErrors(logMessages);
        throw new DomainModelException(errorMessage);
    }
    
    private void logErrors(Collection<String> logMessages) {
        log.error("");
        log.error("");
        log.error("");
        for (String logMessage : logMessages) {
            log.error(logMessage);
        }
        log.error("");
        log.error("");
        log.error("");
    }

}