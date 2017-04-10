/**
 * Copyright 2015-2016 ND Inc. All rights reserved.
 * im-access 
 */
package com.nd.adhoc.push.rpcserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class TimeoutChecker {
    private static Logger log = LoggerFactory.getLogger("TimeoutChecker");
    private Marker marker;
    private AtomicLong size = new AtomicLong(0);
    public ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(1);

    public TimeoutChecker(String name) {
        marker = MarkerFactory.getMarker(name);
    }

    public static abstract class TimeOutItem implements Runnable {
        private TimeoutChecker checker;
        private long timeoutms;
        private AtomicBoolean bcancelled = new AtomicBoolean(false);

        public abstract boolean isExpire();

        public abstract void expired();

        @Override
        public void run() {
            if (bcancelled.get()) {
                return;
            }
            if (isExpire()) {
                expired();
            } else {
                checker.pushItem(this, timeoutms);
            }
        }
    }

    @Override
    public String toString() {
        return scheduExec.toString();
    }

    public boolean pushItem(final TimeOutItem item, long timeoutms) {
        item.checker = this;
        item.timeoutms = timeoutms;
        item.bcancelled.set(false);
        scheduExec.schedule(item, timeoutms, TimeUnit.MILLISECONDS);
        size.addAndGet(1);
        return true;
    }

    public boolean removeItem(TimeOutItem item) {
        item.bcancelled.set(true);
        return true;
    }

}
