package com.nd.adhoc.push.aws;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.nd.adhoc.push.aws.config.IIotInfoProvider;
import com.nd.adhoc.push.core.BasePushChannel;
import com.nd.adhoc.push.core.IPushChannel;
import com.nd.adhoc.push.core.IPushChannelConnectListener;
import com.nd.adhoc.push.core.IPushChannelDataListener;
import com.nd.adhoc.push.core.IPushSendData;
import com.nd.adhoc.push.core.IPushSendResult;
import com.nd.adhoc.push.core.PushRecvDataImpl;
import com.nd.adhoc.push.core.enumConst.PushChannelType;
import com.nd.adhoc.push.core.enumConst.PushConnectStatus;
import com.nd.android.adhoc.basic.log.Logger;
import com.nd.sdp.android.serviceloader.AnnotationServiceLoader;
import com.nd.sdp.android.serviceloader.annotation.Service;

import java.security.KeyStore;
import java.util.Iterator;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

@Service(IPushChannel.class)
public class AwsPushChannel extends BasePushChannel implements IPushChannel {
    private static final String TAG = "AwsPushChannel";
    private AWSIotClient mIotClient;
    private AWSIotMqttManager mMqttManager;
    private KeyStore mKeyStore = null;
    private String mClientID = "";

    private PushConnectStatus mConnectStatus = PushConnectStatus.Disconnected;

    private BehaviorSubject<Boolean> mCognitoInitSubject = BehaviorSubject.create();

    private IIotInfoProvider mIotInfoProvider = null;

    private AwsPushInitStatus mInitStatus = AwsPushInitStatus.Init;
    private AwsPushStartStatus mStartStatus = AwsPushStartStatus.Init;

