#pragma once

#include "com/mapsrahal/core/jni_helper.hpp"

#include "map/everywhere_search_callback.hpp"

#include "search/result.hpp"

#include <vector>

jobjectArray BuildSearchResults(search::Results const & results,
                                std::vector<search::ProductInfo> const & productInfo,
                                bool hasPosition, double lat, double lon);
