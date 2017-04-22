package com.sharemsg.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by Kun on 2016/11/17.
 */

public class BitmapChange {
    private static BitmapChange bitmapChange = null;
    public static BitmapChange getBC(){
        if (bitmapChange==null){
            bitmapChange = new BitmapChange();
        }
        return bitmapChange;
    }
    public  Bitmap getBitmap(String url){
        Bitmap tmpBitmap = null;
        try {
            InputStream is = new URL(url).openStream();
            tmpBitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            Log.e("getBitmap",e.toString());
        }
        return tmpBitmap;
    }

    // bmp2bytearray
    public byte[] bmpToByteArray(Bitmap bmp) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        bmp.recycle();
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 把网络资源图片转化成bitmap
     * @param url  网络资源图片
     * @return  Bitmap
     */
    public Bitmap GetLocalOrNetBitmap(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }
}
