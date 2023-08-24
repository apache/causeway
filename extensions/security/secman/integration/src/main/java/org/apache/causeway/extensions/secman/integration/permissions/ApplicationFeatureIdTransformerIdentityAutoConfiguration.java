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
package org.apache.causeway.extensions.secman.integration.permissions;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfigureOrder(PriorityPrecedence.LATE)
@Configuration
public class ApplicationFeatureIdTransformerIdentityAutoConfiguration {

    @Bean(CausewayModuleExtSecmanApplib.NAMESPACE + ".ApplicationFeatureIdTransformerIdentity")
    @ConditionalOnMissingBean(ApplicationFeatureIdTransformer.class)
    @Qualifier("Identity")
    public ApplicationFeatureIdTransformer ApplicationFeatureIdTransformer() {
        return new ApplicationFeatureIdTransformerIdentity();
    }

}
