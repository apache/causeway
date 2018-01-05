package org.apache.isis.applib.services.urlencoding;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;

public class UrlEncodingServiceWithCompression_Abstract_Test {

    UrlEncodingServiceWithCompressionAbstract service;

    @Before
    public void setUp() throws Exception {
        service = new UrlEncodingServiceWithCompressionAbstract(){};
        service.base64Encoder = new UrlEncodingServiceUsingBaseEncoding();
    }

    @Test
    public void roundtrip() throws Exception {

        final String original = "0-theme-entityPageContainer-entity-rows-2-rowContents-1-col-tabGroups-1-panel-tabPanel-rows-1-rowContents-1-col-fieldSets-1-memberGroup-properties-1-property-scalarTypeContainer-scalarIfRegular-associatedActionLinksBelow-additionalLinkList-additionalLinkItem-0-additionalLink";

        final String encoded = service.encode(original);
        final String decoded = service.decode(encoded);

        Assert.assertThat(decoded, is(equalTo(original)));

        Assert.assertThat(original.length(), is(greaterThan(encoded.length())));
    }


}