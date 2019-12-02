#include "com/mapsrahal/maps/Framework.hpp"

#include "com/mapsrahal/util/crashlytics.h"

#include "com/mapsrahal/platform/GuiThread.hpp"
#include "com/mapsrahal/platform/Platform.hpp"

#include "com/mapsrahal/core/jni_helper.hpp"

crashlytics_context_t * g_crashlytics;

extern "C"

// @UiThread
// static void nativeInitCrashlytics();
JNIEXPORT void JNICALL
Java_com_mapsrahal_maps_analytics_ExternalLibrariesMediator_nativeInitCrashlytics(JNIEnv * env, jclass clazz)
{
  ASSERT(!g_crashlytics, ());
  g_crashlytics = crashlytics_init();
}
