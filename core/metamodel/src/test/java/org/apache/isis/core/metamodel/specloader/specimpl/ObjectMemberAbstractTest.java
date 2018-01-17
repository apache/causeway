package org.apache.isis.core.metamodel.specloader.specimpl;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

public class ObjectMemberAbstractTest {

    @Test
    public void deriveMemberNameFrom_with_underscore() {
        final String suffix = ObjectMemberAbstract.deriveMemberNameFrom("Customer_placeOrder");
        Assert.assertThat(suffix, CoreMatchers.is(equalTo("placeOrder")));
    }

    @Test
    public void deriveMemberNameFrom_with_dollar() {
        final String suffix = ObjectMemberAbstract.deriveMemberNameFrom("Customer$placeOrder");
        Assert.assertThat(suffix, CoreMatchers.is(equalTo("placeOrder")));
    }



}