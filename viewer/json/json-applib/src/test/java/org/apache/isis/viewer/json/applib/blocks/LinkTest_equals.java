package org.apache.isis.viewer.json.applib.blocks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class LinkTest_equals {

    @Test
    public void equalDependsOnMethodAndHref() throws UnsupportedEncodingException {
        Link link = new Link().withHref("http://localhost:8080/objects/ABC:123").withMethod(Method.GET);
        Link link2 = new Link().withHref("http://localhost:8080/objects/ABC:123").withMethod(Method.GET);
        Link link3 = new Link().withHref("http://localhost:8080/objects/ABC:123").withMethod(Method.PUT);
        Link link4 = new Link().withHref("http://localhost:8080/objects/ABC:456").withMethod(Method.GET);
        
        assertThat(link, is(equalTo(link2)));
        assertThat(link, is(not(equalTo(link3))));
        assertThat(link, is(not(equalTo(link4))));
    }

    @Test
    public void equalDoesNotDependsOnMethodAndHref() throws UnsupportedEncodingException {
        Link link = new Link().withHref("http://localhost:8080/objects/ABC:123").withMethod(Method.GET).withRel("something");
        Link link2 = new Link().withHref("http://localhost:8080/objects/ABC:123").withMethod(Method.GET).withRel("else");
        
        assertThat(link, is(equalTo(link2)));
    }

}
