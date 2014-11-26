//
//  MyELTWrapperViewController.m
//  MyELT
//
//  Created by Preeti Gupta on 25/11/14.
//
//

#import "MyELTWrapperViewController.h"
#import "LoginViewController.h"

@interface MyELTWrapperViewController ()

@end

@implementation MyELTWrapperViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

//function to return to HomePage
- (IBAction)home:(id)sender
{
    UIWebView *webview = [[[[self.body subviews] objectAtIndex:0] subviews] objectAtIndex:0];
    NSString *homepageURL = [NSString stringWithFormat:@"%@/ilrn/course/course.do?isNative=true", SERVER_URL, nil];
    NSString *javaScript = [NSString stringWithFormat:@"startMyELT('%@')", homepageURL, nil];
    [webview stringByEvaluatingJavaScriptFromString:javaScript];
    
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
