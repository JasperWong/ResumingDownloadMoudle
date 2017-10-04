package com.jasper;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {
    private static final String DOWNLOAD_URL="http://7xs0af.com1.z0.glb.clouddn.com/High-Wake.mp3";     // 下载网址
    private static final String DOWNLOAD_PATH="J:\\workplace\\ResumingDownloadMoudle\\download\\";      // 本地下载路径
    private static final int THREAD_TOTAL=4;                                                            // 同时下载线程数

    public static void main(String[] args){

        try {
            URL downloadUrl=new URL(DOWNLOAD_URL);
            HttpURLConnection httpURLConnection=(HttpURLConnection)downloadUrl.openConnection();
            int fileLen=httpURLConnection.getContentLength();
            String remoteFilePath=httpURLConnection.getURL().getFile();
            String fileName=remoteFilePath.substring(remoteFilePath.lastIndexOf(File.separator)+2);     // 提取文件名
            String fileDownloadPath = DOWNLOAD_PATH + fileName;
//            System.out.println(fileDownloadPath);
            File downFile=new File(fileDownloadPath);

            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void startDownlaod(int threadNum){

    }
}


