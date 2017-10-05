package com.jasper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ResumingDownloadMoudle{
    private String url;     // 下载网址
    private String downloadPath;      // 本地下载路径
    private int threadCounts;                                                            // 同时下载线程数

    public ResumingDownloadMoudle(String downloadUrl, String downloadPath, int threadCounts) {
        this.url = downloadUrl;
        this.downloadPath = downloadPath;
        this.threadCounts = threadCounts;
    }

    public int init(){
        int fileLen=0;
        try {
            URL downloadUrl=new URL(url);
            HttpURLConnection httpURLConnection=(HttpURLConnection)downloadUrl.openConnection();
            fileLen=httpURLConnection.getContentLength();
            String remoteFilePath=httpURLConnection.getURL().getFile();
            String fileName=remoteFilePath.substring(remoteFilePath.lastIndexOf(File.separator)+2);     // 提取文件名
            String fileDownloadPath = downloadPath + fileName;
            File downFile=new File(fileDownloadPath);
            File parentFile=downFile.getParentFile();                                                   // 避免上级目录不存在产生的错误
            if(!parentFile.exists()){
                parentFile.mkdirs();
            }
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLen;
    }

    public void startDownlaod(int fileLen){
        int eachSize=fileLen/threadCounts;  // 开始下载
        for(int i=0;i<threadCounts;i++){

        }
    }

    class StartTask implements Runnable{

        public static final String TEMP_NAME="tempFile";
        private int mThreadId;
        private long mStartPos;
        private long mEndPos;
        private String mFileDownloadPath;
        private String mDownloadUrl;
        private String mTempFilePath;

        public StartTask(int mThreadId, long mStartPos, long mEndPos, String mFileDownloadPath, String mDownloadUrl) {
            this.mThreadId = mThreadId;
            this.mStartPos = mStartPos;
            this.mEndPos = mEndPos;
            this.mFileDownloadPath = mFileDownloadPath;
            this.mDownloadUrl = mDownloadUrl;
            this.mTempFilePath=mFileDownloadPath+TEMP_NAME+mThreadId;
        }

        @Override
        public void run() {
            try{
                long startTime=System.currentTimeMillis();
                URL url=new URL(mDownloadUrl);
                System.out.println("thread:"+mThreadId+"startPos:"+mStartPos+"endPos"+mEndPos);
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setRequestProperty("Charset","UTF-8");
                httpURLConnection.setRequestProperty("Range","bytes="+mStartPos+"-"+mEndPos);
                httpURLConnection.connect();

                File file=new File(mFileDownloadPath);
                if(!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }

                RandomAccessFile downFile=new RandomAccessFile(mFileDownloadPath,"rw");
                downFile.seek(mStartPos);
                BufferedInputStream bufferedInputStream=new BufferedInputStream(httpURLConnection.getInputStream());
                int size=0,len=0;
                byte[] buffer=new byte[1024];
                while((size=bufferedInputStream.read(buffer))!=-1){
                    len+=size;
                    downFile.write(buffer,0,size);

                }

                long currentTime=System.currentTimeMillis();
                System.out.println("thread"+mThreadId+"finish in " + (currentTime - startTime)+"ms");
                downFile.close();
                httpURLConnection.disconnect();
                bufferedInputStream.close();

            }catch (Exception e){

            }
        }

        private long getProgress(int threadId){
            try {
                File tempFile=new File(mTempFilePath);
                if(!tempFile.exists()){
                    return 0;
                }

            }

        }



    }



    public static void main(String[] args){         //example for using
        String downloadUrl="http://7xs0af.com1.z0.glb.clouddn.com/High-Wake.mp3";
        String downloadPath="J:\\workplace\\ResumingDownloadMoudle\\download\\";
        ResumingDownloadMoudle resumingDownloadMoudle=new ResumingDownloadMoudle(downloadUrl,downloadPath,4);
        int fileLen=resumingDownloadMoudle.init();
        resumingDownloadMoudle.startDownlaod(fileLen);
    }
}


