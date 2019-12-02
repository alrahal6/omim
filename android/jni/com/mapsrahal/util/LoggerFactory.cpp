#include <jni.h>
#include "com/mapsrahal/core/logging.hpp"

extern "C" {
JNIEXPORT void JNICALL
Java_com_mapsrahal_util_log_LoggerFactory_nativeToggleCoreDebugLogs(
    JNIEnv * /*env*/, jclass /*clazz*/, jboolean enabled)
{
  jni::ToggleDebugLogs(enabled);
}
}  // extern "C"
