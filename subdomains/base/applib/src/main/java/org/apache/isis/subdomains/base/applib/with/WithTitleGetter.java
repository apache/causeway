package org.apache.isis.subdomains.base.applib.with;

import org.apache.isis.applib.util.ObjectContracts.ToStringEvaluator;

/**
 * Indicates that the implementing class has a {@link #getTitle() title}.
 */
public interface WithTitleGetter {

    public String getTitle();
    
    /**
     * Utility class for obtaining the string value of an object that implements {@link WithTitleGetter}. 
     */
    public final static class ToString {
        private ToString() {}
        public static ToStringEvaluator evaluator() {
            return new ToStringEvaluator() {
                @Override
                public boolean canEvaluate(final Object o) {
                    return o instanceof WithTitleGetter;
                }
                
                @Override
                public String evaluate(final Object o) {
                    return ((WithTitleGetter)o).getTitle();
                }
            };
        }
    }

}
