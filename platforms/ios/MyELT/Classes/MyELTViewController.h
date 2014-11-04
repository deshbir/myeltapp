//
//  MyELTViewController.h
//  MyELT
//
//  Created by Preeti Gupta on 04/11/14.
//
//


#import <Cordova/CDVViewController.h>
#import <Cordova/CDVCommandDelegateImpl.h>
#import <Cordova/CDVCommandQueue.h>

@interface MyELTViewController : CDVViewController
- (id)initWithUserName:(NSString*)usernameStr password:(NSString*)passwordStr;

@end

@interface MainCommandDelegate : CDVCommandDelegateImpl
@end

@interface MainCommandQueue : CDVCommandQueue
@end
