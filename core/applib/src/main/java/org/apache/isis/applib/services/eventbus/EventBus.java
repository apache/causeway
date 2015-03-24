package org.apache.isis.applib.services.eventbus;


/**
 * Common interface for all Event Bus implementations.
 * <p>
 * Currently, there are implementations based on Guava and Axon frameworks.
 * 
 */
public interface EventBus {

    /**
     * Both singleton and request-scoped domain services can register on the event bus; this should be done in their
     * <code>@PostConstruct</code> callback method.
     *
     * <p>
     *     <b>Important:</b> Request-scoped services should register their proxy, not themselves.  This is because it is
     *     the responsibility of the proxy to ensure that the correct underlying (thread-local) instance of the service
     *     is delegated to.  If the actual instance were to be registered, this would cause a memory leak and all sorts
     *     of other unexpected issues.
     * </p>
     *
     * <p>
     *     Also, request-scoped services should <i>NOT</i> unregister themselves.  This is because the
     *     <code>@PreDestroy</code> lifecycle method is called at the end of each transaction.  The proxy needs to
     *     remain registered on behalf for any subsequent transactions.
     * </p>
     *
     * <p>For example:</p>
     * <pre>
     *     @RequestScoped @DomainService
     *     public class SomeSubscribingService {
     *
     *         @Inject private EventBusService ebs;
     *         @Inject private SomeSubscribingService proxy;
     *
     *         @PostConstruct
     *         public void startRequest() {
     *              // register with bus
     *              ebs.register(proxy);
     *         }
     *         @PreDestroy
     *         public void endRequest() {
     *              //no-op
     *         }
     *     }
     * </pre>
     *
     * <p>
     *     The <code>@PostConstruct</code> callback is the correct place to register for both singleton and
     *     request-scoped services.  For singleton domain services, this is done during the initial bootstrapping of
     *     the system.  For request-scoped services, this is done for the first transaction.  In fact, because
     *     singleton domain services are initialized <i>within a current transaction</i>, the request-scoped services
     *     will actually be registered <i>before</i> the singleton services.  Each subsequent transaction will have the
     *     request-scoped service re-register with the event bus, however the event bus stores its subscribers in a
     *     set and so these re-registrations are basically a no-op.
     * </p>
     *
     * @param domainService
     */
    void register(Object domainService);

    /**
     * Notionally allows subscribers to unregister from the event bus; however this is a no-op.
     *
     * <p>
     *     It is safe for singleton services to unregister from the bus, however this is only ever called when the
     *     app is being shutdown so there is no real effect.  For request-scoped services meanwhile that (as
     *     explained in {@link #register(Object)}'s documentation) actually register their proxy, it would be an error
     *     to unregister the proxy; subsequent transactions (for this thread or others) must be routed through that
     *     proxy.
     * </p>
     */
    void unregister(Object domainService);

    /**
     * Post an event.
     */
    void post(Object event);

}