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
import java.util.stream.Stream;

import jakarta.inject.Named;

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
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;
import org.apache.causeway.core.config.beans.aoppatch.AopPatch;

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
    private Can<CausewayBeanMetaData> componentScanResult;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        causewayBeanTypeClassifier = CausewayBeanTypeClassifier.createInstance(applicationContext);
    }

    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
        var stopWatch = _Timing.now();

        // make sure we have an applicationContext before calling post processing
        Objects.requireNonNull(causewayBeanTypeClassifier,
                "postProcessBeanFactory() called before app-ctx was made available");

        var registry = (BeanDefinitionRegistry) beanFactory;
        var beanDefNames = registry.getBeanDefinitionNames();

        var componentCollector = new CausewayComponentCollector(registry, causewayBeanTypeClassifier);

        Stream.of(beanDefNames)
            //.parallel()
            .forEach(componentCollector::collect);

        beanFactory.registerScope("causeway-domain-object", new CausewayDomainObjectScope(beanFactory));

        this.componentScanResult = Can.ofCollection(componentCollector.introspectableTypes().values());

        log.info("post processing {} bean definitions took {}ms", _NullSafe.size(beanDefNames), stopWatch.getMillis());
    }

    @Bean
    public CausewayBeanTypeClassifier getCausewayBeanTypeClassifier() {
        return causewayBeanTypeClassifier!=null
                ? causewayBeanTypeClassifier
                : (causewayBeanTypeClassifier = CausewayBeanTypeClassifier.createInstance()); // JUnit support
    }

    @Bean
    public CausewayBeanTypeRegistry getComponentScanResult() {
        if(log.isDebugEnabled()) {
            componentScanResult.forEach(type->{
                log.debug("to be introspected: {}", type);
            });
        }
        return new CausewayBeanTypeRegistry(componentScanResult);
    }

}
