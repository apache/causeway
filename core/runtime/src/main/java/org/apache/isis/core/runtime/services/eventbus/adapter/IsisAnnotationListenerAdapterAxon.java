package org.apache.isis.core.runtime.services.eventbus.adapter;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.axonframework.common.Subscribable;
import org.axonframework.common.annotation.ClasspathParameterResolverFactory;
import org.axonframework.common.annotation.HandlerDefinition;
import org.axonframework.common.annotation.MessageHandlerInvoker;
import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventListenerProxy;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventhandling.replay.ReplayAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Prepare for Isis v2
 * Source is copied from Axon to also support spring's EventListener together with the axon EventHandler.
 *
 * Adapter that turns any bean with {@link EventHandler} annotated methods into an {@link
 * org.axonframework.eventhandling.EventListener}.
 *
 * @author Allard Buijze
 * @see org.axonframework.eventhandling.EventListener
 * @since 0.1
 */
public class IsisAnnotationListenerAdapterAxon implements Subscribable, EventListenerProxy, ReplayAware {

    private final MessageHandlerInvoker invoker;
    private final EventBus eventBus;
    private final ReplayAware replayAware;
    private final Class<?> listenerType;

    /**
     * Subscribe the given <code>annotatedEventListener</code> to the given <code>eventBus</code>.
     *
     * @param annotatedEventListener The annotated event listener
     * @param eventBus               The event bus to subscribe to
     * @return an AnnotationEventListenerAdapter that wraps the listener. Can be used to unsubscribe.
     */
    public static org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter subscribe(Object annotatedEventListener, EventBus eventBus) {
        org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter adapter = new org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter(annotatedEventListener);
        eventBus.subscribe(adapter);
        return adapter;
    }

    /**
     * Wraps the given <code>annotatedEventListener</code>, allowing it to be subscribed to an Event Bus.
     *
     * @param annotatedEventListener the annotated event listener
     */
    public IsisAnnotationListenerAdapterAxon(Object annotatedEventListener) {
        // Support Spring EventListener
        HandlerDefinition<? super Method> handlerDefinition = AxonAnnotatedEventHandlerDefinition.INSTANCE;
        if(Arrays.stream(annotatedEventListener.getClass().getMethods())
                .anyMatch(method -> Arrays.stream(method.getAnnotations()).anyMatch(annotation ->
                        "org.springframework.context.event.EventListener".equals(annotation.annotationType().getName())))){
            handlerDefinition = SpringAnnotatedEventHandlerDefinition.INSTANCE;
        }

        this.invoker = new MessageHandlerInvoker(annotatedEventListener,
                ClasspathParameterResolverFactory.forClass(annotatedEventListener.getClass()), false,
                handlerDefinition);
        this.listenerType = annotatedEventListener.getClass();
        if (annotatedEventListener instanceof ReplayAware) {
            this.replayAware = (ReplayAware) annotatedEventListener;
        } else {
            // as soon as annotations are supported, their handlers should come here...
            this.replayAware = new NoOpReplayAware();
        }
        this.eventBus = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(EventMessage event) {
        invoker.invokeHandlerMethod(event);
    }

    /**
     * Unsubscribe the EventListener with the configured EventBus.
     *
     * @deprecated Use {@link EventBus#unsubscribe(org.axonframework.eventhandling.EventListener)} and
     * pass this adapter instance to unsubscribe it.
     */
    @Override
    @PreDestroy
    @Deprecated
    public void unsubscribe() {
        if (eventBus != null) {
            eventBus.unsubscribe(this);
        }
    }

    /**
     * Subscribe the EventListener with the configured EventBus.
     * <p/>
     *
     * @deprecated Use {@link EventBus#subscribe(org.axonframework.eventhandling.EventListener)} and
     * pass this adapter instance to subscribe it.
     */
    @Override
    @PostConstruct
    @Deprecated
    public void subscribe() {
        if (eventBus != null) {
            eventBus.subscribe(this);
        }
    }


    @Override
    public Class<?> getTargetType() {
        return listenerType;
    }

    @Override
    public void beforeReplay() {
        replayAware.beforeReplay();
    }

    @Override
    public void afterReplay() {
        replayAware.afterReplay();
    }

    @Override
    public void onReplayFailed(Throwable cause) {
        replayAware.onReplayFailed(cause);
    }

    private static final class NoOpReplayAware implements ReplayAware {

        @Override
        public void beforeReplay() {
        }

        @Override
        public void afterReplay() {
        }

        @Override
        public void onReplayFailed(Throwable cause) {
        }
    }

}
