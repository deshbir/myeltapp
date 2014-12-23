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
        items = [NSMutableArray new];
        [items addObject:[self topItems]];
        [items addObject:[self settings]];
        languages = [NSArray new];
        currentExpandedIndex = -1;
        languages = [self languages];
        homeCell = YES;
    }
    return self;
}

#pragma mark - Data generators


- (NSArray *)topItems {
    NSMutableArray *itemsArray = [NSMutableArray array];
    [itemsArray addObject:[NSString stringWithFormat:@"Home"]];
    [itemsArray addObject:[NSString stringWithFormat:@"Profile"]];
    [itemsArray addObject:[NSString stringWithFormat:@"Messages"]];
    [itemsArray addObject:[NSString stringWithFormat:@"Help"]];
    return itemsArray;
}
- (NSArray *)settings {
    NSMutableArray *settingsArray = [NSMutableArray array];
    [settingsArray addObject:[NSString stringWithFormat:@"Languages"]];
    [settingsArray addObject:[NSString stringWithFormat:@"Sign Out"]];
    return settingsArray;
}
- (NSArray *)languages {
    NSMutableArray *languagesArray = [NSMutableArray array];
    [languagesArray addObject:[NSString stringWithFormat:@"English"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Spanish"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Portugese"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Japanese"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Korean"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Chinese"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Chinese Traditional"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Vietnamese"]];
    [languagesArray addObject:[NSString stringWithFormat:@"Arabic"]];
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
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [[items objectAtIndex:section] count]+ ((currentExpandedIndex>-1 && section==1)?[languages count] : 0);
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section{
    if(section == 0){
        return nil;
    }else{
        return @"Settings";
    }
}



- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *ParentCellIdentifier = @"ParentCell";
    static NSString *ChildCellIdentifier = @"ChildCell";
    
    BOOL isChild =
    currentExpandedIndex > -1 && indexPath.section == 1
    && indexPath.row > currentExpandedIndex
    && indexPath.row <= currentExpandedIndex + [languages count];
    
    UITableViewCell *cell;
    if(!isChild){
         cell = [tableView dequeueReusableCellWithIdentifier:ParentCellIdentifier];
    }
    if (cell == nil) {
        if(isChild){
            cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle reuseIdentifier:ChildCellIdentifier];
        }else{
            cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:ParentCellIdentifier];
        }
        cell.selectionStyle = UITableViewCellSelectionStyleNone;
    }
    
    if (isChild) {
        cell.textLabel.text = [languages objectAtIndex:indexPath.row - currentExpandedIndex - 1 ] ;
        
        if(lastLanguagesIndexPath != nil && lastLanguagesIndexPath.row == indexPath.row){
            cell.backgroundColor = [UIColor lightGrayColor];
        }
    }else{
        itemsInSection = [items objectAtIndex:indexPath.section];
        itemString = [itemsInSection objectAtIndex:indexPath.row];
        CGSize imageSize = CGSizeMake(30, 30);
        UIGraphicsBeginImageContext(imageSize);
        CGRect imageRect = CGRectMake(0, 0, imageSize.width, imageSize.height);
        
        if(indexPath.row == 0 && indexPath.section == 0){
            if(homeCell){
                lastTopIndexPath = indexPath;
                [cell setBackgroundColor:[UIColor lightGrayColor]];
                homeCell = NO;
            }
            imageName = @"home128.png";
        }else if(indexPath.row == 1 && indexPath.section == 0){
            imageName = @"profile128.png";
        }else if(indexPath.row == 2 && indexPath.section == 0){
            imageName = @"messages128.png";
        }else if(indexPath.row == 3 && indexPath.section == 0){
            imageName = @"help128.png";
        }else if(indexPath.row == 0 && indexPath.section == 1){
            imageName = @"languages128.png";
        }else if(indexPath.row == 1 && indexPath.section == 1){
            imageName = @"logout128.png";
        }
        
        image=[UIImage imageNamed:[NSString stringWithString:imageName]];
        [image drawInRect:imageRect];
        cell.imageView.image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        cell.textLabel.text = itemString;
    }
    return cell;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    BOOL isChild =
    currentExpandedIndex > -1 && indexPath.section == 1
    && indexPath.row > currentExpandedIndex
    && indexPath.row <= currentExpandedIndex + [languages count];
    
    
    if(indexPath.section == 0){
        [[self.tableView cellForRowAtIndexPath:lastTopIndexPath] setBackgroundColor:[UIColor clearColor]];
        [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
        lastTopIndexPath = indexPath;
        if(lastSettingsIndexPath != nil){
             [[self.tableView cellForRowAtIndexPath:lastSettingsIndexPath] setBackgroundColor:[UIColor lightGrayColor]];
        }
        
    }
    if(indexPath.section == 1){
        [[self.tableView cellForRowAtIndexPath:lastSettingsIndexPath] setBackgroundColor:[UIColor clearColor]];
        [[self.tableView cellForRowAtIndexPath:lastTopIndexPath] setBackgroundColor:[UIColor lightGrayColor]];
        [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
        lastSettingsIndexPath = indexPath;
        
    }
    AppDelegate* appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    if(isChild){
        [[self.tableView cellForRowAtIndexPath:lastLanguagesIndexPath]setBackgroundColor:[UIColor clearColor]];
        if(indexPath.row == 1){
            lastLanguagesIndexPath = indexPath;
            [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"0"] forKey:@"savedLocale"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate changeLocaleNative:@"0"];
        }else if(indexPath.row == 2){
            lastLanguagesIndexPath = indexPath;
            [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"2"] forKey:@"savedLocale"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate changeLocaleNative:@"2"];
        }else if(indexPath.row == 3){
            lastLanguagesIndexPath = indexPath;
            [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"5"] forKey:@"savedLocale"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate changeLocaleNative:@"5"];
        }else if(indexPath.row == 4){
            lastLanguagesIndexPath = indexPath;
            [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"3"] forKey:@"savedLocale"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate changeLocaleNative:@"3"];
        }else if(indexPath.row == 5){
            lastLanguagesIndexPath = indexPath;
            [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"4"] forKey:@"savedLocale"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate changeLocaleNative:@"4"];
        }else if(indexPath.row == 6){
            lastLanguagesIndexPath = indexPath;
            [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"6"] forKey:@"savedLocale"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate changeLocaleNative:@"6"];
        }else if(indexPath.row == 7){
            lastLanguagesIndexPath = indexPath;
            [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"7"] forKey:@"savedLocale"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate changeLocaleNative:@"7"];
        }else if(indexPath.row == 8){
            lastLanguagesIndexPath = indexPath;
            [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"9"] forKey:@"savedLocale"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate changeLocaleNative:@"9"];
        }else if(indexPath.row == 9) {
            lastLanguagesIndexPath = indexPath;
            [[self.tableView cellForRowAtIndexPath:indexPath] setBackgroundColor:[UIColor lightGrayColor]];
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"8"] forKey:@"savedLocale"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate changeLocaleNative:@"8"];
        }
        
        return;
    }
    
    [self.tableView beginUpdates];
    
    if (currentExpandedIndex == indexPath.row && indexPath.section == 1) {
        [self collapselanguagesAtIndex:currentExpandedIndex];
        currentExpandedIndex = -1;
    }else{
        if(indexPath.section == 0 && indexPath.row == 0){
            [appDelegate loadUrlInWebView:@"/ilrn/course/course.do"];
        }else if (indexPath.section == 0 && indexPath.row == 1){
            [appDelegate loadUrlInWebView:@"/ilrn/global/changeAccount.do"];
        }else if (indexPath.section == 0 && indexPath.row == 2){
            [appDelegate loadUrlInWebView:@"/ilrn/global/announcements.do"];
        }else if (indexPath.section == 0 && indexPath.row == 3){
            [appDelegate loadUrlInWebView:@"/ilrn/global/myeltHelp.do"];
        }else if (indexPath.section == 1 && indexPath.row == 0){
            currentExpandedIndex = indexPath.row;
            [self expandItemAtIndex:currentExpandedIndex];
        }else{
            [[NSUserDefaults standardUserDefaults] setObject:[NSKeyedArchiver archivedDataWithRootObject:@"true"] forKey:@"firstLogin"];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [appDelegate loadUrlInWebView:@"/ilrn/accounts/logout.do"];
        }
    }
    [self.tableView endUpdates];
}

- (void)expandItemAtIndex:(int)index {
    NSMutableArray *indexPaths = [NSMutableArray new];
    int insertPos = index + 1;
    for (int i = 0; i < [[self languages] count]; i++) {
        [indexPaths addObject:[NSIndexPath indexPathForRow:insertPos++ inSection:1]];
    }
    [self.tableView insertRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
    [self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:index inSection:1] atScrollPosition:UITableViewScrollPositionTop animated:YES];
}

- (void)collapselanguagesAtIndex:(int)index {
    NSMutableArray *indexPaths = [NSMutableArray new];
    for (int i = index + 1; i <= index + [[self languages] count]; i++) {
        [indexPaths addObject:[NSIndexPath indexPathForRow:i inSection:1]];
    }
    [self.tableView deleteRowsAtIndexPaths:indexPaths withRowAnimation:UITableViewRowAnimationFade];
}


@end
