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
package org.apache.causeway.core.metamodel.context;

import jakarta.inject.Named;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

/**
 *
 * @since 2.0
 *
 */
@Configuration
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".MetaModelContextFactory")
public class MetaModelContextFactory {

    @Primary
    @Bean(destroyMethod = "onDestroy")
    public MetaModelContext metaModelContext(final CausewaySystemEnvironment systemEnvironment) {

        var ioc = systemEnvironment.getIocContainer();
        var mmc = new MetaModelContext_usingSpring(ioc);

        if(isIntegrationTesting()) {
            MetaModelContext.setOrReplace(mmc);
            return mmc;
        }

        MetaModelContext.set(mmc);
        return mmc;
    }

    @Bean
    public ObjectManager objectManager(final MetaModelContext mmc) {
        return new ObjectManager(mmc);
    }

    // -- HELPER

    /**
     * Whether we find Spring's ContextCache on the class path.
     */
    private static boolean isIntegrationTesting() {
        try {
            Class.forName("org.springframework.test.context.cache.ContextCache");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
