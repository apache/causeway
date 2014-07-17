package org.apache.isis.security.shiro.authorization;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class IsisPermissionTest_equals {

    @Test
    public void differentByPermGroup() throws Exception {
        final IsisPermission perm1 = new IsisPermission("adm/*");
        final IsisPermission perm2 = new IsisPermission("aba/*");

        Assert.assertThat(perm1.equals(perm2), is(false));
    }


}