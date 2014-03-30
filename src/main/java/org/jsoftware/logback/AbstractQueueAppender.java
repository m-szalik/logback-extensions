package org.jsoftware.logback;

import ch.qos.logback.core.AppenderBase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class for appenders that keep events in Queue.
 * @author szalik
 */
abstract class AbstractQueueAppender<E> extends AppenderBase<E> {
    private static final int MAX_QUEUE_SIZE = 127;
    private final BlockingQueue<E> queue = new SynchronousQueue<E>();

    @Override
    protected void append(E evt) {
        if (evt != null) {
            if (queue.size() > MAX_QUEUE_SIZE) {
                addWarn(formatLogMessage("Queue is over " + MAX_QUEUE_SIZE + " elements - discarding event (" + evt + ")"));
            } else {
                queue.add(evt);
            }
        }
    }


    @Override
    public void start() {
        try {
            init();
            context.getExecutorService().execute(new ProcessEventRunnable());
            super.start();
            addError(formatLogMessage("Queue thread started."));
        } catch (Exception e) {
            started = false;
            addError(formatLogMessage("Unable to initialize appender!"), e);
            throw new RuntimeException("Unable to initialize appender!", e);
        }
    }


    protected String formatLogMessage(String logMessage) {
        return "Appender " + getName() + ": " + logMessage;
    }


    protected void init() throws Exception {
    }

    protected abstract void processEvent(E event);


    private class ProcessEventRunnable implements Runnable {
        @Override
        public void run() {
            while (isStarted()) {
                E event;
                try {
                    event = queue.poll(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e1) {
                    addInfo(formatLogMessage("Exiting because of interruption signal."), e1);
                    return;
                }
                processEvent(event);
            } // while
        }
    } // Runnable
}
