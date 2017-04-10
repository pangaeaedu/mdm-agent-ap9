/**
 * Copyright 2015-2016 ND Inc. All rights reserved.
 * com.nd.sdp.im-access
 */
package com.nd.adhoc.push.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class RenameThreadFac implements ThreadFactory {

    private final String name;
    private AtomicInteger inc = new AtomicInteger(1);

    public RenameThreadFac(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(name + "_" + inc.getAndAdd(1));
        return t;
    }
}
