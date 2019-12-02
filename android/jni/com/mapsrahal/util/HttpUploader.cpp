#include <jni.h>

#include "com/mapsrahal/core/ScopedEnv.hpp"
#include "com/mapsrahal/core/ScopedLocalRef.hpp"
#include "com/mapsrahal/core/jni_helper.hpp"

#include "platform/http_uploader.hpp"

#include "base/assert.hpp"

#include <cstdint>
#include <functional>

#include "private.h"

namespace
{
platform::HttpUploader::Result ToNativeResult(JNIEnv * env, jobject const src)
{
  static jmethodID const getHttpCode =
      env->GetMethodID(g_httpUploaderResultClazz, "getHttpCode", "()I");
  static jmethodID const getDescription =
      env->GetMethodID(g_httpUploaderResultClazz, "getDescription", "()Ljava/lang/String;");

  platform::HttpUploader::Result result;

  result.m_httpCode = static_cast<int32_t>(env->CallIntMethod(src, getHttpCode));

  jni::ScopedLocalRef<jstring> const description(
      env, static_cast<jstring>(env->CallObjectMethod(src, getDescription)));
  result.m_description = jni::ToNativeString(env, description.get());

  return result;
}
}  // namespace

namespace platform
{
HttpUploader::Result HttpUploader::Upload() const
{
  auto env = jni::GetEnv();

  CHECK(env, ());

  static jmethodID const httpUploaderConstructor =
      jni::GetConstructorID(env, g_httpUploaderClazz,
                            "(Ljava/lang/String;Ljava/lang/String;"
                            "[Lcom/mapsrahal/util/KeyValue;"
                            "[Lcom/mapsrahal/util/KeyValue;"
                            "Ljava/lang/String;Ljava/lang/String;Z)V");
  HttpPayload const payload = GetPayload();
  jni::ScopedLocalRef<jstring> const method(env, jni::ToJavaString(env, payload.m_method));
  jni::ScopedLocalRef<jstring> const url(env, jni::ToJavaString(env, payload.m_url));
  jni::ScopedLocalRef<jobjectArray> const params(env, jni::ToKeyValueArray(env, payload.m_params));
  jni::ScopedLocalRef<jobjectArray> const headers(env,
                                                  jni::ToKeyValueArray(env, payload.m_headers));
  jni::ScopedLocalRef<jstring> const fileKey(env, jni::ToJavaString(env, payload.m_fileKey));
  jni::ScopedLocalRef<jstring> const filePath(env, jni::ToJavaString(env, payload.m_filePath));

  jni::ScopedLocalRef<jobject> const httpUploaderObject(
      env, env->NewObject(g_httpUploaderClazz, httpUploaderConstructor, method.get(), url.get(),
                          params.get(), headers.get(), fileKey.get(), filePath.get(),
                          static_cast<jboolean>(payload.m_needClientAuth)));

  static jmethodID const uploadId = jni::GetMethodID(env, httpUploaderObject, "upload",
                                                     "()Lcom/mapsrahal/util/HttpUploader$Result;");

  jni::ScopedLocalRef<jobject> const result(env,
                                            env->CallObjectMethod(httpUploaderObject, uploadId));

  if (jni::HandleJavaException(env))
  {
    Result invalidResult;
    invalidResult.m_httpCode = -1;
    invalidResult.m_description = "Unhandled exception during upload is encountered!";
    return invalidResult;
  }

  return ToNativeResult(env, result);
}
}  // namespace platform

extern "C"
{
  JNIEXPORT jstring JNICALL
  Java_com_mapsrahal_util_HttpUploader_nativeUserBindingCertificate(JNIEnv * env, jclass)
  {
    return jni::ToJavaString(env, USER_BINDING_PKCS12);
  }

  JNIEXPORT jstring JNICALL
  Java_com_mapsrahal_util_HttpUploader_nativeUserBindingPassword(JNIEnv * env, jclass)
  {
    return jni::ToJavaString(env, USER_BINDING_PKCS12_PASSWORD);
  }
}
