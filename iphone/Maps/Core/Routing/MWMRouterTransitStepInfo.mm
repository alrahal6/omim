#import "MWMRouterTransitStepInfo.h"

#include "map/routing_manager.hpp"

namespace
{
MWMRouterTransitType convertType(TransitType type)
{
  switch (type)
  {
  case TransitType::IntermediatePoint: return MWMRouterTransitTypeIntermediatePoint;
  case TransitType::Pedestrian: return MWMRouterTransitTypePedestrian;
  case TransitType::Subway: return MWMRouterTransitTypeSubway;
  case TransitType::Train: return MWMRouterTransitTypeTrain;
  case TransitType::LightRail: return MWMRouterTransitTypeLightRail;
  case TransitType::Monorail: return MWMRouterTransitTypeMonorail;
  }
}

UIColor * convertColor(uint32_t colorARGB)
{
  CGFloat const alpha = CGFloat((colorARGB >> 24) & 0xFF) / 255;
  CGFloat const red = CGFloat((colorARGB >> 16) & 0xFF) / 255;
  CGFloat const green = CGFloat((colorARGB >> 8) & 0xFF) / 255;
  CGFloat const blue = CGFloat(colorARGB & 0xFF) / 255;
  return [UIColor colorWithRed:red green:green blue:blue alpha:alpha];
}
}  // namespace

@interface MWMRouterTransitStepInfo ()

@property(nonatomic, readwrite) MWMRouterTransitType type;
@property(copy, nonatomic, readwrite) NSString * distance;
@property(copy, nonatomic, readwrite) NSString * distanceUnits;
@property(copy, nonatomic, readwrite) NSString * number;
@property(nonatomic, readwrite) UIColor * color;
@property(nonatomic, readwrite) NSInteger intermediateIndex;

@end

@implementation MWMRouterTransitStepInfo

- (instancetype)initWithStepInfo:(TransitStepInfo const &)info
{
  self = [super init];
  if (self)
  {
    _type = convertType(info.m_type);
    _distance = @(info.m_distanceStr.c_str());
    _distanceUnits = @(info.m_distanceUnitsSuffix.c_str());
    _number = @(info.m_number.c_str());
    _color = convertColor(info.m_colorARGB);
    _intermediateIndex = info.m_intermediateIndex;
  }
  return self;
}

@end
