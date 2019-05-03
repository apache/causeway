<span class="version-reference">(since 2.0.0-M2)</span>

When running this demo using one of the JEE enabled images (eg. `payara`), this simple singleton bean is managed by the EJB container via `CDI`:  

```java
import javax.ejb.Singleton;

@Singleton
public class JeeDemoService {
    
    public String getMessage() {
        return "Hello World from JEE";
    }

}
```

Now we can verify that domain object service injection also works with CDI managed beans:

```java
@DomainObject
public class JeeDemo {
    
    @Inject private JeeDemoService jeeDemoService;

    @Action
    public String getJeeMessage(){
        return jeeDemoService.getMessage();
    }
    
    ...

}
```

See the sources for this demo here: [sources](${SOURCES_DEMO}/domainapp/dom/jee).

## Container Managed Connection Pool (advanced JEE usage):

You need to setup your JEE container's built-in connection pool inside the container first. This is vendor specific.
You than can use this connection pool via `JNDI` lookup.    

### Setup JDO

```
isis.persistor.datanucleus.impl.datanucleus.ConnectionFactoryName=jdbc/demo-domain
isis.persistor.datanucleus.impl.datanucleus.ConnectionFactory2Name=jdbc/demo-domain-nontx
isis.persistor.datanucleus.impl.javax.jdo.option.TransactionType=JTA
```

### Setup JNDI resource provider

Add a singleton bean to your application, that provides the lookup resources.

```java
import javax.ejb.*;

@Singleton
@Startup
public class ConnectionResources {
    
	@Resource(lookup="jdbc/demo-domain")
	private javax.sql.DataSource domainDS;

	@Resource(lookup="jdbc/demo-domain-nontx")
	private javax.sql.DataSource domainDS_nontx;

}
```

