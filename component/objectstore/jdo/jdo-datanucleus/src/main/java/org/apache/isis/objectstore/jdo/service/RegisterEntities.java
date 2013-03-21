package org.apache.isis.objectstore.jdo.service;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jdo.annotations.PersistenceCapable;

import com.google.common.base.Splitter;

import org.apache.log4j.Logger;
import org.reflections.Reflections;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.core.runtime.system.context.IsisContext;

@Hidden
public class RegisterEntities {

    private final static Logger LOG = Logger.getLogger(RegisterEntities.class);
    
    private final static String PACKAGE_PREFIX_KEY = "isis.persistor.datanucleus.RegisterEntities.packagePrefix";

    private String packagePrefixes;

    @PostConstruct
    public void init(Map<String,String> props) {
        packagePrefixes = props.get(PACKAGE_PREFIX_KEY);
    }

    @PreDestroy
    public void shutdown() {
    }

    
    @PostConstruct
    public void registerAllPersistenceCapables() {

        if(packagePrefixes == null) {
            LOG.warn("Did not find key '" + PACKAGE_PREFIX_KEY + "' and so entities will not be eagerly registered in the Isis metamodel");
            return;
        }
        
        for (String packagePrefix : Splitter.on(",").split(packagePrefixes)) {
            Reflections reflections = new Reflections(packagePrefix);
            
            Set<Class<?>> entityTypes = 
                    reflections.getTypesAnnotatedWith(PersistenceCapable.class);
            
            for (Class<?> entityType : entityTypes) {
                IsisContext.getSpecificationLoader().loadSpecification(entityType);
            }
        }
    }
    
    
}
