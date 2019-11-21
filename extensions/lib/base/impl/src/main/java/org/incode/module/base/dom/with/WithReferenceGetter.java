package org.incode.module.base.dom.with;

import org.apache.isis.applib.util.ObjectContracts.ToStringEvaluator;


/**
 * Indicates that the implementing class has a {@link #getReference() reference}.
 */
public interface WithReferenceGetter {

    public String getReference();
    
    /**
     * Utility class for obtaining the string value of an object that implements {@link WithReferenceGetter}. 
     */
    public final static class ToString {
        private ToString() {}
        public static ToStringEvaluator evaluator() {
            return new ToStringEvaluator() {
                @Override
                public boolean canEvaluate(final Object o) {
                    return o instanceof WithReferenceGetter;
                }
                
                @Override
                public String evaluate(final Object o) {
                    return ((WithReferenceGetter)o).getReference();
                }
            };
        }
    }

}
