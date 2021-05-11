package com.nd.adhoc.push.core;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Observable;

public abstract class BasePushChannel implements IPushChannel {
    protected List<IPushChannelConnectListener> mConnectListeners = new CopyOnWriteArrayList<>();
    protected List<IPushChannelDataListener> mDataListeners = new CopyOnWriteArrayList<>();

    protected Context mContext = null;

    @Override
    public Observable<Boolean> init(@NonNull Context pContext) {
        mContext = pContext;
        return Observable.just(true);
    }

    @Override
    public void addConnectListener(IPushChannelConnectListener pListener) {
        if(mConnectListeners.contains(pListener)){
            return;
        }

        mConnectListeners.add(pListener);
    }

    @Override
    public void removeConnectListener(IPushChannelConnectListener pListener) {
        if(!mConnectListeners.contains(pListener)){
            return;
        }

        mConnectListeners.remove(pListener);
    }

    @Override
    public void addDataListener(IPushChannelDataListener pListener) {
        if(mDataListeners.contains(pListener)){
            return;
        }

        mDataListeners.add(pListener);
    }

    @Override
    public void removeDataListener(IPushChannelDataListener pListener) {
        if(!mDataListeners.contains(pListener)){
            return;
        }

        mDataListeners.remove(pListener);
    }
}
