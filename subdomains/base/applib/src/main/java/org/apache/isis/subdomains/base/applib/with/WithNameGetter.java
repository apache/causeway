package org.apache.isis.subdomains.base.applib.with;

import org.apache.isis.applib.util.ObjectContracts;

/**
 * Indicates that the implementing class has a {@link #getName() name}.
 */
public interface WithNameGetter {

    public String getName();
    
    /**
     * Utility class for obtaining the string value of an object that implements {@link WithNameGetter}. 
     */
    public final static class ToString {
        private ToString() {}
        public static ObjectContracts.ToStringEvaluator evaluator() {
            return new ObjectContracts.ToStringEvaluator() {
                @Override
                public boolean canEvaluate(final Object o) {
                    return o instanceof WithNameGetter;
                }
                
                @Override
                public String evaluate(final Object o) {
                    return ((WithNameGetter)o).getName();
                }
            };
        }
    }

}
