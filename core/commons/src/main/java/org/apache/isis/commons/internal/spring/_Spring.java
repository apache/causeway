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
package org.apache.isis.commons.internal.spring;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.enterprise.event.Event;
import javax.inject.Qualifier;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.commons.ioc.LifecycleContext;
import org.apache.isis.commons.ioc.BeanSortClassifier;
import org.apache.isis.core.commons.collections.Bin;

import static org.apache.isis.commons.internal.base._NullSafe.stream;
import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Framework internal Spring support.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0.0
 */
@Slf4j
public class _Spring {

    public static boolean isContextAvailable() {
        return _Context.getIfAny(ApplicationContext.class)!=null;
    }
    
    public static void init(ApplicationContext context) {
        _Context.putSingleton(ApplicationContext.class, context);
    }
    
    public static ApplicationContext context() {
        return _Context.getElseFail(ApplicationContext.class);
    }
    
    public static <T> Bin<T> select(final Class<T> requiredType) {
        requires(requiredType, "requiredType");
        
        val allMatchingBeans = context().getBeanProvider(requiredType).orderedStream();
        return Bin.ofStream(allMatchingBeans);
    }
    
    public static <T> Bin<T> select(
            final Class<T> requiredType, 
            @Nullable Set<Annotation> qualifiersRequired) {
        
        requires(requiredType, "requiredType");
        
        val allMatchingBeans = context().getBeanProvider(requiredType)
                .orderedStream();
        
        if(_NullSafe.isEmpty(qualifiersRequired)) {
            return Bin.ofStream(allMatchingBeans);
        }
        
        final Predicate<T> hasAllQualifiers = t -> {
            val qualifiersPresent = _Sets.of(t.getClass().getAnnotations());
            return qualifiersPresent.containsAll(qualifiersRequired);
        };
        
        return Bin.ofStream(allMatchingBeans
                .filter(hasAllQualifiers));
    }
    
    /**
     * 
     * @param classifier
     * @return
     */
    public static Stream<BeanAdapter> streamAllBeans(BeanSortClassifier classifier) {
        
        val context = context();
        val beanFactory = ((ConfigurableApplicationContext)context).getBeanFactory();
        
        return Stream.of(context.getBeanDefinitionNames())
        .map(name->{
            
            val type = context.getType(name);
            val managedObjectSort = classifier.classify(type);
            val id = name; // just reuse the bean's name
            
            val scope = beanFactory.getBeanDefinition(name).getScope();
            val lifecycleContext = LifecycleContext.parse(scope);
            
            val resolvableType = ResolvableType.forClass(type);
            val bean = context.getBeanProvider(resolvableType);
            
            val beanAdapter = BeanAdapterSpring.of(id, lifecycleContext, type, bean, managedObjectSort);
            
            return beanAdapter;
        });

        
    }

    /**
     * @return Spring managed singleton wrapped in an Optional
     */
    public static <T> Optional<T> getSingleton(@Nullable Class<T> type) {
        if(type==null) {
            return Optional.empty();
        }
        return select(type).getSingleton();
    }
    
    /**
     * @return Spring managed singleton
     * @throws NoSuchElementException - if the singleton is not resolvable
     */
    public static <T> T getSingletonElseFail(@Nullable Class<T> type) {
        return getSingleton(type)
                .orElseThrow(()->_Exceptions.noSuchElement("Cannot resolve singleton '%s'", type));
                        
    }

    public static <T> Event<T> event(ApplicationEventPublisher publisher) {
        return new EventSpring<T>(publisher);
    }
    
    // -- QUALIFIER PROCESSING

    /**
     * Filters the input array into a collection, such that only annotations are retained, 
     * that are valid qualifiers for CDI.
     * @param annotations
     * @return non-null
     */
    public static Set<Annotation> filterQualifiers(@Nullable final Annotation[] annotations) {
        if(_NullSafe.isEmpty(annotations)) {
            return Collections.emptySet();
        }
        return stream(annotations)
        .filter(_CDI::isQualifier)
        .collect(Collectors.toSet());
    }
    
    /**
     * @param annotation
     * @return whether or not the annotation is a valid qualifier for CDI
     */
    public static boolean isQualifier(Annotation annotation) {
        if(annotation==null) {
            return false;
        }
        return annotation.annotationType().getAnnotationsByType(Qualifier.class).length>0;
    }

    /**
     * Returns a key/value pair copy of Spring's environment
     * @see {@linkplain <a href="https://jira.spring.io/browse/SPR-10241">https://jira.spring.io/browse/SPR-10241</a>}
     * @param configurableEnvironment
     */
    public static Map<String, String> copySpringEnvironmentToMap(
            ConfigurableEnvironment configurableEnvironment) {
        
        val map = _Maps.<String, String> newHashMap();
        
        for(Iterator<PropertySource<?>> it = configurableEnvironment.getPropertySources().iterator(); it.hasNext(); ) {
            val propertySource = it.next();
            if (propertySource instanceof MapPropertySource) {
                
                val mapPropertySource = (MapPropertySource) propertySource;
                
                mapPropertySource.getSource().forEach((key, value) ->
                    putIfNewValuePresent_warnIfKeyAlreadyExists(key, value, map));
                
            } else if(propertySource instanceof EnumerablePropertySource) {
                
                val enumPropertySource = (EnumerablePropertySource<?>) propertySource;
                
                for (String key : enumPropertySource.getPropertyNames()) {
                    val value = enumPropertySource.getProperty(key);
                    
                    putIfNewValuePresent_warnIfKeyAlreadyExists(key, value, map);
                }
                
            } else {
            
                log.warn("Ignoring PropertySource type '{}', "
                        + "because we don't know how to iterate over its key/value pairs.",
                        propertySource);
            }

        }
        
        return map;
    }
    
    // -- HELPER
    
    private static void putIfNewValuePresent_warnIfKeyAlreadyExists (
            String key,
            Object newValue,
            Map<String, String> dest) {
        
        if(newValue==null) {
            return;
        }
        val oldValue = dest.put(key, newValue.toString());
        if(oldValue!=null) {
            log.warn("overriding exising config key {} with value {} -> {}", key, oldValue, newValue);    
        }
        
    }
    
}
