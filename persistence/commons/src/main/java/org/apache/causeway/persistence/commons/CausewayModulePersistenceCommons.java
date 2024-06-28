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
package org.apache.causeway.persistence.commons;

import org.apache.causeway.persistence.commons.integration.deadlock.DeadlockRecognizerDefault;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.core.runtime.CausewayModuleCoreRuntime;
import org.apache.causeway.persistence.commons.integration.changetracking.EntityChangeTrackerDefault;
import org.apache.causeway.persistence.commons.integration.changetracking.PreAndPostValueEvaluatorServiceDefault;
import org.apache.causeway.persistence.commons.integration.repository.RepositoryServiceDefault;

@Configuration
@Import({
        // modules
        CausewayModuleCoreRuntime.class,

        // @Service's
        EntityChangeTrackerDefault.class,
        PreAndPostValueEvaluatorServiceDefault.class,

        // @Component's
        DeadlockRecognizerDefault.class,

        // @Repository's
        RepositoryServiceDefault.class,

})
public class CausewayModulePersistenceCommons {

    public static final String NAMESPACE = "causeway.persistence.commons";
}
