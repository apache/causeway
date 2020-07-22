package demoapp.dom.annotDomain.Property.mustSatisfy;

import org.apache.isis.applib.spec.AbstractSpecification;

//tag::class[]
public class OfWorkingAgeSpecification extends AbstractSpecification<Integer> {
    @Override
    public String satisfiesSafely(Integer candidate) {
        return candidate > 65
                ? null
                : "Not of retirement age (66 and older)";
    }
}
//end::class[]
