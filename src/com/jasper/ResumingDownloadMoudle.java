package com.jasper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ResumingDownloadMoudle{
    private String mUrl;     // 下载网址
    private String mDownloadPath;      // 本地下载路径
    private int mThreadCounts;                                                            // 同时下载线程数

    public ResumingDownloadMoudle(String downloadUrl, String downloadPath, int threadCounts) {
        this.mUrl = downloadUrl;
        this.mDownloadPath = downloadPath;
        this.mThreadCounts = threadCounts;
    }

    public int init(){
        int fileLen=0;
        try {
            URL downloadUrl=new URL(mUrl);
            HttpURLConnection httpURLConnection=(HttpURLConnection)downloadUrl.openConnection();
            fileLen=httpURLConnection.getContentLength();
            String remoteFilePath=httpURLConnection.getURL().getFile();
            String fileName=remoteFilePath.substring(remoteFilePath.lastIndexOf(File.separator)+2);     // 提取文件名
            mDownloadPath+=fileName;
            File downFile=new File(mDownloadPath);
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
        System.out.println("fileSize: " + (float)Math.round(fileLen/1024000.0*100)/100+"m");
        return fileLen;
    }

    public void startDownlaod(int fileLen){
        int eachSize=fileLen/mThreadCounts;  // 开始下载
        for(int i=0;i<mThreadCounts;i++){
            new Thread(new DownloadTask(i,i*eachSize,(i+1)*eachSize-1,mDownloadPath,mUrl)).start();
        }
    }

    class DownloadTask implements Runnable{

        public static final String TEMP_NAME="tempFile";
        private int mThreadId;
        private long mStartPos;
        private long mEndPos;
        private String mFileDownloadPath;
        private String mDownloadUrl;
        private String mTempFilePath;

        public DownloadTask(int mThreadId, long mStartPos, long mEndPos, String mFileDownloadPath, String mDownloadUrl) {
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
                if(getProgress()!=0){
                    mStartPos=getProgress();
                }

                System.out.println("thread:"+mThreadId+" startPos:"+mStartPos+" endPos:"+mEndPos);

                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
//                httpURLConnection.setReadTimeout(5000);
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
                    saveProgress(mStartPos+len);
                }

                long currentTime=System.currentTimeMillis();
                System.out.println("thread"+mThreadId+" finished in " + (currentTime - startTime)+"ms");
                downFile.close();
                httpURLConnection.disconnect();
                bufferedInputStream.close();

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        
        private long getProgress(){
            try {
                File tempFile=new File(mTempFilePath);
                if(!tempFile.exists()){
                    return 0;
                }
                FileInputStream fileInputStream=new FileInputStream(tempFile);
                BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream);
                byte[] buffer=new byte[1024];
                int len=0;
                String saveProgress="";
                while((len=bufferedInputStream.read(buffer))!=-1){
                    saveProgress+=new String(buffer,0,len);
                }

                fileInputStream.close();
                bufferedInputStream.close();

                long progress= Long.parseLong(saveProgress);
                return progress;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        private void saveProgress(long startPos){
            File tempFile=new File(mTempFilePath);
            File parentFile=tempFile.getParentFile();
            if(!parentFile.exists()){
                parentFile.mkdirs();
            }

            try {
                RandomAccessFile randomTempFile=new RandomAccessFile(tempFile,"rw");
                byte[] bStartPos=String.valueOf(startPos).getBytes();
                randomTempFile.write(bStartPos,0,bStartPos.length);
                randomTempFile.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(startPos>=mEndPos){
                    if(tempFile.exists()){
                        tempFile.delete();
                    }
                }
            }

        }
    }

    public static void main(String[] args){         //example for using
        String downloadUrl="http://down.sandai.net/thunder9/Thunder9.1.41.914.exe";
        String downloadPath="J:\\workplace\\ResumingDownloadMoudle\\download\\";
        ResumingDownloadMoudle resumingDownloadMoudle=new ResumingDownloadMoudle(downloadUrl,downloadPath,10);
        int fileLen=resumingDownloadMoudle.init();
        resumingDownloadMoudle.startDownlaod(fileLen);
    }
}


