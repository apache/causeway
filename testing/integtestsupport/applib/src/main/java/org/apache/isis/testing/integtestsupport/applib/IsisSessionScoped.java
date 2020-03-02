package org.apache.isis.testing.integtestsupport.applib;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtime.session.init.InitialisationSession;

public class IsisSessionScoped implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        isisSessionFactory(extensionContext)
        .ifPresent(isisSessionFactory->isisSessionFactory.openSession(new InitialisationSession()));
    }
    
    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        isisSessionFactory(extensionContext)
        .ifPresent(IsisSessionFactory::closeSessionStack);
    }

    // -- HELPER
    
    private Optional<IsisSessionFactory> isisSessionFactory(ExtensionContext extensionContext) {
        return extensionContext.getTestInstance()
        .filter(IsisIntegrationTestAbstract.class::isInstance)
        .map(IsisIntegrationTestAbstract.class::cast)
        .map(IsisIntegrationTestAbstract::getServiceRegistry)
        .flatMap(serviceRegistry->serviceRegistry.lookupService(IsisSessionFactory.class));
    }
    
}
