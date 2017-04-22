package com.sharemsg;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.sharemsg.activities.WeiboShareReqActivity;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


/**
 * Created by Kun on 2016/11/15.
 */

public class ShareMsgModule extends ReactContextBaseJavaModule implements ActivityEventListener{
    private static int THUMB_SIZE = 100;//缩略图

    private int REQUEST_CODE_SMS = 1001;
    private int REQUEST_CODE_EMAIL = 1002;

    private int NO_INIT = 1011;
    private int NO_INSTALL = 1022;
    public static ReactApplicationContext reactApplicationContext;

    // public static IWXAPI iwxapi;
    public static IWeiboShareAPI weibapi;
    public static Tencent qqapi;

    /** 授权认证所需要的信息 */
    private AuthInfo mAuthInfo;
    /** SSO 授权认证实例 */
    private SsoHandler mSsoHandler;
    /** 登陆认证对应的listener */
    private AuthListener mLoginListener = new AuthListener();

    public ShareMsgModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;
        // 添加回调监听
        getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ShareMsg";
    }

    @ReactMethod
    public void show(String message) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCatalystInstanceDestroy() {
        getReactApplicationContext().removeActivityEventListener(this);
        super.onCatalystInstanceDestroy();
    }

    // install
    @ReactMethod
    public void isInstallWechat(String name,Callback cb){
        cb.invoke(""+isInstall(name));
    }
    @ReactMethod
    public void isInstallWeibo(String name,Callback cb){
        cb.invoke(""+isInstall(name));
    }
    @ReactMethod
    public void isInstallQQ(String name,Callback cb){
        cb.invoke(""+isInstall(name));
    }
    private boolean isInstall(String name){
        final PackageManager packageManager = getReactApplicationContext().getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
    //init
    // @ReactMethod
    // public void initWechat(String app_id){
    //     iwxapi = WXAPIFactory.createWXAPI(getReactApplicationContext(),app_id,true);
    //     resultShareMsgModule("initWechat:"+iwxapi.registerApp(app_id));
    // }

    @ReactMethod
    public void initWeibo(String app_id){
        weibapi = WeiboShareSDK.createWeiboAPI(getReactApplicationContext(), app_id);
        resultShareMsgModule("initWeibo:"+weibapi.registerApp());
    }

    @ReactMethod
    public void initQQ(String app_id){
        qqapi = Tencent.createInstance(app_id, getReactApplicationContext());
        if (qqapi!=null){
            resultShareMsgModule("initQQ:"+true);
        }else {
            resultShareMsgModule("initQQ:"+false);
        }
    }

    //login
    // @ReactMethod
    // public void loginWechat(String scopes,String state){
    //     if (iwxapi==null){
    //         resultShareMsgModule("wechatLogin:"+NO_INIT);
    //         return;
    //     }

    //     if (!iwxapi.isWXAppInstalled()){
    //         resultShareMsgModule("wechatLogin:"+NO_INSTALL);
    //         return;
    //     }
    //     SendAuth.Req req = new SendAuth.Req();
    //     req.scope = TextUtils.isEmpty(scopes)?"snsapi_userinfo":scopes;//需要的权限范围
    //     req.state = TextUtils.isEmpty(state)?"login_test":state;
    //     iwxapi.sendReq(req);
    // }

    @ReactMethod
    public void loginQQ(String scopes){
        qqapi.login(getCurrentActivity(),TextUtils.isEmpty(scopes)? "get_simple_userinfo" : scopes, qqLoginListener);
    }

    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";//根据用时调整
    @ReactMethod
    public void loginWeibo(String app_id,String backUrl,String scopes){
        mAuthInfo = new AuthInfo(getCurrentActivity(), app_id, backUrl, TextUtils.isEmpty(scopes)? SCOPE : scopes);
        if (null == mSsoHandler && mAuthInfo != null) {
            mSsoHandler = new SsoHandler(getCurrentActivity(), mAuthInfo);
        }

        if (mSsoHandler != null) {
            mSsoHandler.authorize(mLoginListener);
        }
    }

    //share
    // @ReactMethod
    // public void shareWechat(String title,String desc,String image,String url,int toWhere){
    //     if (iwxapi==null){
    //         resultShareMsgModule("wechatShare:"+NO_INIT);
    //         return;
    //     }

    //     if (!iwxapi.isWXAppInstalled()){
    //         resultShareMsgModule("wechatShare:"+NO_INSTALL);
    //         return;
    //     }

    //     // 初始化wxwebobject
    //     WXWebpageObject webObject = new WXWebpageObject();
    //     webObject.webpageUrl = url;

    //     // 初始化wxmediamessage
    //     WXMediaMessage msg = new WXMediaMessage(webObject);
    //     msg.title = title;
    //     msg.description = desc;
    //     Bitmap bmp = BitmapChange.getBC().getBitmap(image);
    //     Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp,THUMB_SIZE,THUMB_SIZE,true);
    //     bmp.recycle();
    //     msg.thumbData = BitmapChange.getBC().bmpToByteArray(thumbBmp);

    //     // 构造一个req
    //     SendMessageToWX.Req req = new SendMessageToWX.Req();
    //     req.transaction = String.valueOf(System.currentTimeMillis());
    //     req.message = msg;
    //     //分享到好友SendMessageToWX.Req.WXSceneSession;朋友圈SendMessageToWX.Req.WXSceneTimeline
    //     req.scene = toWhere!=0? toWhere:0;
    //     iwxapi.sendReq(req);
    // }

    @ReactMethod
    public void shareWeibo(int type,String text,String title,String desc,String image,String url){
        if (weibapi!=null) {
            Intent i = new Intent(getCurrentActivity(),WeiboShareReqActivity.class);
            i.putExtra("type",type);//0:图文链接，1:本地图片
            switch (type){
                case 0:
                    i.putExtra("text",text);
                    i.putExtra("title",title);
                    i.putExtra("desc",desc);
                    i.putExtra("image",image);
                    i.putExtra("url",url);
                    break;
                case 1:
                    i.putExtra("path",image);
                    break;
            }
            getCurrentActivity().startActivity(i);
        }else {
            Toast.makeText(getCurrentActivity(), "weiboapi未初始化", Toast.LENGTH_SHORT).show();
        }
    }

    @ReactMethod
    public void shareQQ(int type,String title,String desc,String image,String url){
        if (qqapi!=null) {
            final Bundle params = new Bundle();
            switch (type){
                case QQShare.SHARE_TO_QQ_TYPE_IMAGE:
                    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                    if (image.contains("http")){
                        int index = image.lastIndexOf("/");
                        String name = image.substring(index + 1);
                        String localurl = savaImage(getImageInputStream(image),name);
                        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, localurl);
                    }else {
                        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, image);
                    }
                    break;
                case QQShare.SHARE_TO_QQ_TYPE_DEFAULT:
                    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                    params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
                    params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  desc);
                    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  url);
                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, image);//图片地址
                    break;
            }

            qqapi.shareToQQ(getCurrentActivity(),params,qqShareListener);
        }else {
            Toast.makeText(getCurrentActivity(), "qqapi未初始化", Toast.LENGTH_SHORT).show();
        }
    }

    @ReactMethod
    public void shareSMS(String address,String smsBody){
        Uri smsToUri = Uri.parse( "smsto:" );
        Intent sendIntent =  new Intent(Intent.ACTION_VIEW, smsToUri);
        sendIntent.putExtra("address", address); // 电话号码，这行去掉的话，默认就没有电话
        //短信内容
        sendIntent.putExtra( "sms_body", smsBody);
        sendIntent.setType( "vnd.android-dir/mms-sms");
        getCurrentActivity().startActivityForResult(sendIntent, REQUEST_CODE_SMS);
    }

    @ReactMethod
    public void shareEmail(String address, String emailTitle,String emailBody){
        Intent email = new Intent(Intent.ACTION_SEND);
        String[] tos = {address};
        email.putExtra(Intent.EXTRA_EMAIL, tos);
        email.setType("plain/text");
        //邮件主题
        email.putExtra(android.content.Intent.EXTRA_SUBJECT, emailTitle);
        //邮件内容
        email.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);

        getCurrentActivity().startActivityForResult(Intent.createChooser(email,  "请选择邮件发送内容" ), REQUEST_CODE_EMAIL);
    }


    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == com.tencent.connect.common.Constants.REQUEST_QQ_SHARE) {
            Tencent.onActivityResultData(requestCode, resultCode, data, qqShareListener);
        }else if(requestCode == com.tencent.connect.common.Constants.REQUEST_LOGIN ||
                requestCode == com.tencent.connect.common.Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode,resultCode,data,qqLoginListener);
        }else if (requestCode == 32973 && mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }else if (requestCode == REQUEST_CODE_SMS){
            resultShareMsgModule("sms:"+"处理结束");
        }else if (requestCode == REQUEST_CODE_EMAIL){
            resultShareMsgModule("email:"+"处理结束");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
    }


    //qq回调
    IUiListener qqLoginListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            resultShareMsgModule("qqLogin:"+"complete"+",result:"+o.toString());
        }

        @Override
        public void onError(UiError uiError) {
            resultShareMsgModule("qqLogin:"+"error");
        }

        @Override
        public void onCancel() {
            resultShareMsgModule("qqLogin:"+"cancle");
        }
    };

    IUiListener qqShareListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            resultShareMsgModule("qqShare:"+"complete");
        }

        @Override
        public void onError(UiError uiError) {
            resultShareMsgModule("qqShare:"+"error");
        }

        @Override
        public void onCancel() {
            resultShareMsgModule("qqShare:"+"cancel");
        }
    };

    // 微博回调
    /**
     * 登入按钮的监听器，接收授权结果。
     */
    private class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {

            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                resultShareMsgModule("weiboLogin:"+"complete"+",accessToken:"+accessToken.toString());
            }else {
                resultShareMsgModule("weiboLogin:"+"error");
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            resultShareMsgModule("weiboLogin:"+"error");
        }

        @Override
        public void onCancel() {
            resultShareMsgModule("weiboLogin:"+"cancel");
        }
    }

    public static void resultShareMsgModule(String result){
        if (result!=null&&reactApplicationContext!=null)
            reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("ShareMsgModule", result);
    }

    private final String SAVE_PIC_PATH= Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath(): "/mnt/sdcard";

    //获取网络图片
    private Bitmap getImageInputStream(String imageurl){
        URL url;
        HttpURLConnection connection=null;
        Bitmap bitmap=null;
        try {
            url = new URL(imageurl);
            connection=(HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(6000); //超时设置
            connection.setDoInput(true);
            connection.setUseCaches(false); //设置不使用缓存
            InputStream inputStream=connection.getInputStream();
            bitmap= BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // 保存
    private String savaImage(Bitmap bitmap,String p){
        File file=new File(SAVE_PIC_PATH);
        FileOutputStream fileOutputStream=null;
        //文件夹不存在，则创建它
        if(!file.exists()){
            file.mkdir();
        }
        String path = SAVE_PIC_PATH+"/"+p;

        try {
            fileOutputStream=new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,fileOutputStream);
            fileOutputStream.close();
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            getCurrentActivity().sendBroadcast(intent);
            return path;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
