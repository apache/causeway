package org.apache.isis.schema.utils;

import org.junit.Test;

import org.apache.isis.applib.services.bookmark.Bookmark;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InteractionDtoUtilsTest_deriveLogicalMemberId {

    @Test
    public void happy_case() throws Exception {
        String s = InteractionDtoUtils.deriveLogicalMemberId(new Bookmark("customer.Order", "1234"),
                "com.mycompany.customer.Order#placeOrder");
        assertThat(s, is(equalTo("customer.Order#placeOrder")));
    }
}