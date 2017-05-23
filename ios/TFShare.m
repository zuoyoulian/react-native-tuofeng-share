//
//  TFShare.m
//  RCTTFShare
//
//  Created by 左建军 on 2017/4/22.
//  Copyright © 2017年 tuofeng. All rights reserved.
//

#import "TFShare.h"
#import <MessageUI/MessageUI.h>

@interface TFShare () <MFMailComposeViewControllerDelegate, MFMessageComposeViewControllerDelegate> {
    
}

@property (nonatomic, copy) RCTResponseSenderBlock callback;
@property (nonatomic, copy) NSData *pasteboardContent;
@property (nonatomic, assign) BOOL isShare;

@end

@implementation TFShare

RCT_EXPORT_MODULE();

- (instancetype)init
{
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleOpenURL:) name:@"RCTOpenURLNotification" object:nil];
    }
    return self;
}


#pragma mark-短信分享-

/**
 参数info：
 {
 "phones": ["", ""] // 手机号码
 "body": ""  // 短信正文
 "subject": ""  // 主题
 }
 */
RCT_EXPORT_METHOD(shareToMessageWithInfo :(NSDictionary *)info callback:(RCTResponseSenderBlock)callback) {
    self.callback = callback;
    self.isShare = YES;
    // 获取根视图控制器
    UIViewController *controller = [[[UIApplication sharedApplication] keyWindow] rootViewController];
    
    if( [MFMessageComposeViewController canSendText]) {
        
        MFMessageComposeViewController * messageController = [[MFMessageComposeViewController alloc] init];
        messageController.messageComposeDelegate = self;
        
        // 手机号
        if ([info objectForKey:@"phones"]) {
            messageController.recipients = [info objectForKey:@"phones"];
        }
        
        // 短信内容
        if ([info objectForKey:@"body"]) {
            messageController.body = [info objectForKey:@"body"];
        }
        
        // 如果支持主题
        if([MFMessageComposeViewController canSendSubject]){
            if ([info objectForKey:@"subject"]) {
                messageController.subject = [info objectForKey:@"subject"];
            }
        }
        
        // 如果支持附件
        if ([MFMessageComposeViewController canSendAttachments]) {
            
        }
        
        [controller presentViewController:messageController animated:YES completion:nil];
        
        
    } else {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"提示信息" message:@"该设备不支持短信功能" preferredStyle:(UIAlertControllerStyleAlert)];
        UIAlertAction *fixAlcet = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            
        }];
        [alertController addAction:fixAlcet];
        [controller presentViewController:alertController animated:YES completion:nil];
    }
    
}
-(void)messageComposeViewController:(MFMessageComposeViewController *)controller didFinishWithResult:(MessageComposeResult)result
{
    [controller dismissViewControllerAnimated:YES completion:nil];
    switch (result) {
        case MessageComposeResultSent:
            NSLog(@"Result: 发送成功");
            self.callback(@[@{@"title" : @"短信发送成功", @"res" : @{@"message": @"短信发送成功"}}]);
            break;
        case MessageComposeResultFailed:
            NSLog(@"Result: 发送失败");
            self.callback(@[@{@"title" : @"短信发送失败", @"res" : @{@"message": @"短信发送失败"}}]);
            break;
        case MessageComposeResultCancelled:
            NSLog(@"Result: 取消");
            self.callback(@[@{@"title" : @"短信发送失败", @"res" : @{@"message": @"用户取消发送"}}]);
            break;
        default:
            break;
    }
}

#pragma mark-邮箱分享-

/**
 参数 info:
 {
 "subject": "" //邮件主题
 "toRecipients": ["", ""] // 收件人
 "ccRecipients": ["", ""] // 抄送
 "bccRecipients": ["", ""]  // 密送
 "body": ""  // 邮件正文
 }
 */
