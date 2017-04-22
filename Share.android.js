'use strict'
import {
  NativeModules,DeviceEventEmitter
} from 'react-native';

var ShareMsg = NativeModules.ShareMsg
// var wechat_id = 'wx520a68af75ac86f2'
// var qq_id = '1104812217'
// var weibo_id = '2038575089'
export default class Share {
  /** 注册返回事件处理
   * componentDidMount(){
    //wechat返回码 0 成功;-2 取消;-4 被服务器拒绝;1011 初始化异常;1022 应用未安装
    //    wechatLogin:返回码+code+state;wechatShare:返回码
    //qq返回值 complete 完成;cancle 用户取消;error 错误
    //    qqLogin:返回结果+返回数据;qqShare:返回值
    //weibo返回值 complete 完成;cancle 用户取消;error 错误
    //    weiboLogin:返回结果+返回数据;qqShare:返回值
    //sms:处理结束|email:处理结束    具体参数待测试
    //initWechat:|initWeibo:|initQQ:   true初始化成功|false初始化失败
    DeviceEventEmitter.addListener('ShareMsgModule', event => {
        ShareMsg.show(event)
      })
  }
   */
  //android接口初始化 
  //微信
  // initWechat(wechat_id){
  //   ShareMsg.initWechat(wechat_id)
  // }
  //QQ
  initQQ(qq_id){
    ShareMsg.initQQ(qq_id)
  }
  //微博
  initWeibo(weibo_id){
    ShareMsg.initWeibo(weibo_id)
  }

  // 判断是否安装QQ
  IsQQInstalled() {
    return new Promise(function(resolve, reject){
      ShareMsg.isInstallQQ('com.tencent.mobileqq',cb=>{
        resolve(cb)
      })
    })
  }
  // 判断是否安装微信
  // IsWeixinInstalled() {
  //   return new Promise(function(resolve, reject){
  //     ShareMsg.isInstallWechat('com.tencent.mm',cb=>{
  //       resolve(cb)
  //     })
  //   })
  // }
  // 判断是否安装微博
  IsWeiboInstalled() {
    return new Promise(function(resolve, reject){
      ShareMsg.isInstallWeibo('com.sina.weibo',cb=>{
        resolve(cb)
      })
    })
  }

  // 授权登录
  // weixinLogin(weiXinAppID) {
  //   ShareMsg.loginWechat('','')
  // }

  qqLogin(QQAppID) {
    ShareMsg.loginQQ('')
  }

  weiboLogin(weiboAppKey) {
    ShareMsg.loginWeibo(weiboAppKey,'http://tuofeng.cn','')
  }

// 分享  标题、描述、图片、链接

// 分享到微博
/**
  参数：info.type 0:图文链接；1:本地图片
  info  分享的内容  例如：{title: '测试', desc: 'rn分享', logo: 'http://jijia.tuofeng.cn/plan/images/ins_company/ddhrs.jpg', url: 'http://jijia.tuofeng.cn/#plans'}
**/
shareToWeibo(info, weiboAppKey) {
  switch(info.type){
    case 1:
      ShareMsg.shareWeibo(1,'这款产品真好',info.title,info.desc,info.imagePath,info.url)
      break
    default:
      ShareMsg.shareWeibo(0,'这款产品真好',info.title,info.desc,info.logo,info.url)
  }
}

// 分享到QQ
/**
  参数：
  info.type 1:图文链接；5:本地图片
  info  分享的内容  例如：{title: '测试', desc: 'rn分享', logo: 'http://jijia.tuofeng.cn/plan/images/ins_company/ddhrs.jpg', url: 'http://jijia.tuofeng.cn/#plans'}
  shareTo  类型  0 QQ好友  1 QQ空间  8 QQ收藏   16 QQ数据线 电脑共享
**/
  shareToQQ(info, shareTo, QQAppID) {
    switch(info.type){
    case 5:
      ShareMsg.shareQQ(5,info.title,info.desc,info.imagePath,info.url)
      break
    default:
      ShareMsg.shareQQ(1,info.title,info.desc,info.logo,info.url)
    }
  }

// 分享到微信
/**
  参数：
  info 分享的内容  例如：{title: '测试', desc: 'rn分享', logo: 'http://jijia.tuofeng.cn/plan/images/ins_company/ddhrs.jpg', url: 'http://jijia.tuofeng.cn/#plans'}
  shareTo  分享的类型   0 微信好友  1 微信朋友圈   2 微信收藏
**/
  // shareToWeixin(info, weiXinAppID, shareTo, weiXindict) {
  //   //参数 标题,描述,图标地址,链接地址,好友0|朋友圈1|收藏2
  //   ShareMsg.shareWechat(info.title,info.desc,info.logo,info.url,shareTo)
  // }


 //发送邮件
   /**
   参数 info:
       {
       "title": "" //邮件主题
       "toRecipients": ["", ""] // 收件人
       "ccRecipients": ["", ""] // 抄送
       "bccRecipients": ["", ""]  // 密送
       "body": ""  // 邮件正文
       }
   **/
   shareToMail(info, callBack) {
     //仅支持首个地址
 	  //参数信息 目标地址,标题,内容
    ShareMsg.shareEmail(info.toRecipients[0],info.title,info.body)
   }

   //发送短信
   /**
   参数info：
       {
       "phones": ["", ""] // 手机号码
       "body": ""  // 短信正文
       }
   **/
   shareToMessage(info, callBack) {
     //仅支持首个电话
 	  //参数信息 目标电话,短信文本,附加链接地址
    ShareMsg.shareSMS(info.phones[0],info.body)
   }

}
module.exports = new Share();
