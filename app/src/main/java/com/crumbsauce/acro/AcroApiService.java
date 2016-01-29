package com.crumbsauce.acro;


import com.orhanobut.wasp.http.Body;
import com.orhanobut.wasp.http.GET;
import com.orhanobut.wasp.http.POST;

public interface AcroApiService {
    @POST("/tracking/begin")
    TrackingInfo beginTracking(@Body LocationString location);

    @GET("/tracking/end")
    TrackingStatusMessage endTracking();

    @POST("/tracking/heartbeat")
    LocationResponse sendHeartbeat(@Body LocationString location);
}
