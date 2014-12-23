//
//  RearViewController.h
//  MyELT
//
//  Created by Preeti Gupta on 14/11/14.
//
//

#import <UIKit/UIKit.h>

@interface SideMenuController : UIViewController <UITableViewDelegate, UITableViewDataSource>{
    NSMutableArray *items;
    NSArray *languages;
    int currentExpandedIndex;
    NSIndexPath *lastLanguagesIndexPath;
    NSIndexPath *lastTopIndexPath;
    NSIndexPath *lastSettingsIndexPath;
    NSString *imageName;
    UIImage *image;
    NSArray *itemsInSection;
    NSString *itemString;
    BOOL homeCell;
}

@property (nonatomic, retain) IBOutlet UITableView *tableView;

@end
