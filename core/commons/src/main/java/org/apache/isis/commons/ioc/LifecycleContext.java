package org.apache.isis.commons.ioc;

public enum LifecycleContext {
    ApplicationScoped,
    Singleton,
    SessionScoped,
    RequestScoped,
    ConversationScoped,
    Dependent,
    ;

    public boolean isApplicationScoped() {
        return this == ApplicationScoped;
    }
    
    public boolean isSingleton() {
        return this == Singleton;
    }
    
    public boolean isRequestScoped() {
        return this == RequestScoped;
    }
}
