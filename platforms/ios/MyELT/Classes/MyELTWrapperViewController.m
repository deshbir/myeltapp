//
//  MyELTWrapperViewController.m
//  MyELT
//
//  Created by Preeti Gupta on 25/11/14.
//
//

#import "MyELTWrapperViewController.h"
#import "LoginViewController.h"
#import "SideMenuController.h"
#import "AppDelegate.h"

@interface MyELTWrapperViewController ()

@end

@implementation MyELTWrapperViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Do any additional setup after loading the view from its nib.
}

//Hide navigation bar
- (void)viewWillAppear:(BOOL)animated
{
    self.navigationController.navigationBar.hidden = YES;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

//Function to Load HomePage
- (IBAction)home:(id)sender
{
    [self loadUrlInWebView:@"/ilrn/course/course.do"];
}

//Function to change URL in WebView
- (void)loadUrlInWebView:(NSString*) url{
    UIWebView *webview = [[[[self.body subviews] objectAtIndex:0] subviews] objectAtIndex:0];
    NSString *URL = [NSString stringWithFormat:@"%@%@?isNative=true", SERVER_URL, url, nil];
    NSString *javaScript = [NSString stringWithFormat:@"startMyELT('%@')", URL, nil];
    [webview stringByEvaluatingJavaScriptFromString:javaScript];
}
- (void)changeLocaleNative:(NSString*) locale{
    UIWebView *webview = [[[[self.body subviews] objectAtIndex:0] subviews] objectAtIndex:0];
    NSString *javaScript = [NSString stringWithFormat:@"changeLocaleNative('%@')", locale, nil];
    [webview stringByEvaluatingJavaScriptFromString:javaScript];
}

//Function to toggle side menu
- (IBAction)toggleSideMenu :(id)sender
{
    AppDelegate* appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    [appDelegate toggleSideMenu];
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
