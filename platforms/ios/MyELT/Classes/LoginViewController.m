//
//  LoginViewController.m
//  MyELT
//
//  Created by Preeti Gupta on 20/08/14.
//
//

#import "LoginViewController.h"
#import "MainViewController.h"
#import "MBProgressHUD.h"

@interface LoginViewController ()

@property (nonatomic, weak) IBOutlet UITextField *userName;
@property (nonatomic, weak) IBOutlet UITextField *password;
@property (nonatomic) NSURLSession *session;
@property (nonatomic, assign) MBProgressHUD* activityIndicator;

@end

@implementation LoginViewController

NSString const * SERVER_URL = @"http://myelt3.comprotechnologies.com";

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        NSURLSessionConfiguration *config =
        [NSURLSessionConfiguration defaultSessionConfiguration];
        
        //Initialize session with default configuration
        _session = [NSURLSession sessionWithConfiguration:config
                                                 delegate:nil
                                            delegateQueue:nil];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    UIView *usernamePaddingView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 8, 8)];
    [self userName].leftViewMode = UITextFieldViewModeAlways;
    [self userName].leftView = usernamePaddingView;
    
    UIView *passwordPaddingView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 8, 8)];
    [self password].leftViewMode = UITextFieldViewModeAlways;
    [self password].leftView = passwordPaddingView;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if (textField == self.password) {
        [textField resignFirstResponder];
        [self switchToMainPage];
    } else if (textField == self.userName) {
        [self.password becomeFirstResponder];
    }
    return YES;
}

- (IBAction)login:(id)sender
{
    [self switchToMainPage];
}

- (void)showLoader
{
    self.activityIndicator = nil;
    self.activityIndicator = [MBProgressHUD showHUDAddedTo:self.view.superview animated:YES];
    self.activityIndicator.mode = MBProgressHUDModeIndeterminate;
    self.activityIndicator.labelText = @"Loading";
}

- (void)hideLoader
{
   [self.activityIndicator hide:YES];
}

- (void)switchToMainPage
{
    [self showLoader];
    
    NSString *requestURLString = [NSString stringWithFormat:@"%@/ilrn/api/logincheck?u=%@&p=%@", SERVER_URL, self.userName.text, self.password.text];
    
    NSURL *requestURL = [NSURL URLWithString:requestURLString];
    
    NSURLRequest *loginRequest = [NSURLRequest requestWithURL:requestURL];
    
    NSURLSessionDataTask *dataTask =
    [self.session dataTaskWithRequest:loginRequest
                    completionHandler:
     ^(NSData *data, NSURLResponse *response, NSError *error) {
         //Run this snippet on main UI thread
         dispatch_async(dispatch_get_main_queue(), ^{
             if (error) {
                [self showAlert:@"Something went wrong. Please try again later." title:@"Error"];
             }
             else {
                 if ([data length] > 0) {
                     
                     NSInteger statusCode = [(NSHTTPURLResponse *)response statusCode];
                     
                     if (statusCode != 200) {
                         [self showAlert:@"Something went wrong. Please try again later." title:@"Error"];
                     } else {
                         NSDictionary *jsonObject = [NSJSONSerialization JSONObjectWithData:data                                                                                options:0 error:nil];
                         
                         NSDictionary *responseJSON  = jsonObject[@"response"];
                         NSString *responseStatus  = responseJSON[@"status"];
                         
                         if ([responseStatus isEqual:@"success"]) {
                             MainViewController *mainVC = [[MainViewController alloc] initWithUserName:self.userName.text password:self.password.text];
                             [self presentViewController:mainVC animated:NO completion:nil];
                             [self hideLoader];
                         } else {
                             [self showAlert:@"Incorrect Username or Password." title:@"Error"];
                         }
                     }
                     
                 } else {
                     [self showAlert:@"Something went wrong. Please try again later." title:@"Error"];
                 }
             }
         });
     }];
    [dataTask resume];
}

- (void)showAlert:(NSString *)messageStr title:(NSString *)titleStr
{
    [self hideLoader];
    
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:titleStr
                                                    message:messageStr
                                                   delegate:self
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
}




@end
