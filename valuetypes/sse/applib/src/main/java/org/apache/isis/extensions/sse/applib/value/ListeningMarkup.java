package org.apache.isis.extensions.sse.applib.value;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.extensions.sse.applib.annotations.ServerSentEvents;

/**
 * Immutable value type holding pre-rendered HTML. Supports server sent events.
 * <p>
 * Annotate with {@link ServerSentEvents} to bind this to a channel to listen on.
 *
 */
@Value(semanticsProviderName = 
        "MarkupValueSemanticsProvider")
public class ListeningMarkup extends Markup {

    private static final long serialVersionUID = 1L;

    public static ListeningMarkup valueOfHtml(String html) {
        return new ListeningMarkup(html);
    }

    private ListeningMarkup(String html) {
        super(html);
    }


}
