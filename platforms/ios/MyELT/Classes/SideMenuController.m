//
//  RearViewController.m
//  MyELT
//
//  Created by Preeti Gupta on 14/11/14.
//
//

#import "SideMenuController.h"
#import "LoginViewController.h"
#import "MyELTWrapperViewController.h"
#import "MyELTViewController.h"
#import "AppDelegate.h"

@interface SideMenuController ()
{
     NSInteger _presentedRow;
}

@end

@implementation SideMenuController

@synthesize tableView = _tableView;

- (id)init {
    self = [super init];
    
    if (self) {
        items = [[NSArray alloc] initWithArray:[self items]];
        languages = [NSMutableArray new];
        currentSelectedItem = -1;
        [languages addObject:[self languages]];
        
    }
    return self;
}

#pragma mark - Data generators

- (NSArray *)items {
    NSMutableArray *itemsArray = [NSMutableArray array];
    [itemsArray addObject:[NSString stringWithFormat:@"Languages"]];
    [itemsArray addObject:[NSString stringWithFormat:@"Help"]];
    [itemsArray addObject:[NSString stringWithFormat:@"Sign Out"]];
    return itemsArray;
}

- (NSArray *)languages {
    NSMutableArray *languagesArray = [NSMutableArray array];
    [languagesArray addObject:[NSString stringWithFormat:@"English"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Portugese"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Spanish"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Japanese"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Korean"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Chinese"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Traditional Chinese"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Arabic"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Vietnamese"]];
    return languagesArray;
}

#pragma mark - View management

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)viewWillAppear:(BOOL)animated
{
    self.navigationController.navigationBar.hidden = YES;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [items count] + ((currentSelectedItem > -1) ? [[languages objectAtIndex:currentSelectedItem] count] : 0);
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *ParentCellIdentifier = @"ParentCell";
    static NSString *ChildCellIdentifier = @"ChildCell";
    
    BOOL isChild =
    currentSelectedItem > -1
    && indexPath.row > currentSelectedItem
    && indexPath.row <= currentSelectedItem + [[languages objectAtIndex:currentSelectedItem] count];
    
    UITableViewCell *cell;
    
    if (isChild) {
        cell = [tableView dequeueReusableCellWithIdentifier:ChildCellIdentifier];
    }
    else {
        cell = [tableView dequeueReusableCellWithIdentifier:ParentCellIdentifier];
       
    }
    
    
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:ParentCellIdentifier];
    }
    
    if (isChild) {
        cell.detailTextLabel.text = [[languages objectAtIndex:currentSelectedItem] objectAtIndex:indexPath.row - currentSelectedItem - 1];
    }
    else {
        int topIndex = (currentSelectedItem > -1 && indexPath.row > currentSelectedItem)
        ? indexPath.row - [[languages objectAtIndex:currentSelectedItem] count]
        : indexPath.row;
        
        cell.textLabel.text = [items objectAtIndex:topIndex];
        if(topIndex == 0){
            cell.imageView.image = [UIImage imageNamed:[NSString stringWithFormat:@"languages128.png"]];
        }else if(topIndex == 1){
            cell.imageView.image = [UIImage imageNamed:[NSString stringWithFormat:@"help128.png"]];
        }else{
            cell.imageView.image = [UIImage imageNamed:[NSString stringWithFormat:@"logout128.png"]];
        }
        
    }
    
    return cell;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    BOOL isChild =
    currentSelectedItem > -1
    && indexPath.row > currentSelectedItem
    && indexPath.row <= currentSelectedItem + [[languages objectAtIndex:currentSelectedItem] count];
    
    if (isChild) {
        NSLog(@"%ld" , (long)indexPath.row);
        NSLog(@"A child was tapped, do what you will with it");
        return;
    }
    
    [self.tableView beginUpdates];
    
    if (currentSelectedItem == indexPath.row) {
        [self collapselanguagesAtIndex:currentSelectedItem];
        currentSelectedItem = -1;
    }
    else {
        BOOL shouldCollapse = currentSelectedItem > -1;
        if (shouldCollapse && indexPath.row == 0) {
            [self collapselanguagesAtIndex:currentSelectedItem];
        }
        if(indexPath.row == 0){
        currentSelectedItem = (shouldCollapse && indexPath.row > currentSelectedItem) ? indexPath.row - [[languages objectAtIndex:currentSelectedItem] count] : indexPath.row;
        
            [self expandItemAtIndex:currentSelectedItem];
        }else if(indexPath.row == 1){
            AppDelegate* appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
            [appDelegate loadHelpPage];
          
        }
    }
    
    [self.tableView endUpdates];
    
}

- (void)expandItemAtIndex:(int)index {
    NSMutableArray *indexPaths = [NSMutableArray new];
    NSArray *currentlanguages = [languages objectAtIndex:index];
    int insertPos = index + 1;
    for (int i = 0; i < [currentlanguages count]; i++) {
        [indexPaths addObject:[NSIndexPath indexPathForRow:insertPos++ inSection:0]];
    }
    [self.tableView insertRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
    [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:index inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:YES];
    
}

- (void)collapselanguagesAtIndex:(int)index {
    NSMutableArray *indexPaths = [NSMutableArray new];
    for (int i = index + 1; i <= index + [[languages objectAtIndex:index] count]; i++) {
        [indexPaths addObject:[NSIndexPath indexPathForRow:i inSection:0]];
    }
    [self.tableView deleteRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
    
}


@end
