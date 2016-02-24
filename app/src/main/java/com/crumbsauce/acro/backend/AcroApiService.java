package com.crumbsauce.acro.backend;


import com.orhanobut.wasp.Callback;
import com.orhanobut.wasp.http.Body;
import com.orhanobut.wasp.http.GET;
import com.orhanobut.wasp.http.POST;
import com.orhanobut.wasp.http.Path;

public interface AcroApiService {
    @POST("/tracking/begin")
    TrackingInfo beginTracking(@Body LocationString location);

    @GET("/tracking/end")
    TrackingStatusMessage endTracking();

    @POST("/tracking/heartbeat")
    LocationResponse sendHeartbeat(@Body LocationString location);

    @GET("/users/{email}/trackinginfo")
    void getTrackingInfo(@Path("email") String email, Callback<TrackingInfo> callback);

    @GET("/users/{email}/contacts")
    void getContacts(@Path("email") String email, Callback<Contact[]> callback);

    @POST("/users/{email}/contacts")
    void createContact(@Path("email") String email, @Body Contact newContact, Callback<Contact> callback);
}
