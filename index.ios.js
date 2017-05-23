import {
  Linking,
  NativeModules,
} from 'react-native'
import { Buffer } from 'buffer'

const openShare = NativeModules.TFShare

export default class Share {

  // 判断是否安装QQ
  static IsQQInstalled() {
    return new Promise((resolve) => {
      Linking.canOpenURL('mqqapi://').then((ret) => {
        resolve(ret)
      })
    })
  }

  // 判断是否安装微博
  static IsWeiboInstalled() {
    return new Promise((resolve) => {
      Linking.canOpenURL('weibosdk://request').then((ret) => {
        resolve(ret)
      })
    })
  }
  // 判断是否安装微信
  static IsWeixinInstalled() {
    return new Promise((resolve) => {
      Linking.canOpenURL('weixin://').then((ret) => {
        resolve(ret)
      })
    })
  }

  // 授权登录
  static weixinLogin(weiXinAppID) {
    const openUrl = `weixin://app/${weiXinAppID}/auth/?scope=snsapi_userinfo&state=Weixinauth`
    Linking.openURL(openUrl)
  }

  static qqLogin(QQAppID) {
    openShare.qqLoginAppID(QQAppID.toString(), () => {
      const openUrl = `mqqOpensdkSSoLogin://SSoLogin/tencent${QQAppID.toString()}/com.tencent.tencent${QQAppID.toString()}?generalpastboard=1`
      Linking.openURL(openUrl)
    })
  }

  static weiboLogin(weiboAppKey) {
    openShare.weiboLoginAppKey(weiboAppKey, (response) => {
      const openUrl = `weibosdk://request?id=${response.uuid}&sdkversion=003013000`
      Linking.openURL(openUrl)
    })
  }


  // 分享  标题、描述、图片、链接

  // 分享到微博
  /**
    参数：
    info  分享的内容  例如：{title: '测试', desc: 'rn分享', logo: 'http://jijia.tuofeng.cn/plan/images/ins_company/ddhrs.jpg', url: 'http://jijia.tuofeng.cn/#plans', type: 1}
    type  0 分享链接，1分享图片
  **/
  static shareToWeibo(info, weiboAppKey) {
    const message = Object.assign({}, { 'objectID': 'identifier1' }, { description: info.desc, title: info.title, webpageUrl: info.url })
    const type = (info.type && info.type === 1) ? '1' : '0'
    openShare.shareToWeiboWithInfo(message, info.logo, type, weiboAppKey, (response) => {
      const openUrl = `weibosdk://request?id=${response.uuid}&sdkversion=003013000`
      Linking.openURL(openUrl)
    })
  }


  // 分享到QQ
  /**
    参数：
    info  分享的内容  例如：{title: '测试', desc: 'rn分享', logo: 'http://jijia.tuofeng.cn/plan/images/ins_company/ddhrs.jpg', url: 'http://jijia.tuofeng.cn/#plans', type: 1}
    shareTo  类型  0 QQ好友  1 QQ空间  8 QQ收藏   16 QQ数据线 电脑共享
    type     0 分享链接，1分享图片
  **/
  static shareToQQ(info, shareTo, QQAppID) {
    // callback_name 是 'QQ' 拼接 QQAppID的16进制
    const callbackName = `QQ${(QQAppID).toString(16)}`
    const title = encodeURI(Buffer(info.title).toString('base64'))
    const url = encodeURI(Buffer(info.url).toString('base64'))
    const description = encodeURI(Buffer(info.desc).toString('base64'))
    const type = (info.type && info.type === 1) ? '1' : '0'

    openShare.shareToQQWithLogo(info.logo, type, (response) => {
      const thirdAppDisplayName = Buffer(response.thirdAppDisplayName).toString('base64')

      // 发送图片
      if (type === '1') {
        const openUrl = `mqqapi://share/to_fri?thirdAppDisplayName=${thirdAppDisplayName}&version=1&cflag=${shareTo}&callback_type=scheme&generalpastboard=1&callback_name=${callbackName}&src_type=app&shareType=0&file_type=img&title=${title}&objectlocation=pasteboard&description=${description}`
        Linking.openURL(openUrl)
      } else {
        const openUrl = `mqqapi://share/to_fri?thirdAppDisplayName=${thirdAppDisplayName}&version=1&cflag=${shareTo}&callback_type=scheme&generalpastboard=1&callback_name=${callbackName}&src_type=app&shareType=0&file_type=news&title=${title}&url=${url}&description=${description}&objectlocation=pasteboard`
        Linking.openURL(openUrl)
      }
    })
  }

  // 分享到微信
  /**
    参数：
    info 分享的内容  例如：{title: '测试', desc: 'rn分享', logo: 'http://jijia.tuofeng.cn/plan/images/ins_company/ddhrs.jpg', url: 'http://jijia.tuofeng.cn/#plans', type: 1}
    shareTo  分享的类型   0 微信好友  1 微信朋友圈   2 微信收藏
    type     0 分享链接，1分享图片
  **/
  static shareToWeixin(info, weiXinAppID, shareTo, weiXindict) {
    let message = {}
    const type = (info.type && info.type === 1) ? '1' : '0'
    if (type === '1') {
      message = Object.assign({}, weiXindict, { 'scene': shareTo }, { title: info.title, objectType: '2' })
    } else {
      message = Object.assign({}, weiXindict, { 'scene': shareTo }, { description: info.desc, title: info.title, mediaUrl: info.url, objectType: '5' })
    }

    openShare.shareToWeixinWithInfo(message, weiXinAppID, info.logo, type, () => {
      const openUrl = `weixin://app/${weiXinAppID}/sendreq/?`
      Linking.openURL(openUrl)
    })
  }

  // 分享到微信小程序
  /**
    参数：
    info 分享内容 {path: 小程序页面路径, userName: 小程序的帐户ID, webpageUrl: 低版本网页链接，title: 标题, desc: 描述, logo: 缩略图}
    shareTo  分享场景  0 目前只支持会话
    objectType  36表示分享微信小程序
  **/
  static shareToWeixinMini(info, weiXinAppID, shareTo, weiXindict) {
    const message = Object.assign({}, weiXindict, { 'scene': shareTo }, { appBrandPath: info.path, appBrandUserName: info.userName, mediaUrl: info.webpageUrl, title: info.title, description: info.desc, objectType: '36' })
    openShare.shareToWeixinMiniWithInfo(message, weiXinAppID, info.logo, () => {
      const openUrl = `weixin://app/${weiXinAppID}/sendreq/?`
      Linking.openURL(openUrl)
    })
  }


  // 发送邮件
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
  static shareToMail(info, callBack) {
    // Linking.openURL('mailto:601512479@qq.com')
    openShare.shareToMailWithInfo(info, (respone) => {
      callBack(respone)
    })
  }

  // 发送短信
  /**
    参数info：
     {
     "phones": ["", ""] // 手机号码
     "body": ""  // 短信正文
     }
  **/
  static shareToMessage(info, callBack) {
    // Linking.openURL('sms:130513360408')
    openShare.shareToMessageWithInfo(info, (respone) => {
      callBack(respone)
    })
  }

  // 处理openurl回调
  /**
     参数：
     returnedURL: 回调回来的url地址；
     appID: 平台对应的应用的ID
     callBack: 回调返回数据
  **/
  static handleOpenURL(returnedURL, appID, callBack) {
    openShare.handleOpenURL(returnedURL, appID, (result) => {
      // 将返回的数据回调给调用处
      callBack(result)
    })
  }
}
