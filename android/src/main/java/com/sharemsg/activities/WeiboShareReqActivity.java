package com.sharemsg.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.sharemsg.ShareMsgModule;
import com.sharemsg.utils.BitmapChange;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.utils.Utility;

import java.io.IOException;
import java.net.URL;

/**
 * Created by Kun on 2016/11/16.
 */

public class WeiboShareReqActivity extends Activity implements IWeiboHandler.Response{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && ShareMsgModule.weibapi!=null) {
            ShareMsgModule.weibapi.handleWeiboResponse(getIntent(), this);
        }

        if (ShareMsgModule.weibapi!=null && getIntent()!=null && getIntent().getExtras()!=null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bundle ertra = getIntent().getExtras();
                    int type = ertra.getInt("type");
                    switch (type){
                        case 0:
                            sendMultiMessage(ertra.getString("text"),ertra.getString("title"),
                                    ertra.getString("desc"),ertra.getString("image"),ertra.getString("url"));//发微博
                            break;
                        case 1:
                            sendImage(ertra.getString("path"));
                            break;
                    }
                }
            }).start();
        }

        if (savedInstanceState != null && ShareMsgModule.weibapi==null){
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    public void sendMultiMessage(String text,String title,String desc,String image,String url) {
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();//初始化微博的分享消息
        weiboMessage.mediaObject = getWebpageObj(title,desc,url,image,this);
        weiboMessage.textObject = getTextObj(text);
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        ShareMsgModule.weibapi.sendRequest(this,request); //发送请求消息到微博，唤起微博分享界面
    }


    private TextObject getTextObj(String text) {
        TextObject textObject = new TextObject();
        textObject.text = text;
        return textObject;
    }

    private ImageObject getImageObj(String path) {
        ImageObject imageObject = new ImageObject();
        Bitmap  bitmap = null;
        if (path.contains("http")){
            try {
                bitmap = BitmapFactory.decodeStream(new URL(path).openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bitmap = BitmapFactory.decodeFile(path);
        }

        imageObject.setImageObject(bitmap);
        return imageObject;
    }

    private void sendImage(String path) {
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.mediaObject = getImageObj(path);

        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        ShareMsgModule.weibapi.sendRequest(this,request);
    }

    private WebpageObject getWebpageObj(String title,String des,String url,String img, Context context){
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = title;
        mediaObject.description = des;
        // 设置 Bitmap 类型的图片到视频对象里
        //img = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Bitmap thumb =Bitmap.createScaledBitmap(BitmapChange.getBC().GetLocalOrNetBitmap(img), 120, 120, true);//压缩Bitmap
        mediaObject.setThumbImage(thumb);

        mediaObject.actionUrl = url;
        return mediaObject;
    }

    @Override
    public void onResponse(BaseResponse baseResponse) {
        switch (baseResponse.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                ShareMsgModule.resultShareMsgModule("weiboShare:"+"complete");
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                ShareMsgModule.resultShareMsgModule("weiboShare:"+"cancel");
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                ShareMsgModule.resultShareMsgModule("weiboShare:"+"error");
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        ShareMsgModule.weibapi.handleWeiboResponse(intent, this);
        this.finish();
    }
}
