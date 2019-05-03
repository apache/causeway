package org.apache.isis.testdomain.jdo;

import org.apache.isis.core.integtestsupport.IntegrationTestJupiter;

public abstract class JdoTestDomainIntegTest extends IntegrationTestJupiter {

    protected JdoTestDomainIntegTest() {
        super(new JdoTestDomainModule());
    }

}
