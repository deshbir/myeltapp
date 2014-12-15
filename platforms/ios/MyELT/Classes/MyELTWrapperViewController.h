//
//  MyELTWrapperViewController.h
//  MyELT
//
//  Created by Preeti Gupta on 25/11/14.
//
//

#import <UIKit/UIKit.h>

@interface MyELTWrapperViewController : UIViewController

@property(nonatomic,strong) IBOutlet UIView* header;
@property(nonatomic,strong) IBOutlet UIView* body;

- (void)loadUrlInWebView:(NSString*) url;

@end
