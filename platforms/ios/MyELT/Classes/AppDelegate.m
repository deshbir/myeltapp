

/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

//
//  AppDelegate.m
//  MyELT
//
//  Created by ___FULLUSERNAME___ on ___DATE___.
//  Copyright ___ORGANIZATIONNAME___ ___YEAR___. All rights reserved.
//

#import "AppDelegate.h"
#import "MyELTViewController.h"
#import "LoginViewController.h"

#import <Cordova/CDVPlugin.h>

@implementation AppDelegate

@synthesize window, viewController, loginVC, myeltVC, myeltWrapperVC;

- (id)init
{
    /** If you need to do any extra app-specific initialization, you can do it here
     *  -jm
     **/
    NSHTTPCookieStorage* cookieStorage = [NSHTTPCookieStorage sharedHTTPCookieStorage];

    [cookieStorage setCookieAcceptPolicy:NSHTTPCookieAcceptPolicyAlways];

    int cacheSizeMemory = 8 * 1024 * 1024; // 8MB
    int cacheSizeDisk = 32 * 1024 * 1024; // 32MB
#if __has_feature(objc_arc)
        NSURLCache* sharedCache = [[NSURLCache alloc] initWithMemoryCapacity:cacheSizeMemory diskCapacity:cacheSizeDisk diskPath:@"nsurlcache"];
#else
        NSURLCache* sharedCache = [[[NSURLCache alloc] initWithMemoryCapacity:cacheSizeMemory diskCapacity:cacheSizeDisk diskPath:@"nsurlcache"] autorelease];
#endif
    [NSURLCache setSharedURLCache:sharedCache];

    self = [super init];
    return self;
}

#pragma mark UIApplicationDelegate implementation

/**
 * This is main kick off after the app inits, the views and Settings are setup here. (preferred - iOS4 and up)
 */
- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions
{
    CGRect screenBounds = [[UIScreen mainScreen] bounds];

#if __has_feature(objc_arc)
        self.window = [[UIWindow alloc] initWithFrame:screenBounds];
#else
        self.window = [[[UIWindow alloc] initWithFrame:screenBounds] autorelease];
#endif
    self.window.autoresizesSubviews = YES;

    // Set your app's start page by setting the <content src='foo.html' /> tag in config.xml.
    // If necessary, uncomment the line below to override it.
    // self.viewController.startPage = @"index.html";

    // NOTE: To customize the view's frame size (which defaults to full screen), override
    // [self.viewController viewWillAppear:] in your view controller.
    
    loginVC = [[LoginViewController alloc] init];
    
    [self.window setRootViewController:loginVC];
    [self.window makeKeyAndVisible];
    
    return YES;
}

//Initializes MyELT view in background
- (void)initMyELTViewWithUserName:(NSString*)userName password:(NSString*)password
{
    myeltWrapperVC = [[MyELTWrapperViewController alloc] init];
    [self.window addSubview:myeltWrapperVC.view];
    [self.window sendSubviewToBack:myeltWrapperVC.view];

    myeltVC = [[MyELTViewController alloc] initWithUserName:userName password:password];
    myeltVC.view.frame = myeltWrapperVC.body.bounds;
    [myeltWrapperVC.body addSubview:myeltVC.view];
}

//Show MyELT view and hide Login View
- (void)showMyELTView
{
    CGRect screenRect = [[UIScreen mainScreen] bounds];
    myeltWrapperVC.view.frame = screenRect;
    [loginVC hideLoader];
    dispatch_async(dispatch_get_main_queue(), ^{
        [loginVC.view removeFromSuperview];
        [self.window setRootViewController:myeltWrapperVC];
    });
}

//Show Login view and hide MyELT View
- (void)showLoginView
{
    [myeltVC.view removeFromSuperview];
    [self.window addSubview:loginVC.view];
    [self.window setRootViewController:loginVC];
}



// this happens while we are running ( in the background, or from within our own app )
// only valid if MyELT-Info.plist specifies a protocol to handle
- (BOOL)application:(UIApplication*)application openURL:(NSURL*)url sourceApplication:(NSString*)sourceApplication annotation:(id)annotation
{
    if (!url) {
        return NO;
    }

    // calls into javascript global function 'handleOpenURL'
    NSString* jsString = [NSString stringWithFormat:@"handleOpenURL(\"%@\");", url];
    [self.viewController.webView stringByEvaluatingJavaScriptFromString:jsString];

    // all plugins will get the notification, and their handlers will be called
    //[[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:CDVPluginHandleOpenURLNotification object:url]];

    return YES;
}

// repost all remote and local notification using the default NSNotificationCenter so multiple plugins may respond
//- (void)            application:(UIApplication*)application
//    didReceiveLocalNotification:(UILocalNotification*)notification
//{
//    // re-post ( broadcast )
//    [[NSNotificationCenter defaultCenter] postNotificationName:CDVLocalNotification object:notification];
//}

//- (void)                                application:(UIApplication *)application
//   didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
//{
//    // re-post ( broadcast )
//    NSString* token = [[[[deviceToken description]
//                         stringByReplacingOccurrencesOfString: @"<" withString: @""]
//                        stringByReplacingOccurrencesOfString: @">" withString: @""]
//                       stringByReplacingOccurrencesOfString: @" " withString: @""];
//
//    [[NSNotificationCenter defaultCenter] postNotificationName:CDVRemoteNotification object:token];
//}
//
//- (void)                                 application:(UIApplication *)application
//    didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
//{
//    // re-post ( broadcast )
//    [[NSNotificationCenter defaultCenter] postNotificationName:CDVRemoteNotificationError object:error];
//}

- (NSUInteger)application:(UIApplication*)application supportedInterfaceOrientationsForWindow:(UIWindow*)window
{
    // iPhone doesn't support upside down by default, while the iPad does.  Override to allow all orientations always, and let the root view controller decide what's allowed (the supported orientations mask gets intersected).
    NSUInteger supportedInterfaceOrientations = (1 << UIInterfaceOrientationPortrait) | (1 << UIInterfaceOrientationLandscapeLeft) | (1 << UIInterfaceOrientationLandscapeRight) | (1 << UIInterfaceOrientationPortraitUpsideDown);

    return supportedInterfaceOrientations;
}

- (void)applicationDidReceiveMemoryWarning:(UIApplication*)application
{
    [[NSURLCache sharedURLCache] removeAllCachedResponses];
}

@end
