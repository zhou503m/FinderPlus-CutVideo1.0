package com.rmi;

import com.utils.Parameters;

import java.lang.reflect.Parameter;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Policy;
import java.util.List;
import java.util.Map;

/**
 * RMI Interface
 * Created by Wangke on 2017/9/14.
 */
public interface IService extends Remote{
    void startRecord() throws RemoteException;
    void stopRecord() throws RemoteException;
    Map<String, String> cutVideoClip(List<Parameters> parameters) throws RemoteException;
}
