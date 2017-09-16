import com.utils.HDFSHelper;

import java.io.File;

public class HDFSTest {
    public static void main(String[] args) {
        String hdfsFilePath = "hdfs://192.168.1.6:8020/user/hadoop/zm/test.txt";
        HDFSHelper hdfsHelper = new HDFSHelper(null);
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        hdfsHelper.upload(new File("D:\\Code\\log.txt"), hdfsFilePath);
    }
}