RCT_EXPORT_METHOD(shareToMailWithInfo :(NSDictionary *)info callback:(RCTResponseSenderBlock)callback) {
    self.callback = callback;
    self.isShare = YES;
    // 获取根视图控制器
    UIViewController *controller = [[[UIApplication sharedApplication] keyWindow] rootViewController];
    if ([MFMailComposeViewController canSendMail]) {
        
        MFMailComposeViewController *picker = [[MFMailComposeViewController alloc] init];
        picker.mailComposeDelegate =self;
        
        // 设置标题
        if ([info objectForKey:@"subject"]) {
            [picker setSubject: [info objectForKey:@"subject"]];
        }
        if ([info objectForKey:@"toRecipients"]) {
            [picker setToRecipients: [info objectForKey:@"toRecipients"]]; // 收件人
        }
        if ([info objectForKey:@"ccRecipients"]) {
            [picker setCcRecipients: [info objectForKey:@"ccRecipients"]]; // 抄送
        }
        if ([info objectForKey:@"bccRecipients"]) {
            [picker setBccRecipients: [info objectForKey:@"bccRecipients"]];  // 密送
        }
        
        // 附件图片
        
        // 文件附件
        
        // 邮件正文
        if ([info objectForKey:@"body"]) {
            [picker setMessageBody:[info objectForKey:@"body"] isHTML:YES];
        }
        
        [controller presentViewController:picker animated:YES completion:nil];
        
        
    } else {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"提示信息" message:@"该设备不支持邮箱功能" preferredStyle:(UIAlertControllerStyleAlert)];
        UIAlertAction *fixAlcet = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            
        }];
        [alertController addAction:fixAlcet];
        [controller presentViewController:alertController animated:YES completion:nil];
    }
}
- (void)mailComposeController:(MFMailComposeViewController*)controller
          didFinishWithResult:(MFMailComposeResult)result error:(NSError*)error {
    
    [controller dismissViewControllerAnimated:YES completion:nil];
    
    switch (result)
    {
        case MFMailComposeResultCancelled:
            NSLog(@"Result: 取消");
            self.callback(@[@{@"title" : @"邮箱分享失败", @"res" : @{@"message": @"用户取消发送"}}]);
            break;
        case MFMailComposeResultSaved:
            NSLog(@"Result: 保存");
            self.callback(@[@{@"title" : @"邮箱分享失败", @"res" : @{@"message": @"用户保存邮件"}}]);
            break;
        case MFMailComposeResultSent:
            NSLog(@"Result: 发送成功");
            self.callback(@[@{@"title" : @"邮箱发送成功", @"res" : @{@"message": @"邮件发送成功"}}]);
            break;
        case MFMailComposeResultFailed:
            NSLog(@"Result: 发送失败");
            self.callback(@[@{@"title" : @"邮箱分享失败", @"res" : @{@"message": @"邮件发送失败"}}]);
            break;
        default:
            NSLog(@"Result: 没有发送");
            break;
    }
}


#pragma mark-分享-

