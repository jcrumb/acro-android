package com.crumbsauce.acro.backend;


public interface ApiCallStatusReceiver {
    void receiveCallStatus(boolean ok, String method);
}
