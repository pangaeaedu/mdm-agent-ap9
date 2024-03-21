package com.nd.mdm.permission;

import android.Manifest;
import androidx.annotation.NonNull;

import com.nd.android.adhoc.basic.common.AdhocBasicConfig;
import com.nd.android.adhoc.basic.frame.api.permission.AdhocPermissionRequestAbs;
import com.nd.android.adhoc.basic.util.permission.AdhocRxPermissions;

import rx.Subscriber;

/**
 * Created by HuangYK on 2019/2/13.
 */
//@Service(AdhocPermissionRequestAbs.class)
public class PermissionRequest_WriteExternalStorage extends AdhocPermissionRequestAbs {

    @NonNull
    @Override
    public String getPermission() {
        return Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }

    @NonNull
    @Override
    public String geManifestPermission() {
        return getPermission();
    }

    @Override
    public void doPermissionRequest(@NonNull final IAdhocPermissionRequestCallback pCallback) {
        AdhocRxPermissions.getInstance(AdhocBasicConfig.getInstance().getAppContext())
                .request(getPermission())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        pCallback.onResult(false);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        pCallback.onResult(aBoolean);
                    }
                });
    }
}
