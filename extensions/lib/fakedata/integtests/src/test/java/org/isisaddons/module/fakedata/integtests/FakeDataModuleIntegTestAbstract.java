package org.isisaddons.module.fakedata.integtests;


import javax.inject.Inject;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.sessmgmt.SessionManagementService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.extensions.fixtures.IsisIntegrationTestAbstractWithFixtures;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        classes = FakeDataModuleAppManifestForTesting.class,
        properties = {
                "logging.config=log4j2-test.xml",
        })
@ContextConfiguration
@Transactional
public abstract class FakeDataModuleIntegTestAbstract extends IsisIntegrationTestAbstractWithFixtures {

}
