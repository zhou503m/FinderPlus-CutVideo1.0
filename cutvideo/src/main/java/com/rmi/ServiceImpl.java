package com.rmi;

import com.VideoMain;
import com.utils.FileLogger;
import com.utils.HDFSHelper;
import com.utils.Parameters;
import com.utils.VideoTime;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * RMI Service Implementation
 * Created by Wangke on 2017/9/14.
 */
public class ServiceImpl extends UnicastRemoteObject implements IService {

    private HDFSHelper hdfsHelper;

    private FileLogger fileLogger;

    public ServiceImpl() throws RemoteException {
        hdfsHelper = new HDFSHelper(null);
        fileLogger = new FileLogger("D:\\Code\\log.txt");
    }

    public void startRecord() throws RemoteException {
        VideoMain.recording = true;
    }

    public void stopRecord() throws RemoteException {
        VideoMain.recording = false;
    }

    public Map<String, String> cutVideoClip(List<Parameters> parameters) throws RemoteException {

        Map<String, String> result = new HashMap<String, String>();


        for (Parameters p : parameters) {

            File file = new File(VideoMain.ClipRoot + p.cameraId + File.separator);
            File[] files;
            if (file.exists()) {
                files = file.listFiles();

            } else {
                continue;
            }
            if (files.length == 0) {
                continue;
            }

            Long[] fileNames = new Long[files.length];
            for (int i = 0; i < files.length; i++) {
                String strFile = files[i].getName();
                if (!strFile.endsWith(".flv")) {
                    fileNames[i] = 0l;
                    continue;
                }

                try {
                    String s = strFile.substring(0, strFile.length() - 4);
                    fileNames[i] = Long.parseLong(s);
                } catch (Exception e) {
                    fileNames[i] = 0l;
                    continue;
                }
            }

            Arrays.sort(fileNames/*, (Long o1, Long o2) -> {
                if (o1 < o2) {
                    return 1;
                } else if (o1 > o2) {
                    return -1;
                } else {
                    return 0;
                }
            }*/);


            for (int i = 0; i < fileNames.length; i++) {

                String str = VideoMain.ClipRoot + p.cameraId + File.separator + fileNames[i] +".flv";
                int videoTime = VideoTime.getVideoTime(str,"E:\\Software\\ffmpeg\\bin\\ffmpeg.exe");
                boolean timeJudge = videoTime < 8;

                if (timeJudge)
                    continue;

                if (fileNames[i] <= p.startTime && (i == fileNames.length - 1 || fileNames[i + 1] > p.startTime)) {

                    String inputFile = VideoMain.ClipRoot + p.cameraId + File.separator + fileNames[i] + ".flv";
                    String outputFile = VideoMain.ClipRoot + p.cameraId + File.separator + fileNames[i] + "_clipped.flv";

                    String hdfsPath = "hdfs://192.168.1.6:8020/user/hadoop/zm/" + p.cameraId + "_" + fileNames[i] + "_clipped.flv";

                    long ref1 = (p.startTime - fileNames[i]) / 1000;
//                    long ref2 = (p.endTime - fileNames[i]) / 1000;
                    long hour1 = ref1 / 3600;
//                    long hour2 = ref2 / 3600;
                    long minute1 = ref1 % 3600 / 60;
//                    long minute2 = ref2 % 3600 / 60;
                    long second1 = ref1 % 3600 % 60;
//                    long second2 = ref2 % 3600 % 60;
                    String s = (hour1 < 10 ? "0" + hour1 : "" + hour1) + ":" +
                            (minute1 < 10 ? "0" + minute1 : "" + minute1) + ":" +
                            (second1 < 10 ? "0" + second1 : "" + second1);
//                    String e = (hour2 < 10 ? "0" + hour2 : "" + hour2) + ":" +
//                            (minute2 < 10 ? "0" + minute2 : "" + minute2) + ":" +
//                            (second2 < 10 ? "0" + second2 : "" + second2);

                    long reft = ref1 + 8;
                    long hourt = reft / 3600;
                    long minutet = reft % 3600 / 60;
                    long secondt = reft % 3600 % 60;
                    String et = (hourt < 10 ? "0" + hourt : "" + hourt) + ":" +
                            (minutet < 10 ? "0" + minutet : "" + minutet) + ":" +
                            (secondt < 10 ? "0" + secondt : "" + secondt);

                    fileLogger.log("video time is ", videoTime+"");
                    fileLogger.log("cutPath is ", inputFile);
                    fileLogger.log("start time is ", s);
                    fileLogger.log("end time is ", et);


                    try {
                        Runtime.getRuntime().exec("ffmpeg -i " + inputFile + " -vcodec copy -ss " + s + " -to " + et + " " + outputFile + " -y");
                       // hdfsHelper.upload(outputFile, hdfsPath);
                        result.put(outputFile, hdfsPath);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
            }

        }



        return result;
    }
}
