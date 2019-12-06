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
package org.apache.isis.config.beans;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.ViewModel;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javax.inject.Named;

/**
 * The framework's stereotypes {@link DomainService}, {@link DomainObject}, {@link ViewModel}, etc. 
 * are meta annotated with eg. {@link Component}, which allows for the Spring framework to pick up the 
 * annotated type as candidate to become a managed bean. 
 * <p>
 * By plugging into Spring's bootstrapping via a {@link BeanFactoryPostProcessor}, intercepting those 
 * types is possible. Eg. {@link ViewModel} should not be managed by Spring, only discovered.
 * 
 * @since 2.0
 *
 */
@Component
@Named("isisConfig.isisBeanFactoryPostProcessorForSpring")
@Log4j2
public class IsisBeanFactoryPostProcessorForSpring
implements BeanFactoryPostProcessor, IsisBeanTypeRegistryHolder {

    @Getter(onMethod = @__(@Override))
    private final IsisBeanTypeRegistry isisBeanTypeRegistry = new IsisBeanTypeRegistry();
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        
        val registry = (BeanDefinitionRegistry) beanFactory;
        val interceptor = isisBeanTypeRegistry;
        
        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            
            log.debug("processing bean definition {}", beanDefinitionName);
            
            val beanDefinition = registry.containsBeanDefinition(beanDefinitionName) 
                    ? registry.getBeanDefinition(beanDefinitionName) 
                            : null;
                    
            if(beanDefinition==null || beanDefinition.getBeanClassName() == null) {
                continue; // check next beanDefinition
            }
            
            val typeMetaData = TypeMetaData.of(
                    beanDefinition.getBeanClassName(),
                    beanDefinitionName);
            
            interceptor.intercept(typeMetaData);
            
            if(typeMetaData.isInjectable()) {
                
                val beanNameOverride = typeMetaData.getBeanNameOverride(); 
                if(beanNameOverride!=null) {
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

}
