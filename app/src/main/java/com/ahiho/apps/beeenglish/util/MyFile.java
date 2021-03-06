package com.ahiho.apps.beeenglish.util;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.Toast;

import com.ahiho.apps.beeenglish.my_interface.OnCallbackDownload;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.realm.internal.IOException;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by theptokim on 11/30/16.
 */

public class MyFile {

    public static final String APP_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +"/bee_english";
    public static final String PICTURE_FOLDER = "pictures";
    public static final String BOOK_FOLDER = "books";
    public static final String DOWNLOADS_FOLDER = "downloads";
    private static final String TAG="RESPONSE_FILE";

    public static long getFolderSize(File f) {
        long size = 0;
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                size += getFolderSize(file);
            }
        } else {
            size = f.length();
        }
        return size;
    }

    public static String getFolderSizeString(File f) {
        String strSize;
        long Filesize = getFolderSize(f) / 1024;//call function and convert bytes into Kb
        if (Filesize >= 1024)
            strSize = Filesize / 1024 + " Mb";
        else
            strSize = Filesize + " Kb";
        return strSize;
    }

    public static File getFileFromStringURI(String uri) {
        File file = new File(uri);
        return file;
    }

    public static String saveImageFile(Context context, String imageUrl) {
//    final String imageUrl = promtionObject.getImageURL();
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1); //promtionObject.getPromotionId() + ".png";
            URL url = new URL(imageUrl);
            URLConnection conn = url.openConnection();
            Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            File myDir = new File(context.getFilesDir() + "/" + PICTURE_FOLDER);
            if (!myDir.exists()) {
                myDir.mkdirs();
            }
            myDir = new File(myDir, fileName);
            if (!myDir.exists()) {

                FileOutputStream out = new FileOutputStream(myDir);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            }
            return Uri.fromFile(myDir).toString();
        } catch (Exception e) {
            return "";
        }
    }

    /***
     * @param context
     * @param path
     * @param fileName with extend file "yourfile.png"
     * @return
     */

    public static String downloadFile(Context context, final String path, String fileName, OnCallbackDownload callbackDownload) {
        String uriFile = "";
        try {
            URL url = new URL(path);

            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(10000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

            File file = new File(context.getDir(PICTURE_FOLDER, Context.MODE_PRIVATE) + "/" + fileName);

            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            final long size = ucon.getContentLength();
            final int sizeBuff = 5 * 1024;

            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[sizeBuff];

            int len;
            int total = 0;
            while ((len = inStream.read(buff)) != -1) {
                outStream.write(buff, 0, len);
                total += len;
                callbackDownload.postProgress((total * 100) / size);
            }

            outStream.flush();
            outStream.close();
            inStream.close();

            uriFile = Uri.fromFile(file).toString();
            callbackDownload.downloadSuccess(uriFile);
        } catch (Exception e) {
            callbackDownload.downloadError(e);
        }

        return uriFile;
    }

    public static String getFileName(String path) {
        String s = URLUtil.guessFileName(path, null, null);
        return s;
    }

    public static String convertUri2FileUri(String uri) {
        Log.e("RESPONSE",uri);
        return uri.replace("file://", "");
    }

    public static void copy(File src, File dst) {
        InputStream in = null;
        try {
            in = new FileInputStream(src);

            OutputStream out = new FileOutputStream(dst);
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

    public static boolean unzip(String zipFile, String location) {
        boolean result=true;
        try {
            File f = new File(location);
            if(!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if(!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    }
                    else {
                        FileOutputStream fout = new FileOutputStream(path, false);
                        try {
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);
                            }
                            zin.closeEntry();
                        }catch (Exception e) {
                            result = false;
                        }
                        finally {
                            fout.close();
                        }
                    }
                }
            } catch (Exception e) {
                result = false;
            }
            finally {
                zin.close();
            }
        }
        catch (Exception e) {
            result=false;
            Log.e(TAG, "Unzip exception", e);
        }
        return result;
    }
    public static boolean unzipWithLib(String source,String destination,String password ){
        boolean result=true;
        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(password);
            }
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            Log.e(TAG,e.getMessage());
            result=false;
        }
        return result;
    }

    public static String readFileText(String uri){
        String result="";
        File file = new File(uri);
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
            result=text.toString();
        }
        catch (java.io.IOException e) {
            result="";
        }
        return result;
    }

}
