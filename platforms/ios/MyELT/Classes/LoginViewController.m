//
//  LoginViewController.m
//  MyELT
//
//  Created by Preeti Gupta on 20/08/14.
//
//

#import "LoginViewController.h"
#import "MyELTViewController.h"
#import "MBProgressHUD.h"
#import "AppDelegate.h"

@interface LoginViewController ()

@property (nonatomic, weak) IBOutlet UITextField *userName;
@property (nonatomic, weak) IBOutlet UITextField *password;
@property (nonatomic, weak) IBOutlet UIImageView *appIcon;
@property (nonatomic) NSURLSession *session;
@property (nonatomic, assign) MBProgressHUD* activityIndicator;
@property (nonatomic) NSArray *verticalConstraintsLand;
@property (nonatomic) NSArray *verticalConstraintsPort;
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
    
    //Initialize constraints for Iphone
    if ( UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad )
    {
        NSDictionary *nameMap = @{@"userName":_userName, @"appIcon":_appIcon};
        _verticalConstraintsLand = [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-20-[appIcon]-10-[userName]" options:0 metrics:nil      views:nameMap];
        _verticalConstraintsPort = [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-75-[appIcon]-40-[userName]" options:0 metrics:nil views:nameMap];
    }
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
                             AppDelegate* appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];                             
                             [appDelegate initMyELTViewWithUserName:self.userName.text password:self.password.text];
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


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation {
    return YES;
}

- (void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    //Update constraints for Iphone
    if ( UI_USER_INTERFACE_IDIOM() != UIUserInterfaceIdiomPad )
    {
      
        if (toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft ||
            toInterfaceOrientation == UIInterfaceOrientationLandscapeRight)
        {
            
            [self.view removeConstraints:_verticalConstraintsPort];
            [self.view addConstraints:_verticalConstraintsLand];
                                            
        }
        else if (toInterfaceOrientation == UIInterfaceOrientationPortrait ||
                 toInterfaceOrientation == UIInterfaceOrientationPortraitUpsideDown)
        {
            [self.view removeConstraints:_verticalConstraintsLand];
            [self.view addConstraints:_verticalConstraintsPort];
        }
    }
}



@end
