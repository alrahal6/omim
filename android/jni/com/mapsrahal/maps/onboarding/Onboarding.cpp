#include "com/mapsrahal/core/jni_helper.hpp"

#include "map/onboarding.hpp"

extern "C"
{
JNIEXPORT jobject JNICALL
Java_com_mapsrahal_maps_onboarding_OnboardingTip_nativeGetTip(JNIEnv * env, jclass)
{
  if (!onboarding::CanShowTipButton())
    return nullptr;

  static jclass g_tipClass =
      jni::GetGlobalClassRef(env, "com/mapsrahal/maps/onboarding/OnboardingTip");
  static jmethodID g_tipConstructor =
      jni::GetConstructorID(env, g_tipClass, "(ILjava/lang/String;)V");

  auto const tip = onboarding::GetTip();

  jni::TScopedLocalRef url(env, jni::ToJavaString(env, tip.m_url));

  return env->NewObject(g_tipClass, g_tipConstructor, static_cast<jint>(tip.m_type), url.get());
}
}  // extern "C"
