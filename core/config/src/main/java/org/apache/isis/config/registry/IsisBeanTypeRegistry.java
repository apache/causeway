package org.apache.isis.config.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Vetoed;
import javax.inject.Singleton;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.ioc.BeanSort;
import org.apache.isis.commons.ioc.BeanSortClassifier;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.components.TransactionScopedComponent;

import static org.apache.isis.commons.internal.base._With.requires;
import static org.apache.isis.commons.internal.reflection._Reflect.getAnnotation;

import lombok.Getter;
import lombok.val;

/**
 * Holds the set of domain services, persistent entities and fixture scripts.services etc.
 * @since 2.0.0-M3
 */
public final class IsisBeanTypeRegistry implements BeanSortClassifier, AutoCloseable {

    private final static _Probe probe = _Probe.unlimited().label(IsisBeanTypeRegistry.class.getSimpleName());

    public static IsisBeanTypeRegistry current() {
        return _Context.computeIfAbsent(IsisBeanTypeRegistry.class, IsisBeanTypeRegistry::new);
    }

    private final Map<Class<?>, BeanSort> inbox = new HashMap<>();

    //TODO replace this getters: don't expose the sets for modification!?
    @Getter private final Set<Class<?>> beanTypes = new HashSet<>();
    @Getter private final Set<Class<?>> entityTypes = new HashSet<>();
    @Getter private final Set<Class<?>> mixinTypes = new HashSet<>();
//    @Getter private final Set<Class<? extends FixtureScript>> fixtureScriptTypes = new HashSet<>();
//    @Getter private final Set<Class<?>> viewModelTypes = new HashSet<>();
//    @Getter private final Set<Class<?>> xmlElementTypes = new HashSet<>();
    
    //@Getter private final Set<Class<?>> iocManaged = new HashSet<>();
    //@Getter private final Set<Class<?>> domainObjectTypes = new HashSet<>();
//
    private final List<Set<? extends Class<? extends Object>>> allTypeSets = _Lists.of(
            beanTypes,
            entityTypes,
            mixinTypes
            );

    @Override
    public void close() {
        inbox.clear();
        allTypeSets.forEach(Set::clear);
    }

    // -- STREAM ALL

    //	public Stream<Class<?>> streamAllTypes() {
    //
    //		return _Lists.of(
    //				iocManaged,
    //				entityTypes,
    //				mixinTypes,
    //				fixtureScriptTypes,
    //				domainServiceTypes,
    //				domainObjectTypes,
    //				viewModelTypes,
    //				xmlElementTypes)
    //				.stream()
    //				.distinct()
    //				.flatMap(Collection::stream)
    //				;
    //	}

    // -- INBOX

    public void addToInbox(BeanSort sort, Class<?> type) {
        synchronized (inbox) {
            inbox.put(type, sort);
        }
    }

    /**
     * Implemented as a one-shot, that clears the inbox afterwards.
     * @return
     */
    public Stream<Map.Entry<Class<?>, BeanSort>> streamAndClearInbox() {

        final Map<Class<?>, BeanSort> defensiveCopy;

        synchronized (inbox) {
            defensiveCopy = new HashMap<>(inbox);
            inbox.clear();
        }

        defensiveCopy.forEach((k, v)->{
            probe.println("stream inbox: %s [%s]", k.getName(), v.name());
        });

        return defensiveCopy.entrySet().stream();
    }

    // -- FILTER

    // don't categorize this early, instead push candidate classes onto a queue for 
    // later processing when the SpecLoader initializes.
    public boolean isIoCManagedType(TypeMetaData typeMetaData) {

        val type = typeMetaData.getUnderlyingClass();
        val beanSort = classify(type);

        val isToBeProvisioned = beanSort.isBean();
        val isToBeInspected = !beanSort.isUnknown();

        if(isToBeInspected) {
            addToInbox(beanSort, type);
        }

        if(isToBeInspected || isToBeProvisioned) {
            probe.println("%s %s [%s]",
                    isToBeProvisioned ? "provision" : "skip",
                    _Probe.compact(type),
                    beanSort.name());
        }

        return isToBeProvisioned;

    }

    @Override
    public BeanSort classify(Class<?> type) {

        requires(type, "type");
        
        if(getAnnotation(type, Vetoed.class)!=null) {
            return BeanSort.UNKNOWN; // exclude from provisioning
        }

        val aDomainService = getAnnotation(type, DomainService.class);
        if(aDomainService!=null) {
            return BeanSort.BEAN;
        }

        val aDomainObject = getAnnotation(type, DomainObject.class);
        if(aDomainObject!=null) {
            switch (aDomainObject.nature()) {
            case EXTERNAL_ENTITY:
            case INMEMORY_ENTITY:
            case JDO_ENTITY:
                return BeanSort.ENTITY;
            case MIXIN:
                return BeanSort.MIXIN;
            case VIEW_MODEL:
                return BeanSort.VIEW_MODEL;

            case NOT_SPECIFIED:
            default:
                // continue
                break; 
            } 
        }

        if(getAnnotation(type, ViewModel.class)!=null) {
            return BeanSort.VIEW_MODEL;
        }

        if(org.apache.isis.applib.ViewModel.class.isAssignableFrom(type)) {
            return BeanSort.VIEW_MODEL;
        }

        if(ApplicationScopedComponent.class.isAssignableFrom(type)) {
            return BeanSort.BEAN;
        }

        if(SessionScopedComponent.class.isAssignableFrom(type)) {
            return BeanSort.BEAN;
        }

        if(TransactionScopedComponent.class.isAssignableFrom(type)) {
            return BeanSort.BEAN;
        }

        if(getAnnotation(type, RequestScoped.class)!=null) {
            return BeanSort.BEAN;
        }

        if(getAnnotation(type, Singleton.class)!=null) {
            return BeanSort.BEAN;
        }

        return BeanSort.UNKNOWN;
    }


}