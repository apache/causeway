package org.apache.isis.extensions.spring.service;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

@Getter
@ToString
public final class BeanDescriptor {

    private final String beanName;
    private final List<String> aliases;
    private final Object bean;
    private final String scope;
    private final Class<?> type;
    private final String resource;
    private final Map<String, Object> dependenciesByName;

    BeanDescriptor(
            final String beanName,
            final ConfigurableApplicationContext context) {
        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        this.beanName = beanName;

        final BeanDefinition definition = beanFactory.getBeanDefinition(beanName);

        this.aliases = Collections.unmodifiableList(Arrays.asList(beanFactory.getAliases(beanName)));
        this.bean = beanFactory.getBean(beanName);

        final String scope = definition.getScope();
        this.scope = StringUtils.hasText(scope) ? scope : "singleton";

        this.type = beanFactory.getType(beanName);
        this.resource = definition.getResourceDescription();

        this.dependenciesByName = Arrays.stream(beanFactory.getDependenciesForBean(beanName))
                                .filter(name -> {
                                    try {
                                        return beanFactory.getBean(name) != null;
                                    } catch(NoSuchBeanDefinitionException ex) {
                                        return false;
                                    }
                                })
                                .collect(Collectors.toMap(Function.identity(), beanFactory::getBean));
    }

    <T> List<T> dependencies(Class<T> cls) {
        return dependenciesByName.values().stream()
                .filter(cls::isInstance)
                .map(cls::cast)
                .collect(Collectors.toList());
    }

}
