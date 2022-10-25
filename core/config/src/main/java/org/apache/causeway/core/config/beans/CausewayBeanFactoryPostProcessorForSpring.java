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
package org.apache.causeway.core.config.beans;

import java.util.Objects;

import javax.inject.Named;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;
import org.apache.causeway.core.config.beans.aoppatch.AopPatch;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * The framework's stereotypes {@link DomainService}, {@link DomainObject}, etc.
 * are meta annotated with eg. {@link Component}, which allows for the Spring framework to pick up the
 * annotated type as candidate to become a managed bean.
 * <p>
 * By plugging into Spring's bootstrapping via a {@link BeanFactoryPostProcessor}, intercepting those
 * types is possible. Eg. {@link DomainObject} should not be managed by Spring, only discovered.
 *
 * @since 2.0
 *
 */
@Component
@Named(CausewayModuleCoreConfig.NAMESPACE + "..CausewayBeanFactoryPostProcessorForSpring")
@Import({
    AopPatch.class
})
@Log4j2
public class CausewayBeanFactoryPostProcessorForSpring
implements
    BeanFactoryPostProcessor,
    ApplicationContextAware {

    private CausewayBeanTypeClassifier causewayBeanTypeClassifier;
    private CausewayComponentScanInterceptor causewayComponentScanInterceptor;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        causewayBeanTypeClassifier = CausewayBeanTypeClassifier.createInstance(applicationContext);
        causewayComponentScanInterceptor = CausewayComponentScanInterceptor.createInstance(causewayBeanTypeClassifier);
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {

        // make sure we have an applicationContext before calling post processing
        Objects.requireNonNull(causewayBeanTypeClassifier,
                "postProcessBeanFactory() called before app-ctx was made available");

        val registry = (BeanDefinitionRegistry) beanFactory;

        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {

            log.debug("processing bean definition {}", beanDefinitionName);

            val beanDefinition = registry.containsBeanDefinition(beanDefinitionName)
                    ? registry.getBeanDefinition(beanDefinitionName)
                    : null;

            if(beanDefinition==null || beanDefinition.getBeanClassName() == null) {
                continue; // check next beanDefinition
            }

            val typeMetaData = ScannedTypeMetaData.of(
                    beanDefinition.getBeanClassName(),
                    beanDefinitionName);

            causewayComponentScanInterceptor.intercept(typeMetaData);

            if(typeMetaData.isVetoedForInjection()) {
                registry.removeBeanDefinition(beanDefinitionName);
                log.debug("vetoing bean {}", beanDefinitionName);
            } else {
                val beanNameOverride = typeMetaData.getBeanNameOverride();
                if(_Strings.isNotEmpty(beanNameOverride)) {
                    registry.removeBeanDefinition(beanDefinitionName);
                    registry.registerBeanDefinition(beanNameOverride, beanDefinition);
                    log.debug("renaming bean {} -> {}", beanDefinitionName, beanNameOverride);
                }
            }

        }

    }

    @Bean
    public CausewayBeanTypeClassifier getCausewayBeanTypeClassifier() {
        return causewayBeanTypeClassifier!=null
                ? causewayBeanTypeClassifier
                : (causewayBeanTypeClassifier = CausewayBeanTypeClassifier.createInstance()); // JUnit support
    }

    @Bean("causeway.bean-meta-data")
    public Can<CausewayBeanMetaData> getComponentScanResult() {
        return causewayComponentScanInterceptor.getAndDrainIntrospectableTypes();
    }


}
