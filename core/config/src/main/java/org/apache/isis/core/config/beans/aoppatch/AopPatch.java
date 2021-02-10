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
package org.apache.isis.core.config.beans.aoppatch;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * This component listens for Spring's {@link ApplicationContext} to become available,
 * then allows for replacement of the built-in {@link TransactionInterceptor} via the 
 * {@link TransactionInterceptorFactory}. If no such factory is registered with the 
 * context, the default behavior is maintained.
 * 
 * @since 2.0
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Named("isis.config.AopPatch")
@Log4j2
public class AopPatch implements ApplicationContextAware {
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        
        if(!applicationContext.containsBean(TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)) {
            return;
        }
        
        val advisor = (BeanFactoryTransactionAttributeSourceAdvisor) 
                applicationContext.getBean(TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME);
        
        val attrSource = applicationContext
                .getBeanProvider(TransactionAttributeSource.class, false)
                .getIfAvailable();
        
        val transactionInterceptorFactory = applicationContext
                .getBeanProvider(TransactionInterceptorFactory.class, false)
                .getIfAvailable();
        
        val transactionManager = applicationContext
                .getBeanProvider(TransactionManager.class, false)
                .getIfAvailable();
        
        log.info("installing patched tx interceptor");
        
        advisor.setAdvice(patchedTransactionInterceptor(
                attrSource, 
                transactionInterceptorFactory,
                transactionManager));
    }
    
    // -- HELPER

    private TransactionInterceptor patchedTransactionInterceptor(
            final @NonNull  TransactionAttributeSource transactionAttributeSource,
            final @Nullable TransactionInterceptorFactory transactionInterceptorFactory,
            final @NonNull  TransactionManager txManager) {
        
        final TransactionInterceptor interceptor = transactionInterceptorFactory==null
                ? new TransactionInterceptor()
                : transactionInterceptorFactory.createTransactionInterceptor(); 
        
        interceptor.setTransactionAttributeSource(transactionAttributeSource);
        if (txManager != null) {
            interceptor.setTransactionManager(txManager);
        }
        return interceptor;
    }

    
}