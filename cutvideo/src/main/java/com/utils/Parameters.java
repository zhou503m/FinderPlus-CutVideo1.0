package com.utils;

import java.io.Serializable;

/**
 * As a struct
 * Created by Wangke on 2017/9/14.
 */
public class Parameters implements Serializable {
    public String cameraId;
    public long startTime;
    public long endTime;

    public Parameters(String camId, long startTime, long endTime) {
        this.cameraId = camId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
