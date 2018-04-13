package org.apache.isis.applib.services.urlencoding;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UrlEncodingServiceTest {

    UrlEncodingServiceWithCompression serviceWithCompression;
    UrlEncodingServiceUsingBaseEncodingAbstract serviceBaseEncoding;

    @Before
    public void setUp() throws Exception {
    	serviceWithCompression = new UrlEncodingServiceWithCompression();
    	serviceBaseEncoding = new UrlEncodingServiceUsingBaseEncodingAbstract(){};
    }

    @Test
    public void roundtrip() throws Exception {
    	roundtrip(serviceBaseEncoding, false);
    }
    
    @Test
    public void roundtrip_with_compression() throws Exception {
    	roundtrip(serviceWithCompression, true);
    }
    
    private void roundtrip(UrlEncodingService service, boolean testIsCompressing) throws Exception {

        final String original = "0-theme-entityPageContainer-entity-rows-2-rowContents-1-col-tabGroups-1-panel-tabPanel-rows-1-rowContents-1-col-fieldSets-1-memberGroup-properties-1-property-scalarTypeContainer-scalarIfRegular-associatedActionLinksBelow-additionalLinkList-additionalLinkItem-0-additionalLink";

        final String encoded = service.encodeString(original);
        final String decoded = service.decodeToString(encoded);

        Assert.assertThat(decoded, is(equalTo(original)));

        if(testIsCompressing) {
        	Assert.assertThat(original.length(), is(greaterThan(encoded.length())));	
        }
        
    }


}