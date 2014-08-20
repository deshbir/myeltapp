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
    // Do any additional setup after loading the view from its nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)login:(id)sender
{
    MainViewController *mainVC = [[MainViewController alloc] init];
    [self presentViewController:mainVC animated:NO completion:nil];
}

@end
