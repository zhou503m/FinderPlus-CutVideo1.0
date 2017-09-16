package com;

import com.rmi.IService;
import com.utils.Parameters;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RMI client for test
 * Created by Wangke on 2017/9/14.
 */
public class RMIClient {
    public static void main(String args[]) throws  InterruptedException{
        try {
            IService service =(IService) Naming.lookup("rmi://localhost:8889/service");
            service.stopRecord();


//            service.startRecord();
//            Thread.sleep(30000);
//            service.stopRecord();



         Parameters p = new Parameters("36", 1505480775078l, 1505480775078l);
         List<Parameters> list = new ArrayList<Parameters>();
         list.add(p);
         Map<String, String> l = service.cutVideoClip(list);
         l.put("test","test");
         for (Map.Entry<String, String> entry : l.entrySet()) {
             System.out.println("剪辑视频本地路径"+entry.getKey());
         }


//
//            Thread.sleep(10000);
//            service.startRecord();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch ( RemoteException /*| InterruptedException*/ e){
            e.printStackTrace();
        }
    }
}
