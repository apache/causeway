package org.apache.isis.extensions.base.dom.with;

import org.apache.isis.applib.util.ObjectContracts.ToStringEvaluator;

/**
 * Indicates that the implementing class has a {@link #getCode() code}.
 */
public interface WithCodeGetter {

    public String getCode();
    
    /**
     * Utility class for obtaining the string value of an object that implements {@link WithCodeGetter}. 
     */
    public final static class ToString {
        private ToString() {}
        public static ToStringEvaluator evaluator() {
            return new ToStringEvaluator() {
                @Override
                public boolean canEvaluate(final Object o) {
                    return o instanceof WithCodeGetter;
                }
                
                @Override
                public String evaluate(final Object o) {
                    return ((WithCodeGetter)o).getCode();
                }
            };
        }
    }
}
