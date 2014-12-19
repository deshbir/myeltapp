

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
#import "MFSideMenu.h"
#import <Cordova/CDVPlugin.h>

@implementation AppDelegate

@synthesize window, loginVC, myeltVC, myeltWrapperVC,sideMenuVC, layoutContainer;

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
    
    //Initialize and Show Login Page/VC on App startup
    [[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleLightContent];
    NSData *_firstLoginData = [[NSUserDefaults standardUserDefaults] objectForKey:@"firstLogin"];
    NSString *firstLogin = [NSKeyedUnarchiver unarchiveObjectWithData:_firstLoginData];
    loginVC = [[LoginViewController alloc] init];
    if ([firstLogin  isEqual: @"false"]) {
        NSData *userNameData = [[NSUserDefaults standardUserDefaults] objectForKey:@"userName"];
        NSString *userName = [NSKeyedUnarchiver unarchiveObjectWithData:userNameData];
        NSData *passwordData = [[NSUserDefaults standardUserDefaults] objectForKey:@"password"];
        NSString *password = [NSKeyedUnarchiver unarchiveObjectWithData:passwordData];
        [self initMyELTViewWithUserName:userName password:password];
        
    }else{
        [self.window setRootViewController:loginVC];
        [self.window makeKeyAndVisible];
        
    }
    
    return YES;
}

//Initializes layoutContainer using MFSideMenu for SideMenu functionality
-(void) initLayoutForSideMenu{
    myeltWrapperVC = [[MyELTWrapperViewController alloc] init];
    sideMenuVC = [[SideMenuController alloc] init];
    UINavigationController *myeltWrapperNavigationController = [[UINavigationController alloc] initWithRootViewController:myeltWrapperVC];
    layoutContainer = [MFSideMenuContainerViewController
                                                    containerWithCenterViewController:myeltWrapperNavigationController
                                                    leftMenuViewController:sideMenuVC
                                                    rightMenuViewController:NULL];
    self.window.rootViewController = layoutContainer;
    [self.window makeKeyAndVisible];
}

//Function to toggle side menu
-(void) toggleSideMenu {
    [layoutContainer toggleLeftSideMenuCompletion:^{}];
}

//Function to load help page
-(void) loadUrlInWebView:(NSString *)url{
    [myeltWrapperVC loadUrlInWebView:url];
}

- (void)changeLocaleNative:(NSString*) locale{
    [myeltWrapperVC changeLocaleNative:locale];
}

//Initializes MyELT CordovaView in background
- (void)initMyELTViewWithUserName:(NSString*)userName password:(NSString*)password
{
    [self initLayoutForSideMenu];
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
    });
}

//Show Login view and hide MyELT View
- (void)showLoginView
{
    [myeltVC.view removeFromSuperview];
    [self.window addSubview:loginVC.view];
    [self.window setRootViewController:loginVC];
}

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
