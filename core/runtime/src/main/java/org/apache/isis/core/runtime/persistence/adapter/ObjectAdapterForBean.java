package org.apache.isis.core.runtime.persistence.adapter;

import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of") 
public class ObjectAdapterForBean implements ObjectAdapter {
    
    private final BeanAdapter bean;
    private final SpecificationLoader specificationLoader;
    
    private ObjectSpecification spec;

    @Override
    public ObjectSpecification getSpecification() {
        if(spec==null) {
            spec = specificationLoader.loadSpecification(bean.getBeanClass());
        }
        return spec;
    }

    @Override
    public Object getPojo() {
        return bean.getInstance().iterator().next();
    }

    @Override
    public void checkLock(Version version) {
    }

    @Override
    public Oid getOid() {
        val spec = getSpecification();
        return Oid.Factory.persistentOf(spec.getSpecId(), bean.getId());
    }

    @Override
    public ObjectAdapter getAggregateRoot() {
        return this;
    }

    @Override
    public Version getVersion() {
        return null;
    }

    @Override
    public void setVersion(Version version) {
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public boolean isRepresentingPersistent() {
        return false;
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("ObjectAdapterForBean[specId=%s, featureType=%s, moSort=%s]", 
                spec.getSpecId(), 
                spec.getFeatureType(),
                spec.getBeanSort().name());
    }
    
}