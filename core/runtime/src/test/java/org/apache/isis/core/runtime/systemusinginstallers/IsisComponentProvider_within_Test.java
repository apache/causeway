package org.apache.isis.core.runtime.systemusinginstallers;

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.Sets;

import org.junit.Assert;
import org.junit.Test;

import org.apache.isis.core.runtime.systemusinginstallers.fixture.budget.SomeServiceToInclude;
import org.apache.isis.core.runtime.systemusinginstallers.fixture.budgetassignment.SomeServiceNotToInclude;

import static org.hamcrest.CoreMatchers.*;

public class IsisComponentProvider_within_Test {

    @Test
    public void within() throws Exception {
        final String budgetPackageWithDot =
                SomeServiceToInclude.class.getPackage().getName() + ".";
        final String budgetAssignmentPackageWithDot =
                SomeServiceNotToInclude.class.getPackage().getName()  + ".";

        final Set<Class<?>> within = IsisComponentProvider.within(Arrays.asList(budgetPackageWithDot),
                Sets.newHashSet(SomeServiceToInclude.class, SomeServiceNotToInclude.class));

        Assert.assertThat(within.size(), is(equalTo(1)));
    }
}