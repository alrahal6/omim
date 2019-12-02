package com.mapsrahal.maps.auth;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapsrahal.maps.Framework;

interface TokenHandler
{
  boolean checkToken(int requestCode, @NonNull Intent data);

  @Nullable
  String getToken();

  @Framework.AuthTokenType
  int getType();
}
