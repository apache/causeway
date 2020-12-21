package org.apache.isis.persistence.jdo.datanucleus.entities;

import org.datanucleus.enhancement.Persistable;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.persistence.jdo.provider.entities.JdoEntityStateProvider;

import lombok.val;

@Component
public class DnEntityStateProvider implements JdoEntityStateProvider {

    @Override
    public EntityState getEntityState(Object pojo) {

        if(pojo==null) {
            return EntityState.NOT_PERSISTABLE;
        }
        
        if (pojo!=null && pojo instanceof Persistable) {
            val persistable = (Persistable) pojo;
            val isDeleted = persistable.dnIsDeleted();
            if(isDeleted) {
                return EntityState.PERSISTABLE_DESTROYED;
            }
            val isPersistent = persistable.dnIsPersistent();
            if(isPersistent) {
                return EntityState.PERSISTABLE_ATTACHED;
            }
            return EntityState.PERSISTABLE_DETACHED;
        }
        return EntityState.NOT_PERSISTABLE;
    }
    
}
