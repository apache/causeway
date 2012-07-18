package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.bytecode;

import org.apache.isis.core.metamodel.specloader.classsubstitutor.CglibEnhanced;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutorAbstract;

public class DataNucleusTypesClassSubstitutor extends ClassSubstitutorAbstract {

    /**
     * If {@link CglibEnhanced} then return superclass, else as per
     * {@link ClassSubstitutorAbstract#getClass(Class) superclass'}
     * implementation.
     */
    @Override
    public Class<?> getClass(final Class<?> cls) {
        if(cls.getName().startsWith("org.datanucleus")) {
            return getClass(cls.getSuperclass());
        }
        return super.getClass(cls);
    }

    
}
