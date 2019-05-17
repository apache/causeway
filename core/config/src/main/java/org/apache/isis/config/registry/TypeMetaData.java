package org.apache.isis.config.registry;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Value;
import lombok.val;

@Value(staticConstructor="of")
public class TypeMetaData {
    
    /**
     * Fully qualified name of the underlying class.
     */
    String className;
    
//    /**
//     * Fully qualified class names of all annotation types that are present on the underlying class.
//     */
//    Set<String> annotationTypes;
    
//    public boolean hasSingletonAnnotation() {
//        return annotationTypes.contains(singletonAnnotation);
//    }
//    
//    public boolean hasRequestScopedAnnotation() {
//        return annotationTypes.contains(requestScopedAnnotation);
//    }
//    
//    public boolean hasDomainServiceAnnotation() {
//        return annotationTypes.contains(domainServiceAnnotation);
//    }
//    
//    public boolean hasDomainObjectAnnotation() {
//        return annotationTypes.contains(domainObjectAnnotation);
//    }
//    
//    public boolean hasMixinAnnotation() {
//        return annotationTypes.contains(mixinAnnotation);
//    }
//    
//    public boolean hasViewModelAnnotation() {
//        return annotationTypes.contains(viewModelAnnotation);
//    }
    
    /**
     * @return the underlying class of this TypeMetaData
     */
    public Class<?> getUnderlyingClass() {
        try {
            return _Context.loadClass(className);
        } catch (ClassNotFoundException e) {
            val msg = String.format("Failed to load class for name '%s'", className);
            throw _Exceptions.unrecoverable(msg, e);
        }
    }
    
//    private final static String singletonAnnotation = 
//    		javax.inject.Singleton.class.getName();
//    private final static String requestScopedAnnotation = 
//    		javax.enterprise.context.RequestScoped.class.getName();
//    private final static String domainServiceAnnotation = 
//            org.apache.isis.applib.annotation.DomainService.class.getName();
//    private final static String domainObjectAnnotation = 
//            org.apache.isis.applib.annotation.DomainObject.class.getName();
//    private final static String mixinAnnotation = 
//            org.apache.isis.applib.annotation.Mixin.class.getName();
//    private final static String viewModelAnnotation = 
//            org.apache.isis.applib.annotation.ViewModel.class.getName();


    
    
}
