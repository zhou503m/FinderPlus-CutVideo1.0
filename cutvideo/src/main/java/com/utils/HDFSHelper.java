package com.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;

import java.io.*;
import java.net.URI;

/**
 * Created by taozhiheng on 16-7-17.
 * <p>
 * simple operations on hdfs
 */
public class HDFSHelper implements Serializable {

    private String ip;
    private URI uri;
    private FileSystem fs;

    public HDFSHelper(String ip) {
        this.ip = ip;
        //open("hdfs://hadoop01:9000");
    }

    /**
     * upload local file or directory to remote hdfs system
     *
     * @param local  the local file or directory path
     * @param remote the remote hdfs system absolute file or directory path
     */
    public boolean upload(String local, String remote) {
        return upload(new File(local), remote);
    }

    /**
     * upload local file or directory to remote hdfs system
     *
     * @param file   the local file or directory
     * @param remote the remote hdfs system absolute file or directory path
     */
    public boolean upload(File file, String remote) {
        if (!file.exists())
            return false;
        if (file.isFile()) {
            try {
                //just upload the file to hdfs (remote)
                return upload(new BufferedInputStream(new FileInputStream(file)), remote);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            File[] files = file.listFiles();

            if (files != null) {
                int size = files.length;
                //upload each file to hads (remote + "/" + f.getName())
                for (File f : files) {
                    if (upload(f, remote + /*File.separator*/ "/" + f.getName()))
                        size--;
                }
                return size == 0;
            }
            return false;
        }
    }

    //just for test
    private FileLogger mLogger;

    public void setLogger(FileLogger logger) {
        this.mLogger = logger;
    }

    /**
     * upload local file or directory to remote hdfs system
     *
     * @param is     the input stream of local file
     * @param remote the remote hdfs system absolute file path
     */
    public boolean upload(InputStream is, String remote) {

        String dst;
        if (ip != null)
            dst = ip + /*File.separator*/ "/" + remote;
        else
            dst = remote;
//        Configuration conf = new Configuration();
        try {
//            FileSystem fs = FileSystem.get(URI.create(dst), conf);
            open(dst);
            if (fs == null)
                return false;
            OutputStream out = fs.create(new Path(dst), new Progressable() {
                public void progress() {
//                    System.out.print(".");
                    if (mLogger != null) {
                        mLogger.getPrintWriter().print(".");
                    }
                }
            });
            IOUtils.copyBytes(is, out, 4096, true);
            if (mLogger != null) {
                mLogger.log(dst, "upload ok");
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            if (mLogger != null) {
                e.printStackTrace(mLogger.getPrintWriter());
                mLogger.getPrintWriter().flush();
            }
            return false;
        }
    }

    /**
     * download file from remote hdfs systemt to local file
     *
     * @param local  the local file path
     * @param remote the remote hdfs system absolute file path
     */
    public boolean download(String local, String remote) {
        return download(new File(local), remote);
    }

    /**
     * download file from remote hdfs systemt to local file
     *
     * @param file   the local file
     * @param remote the remote hdfs system absolute file path
     */
    public boolean download(File file, String remote) {
        if (!file.exists() || file.isFile()) {
            try {
                OutputStream os = new FileOutputStream(file);
                boolean res = download(os, remote);
                os.close();
                return res;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * download file from remote hdfs systemt to local file
     *
     * @param os     the output stream of the local file
     * @param remote the remote hdfs system absolute file path
     */
    public boolean download(OutputStream os, String remote) {
        String dst;
        if (ip != null)
            dst = ip + File.separator + remote;
        else
            dst = remote;
//        Configuration conf = new Configuration();
        try {
//            FileSystem fs = FileSystem.get(URI.create(dst), conf);
            open(dst);
            if (fs == null)
                return false;
            Path path = new Path(dst);
            if (!fs.exists(path)) {
                return false;
            }
            FSDataInputStream fsDataInputStream = fs.open(path);
            IOUtils.copyBytes(fsDataInputStream, os, 1024, false);
            fsDataInputStream.close();
            fsDataInputStream = null;
//            byte[] ioBuffer = new byte[1024];
//            int readLen = fsDataInputStream.read(ioBuffer);
//            while (-1 != readLen) {
//                os.write(ioBuffer, 0, readLen);
//                readLen = fsDataInputStream.read(ioBuffer);
//            }
//            fsDataInputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * delete file in remote hdfs system
     *
     * @param remote the remote hdfs system absolute file path
     */
    private boolean delete(String remote) {
        String dst;
        if (ip != null)
            dst = ip + File.separator + remote;
        else
            dst = remote;
//        Configuration conf = new Configuration();
        try {
//            fs = FileSystem.get(URI.create(dst), conf);
            open(dst);
            if (fs == null)
                return false;
            fs.deleteOnExit(new Path(dst));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void open(String url) {
        URI curUri = URI.create(url);
        if (fs != null && uri != null) {
            if (uri.getScheme() == null) {
                if (curUri.getScheme() == null)
                    return;
            } else if (uri.getScheme().equals(curUri.getScheme())) {
                return;
            }
            if (uri.getAuthority() == null) {
                if (curUri.getAuthority() == null)
                    return;
            } else if (uri.getAuthority().equals(curUri.getAuthority())) {
                return;
            }
        }
        uri = curUri;
        try {
            Configuration conf = new Configuration();
            conf.setBoolean("fs.hdfs.impl.disable.cache", true);
            fs = FileSystem.get(uri, conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (fs != null) {
            try {
                fs.close();
                fs = null;
                uri = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
