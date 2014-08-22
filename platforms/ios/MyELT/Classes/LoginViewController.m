//
//  LoginViewController.m
//  MyELT
//
//  Created by Preeti Gupta on 20/08/14.
//
//

#import "LoginViewController.h"
#import "MainViewController.h"

@interface LoginViewController ()

@property (nonatomic, weak) IBOutlet UITextField *userName;
@property (nonatomic, weak) IBOutlet UITextField *password;

@end

@implementation LoginViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
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

- (void)switchToMainPage
{
    MainViewController *mainVC = [[MainViewController alloc] initWithUserName:self.userName.text password:self.password.text];
    [self presentViewController:mainVC animated:NO completion:nil];
}



@end
