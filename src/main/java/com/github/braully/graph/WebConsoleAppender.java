package com.github.braully.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class WebConsoleAppender extends AppenderSkeleton {

    public static final String _revision = "$Id: WebConsoleAppender.java,v 1.3 2013-09-24 02:37:09 knoxg Exp $";

    public final static long DEFAULT_LOG_SIZE = 500;
    private static long maximumLogSize = DEFAULT_LOG_SIZE;
    private static final LinkedList<LoggingEvent> loggingEvents = new LinkedList<LoggingEvent>();

    public WebConsoleAppender() {

    }

    public WebConsoleAppender(int logSize) {
        maximumLogSize = logSize;
    }

    public void setMaximumLogSize(long logSize) {
        maximumLogSize = logSize;
    }

    public long getMaximumLogSize() {
        return maximumLogSize;
    }

    @Override
    public synchronized void append(LoggingEvent event) {
        synchronized (loggingEvents) {
            if (loggingEvents.size() >= maximumLogSize) {
                loggingEvents.removeLast();
            }

            loggingEvents.addFirst(event);
        }
    }

    protected boolean checkEntryConditions() {
        return true;
    }

    @Override
    public synchronized void close() {
        loggingEvents.clear();
    }

    public static synchronized void clear() {
        synchronized (loggingEvents) {
            loggingEvents.clear();
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public static List<LoggingEvent> getLoggingEvents() {
        return new ArrayList<LoggingEvent>(loggingEvents);
    }

    public static List<LoggingEvent> getLoggingEvents(long timestamp) {
        List<LoggingEvent> tmpLoggs = new ArrayList<LoggingEvent>();
        for (LoggingEvent e : loggingEvents) {
            if (timestamp < e.getTimeStamp()) {
                tmpLoggs.add(e);
            }
        }
        return tmpLoggs;
    }

    static List<String> getLines() {
        List<String> lines = new ArrayList<>();
        if (loggingEvents != null) {
            for (LoggingEvent e : loggingEvents) {
                Object message = e.getMessage();
                lines.add("" + message);
            }
        }

        return lines;
    }
}
