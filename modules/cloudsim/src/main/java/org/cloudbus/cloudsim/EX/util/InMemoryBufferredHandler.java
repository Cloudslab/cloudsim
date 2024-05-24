package org.cloudbus.cloudsim.EX.util;

import java.io.UnsupportedEncodingException;
import java.util.logging.*;

/**
 * Keeps in memory a buffer of log records with a predefined size. When the
 * buffer is full - pushes the records to an aggregated handler.
 * 
 * <br>
 * <br>
 * <strong>NOTE! </strong> - this handler does not perform any formatting.
 * filtering etc. All of these operations are delegated to the
 * wrapped/aggregated handler. Hence the set(get)Format/Filter/Level methods
 * only delegate to the wrapped/aggregated handler
 * 
 * @author nikolay.grozev
 * 
 */
public class InMemoryBufferredHandler extends Handler {

    private final Handler handler;

    private final LogRecord[] records;
    private int idx = 0;

    /**
     * Constr.
     * 
     * @param handler
     *            - the embedded/aggregated handler. Must not be null.
     * @param bufferSize
     *            - the size of the buffer. Must be positive.
     */
    public InMemoryBufferredHandler(final Handler handler, final int bufferSize) {
        super();
        if (bufferSize <= 0) {
            throw new IllegalAccessError("The size of the buffer must be positive");
        }
        this.handler = handler;

        records = new LogRecord[bufferSize];
    }

    @Override
    public void publish(final LogRecord record) {
        if (!isLoggable(record) && record.getLevel().intValue() >= handler.getLevel().intValue()) {
            return;
        }

        records[idx] = record;
        idx++;

        if (idx == records.length) {
            emptyBuffer();
        }
    }

    @Override
    public void flush() {
        emptyBuffer();
        handler.flush();
    }

    public void emptyBuffer() {
        // handler.publish(new LogRecord(Level.SEVERE, "Pushing " + idx +
        // " messages together"));
        for (int i = 0; i < idx; i++) {
            handler.publish(records[i]);
            records[i] = null;
        }
        idx = 0;
    }

    @Override
    public void close() throws SecurityException {
        try {
            flush();
            handler.flush();
        } finally {
            handler.close();
        }
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return record != null && handler.isLoggable(record);
    }

    @Override
    public void setFilter(Filter newFilter) throws SecurityException {
        handler.setFilter(newFilter);
    }

    @Override
    public Filter getFilter() {
        return handler.getFilter();
    }

    @Override
    public void setFormatter(Formatter newFormatter) throws SecurityException {
        handler.setFormatter(newFormatter);
    }

    @Override
    public Formatter getFormatter() {
        return handler.getFormatter();
    }

    @Override
    public synchronized void setLevel(Level newLevel) throws SecurityException {
        handler.setLevel(newLevel);
    }

    @Override
    public synchronized Level getLevel() {
        return handler.getLevel();
    }

    @Override
    public void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
        handler.setEncoding(encoding);
    }

    @Override
    public String getEncoding() {
        return handler.getEncoding();
    }

    @Override
    public void setErrorManager(ErrorManager em) {
        handler.setErrorManager(em);
    }

    public ErrorManager getErrorManager() {
        return handler.getErrorManager();
    }
}
