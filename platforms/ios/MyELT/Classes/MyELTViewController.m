//
//  MyELTViewController.m
//  MyELT
//
//  Created by Preeti Gupta on 04/11/14.
//
//

#import "MyELTViewController.h"
#import "LoginViewController.h"
#import "JavaScriptCore/JavaScriptCore.h"
#import "AppDelegate.h"

@interface MyELTViewController ()
{
    NSString *_username;
    NSString *_password;
}

- (void)setUsername:(NSString *)str;
- (NSString *)Username;

- (void)setPassword:(NSString *)str;
- (NSString *)Password;

@end

@implementation MyELTViewController

- (id)initWithNibName:(NSString*)nibNameOrNil bundle:(NSBundle*)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Uncomment to override the CDVCommandDelegateImpl used
        // _commandDelegate = [[MainCommandDelegate alloc] initWithViewController:self];
        // Uncomment to override the CDVCommandQueue used
        // _commandQueue = [[MainCommandQueue alloc] initWithViewController:self];
    }
    return self;
}

- (instancetype)initWithUserName:(NSString*)usernameStr password:(NSString*)passwordStr
{
    self = [super init];
    if (self) {
        // Uncomment to override the CDVCommandDelegateImpl used
        // _commandDelegate = [[MainCommandDelegate alloc] initWithViewController:self];
        // Uncomment to override the CDVCommandQueue used
        // _commandQueue = [[MainCommandQueue alloc] initWithViewController:self];
        _username = usernameStr;
        _password = passwordStr;
    }
    return self;
}

- (id)init
{
    self = [super init];
    if (self) {
        // Uncomment to override the CDVCommandDelegateImpl used
        // _commandDelegate = [[MainCommandDelegate alloc] initWithViewController:self];
        // Uncomment to override the CDVCommandQueue used
        // _commandQueue = [[MainCommandQueue alloc] initWithViewController:self];
    }
    return self;
}

- (void)setUsername:(NSString *)str {
    _username = str;
}

- (NSString *)Username {
    return _username;
}

- (void)setPassword:(NSString *)str {
    _password = str;
}

- (NSString *)Password {
    return _password;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark View lifecycle

- (void)viewWillAppear:(BOOL)animated
{
    // View defaults to full size.  If you want to customize the view's size, or its subviews (e.g. webView),
    // you can do so here.
    
    [super viewWillAppear:animated];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return [super shouldAutorotateToInterfaceOrientation:interfaceOrientation];
}

/* Comment out the block below to over-ride */

/*
 - (UIWebView*) newCordovaViewWithFrame:(CGRect)bounds
 {
 return[super newCordovaViewWithFrame:bounds];
 }
 */

#pragma mark UIWebDelegate implementation

- (void)webViewDidFinishLoad:(UIWebView*)theWebView
{
    // Black base color for background matches the native apps
    theWebView.backgroundColor = [UIColor blackColor];
    
    /*********************************************************************************************
     * When web view finishes loading, call startMyELT function of javascript to load MyELT iframe.
     *********************************************************************************************/
    
    NSString *myeltURL = [NSString stringWithFormat:@"%@/ilrn/global/extlogin.do?u=%@&p=%@&isNative=true", SERVER_URL, [self Username], [self Password], nil];
    NSString *javaScript = [NSString stringWithFormat:@"startMyELT('%@')", myeltURL, nil];
    [theWebView stringByEvaluatingJavaScriptFromString:javaScript];
    
    /***************************************************
     * Add jsContext to call native code/functions from Javascript
     ***************************************************/
    JSContext *jsContext =  [theWebView valueForKeyPath:@"documentView.webView.mainFrame.javaScriptContext"];
    
    //This function is called from javascript when user logs out from MyELT
    jsContext[@"NativeShowLoginView"] = ^() {
        AppDelegate* appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
        [appDelegate showLoginView];
        
    };
    
    //This function is called from javascript when user logs out from MyELT
    jsContext[@"NativeShowMyELTView"] = ^() {
        AppDelegate* appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
        [appDelegate showMyELTView];
    };
    
    
    
    return [super webViewDidFinishLoad:theWebView];
}

/* Comment out the block below to over-ride */

/*
 
 - (void) webViewDidStartLoad:(UIWebView*)theWebView
 {
 return [super webViewDidStartLoad:theWebView];
 }
 
 - (void) webView:(UIWebView*)theWebView didFailLoadWithError:(NSError*)error
 {
 return [super webView:theWebView didFailLoadWithError:error];
 }
 
 - (BOOL) webView:(UIWebView*)theWebView shouldStartLoadWithRequest:(NSURLRequest*)request navigationType:(UIWebViewNavigationType)navigationType
 {
 return [super webView:theWebView shouldStartLoadWithRequest:request navigationType:navigationType];
 }
 */

@end

@implementation MainCommandDelegate

/* To override the methods, uncomment the line in the init function(s)
 in MainViewController.m
 */

#pragma mark CDVCommandDelegate implementation

- (id)getCommandInstance:(NSString*)className
{
    return [super getCommandInstance:className];
}

/*
 NOTE: this will only inspect execute calls coming explicitly from native plugins,
 not the commandQueue (from JavaScript). To see execute calls from JavaScript, see
 MainCommandQueue below
 */
- (BOOL)execute:(CDVInvokedUrlCommand*)command
{
    return [super execute:command];
}

- (NSString*)pathForResource:(NSString*)resourcepath;
{
    return [super pathForResource:resourcepath];
}

@end

@implementation MainCommandQueue

/* To override, uncomment the line in the init function(s)
 in MainViewController.m
 */
- (BOOL)execute:(CDVInvokedUrlCommand*)command
{
    return [super execute:command];
}

@end
