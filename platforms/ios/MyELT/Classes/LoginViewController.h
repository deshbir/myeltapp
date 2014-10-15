//
//  LoginViewController.h
//  MyELT
//
//  Created by Preeti Gupta on 20/08/14.
//
//

#import <UIKit/UIKit.h>

@interface LoginViewController : UIViewController

//MyELT Server URL for HTTP/REST calls
extern NSString const * SERVER_URL;

- (void)hideLoader;

@end
