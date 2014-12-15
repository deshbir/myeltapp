//
//  RearViewController.h
//  MyELT
//
//  Created by Preeti Gupta on 14/11/14.
//
//

#import <UIKit/UIKit.h>

@interface SideMenuController : UIViewController <UITableViewDelegate, UITableViewDataSource>{
    NSArray *items;
    NSMutableArray *languages;    
    int currentSelectedItem;
}

@property (nonatomic, retain) IBOutlet UITableView *tableView;

@end
