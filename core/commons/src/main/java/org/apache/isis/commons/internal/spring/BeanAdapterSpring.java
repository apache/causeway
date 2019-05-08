package org.apache.isis.commons.internal.spring;

import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;

import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.commons.ioc.LifecycleContext;
import org.apache.isis.core.commons.collections.Bin;

import lombok.Value;
import lombok.val;

@Value(staticConstructor="of")
final class BeanAdapterSpring implements BeanAdapter {

    private final String id;
    private final LifecycleContext lifecycleContext;
    private final Class<?> beanClass;
    private final ObjectProvider<?> beanProvider;
    private final boolean domainService;
    
    @Override
    public Bin<?> getInstance() {
        val allMatchingBeans = beanProvider
                .orderedStream()
                .collect(Collectors.toList());
        return Bin.ofCollection(allMatchingBeans);
    }
    
    @Override
    public boolean isCandidateFor(Class<?> requiredType) {
        return beanProvider.stream()
        .map(Object::getClass)
        .anyMatch(type->requiredType.isAssignableFrom(type));
    }
    
}
