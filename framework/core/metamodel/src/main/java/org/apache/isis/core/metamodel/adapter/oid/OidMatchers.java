package org.apache.isis.core.metamodel.adapter.oid;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class OidMatchers {
    
    private OidMatchers(){}

    public static Matcher<Oid> matching(final String objectType, final String identifier) {
        return new TypeSafeMatcher<Oid>() {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("matching [" + objectType + ", " + identifier +"]");
            }

            @Override
            public boolean matchesSafely(Oid oid) {
                if(oid instanceof RootOid) {
                    RootOid rootOid = (RootOid) oid;
                    return rootOid.getObjectSpecId().equals(objectType) && rootOid.getIdentifier().equals(identifier);
                }
                return false;
            }
        };
    }

}
