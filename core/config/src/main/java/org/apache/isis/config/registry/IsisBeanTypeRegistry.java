package org.apache.isis.config.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.debug._Probe;

import lombok.Getter;
import lombok.val;

/**
 * Holds the set of domain services, persistent entities and fixture scripts.services etc.
 * @since 2.0.0-M3
 */
public final class IsisBeanTypeRegistry implements AutoCloseable {

	private final static _Probe probe = _Probe.unlimited().label(IsisBeanTypeRegistry.class.getSimpleName());
	
    public static IsisBeanTypeRegistry current() {
        return _Context.computeIfAbsent(IsisBeanTypeRegistry.class, IsisBeanTypeRegistry::new);
    }
    
    private final Set<Class<?>> inbox = new HashSet<>();

    //TODO replace this getters: don't expose the sets for modification!?
    @Getter private final Set<Class<?>> iocManaged = new HashSet<>();
    @Getter private final Set<Class<?>> entityTypes = new HashSet<>();
    @Getter private final Set<Class<?>> mixinTypes = new HashSet<>();
    @Getter private final Set<Class<? extends FixtureScript>> fixtureScriptTypes = new HashSet<>();
    @Getter private final Set<Class<?>> domainServiceTypes = new HashSet<>();
    @Getter private final Set<Class<?>> domainObjectTypes = new HashSet<>();
    @Getter private final Set<Class<?>> viewModelTypes = new HashSet<>();
    @Getter private final Set<Class<?>> xmlElementTypes = new HashSet<>();

    private final List<Set<? extends Class<? extends Object>>> allTypeSets = _Lists.of(
            entityTypes,
            mixinTypes,
            fixtureScriptTypes,
            domainServiceTypes,
            domainObjectTypes,
            viewModelTypes,
            xmlElementTypes);
    
    
    @Override
    public void close() {
        inbox.clear();
        allTypeSets.forEach(Set::clear);
    }

	// -- STREAM ALL

	public Stream<Class<?>> streamAllTypes() {

		return _Lists.of(
				iocManaged,
				entityTypes,
				mixinTypes,
				fixtureScriptTypes,
				domainServiceTypes,
				domainObjectTypes,
				viewModelTypes,
				xmlElementTypes)
				.stream()
				.distinct()
				.flatMap(Collection::stream)
				;
	}

	// -- INBOX
	
	public void addToInbox(Class<?> type) {
	    synchronized (inbox) {
            inbox.add(type);
        }
	}
	
	/**
	 * Implemented as a one-shot, that clears the inbox afterwards.
	 * @return
	 */
    public Stream<Class<?>> streamAndClearInbox() {
        
        final List<Class<?>> defensiveCopy;
        
        synchronized (inbox) {
            defensiveCopy = new ArrayList<>(inbox);
            inbox.clear();
        }
        
        defensiveCopy.forEach(cls->{
        	probe.println("stream inbox: " + cls.getName());
        });
        
        return defensiveCopy.stream();
    }
    
    public Stream<Class<?>> streamCdiManaged() {
    	return iocManaged.stream();
    }
    
    // -- FILTER

    // don't categorize this early, instead push candidate classes onto a queue for 
    // later processing when the SpecLoader initializes.
    public boolean isIoCManagedType(TypeMetaData typeMetaData) {
        boolean toInbox = false;
        boolean toIoC = false;
        
        val what = new StringBuilder();
        
        if(typeMetaData.hasDomainServiceAnnotation()) {
            //domainServiceTypes.add(typeMetaData.getUnderlyingClass());
            toInbox = true;
            toIoC = true;
            what.append("+@DomainService");
        }
        
        if(typeMetaData.hasDomainObjectAnnotation()) {
            //domainObjectTypes.add(typeMetaData.getUnderlyingClass());
            toInbox = true;
            what.append("+@DomainObject");
        }
        
        if(typeMetaData.hasViewModelAnnotation()) {
            //domainObjectTypes.add(typeMetaData.getUnderlyingClass());
            toInbox = true;
            what.append("+@ViewModel");
        }
        
        if(typeMetaData.hasSingletonAnnotation()) {
        	toIoC = true;
        	what.append("+@Singleton");
        }
        
        if(typeMetaData.hasRequestScopedAnnotation()) {
            toIoC = true;
            what.append("+@RequestScoped");
        }
        
        if(toIoC) {
        	iocManaged.add(typeMetaData.getUnderlyingClass());
        }
        if(toInbox) {
            addToInbox(typeMetaData.getUnderlyingClass());
        }
        
        if(toIoC || toInbox) {
            val where = new StringBuilder();
            if(toIoC) where.append("+Spring");
            if(toIoC) where.append("+Model");
            probe.println("%s %s [%s]", 
                    where, 
                    _Probe.compact(typeMetaData.getUnderlyingClass()),
                    what
                    );
        }
        
        return toIoC;
    }
    
    // -- HELPER
    
    @Deprecated // not typesafe
    public final static List<String> FRAMEWORK_PROVIDED_SERVICE_PACKAGES = _Lists.of(
            "org.apache.isis.applib",
            "org.apache.isis.core.wrapper" ,
            "org.apache.isis.core.metamodel.services" ,
            "org.apache.isis.core.runtime.services" ,
            "org.apache.isis.schema.services" ,
            "org.apache.isis.objectstore.jdo.applib.service" ,
            "org.apache.isis.viewer.restfulobjects.rendering.service" ,
            "org.apache.isis.objectstore.jdo.datanucleus.service.support" ,
            "org.apache.isis.objectstore.jdo.datanucleus.service.eventbus" ,
            "org.apache.isis.viewer.wicket.viewer.services", 
            "org.apache.isis.core.integtestsupport.components");

    @Deprecated // not typesafe
    public final static List<String> FRAMEWORK_PROVIDED_TYPES_FOR_SCANNING = _Lists.of(
            "org.apache.isis.config.AppConfig",
            "org.apache.isis.applib.IsisApplibModule",
            "org.apache.isis.core.wrapper.WrapperFactoryDefault",
            "org.apache.isis.core.metamodel.MetamodelModule",
            "org.apache.isis.core.runtime.RuntimeModule",
            "org.apache.isis.core.runtime.services.RuntimeServicesModule",
//            "org.apache.isis.jdo.JdoModule",
            "org.apache.isis.viewer.restfulobjects.rendering.RendererContext"
            );

}