#import "DeepLinkRouteStrategyAdapter.h"
#import <CoreApi/Framework.h>
#import <Crashlytics/Crashlytics.h>
#import "MWMCoreRouterType.h"
#import "MWMRoutePoint+CPP.h"

@implementation DeepLinkRouteStrategyAdapter

- (instancetype)init:(NSURL *)url {
  self = [super init];
  if (self) {
    auto const parsedData = GetFramework().GetParsedRoutingData();
    auto const points = parsedData.m_points;

    if (points.size() == 2) {
      _p1 = [[MWMRoutePoint alloc] initWithURLSchemeRoutePoint:points.front()
                                                          type:MWMRoutePointTypeStart
                                             intermediateIndex:0];
      _p2 = [[MWMRoutePoint alloc] initWithURLSchemeRoutePoint:points.back()
                                                          type:MWMRoutePointTypeFinish
                                             intermediateIndex:0];
      _type = routerType(parsedData.m_type);
    } else {
      NSError *err = [[NSError alloc] initWithDomain:kMapsmeErrorDomain
                                                code:5
                                            userInfo:@{@"Description": @"Invalid number of route points", @"URL": url}];
      [[Crashlytics sharedInstance] recordError:err];
      return nil;
    }
  }
  return self;
}

@end
