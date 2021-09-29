package com.nd.adhoc.push.core;

import java.util.Map;

public interface IPushRecvData {
    byte[] getContent();

    Map<String, String> getExtraInfos();
}
