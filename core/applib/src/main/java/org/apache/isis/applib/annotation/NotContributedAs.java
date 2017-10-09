package org.apache.isis.applib.annotation;

/**
 * @deprecated
 */
@Deprecated
public enum NotContributedAs {
    ACTION,
    ASSOCIATION,
    EITHER,
    NEITHER; /* ie contributed as both ! */

    /**
     * @deprecated
     */
    @Deprecated
    public static NotContributedAs notFrom(final Contributed contributed) {
        if(contributed == null) { return null; }
        switch (contributed) {
            case AS_ACTION: return NotContributedAs.ASSOCIATION;
            case AS_ASSOCIATION: return NotContributedAs.ACTION;
            case AS_NEITHER: return NotContributedAs.EITHER;
            case AS_BOTH: return null;
        }
        return null;
    }

}
