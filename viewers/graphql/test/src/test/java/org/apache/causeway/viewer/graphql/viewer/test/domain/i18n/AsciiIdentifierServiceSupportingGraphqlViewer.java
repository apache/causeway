package org.apache.causeway.viewer.graphql.viewer.test.domain.i18n;

import org.apache.causeway.applib.services.ascii.AsciiIdentifierService;

import org.springframework.stereotype.Service;

@Service
public class AsciiIdentifierServiceSupportingGraphqlViewer implements AsciiIdentifierService {
    @Override
    public String asciiIdFor(String featureId) {
        return featureId.replace("ä", "a");
    }
}
