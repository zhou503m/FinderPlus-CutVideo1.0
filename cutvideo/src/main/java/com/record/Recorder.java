package com.record;

import com.VideoMain;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Grab and record the streams
 * Created by Wangke on 2017/9/14.
 */
public class Recorder implements Runnable {

    private List<Long> fileName;
    private File directory;

    private String cameraId;
    private String address;

    public Recorder (String cameraId, String address) {
        this.cameraId = cameraId;
        this.address = address;

        fileName = VideoMain.fileMap.get(cameraId);

        directory = new File( VideoMain.ClipRoot + cameraId + File.separator);
        if (directory.exists()) {
            return;
        }
        if (!directory.mkdirs()) {
            System.out.println("创建目录失败！");
        }
    }

    public void run() {

            try {
                long currentTimeMillis = System.currentTimeMillis();
                String outFile = directory.getAbsolutePath() + File.separator + currentTimeMillis + ".flv";
                fileName.add(currentTimeMillis);

                FFmpegFrameGrabber grabber = FFmpegFrameGrabber.createDefault(address);
                grabber.setOption("rtsp_transport", "tcp");
                grabber.setImageWidth(1280);
                grabber.setImageHeight(720);
                grabber.start();

                OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
                opencv_core.IplImage grabbedImage = converter.convert(grabber.grab());
                int width = grabbedImage.width();
                int height = grabbedImage.height();

                FrameRecorder recorder = FrameRecorder.createDefault(outFile, width, height);
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("flv");
                recorder.setFrameRate(grabber.getFrameRate());

                recorder.start();
                long startTime = 0;
                long videoTS = 0;

                int i = 0;
                while (VideoMain.recording && i++ < 15000) {
                    Frame frame = grabber.grab();
                    if (startTime == 0) {
                        startTime = System.currentTimeMillis();
                    }
                    videoTS = 1000 * (System.currentTimeMillis() - startTime);
                    recorder.setTimestamp(videoTS);
                    recorder.record(frame);
                }

                recorder.stop();
                recorder.release();
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static void main(String[] args){

        Recorder rec = new Recorder("36", "rtsp://admin:123456@192.168.1.98:554/unicast/c1/s0/live");
        Thread thread = new Thread(rec);
        thread.start();
    }
}


