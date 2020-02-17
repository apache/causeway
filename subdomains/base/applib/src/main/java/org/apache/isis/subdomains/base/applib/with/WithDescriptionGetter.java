package org.apache.isis.subdomains.base.applib.with;

import org.apache.isis.applib.util.ObjectContracts.ToStringEvaluator;

/**
 * Indicates that the implementing class has a {@link #getDescription() description}.
 */
public interface WithDescriptionGetter {

    public String getDescription();
    
    /**
     * Utility class for obtaining the string value of an object that implements {@link WithDescriptionGetter}. 
     */
    public static final class ToString {
        private ToString() {}
        public static ToStringEvaluator evaluator() {
            return new ToStringEvaluator() {
                @Override
                public boolean canEvaluate(final Object o) {
                    return o instanceof WithDescriptionGetter;
                }
                
                @Override
                public String evaluate(final Object o) {
                    return ((WithDescriptionGetter)o).getDescription();
                }
            };
        }
    }

}
