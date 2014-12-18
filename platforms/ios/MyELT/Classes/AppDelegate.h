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
//  AppDelegate.h
//  MyELT
//
//  Created by ___FULLUSERNAME___ on ___DATE___.
//  Copyright ___ORGANIZATIONNAME___ ___YEAR___. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "LoginViewController.h"
#import "MyELTViewController.h"
#import "MyELTWrapperViewController.h"
#import "SideMenuController.h"
#import "MFSideMenu.h"

@interface AppDelegate : NSObject <UIApplicationDelegate>{}

// invoke string is passed to your app on launch, this is only valid if you
// edit MyELT-Info.plist to add a protocol
// a simple tutorial can be found here :
// http://iphonedevelopertips.com/cocoa/launching-your-own-application-via-a-custom-url-scheme.html

//UIWIndow Object
@property (nonatomic, strong) IBOutlet UIWindow* window;

//ViewController for Login Page
@property (nonatomic, strong) LoginViewController* loginVC;

//ViewController for MyELT Cordova view(MyELT is launched inside this VC)
@property (nonatomic, strong) MyELTViewController* myeltVC;

//Top ViewController containing fixed header and MyELT Cordova view
@property (nonatomic, strong) MyELTWrapperViewController* myeltWrapperVC;

//ViewController for Side Menu
@property (nonatomic, strong) SideMenuController* sideMenuVC;

//ViewController containing both myeltWrapperVC and sideMenuVC. This is needed for side menu functionality.
@property (nonatomic, strong) MFSideMenuContainerViewController* layoutContainer;

- (void)showMyELTView;
- (void)showLoginView;
- (void)initLayoutForSideMenu;
- (void)toggleSideMenu;
- (void)loadUrlInWebView:(NSString*) url;
- (void)changeLocaleNative:(NSString*) locale;
- (void)initMyELTViewWithUserName:(NSString*)userName password:(NSString*)password;

@end
