package demoapp.dom.annotDomain.Property.mustSatisfy;

import org.apache.isis.applib.spec.AbstractSpecification;

//tag::class[]
public class OfWorkingAgeSpecification extends AbstractSpecification<Integer> {
    @Override
    public String satisfiesSafely(Integer candidate) {
        return candidate >= 18 && candidate <= 65
                ? null
                : "Not of working age [18-65]";
    }
}
//end::class[]
