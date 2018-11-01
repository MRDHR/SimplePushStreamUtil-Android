package com.dhr.simplepushstreamutil.runnable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class PushStreamRunnable implements Runnable {
    private final OutputStream ostrm_;
    private final InputStream istrm_;
    private PushStreamCallBack pushStreamCallBack;

    public PushStreamRunnable(InputStream istrm, OutputStream ostrm, PushStreamCallBack pushStreamCallBack) {
        istrm_ = istrm;
        ostrm_ = ostrm;
        this.pushStreamCallBack = pushStreamCallBack;
    }

    @Override
    public void run() {
        try {
            String line = null;
            BufferedReader bufferedReader = new BufferedReader
                    (new InputStreamReader(istrm_, "GB2312"));
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                String trim = line.replaceAll(" ", "").toLowerCase().trim();
                if (trim.startsWith("frame") || trim.startsWith("size")) {
                    String size;
                    String time;
                    String bitrate;
                    int sizeStartIndex = trim.indexOf("size=");
                    int sizeEndTimeStartIndex = trim.indexOf("kbtime=");
                    int timeEndBitrateStartIndex = trim.indexOf("bitrate=");
                    int BitrateENDIndex = trim.indexOf("kbits/s");
                    size = trim.substring(sizeStartIndex, sizeEndTimeStartIndex);
                    time = trim.substring(sizeEndTimeStartIndex, timeEndBitrateStartIndex);
                    bitrate = trim.substring(timeEndBitrateStartIndex, BitrateENDIndex);
                    pushStreamCallBack.pushing(size.replaceAll("size=", ""), time.replaceAll("kbtime=", ""), bitrate.replaceAll("bitrate=", ""));
                } else if (line.toLowerCase().contains("error")) {
                    pushStreamCallBack.pushFail(line);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("处理命令出现错误：" + e.getMessage());
        }
    }

    public interface PushStreamCallBack {
        void pushing(String size, String time, String bitrate);

        void pushFail(String reason);
    }

}
