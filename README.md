
#0.1版本增加微信小程序类型分享
##iOS微信小程序类型分享说明
###分享微信小程序的方法
####原生端
`shareToWeixinMiniWithInfo:(NSDictionary *)info appid:(NSString *)appid logo:(NSString *)logo callback:(RCTResponseSenderBlock)callback`
参数：  
info: 分享的内容   
appid: 微信id   
logo: 缩略图 微信缩略图不超过32k，需要压缩   
####js端
`shareToWeixinMini(info, weiXinAppID, shareTo, weiXindict)`
参数:  
info: 分享内容
{  
 path: 小程序页面路径  
 userName: 小程序账户ID  
 webpageUrl: 低版本网页链接，微信版本低于6.5.6，小程序类型分享将自动转成网页类型分享  
 title: 标题  
 desc: 描述  
 logo: 缩略图    
}  
weiXinAppID: 微信AppID  
shareTo: 分享场景，0目前只支持会话场景  

#android插件依赖
npm 插件包
rnpm link 包名
build.gradle查看是否添加了包
Mainapplication查看是否导入了插件
js端调用(查看index.android.js文件)

#android部分附加信息     
app/build.gradle文件添加    
      defaultConfig{
        ...
        manifestPlaceholders = [
                QQ_APPID: "qqappid"
        ]
      }

项目目录下复制粘贴文件夹wxapi并修改对应java文件包名



注意android方法返回参数参考share.android.js事件注册


安装TFShare
"React-Native-TFShare": "git+ssh://git@git.tuofeng.cn:10022/zuojianjun/React-Native-TFShare.git",
在react native中没有Buffer，需要自己安装，运行命令：npm install buffer --save 
