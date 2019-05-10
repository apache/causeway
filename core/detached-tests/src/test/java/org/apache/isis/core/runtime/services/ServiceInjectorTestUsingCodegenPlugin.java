/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.context.RequestScoped;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

public class ServiceInjectorTestUsingCodegenPlugin {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    private ServiceInstantiator serviceInstantiator;
    private ServiceRegistry serviceRegistry;
    private ServiceInjector serviceInjector;
    

    @Before
    public void setUp() throws Exception {

        serviceInstantiator = new ServiceInstantiator();
        
        MetaModelContext.preset(MetaModelContext.builder()
                .configuration(_Config.getConfiguration())
                .singleton(serviceInstantiator.createInstance(SingletonCalculator.class))
                .singleton(serviceInstantiator.createInstance(AccumulatingCalculator.class))
                .build());
        
        serviceRegistry = IsisContext.getServiceRegistry();
        serviceInjector = IsisContext.getServiceInjector();
    }

    @Test
    public void singleton() {
        SingletonCalculator calculator = serviceRegistry.lookupService(SingletonCalculator.class).get();
        assertThat(calculator.add(3), is(3));
        calculator = serviceRegistry.lookupService(SingletonCalculator.class).get();
        assertThat(calculator.add(4), is(7));
    }

    @Test
    public void requestScoped_instantiate() {
        final AccumulatingCalculator calculator = serviceRegistry.lookupService(AccumulatingCalculator.class).get();
        assertThat(calculator instanceof RequestScopedService, is(true));
    }

    @Test
    public void requestScoped_justOneThread() {
        final AccumulatingCalculator calculator = serviceRegistry.lookupService(AccumulatingCalculator.class).get();
        
        try {
            ((RequestScopedService)calculator).__isis_startRequest(serviceInjector);
            assertThat(calculator.add(3), is(3));
            assertThat(calculator.add(4), is(7));
            assertThat(calculator.getTotal(), is(7));
        } finally {
            ((RequestScopedService)calculator).__isis_endRequest();
        }
    }

    @Test
    public void requestScoped_multipleThreads() throws InterruptedException, ExecutionException {
        
        final AccumulatingCalculator calculator = serviceRegistry.lookupService(AccumulatingCalculator.class).get();
        final ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // setup 32 tasks
        final List<Callable<Integer>> tasks = IntStream.range(0, 32)
        .<Callable<Integer>>mapToObj(index->()->{
            
            // within each task setup a new calculator instance that adds the numbers from 1 .. 100 = 5050
            ((RequestScopedService)calculator).__isis_startRequest(serviceInjector);
            for(int i=1; i<=100; i++) {
                calculator.add(i);    
            }
            try {
                return calculator.getTotal();
            } finally {
                ((RequestScopedService)calculator).__isis_endRequest();
            }
        })
        .collect(Collectors.toList());
        
        
        final List<Future<Integer>> results = executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // we expect that each of the 32 calculators have calculated the sum correctly
        for(Future<Integer> future: results) {
            assertThat(future.get(), is(5050));
        }
    }

    public static class SingletonCalculator {
        private int total;
        public int add(int x) {
            total += x;
            return getTotal();
        }
        public int getTotal() {
            return total;
        }
    }

    @RequestScoped
    public static class AccumulatingCalculator {
        private int total;
        public int add(int x) {
            total += x;
            return getTotal();
        }
        public int getTotal() {
            return total;
        }
    }

}
