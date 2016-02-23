package com.crumbsauce.acro.backend;


public interface ApiCallStatusReceiver<T> {
    void onSuccess(T result);
    void onError(String error);
}
