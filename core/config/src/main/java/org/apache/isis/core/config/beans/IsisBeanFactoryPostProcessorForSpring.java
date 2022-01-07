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
package org.apache.isis.core.config.beans;

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

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.beans.aoppatch.AopPatch;

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
@Named("isis.config.IsisBeanFactoryPostProcessorForSpring")
@Import({
    AopPatch.class
})
@Log4j2
public class IsisBeanFactoryPostProcessorForSpring
implements
    BeanFactoryPostProcessor,
    ApplicationContextAware {

    private IsisBeanTypeClassifier isisBeanTypeClassifier;
    private IsisComponentScanInterceptor isisComponentScanInterceptor;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        isisBeanTypeClassifier = IsisBeanTypeClassifier.createInstance(applicationContext);
        isisComponentScanInterceptor = IsisComponentScanInterceptor.createInstance(isisBeanTypeClassifier);
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {

        // make sure we have an applicationContext before calling post processing
        Objects.requireNonNull(isisBeanTypeClassifier,
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

            isisComponentScanInterceptor.intercept(typeMetaData);

            if(typeMetaData.isInjectable()) {

                val beanNameOverride = typeMetaData.getBeanNameOverride();
                if(_Strings.isNotEmpty(beanNameOverride)) {
                    registry.removeBeanDefinition(beanDefinitionName);
                    registry.registerBeanDefinition(beanNameOverride, beanDefinition);
                    log.debug("renaming bean {} -> {}", beanDefinitionName, beanNameOverride);
                }


            } else {
                registry.removeBeanDefinition(beanDefinitionName);
                log.debug("vetoing bean {}", beanDefinitionName);
            }

        }

    }

    @Bean
    public IsisBeanTypeClassifier getIsisBeanTypeClassifier() {
        return isisBeanTypeClassifier!=null
                ? isisBeanTypeClassifier
                : (isisBeanTypeClassifier = IsisBeanTypeClassifier.createInstance()); // JUnit support
    }

    @Bean("isis.bean-meta-data")
    public Can<IsisBeanMetaData> getIsisComponentScanInterceptor() {
        return isisComponentScanInterceptor.getAndDrainIntrospectableTypes();
    }


}
