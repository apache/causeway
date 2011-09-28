package org.apache.isis.viewer.json.applib;

import static org.junit.Assert.assertSame;

import org.apache.isis.viewer.json.applib.RestfulRequest.DomainModel;
import org.junit.Test;

public class RestfulRequestDomainModelTest_parser {

    @Test
    public void parser_roundtrips() {
        final Parser<DomainModel> parser = RestfulRequest.DomainModel.parser();
        for (DomainModel domainModel : DomainModel.values()) {
            final String asString = parser.asString(domainModel);
            final DomainModel roundtripped = parser.valueOf(asString);
            assertSame(roundtripped, domainModel);
        }
    }

}
