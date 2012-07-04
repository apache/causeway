package org.apache.isis.runtimes.dflt.objectstores.datanucleus;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntityRepository;

public class DataNucleusPersistenceMechanismInstallerTest_services {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .withServices(repo)
        .build();

    @Test
    public void servicesBootstrapped() {
        final List<Object> services = IsisContext.getServices();
        assertThat(services.size(), is(1));
        assertThat(services.get(0), is((Object)repo));
        
        final ObjectAdapter serviceAdapter = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(repo);
        assertThat(serviceAdapter, is(not(nullValue())));
        
        assertThat(serviceAdapter.getOid(), is(equalTo((Oid)RootOidDefault.create(ObjectSpecId.of("PrimitiveValuedEntities"), "1"))));
    }
    

}