//  分享微博
RCT_EXPORT_METHOD(shareToWeiboWithInfo:(NSDictionary *)info logo:(NSString *)logo type:(NSString *)type appKey:(NSString *)appKey callback:(RCTResponseSenderBlock)callback){
    self.isShare = YES;
    
    logo = [logo stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    
    NSMutableDictionary *dic=[[NSMutableDictionary alloc] initWithDictionary:info];
    
    UIImage *img;
    // 网络图片
    if ([logo hasPrefix: @"http://"] || [logo hasPrefix: @"https://"]) {
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:logo]]];
    } else {  // 本地图片
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfFile:logo]];
    }
    
    NSDictionary *message;
    // 分享图片
    if ([type integerValue] == 1) {
        message = @{@"__class" : @"WBMessageObject", @"imageObject":@{
                            @"imageData": UIImageJPEGRepresentation(img, 1)
                            },
                    @"text" : info[@"title"]};
    } else {
        dic[@"thumbnailData"] = [self thumbDataWithImg: img];
        dic[@"__class"] = @"WBWebpageObject";
        message = @{@"__class" : @"WBMessageObject", @"mediaObject":dic};
    }
    
    NSString *uuid=[[NSUUID UUID] UUIDString];
    NSArray *messageData = @[
                             @{@"transferObject":[NSKeyedArchiver archivedDataWithRootObject:@{@"__class" :@"WBSendMessageToWeiboRequest", @"message":message, @"requestID" :uuid}]},
                             @{@"userInfo":[NSKeyedArchiver archivedDataWithRootObject:@{}]},
                             @{@"app":[NSKeyedArchiver archivedDataWithRootObject:
                                       @{ @"appKey" : appKey,
                                          @"bundleID" : [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleIdentifier"]}]}
                             ];
    [UIPasteboard generalPasteboard].items = messageData;
    
    callback(@[@{@"uuid" : uuid}]);
}
//  分享QQ
RCT_EXPORT_METHOD(shareToQQWithLogo:(NSString *)logo type:(NSString *)type callback:(RCTResponseSenderBlock)callback){
    self.isShare = YES;
    
    logo = [logo stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    
    UIImage *img;
    // 网络图片
    if ([logo hasPrefix: @"http://"] || [logo hasPrefix: @"https://"]) {
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:logo]]];
    } else {  // 本地图片
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfFile:logo]];
    }
    
    
    // 压缩缩略图
    NSDictionary *previewimagedata = @{@"previewimagedata":[self thumbDataWithImg: img]};
    
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] initWithDictionary:previewimagedata];
    
    // 分享图片
    if ([type integerValue] == 1) {
        dic[@"file_data"] = UIImageJPEGRepresentation(img, 1);
    }
    
    NSData *data=[NSKeyedArchiver archivedDataWithRootObject:dic];
    [[UIPasteboard generalPasteboard] setData:data forPasteboardType:@"com.tencent.mqq.api.apiLargeData"];
    
    callback(@[@{@"thirdAppDisplayName" : [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleName"]}]);
}

//  分享微信
RCT_EXPORT_METHOD(shareToWeixinWithInfo:(NSDictionary *)info appid:(NSString *)appid logo:(NSString *)logo type:(NSString *)type callback:(RCTResponseSenderBlock)callback) {
    self.isShare = YES;
    
    logo = [logo stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    
    NSMutableDictionary *dic=[[NSMutableDictionary alloc] initWithDictionary:info];
    
    UIImage *img;
    if ([logo hasPrefix: @"http://"] || [logo hasPrefix: @"https://"]) {  // 网络图片
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:logo]]];
    } else { // 本地图片
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfFile:logo]];
    }
    
    // 发图片
    if ([type integerValue] == 1) {
        dic[@"fileData"] = UIImageJPEGRepresentation(img, 1);
    }
    
    // 压缩缩略图
    dic[@"thumbData"] = [self thumbDataWithImg: img];
    
    NSData *output=[NSPropertyListSerialization dataWithPropertyList:@{appid:dic} format:NSPropertyListBinaryFormat_v1_0 options:0 error:nil];
    [[UIPasteboard generalPasteboard] setData:output forPasteboardType:@"content"];
    
    callback(@[]);
}

// 分享微信小程序
RCT_EXPORT_METHOD(shareToWeixinMiniWithInfo:(NSDictionary *)info appid:(NSString *)appid logo:(NSString *)logo callback:(RCTResponseSenderBlock)callback) {
    self.isShare = YES;
    
    logo = [logo stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    
    NSMutableDictionary *dic=[[NSMutableDictionary alloc] initWithDictionary:info];
    
    UIImage *img;
    if ([logo hasPrefix: @"http://"] || [logo hasPrefix: @"https://"]) {  // 网络图片
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:logo]]];
    } else { // 本地图片
        img = [[UIImage alloc] initWithData:[NSData dataWithContentsOfFile:logo]];
    }
    
    // 压缩缩略图
    dic[@"thumbData"] = [self thumbDataWithImg: img];
    
    NSData *output=[NSPropertyListSerialization dataWithPropertyList:@{appid:dic} format:NSPropertyListBinaryFormat_v1_0 options:0 error:nil];
    [[UIPasteboard generalPasteboard] setData:output forPasteboardType:@"content"];
    
    callback(@[]);
}

// 压缩缩略图
-(NSData *)thumbDataWithImg: (UIImage *)img {
    CGSize size = CGSizeMake((int)(200 * img.size.width / img.size.height), 200);
    UIGraphicsBeginImageContext(size);
    [img drawInRect:CGRectMake(0,0, size.width, size.height)];
    
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    CGFloat maxFileSize = 32*1024; // 微信缩略图最大32k
    CGFloat compression = 0.9f;
    CGFloat maxCompression = 0.1f;
    NSData *thumbData = UIImageJPEGRepresentation(scaledImage, compression);
    while ([thumbData length] > maxFileSize && compression > maxCompression) {
        compression -= 0.1;
        thumbData = UIImageJPEGRepresentation(scaledImage, compression);
    }
    return thumbData;
}