    private AWSIotMqttClientStatusCallback mConnStatusCallback = new AWSIotMqttClientStatusCallback() {
        @Override
        public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
            Log.e(TAG, "Status = " + String.valueOf(status));
            if (status == AWSIotMqttClientStatus.Connected) {
                mConnectStatus = PushConnectStatus.Connected;

                String subTopic = mIotInfoProvider.getTransInfoProvider().getSubscribeTopic();
                AWSIotMqttQos qos = mIotInfoProvider.getTransInfoProvider().getSubscribeQos();

                Log.e(TAG, "subscribeToTopic:"+subTopic);
                mMqttManager.subscribeToTopic(subTopic, qos, mRecvDataCallback);

            } else {
                mConnectStatus = PushConnectStatus.Disconnected;
            }

            for (IPushChannelConnectListener listener : mConnectListeners) {
                listener.onConnectStatusChanged(AwsPushChannel.this,
                        PushConnectStatus.Connected);
            }
        }
    };

    private AWSIotMqttNewMessageCallback mRecvDataCallback = new AWSIotMqttNewMessageCallback() {
        @Override
        public void onMessageArrived(final String topic, final byte[] data) {
            Log.e(TAG, new String(data));
            try {
                for (IPushChannelDataListener listener : mDataListeners) {
                    PushRecvDataImpl recvData = new PushRecvDataImpl(data);
                    listener.onPushDataArrived(AwsPushChannel.this, recvData);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(TAG, "get error:" + e.toString() + "\n with messege:" + new String(data));
            }
        }
    };

    protected IIotInfoProvider retrieveIotInfoProvider() {
        IIotInfoProvider provider = null;
        Iterator<IIotInfoProvider> iterator = AnnotationServiceLoader
                .load(IIotInfoProvider.class).iterator();
        if (iterator.hasNext()) {
            provider = iterator.next();
        }

        return provider;
    }

    @Override
    public int getChannelType() {
        return PushChannelType.Aws.getValue();
    }

    @Override
    public Observable<Boolean> init(@NonNull Context pContext) {
        return super.init(pContext)
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean pBoolean) {
                        try {
                            doAwsIotInit();
                            return mCognitoInitSubject.asObservable().first();
                        }catch (Exception e){
                            return Observable.error(e);
                        }
                    }
                });
    }

    private void doAwsIotInit() {
        mIotInfoProvider = retrieveIotInfoProvider();
        if (mIotInfoProvider == null) {
            throw new RuntimeException("iot info provider not found");
        }

        mClientID = mIotInfoProvider.createClientID();
        if (TextUtils.isEmpty(mClientID)) {
            throw new RuntimeException("client id can not be empty, check you iot info provider");
        }

        // Initialize the AWS Cognito credentials provider
        AWSMobileClient.getInstance()
                .initialize(mContext, new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        try {
                            initAwsIotEnv();
                            mCognitoInitSubject.onNext(true);
//                            mCognitoInitSubject.onCompleted();
                        } catch (Exception e) {
                            mCognitoInitSubject.onError(e);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        mCognitoInitSubject.onError(e);
                    }
                });
    }

    private void initMqttManager() {
        // MQTT Client
        String endPoint = mIotInfoProvider.getEnvInfoProvider().getEndPoint();
        int aliveSec = mIotInfoProvider.getMqttInfoProvider().getMqttAliveSec();
        mMqttManager = new AWSIotMqttManager(mClientID, endPoint);
        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mMqttManager.setKeepAlive(aliveSec);

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        String lastWillTopic = mIotInfoProvider.getMqttInfoProvider().getLastWillTopic();
        String lastWillMessage = mIotInfoProvider.getMqttInfoProvider().getLastWillMessage();
        AWSIotMqttQos qos = mIotInfoProvider.getMqttInfoProvider().getLastWillQos();

        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament(lastWillTopic,
                lastWillMessage, qos);
        mMqttManager.setMqttLastWillAndTestament(lwt);
    }

    private void initIotClient() {
        // IoT Client (for creation of certificate if needed)
        mIotClient = new AWSIotClient(AWSMobileClient.getInstance());
        Region region = mIotInfoProvider.getEnvInfoProvider().getRegion();
        mIotClient.setRegion(region);
    }

    private KeyStore loadKeystoreFromFileSystem() throws Exception {
        if (!AWSIotKeystoreHelper.isKeystorePresent(mIotInfoProvider.getEnvInfoProvider().getKeystoreFolder(),
                mIotInfoProvider.getEnvInfoProvider().getKeystoreName())) {
            Logger.d(TAG, "Keystore " + mIotInfoProvider.getEnvInfoProvider().getKeystoreFolder()
                    + "/" + mIotInfoProvider.getEnvInfoProvider().getKeystoreName() + " not found.");
            return null;
        }

        if (!AWSIotKeystoreHelper.keystoreContainsAlias(mIotInfoProvider.getEnvInfoProvider().getCertificateId(),
                mIotInfoProvider.getEnvInfoProvider().getKeystoreFolder(),
                mIotInfoProvider.getEnvInfoProvider().getKeystoreName(),
                mIotInfoProvider.getEnvInfoProvider().getKeystorePassword())) {
            Logger.d(TAG, "Key/cert " + mIotInfoProvider.getEnvInfoProvider().getCertificateId() + " not found in " +
                    "keystore.");
            return null;
        }

        Logger.d(TAG, "Certificate " + mIotInfoProvider.getEnvInfoProvider().getCertificateId()
                + " found in keystore - using for MQTT.");
        // load keystore from file into memory to pass on connection
        return AWSIotKeystoreHelper.getIotKeystore(mIotInfoProvider.getEnvInfoProvider().getCertificateId(),
                mIotInfoProvider.getEnvInfoProvider().getKeystoreFolder(),
                mIotInfoProvider.getEnvInfoProvider().getKeystoreName(),
                mIotInfoProvider.getEnvInfoProvider().getKeystorePassword());
    }

    private void loadKeystoreFromServerThenAttachPolicy() throws Exception {
        // Create a new private key and certificate. This call
        // creates both on the server and returns them to the
        // device.
        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                new CreateKeysAndCertificateRequest();
        createKeysAndCertificateRequest.setSetAsActive(true);
        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
        createKeysAndCertificateResult =
                mIotClient.createKeysAndCertificate(createKeysAndCertificateRequest);
        Log.i(TAG,
                "Cert ID: " + createKeysAndCertificateResult.getCertificateId() +
                        " created.");

        // store in keystore for use in MQTT client
        // saved as alias "default" so a new certificate isn't
        // generated each run of this application
        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(mIotInfoProvider.getEnvInfoProvider().getCertificateId(),
                createKeysAndCertificateResult.getCertificatePem(),
                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                mIotInfoProvider.getEnvInfoProvider().getKeystoreFolder(),
                mIotInfoProvider.getEnvInfoProvider().getKeystoreName(),
                mIotInfoProvider.getEnvInfoProvider().getKeystorePassword());

        // load keystore from file into memory to pass on
        // connection
        mKeyStore = AWSIotKeystoreHelper.getIotKeystore(mIotInfoProvider.getEnvInfoProvider().getCertificateId(),
                mIotInfoProvider.getEnvInfoProvider().getKeystoreFolder(),
                mIotInfoProvider.getEnvInfoProvider().getKeystoreName(),
                mIotInfoProvider.getEnvInfoProvider().getKeystorePassword());

        // Attach a policy to the newly created certificate.
        // This flow assumes the policy was already created in
        // AWS IoT and we are now just attaching it to the
        // certificate.
        AttachPrincipalPolicyRequest policyAttachRequest = new AttachPrincipalPolicyRequest();
        policyAttachRequest.setPolicyName(mIotInfoProvider.getEnvInfoProvider().getPolicyName());
        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                .getCertificateArn());
        mIotClient.attachPrincipalPolicy(policyAttachRequest);
    }

    private void initAwsIotEnv() throws Exception {
        initMqttManager();
        initIotClient();
        initKeystore();
    }

    private void initKeystore() throws Exception {
        mKeyStore = loadKeystoreFromFileSystem();
        if (mKeyStore == null) {
            loadKeystoreFromServerThenAttachPolicy();
        }
    }

    @Override
    public void uninit() {
        mClientID = "";
        mMqttManager.disconnect();
        mMqttManager = null;
        mIotClient = null;

        mInitStatus = AwsPushInitStatus.Init;
        mStartStatus = AwsPushStartStatus.Init;
    }

    @Override
    public Observable<Boolean> start() {
        if(getCurrentStatus() == PushConnectStatus.Connected){
            return Observable.just(true);
        }

        return mCognitoInitSubject.asObservable()
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean pInitSuccess) {
                        if (!pInitSuccess) {
                            return Observable.error(new Exception("init failed"));
                        }

                        return Observable
                                .create(new Observable.OnSubscribe<Boolean>() {
                                    @Override
                                    public void call(Subscriber<? super Boolean> pSubscriber) {
                                        try {
                                            if(getCurrentStatus() == PushConnectStatus.Connected){
                                                pSubscriber.onNext(true);
                                                pSubscriber.onCompleted();
                                                return;
                                            }

                                            mMqttManager.connect(mKeyStore, mConnStatusCallback);
//                                            String subTopic = mIotInfoProvider.getTransInfoProvider().getSubscribeTopic();
//                                            AWSIotMqttQos qos = mIotInfoProvider.getTransInfoProvider().getSubscribeQos();
//
//                                            if (TextUtils.isEmpty(subTopic)) {
//                                                pSubscriber.onError(new Exception("sub topic not found"));
//                                                return;
//                                            }
//
//                                            Log.e(TAG, "subscribeToTopic:"+subTopic);
//                                            mMqttManager.subscribeToTopic(subTopic, qos, mRecvDataCallback);
                                            pSubscriber.onNext(true);
                                            pSubscriber.onCompleted();
                                        } catch (Exception e) {
                                            pSubscriber.onError(e);
                                        }
                                    }
                                });
                    }
                });
    }


    @Override
    public Observable<Boolean> stop() {
        return Observable.just(true);
    }

    @Override
    public String getPushID() {
        return mClientID;
    }

    @Override
    public PushConnectStatus getCurrentStatus() {
        return mConnectStatus;
    }

    @Override
    public IPushSendResult send(IPushSendData pData) {
        Log.e(TAG, "for now, we do not support send data in aws channel");
        return null;
    }
}
