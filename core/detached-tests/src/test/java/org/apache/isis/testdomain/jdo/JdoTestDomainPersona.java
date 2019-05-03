package org.apache.isis.testdomain.jdo;

import java.util.HashSet;
import java.util.Set;

import org.apache.isis.applib.fixturescripts.BuilderScriptAbstract;
import org.apache.isis.applib.fixturescripts.PersonaWithBuilderScript;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.Inventory;
import org.apache.isis.testdomain.jdo.Product;

import lombok.val;

public enum JdoTestDomainPersona 
implements PersonaWithBuilderScript<BuilderScriptAbstract<Inventory>>  {
    
    PurgeAll {
        @Override
        public BuilderScriptAbstract<Inventory> builder() {
            return new BuilderScriptAbstract<Inventory>() {

                @Override
                protected void execute(ExecutionContext ec) {
                    
                    val repository = IsisContext.getServicesInjector()
                            .lookupServiceElseFail(RepositoryService.class);
                    
                    repository.allInstances(Inventory.class)
                    .forEach(repository::remove);
                    
                    repository.allInstances(Book.class)
                    .forEach(repository::remove);
                    
                    repository.allInstances(Product.class)
                    .forEach(repository::remove);
                    
                }

                @Override
                public Inventory getObject() {
                    return null;
                }
                
            };
        }    
    },
    
    InventoryWith1Book {
        @Override
        public BuilderScriptAbstract<Inventory> builder() {
            return new BuilderScriptAbstract<Inventory>() {

                private Inventory inventory;
                
                @Override
                protected void execute(ExecutionContext ec) {
                    
//                    val factory = IsisContext.getServicesInjector()
//                            .lookupServiceElseFail(FactoryService.class);
                    
                    val repository = IsisContext.getServicesInjector()
                            .lookupServiceElseFail(RepositoryService.class);
                    
                    Set<Product> products = new HashSet<>();
                    
                    products.add(Book.of(
                            "Sample Book", "A sample book for testing.", 99.,
                            "Sample Author", "Sample ISBN", "Sample Publisher"));
                    
                    inventory = Inventory.of("Sample Inventory", products);
                    repository.persist(inventory);
                    
                }

                @Override
                public Inventory getObject() {
                    return inventory;
                }
                
            };
        }    
    }
    
    ;

    
    
}