#pragma mark-登录-

//  QQ登录
RCT_EXPORT_METHOD(qqLoginAppID:(NSString *)appid callback:(RCTResponseSenderBlock)callback) {
    NSDictionary *authData = @{
                               @"app_id" : appid,
                               @"app_name" : [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleName"],
                               //@"bundleid":[self CFBundleIdentifier],//或者有，或者正确(和后台配置一致)，建议不填写。
                               @"client_id" : appid,
                               @"response_type" : @"token",
                               @"scope" : @"get_user_info",//@"get_user_info,get_simple_userinfo,add_album,add_idol,add_one_blog,add_pic_t,add_share,add_topic,check_page_fans,del_idol,del_t,get_fanslist,get_idollist,get_info,get_other_info,get_repost_list,list_album,upload_pic,get_vip_info,get_vip_rich_info,get_intimate_friends_weibo,match_nick_tips_weibo",
                               @"sdkp" :@"i",
                               @"sdkv" : @"2.9",
                               @"status_machine" : [[UIDevice currentDevice] model],
                               @"status_os" : [[UIDevice currentDevice] systemVersion],
                               @"status_version" : [[UIDevice currentDevice] systemVersion]};
    
    NSData *data = [NSKeyedArchiver archivedDataWithRootObject:authData];
    [[UIPasteboard generalPasteboard] setData:data forPasteboardType:[@"com.tencent.tencent" stringByAppendingString:appid]];
    
    callback(@[]);
}

//  微博登录
RCT_EXPORT_METHOD(weiboLoginAppKey:(NSString *)appKey callBack:(RCTResponseSenderBlock)callback) {
    NSString *uuid=[[NSUUID UUID] UUIDString];
    NSArray *authData=@[
                        @{@"transferObject" : [NSKeyedArchiver archivedDataWithRootObject:
                                               @{@"__class" :@"WBAuthorizeRequest",
                                                 @"redirectURI":@"http://sina.com",
                                                 @"requestID" :uuid,
                                                 @"scope": @"all"}]},
                        @{@"userInfo":[NSKeyedArchiver archivedDataWithRootObject:
                                       @{ @"mykey":@"as you like",
                                          @"SSO_From" : @"SendMessageToWeiboViewController"}]},
                        @{@"app":[NSKeyedArchiver archivedDataWithRootObject:
                                  @{@"appKey" : appKey,
                                    @"bundleID" : [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleIdentifier"],
                                    @"name" :[[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleName"]}]}];
    [UIPasteboard generalPasteboard].items=authData;
    
    callback(@[@{@"uuid" : uuid}]);
}


#pragma mark-回调处理-

- (void)handleOpenURL:(NSNotification *)aNotificatio {
    // 将剪切板内容copy，防止剪切板被清空
    self.pasteboardContent = [[UIPasteboard generalPasteboard] dataForPasteboardType:@"content"];
}

RCT_EXPORT_METHOD(handleOpenURL:(NSString *)returnedURL appID:(NSString *)appID callBack:(RCTResponseSenderBlock)callback) {
    
    NSURL* url=[NSURL URLWithString:returnedURL];
    NSDictionary *result = nil;
    if ([url.scheme hasPrefix:@"wx"]) {
        result = [self Weixin_handleOpenURL:url andAppID:appID];
    } else if ([url.scheme hasPrefix:@"wb"]) {
        result = [self Weibo_handleOpenURL:url];
    } else if ([url.scheme hasPrefix:@"QQ"]) {
        result = [self QQShare_handleOpenURL:url];
    } else if([url.scheme hasPrefix:@"tencent"]) {
        result = [self QQAuth_handleOpenURL:url andAppID:appID];
        
    } else {
        
    }
    
    self.isShare = NO;
    callback(@[result]);
}

//  微信回调处理
-(NSDictionary *)Weixin_handleOpenURL:(NSURL *)url andAppID:(NSString *)appID {
    NSDictionary *retDic=[NSPropertyListSerialization propertyListWithData:self.pasteboardContent?:[[NSData alloc] init] options:0 format:0 error:nil][appID];
    
    NSString *type = self.isShare ? @"share" : @"login";
    
    if ([url.absoluteString rangeOfString:@"://oauth"].location != NSNotFound) {
        // 登录成功
        return @{@"result" : @YES, @"type": type, @"title" : @"微信登录成功"};
    } else {
        if (retDic[@"state"] && [retDic[@"state"] isEqualToString:@"Weixinauth"] && [retDic[@"result"] intValue] != 0) {
            // 登录失败
            return @{@"result" : @NO, @"type": type, @"title" : @"微信登录失败"};
        }else if([retDic[@"result"] intValue] == 0){
            // 分享成功
            return @{@"result" : @YES, @"type": type, @"title" : @"微信分享成功"};
        }else{
            // 分享失败
            return @{@"result" : @NO, @"type": type, @"title" : @"微信分享失败"};
        }
    }
}

//  微博回调处理
-(NSDictionary *)Weibo_handleOpenURL:(NSURL *)url {
    NSArray *items = [UIPasteboard generalPasteboard].items;
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithCapacity:items.count];
    for (NSDictionary *item in items) {
        for (NSString *k in item) {
            ret[k]=[k isEqualToString:@"sdkVersion"]?item[k]:[NSKeyedUnarchiver unarchiveObjectWithData:item[k]];
        }
    }
    NSDictionary *transferObject = ret[@"transferObject"];
    if ([transferObject[@"__class"] isEqualToString:@"WBAuthorizeResponse"]) {
        //auth
        if ([transferObject[@"statusCode"] intValue] == 0) {
            return @{@"result" : @YES, @"type": @"login", @"title" : @"微博登录成功"};
        }else{
            return @{@"result" : @NO, @"type": @"login", @"title" : @"微博登录失败"};
        }
    }else if ([transferObject[@"__class"] isEqualToString:@"WBSendMessageToWeiboResponse"]) {
        //分享回调
        if ([transferObject[@"statusCode"] intValue] == 0) {
            return @{@"result" : @YES, @"type": @"share", @"title" : @"微博分享成功"};
        }else{
            return @{@"result" : @NO, @"type": @"share", @"title" : @"微博分享失败"};
        }
    } else {
        return @{};
    }
}

//  QQ auth回调处理
- (NSDictionary *)QQAuth_handleOpenURL:(NSURL *)url andAppID:(NSString *)appID {
    NSData *data = [[UIPasteboard generalPasteboard] dataForPasteboardType:[@"com.tencent.tencent" stringByAppendingString:appID]];
    NSMutableDictionary *ret = [NSMutableDictionary dictionaryWithDictionary:[NSKeyedUnarchiver unarchiveObjectWithData:data]];
    
    if (ret[@"ret"] && [ret[@"ret"] intValue] == 0) {
        return @{@"result" : @YES, @"type": @"login", @"title": @"QQ登录成功"};
    }else{
        return @{@"result" : @NO, @"type": @"login", @"title": @"QQ登录失败"};
    }
}
//  QQ 分享回调处理
- (NSDictionary *)QQShare_handleOpenURL:(NSURL *)url {
    NSDictionary *dic = [self parseUrl:url];
    if (dic[@"error_description"]) {
        [dic setValue:[self base64Decode:dic[@"error_description"]] forKey:@"error_description"];
    }
    if ([dic[@"error"] intValue] != 0) {
        return @{@"result" : @NO, @"type": @"share", @"title" : @"QQ分享失败"};
    }else {
        return @{@"result" : @YES, @"type": @"share", @"title" : @"QQ分享成功"};
    }
}

//  解析url信息
- (NSMutableDictionary *)parseUrl:(NSURL*)url{
    NSMutableDictionary *queryStringDictionary = [[NSMutableDictionary alloc] init];
    NSArray *urlComponents = [[url query] componentsSeparatedByString:@"&"];
    
    for (NSString *keyValuePair in urlComponents)
    {
        NSRange range=[keyValuePair rangeOfString:@"="];
        [queryStringDictionary setObject:range.length>0?[keyValuePair substringFromIndex:range.location+1]:@"" forKey:(range.length?[keyValuePair substringToIndex:range.location]:keyValuePair)];
    }
    return queryStringDictionary;
}

//  base64解码
-(NSString*)base64Decode:(NSString *)input{
    return [[NSString alloc ] initWithData:[[NSData alloc] initWithBase64EncodedString:input options:0] encoding:NSUTF8StringEncoding];
}

@end
