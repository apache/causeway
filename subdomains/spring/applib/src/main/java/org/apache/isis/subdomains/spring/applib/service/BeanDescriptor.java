package org.apache.isis.subdomains.spring.applib.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import org.apache.isis.core.commons.internal.base._NullSafe;

import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Getter
@ToString
@Log4j2
public final class BeanDescriptor {

    private final String beanName;
    private final List<String> aliases;
    private final String scope;
    private final Class<?> type;
    private final String resource;
    private final Map<String, BeanNameAndDescriptor> dependenciesByName;
    
    @Value(staticConstructor = "of")
    private static class BeanNameAndDescriptor {
        final String beanName;
        final BeanDefinition beanDefinition;
        final BeanFactory beanFactory;
        Object getBean() {
            return beanFactory.getBean(beanName);
        }
    }
    

    BeanDescriptor(
            final String beanName,
            final ConfigurableApplicationContext context) {
        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        this.beanName = beanName;

        final BeanDefinition definition = beanFactory.getBeanDefinition(beanName);

        this.aliases = Collections.unmodifiableList(Arrays.asList(beanFactory.getAliases(beanName)));

        final String scope = definition.getScope();
        this.scope = StringUtils.hasText(scope) ? scope : "singleton";

        this.type = beanFactory.getType(beanName);
        this.resource = definition.getResourceDescription();

        val dependencies = beanFactory.getDependenciesForBean(beanName);
        
        this.dependenciesByName = _NullSafe.stream(dependencies)
            .map(name -> {
                try {
                    return BeanNameAndDescriptor.of(name, beanFactory.getBeanDefinition(name), beanFactory);
                } catch(NoSuchBeanDefinitionException ex) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(BeanNameAndDescriptor::getBeanName, Function.identity()));
        
    }
    
    <T> List<T> dependencies(Class<T> cls) {
        log.warn("non optimzed implementation: creates a bean for each dependency, even if not returned with the result");
        return dependenciesByName.values().stream()
                .map(BeanNameAndDescriptor::getBean)
                .filter(cls::isInstance)
                .map(cls::cast)
                .collect(Collectors.toList());
    }

}
