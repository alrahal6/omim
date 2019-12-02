#pragma once

#include "com/mapsrahal/core/jni_helper.hpp"

namespace promo
{
struct CityGallery;

jobject MakeCityGallery(JNIEnv * env, promo::CityGallery const & gallery);
}  // namespace promo
