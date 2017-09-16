package com;

import com.record.Monitor;
import com.record.Recorder;
import com.rmi.IService;
import com.rmi.ServiceImpl;
import com.utils.Parameters;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Class
 * Created by Wangke on 2017/9/14.
 */
public class VideoMain {

   // public static final String ClipRoot = "/home/hadoop/";
    public static final String ClipRoot = "D:\\Code\\video\\";

//    public HDFSHelper hdfsHelper = new HDFSHelper("hdfs://localhost:9000");
    public static boolean recording = true;
    public static Map<Runnable, Thread> threadMap;
    public static Map<String, List<Long>> fileMap;

    public static void main(String[] args) {

        Map<String, String> map = new HashMap<String, String>();
        map.put("31", "rtsp://admin:123456@192.168.1.31:554/unicast/c1/s0/live");
        map.put("32", "rtsp://admin:123456@192.168.1.32:554/unicast/c1/s0/live");
        map.put("33", "rtsp://admin:123456@192.168.1.33:554/unicast/c1/s0/live");
        map.put("34", "rtsp://admin:123456@192.168.1.34:554/unicast/c1/s0/live");
        map.put("36", "rtsp://admin:123456@192.168.1.36:554/unicast/c1/s0/live");
        map.put("38", "rtsp://admin:123456@192.168.1.38:554/unicast/c1/s0/live");

        threadMap = new HashMap<Runnable, Thread>();
        fileMap = new HashMap<String, List<Long>>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String cameraId = entry.getKey();
            fileMap.put(cameraId, new ArrayList<Long>());

            Recorder recorder = new Recorder(cameraId, entry.getValue());
            Thread thread = new Thread(recorder);
            threadMap.put(recorder, thread);

            thread.start();
        }

        Monitor monitor = new Monitor(threadMap);
        monitor.start();

        try {
            IService service = new ServiceImpl();
            LocateRegistry.createRegistry(8889);
            Naming.bind("rmi://localhost:8889/service", service);
            System.out.println("RMI对象绑定成功！");
        } catch (RemoteException  e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
