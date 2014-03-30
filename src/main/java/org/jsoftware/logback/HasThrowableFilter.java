package org.jsoftware.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Logback filter that accepts all events with Throwable
 * @author szalik
 */
public class HasThrowableFilter extends AbstractMatcherFilter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (! isStarted()) {
            return FilterReply.NEUTRAL;
        }

        if (event.getThrowableProxy() != null) {
            return onMatch;
        } else {
            return onMismatch;
        }
    }

}
