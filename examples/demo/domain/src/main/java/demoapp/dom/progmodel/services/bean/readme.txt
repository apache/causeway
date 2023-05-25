
TODO: create a new demo to show the BEAN nature.
TODO: this also demonstrates async call of WrapperFactory

This is the original version of EventSubscriberForDemo (since simplified):

@Service
@Named("demo.eventSubscriber")
@Qualifier("demo")
@Log4j2
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class EventSubscriberForDemo {

    final FactoryService factoryService;
    final WrapperFactory wrapper;

    @EventListener(UiButtonEvent.class) // <.>  <.> listen on the event, triggered by button in the UI
    public void on(final UiButtonEvent event) {

        log.info(emphasize("UiButtonEvent"));

        val eventLogWriter = factoryService.get(EventLogWriter.class); // <.> <.> get a new writer from Spring

        wrapper.asyncWrap(eventLogWriter, AsyncControl.returningVoid()).storeEvent(event);
    }

    @Named("demo.eventLogWriter")
    @DomainObject(nature = Nature.BEAN) // <.> <.> have this Object's lifecycle managed by Spring
    @Scope("prototype")
    public static class EventLogWriter {

        @Inject private EventLogEntryRepository<? extends EventLogEntry> eventLogEntryRepository;

        @Action // called asynchronously by above invocation
        public void storeEvent(final UiButtonEvent event) {
            eventLogEntryRepository.storeEvent(event);
        }
    }
}


