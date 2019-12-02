package com.mapsrahal.maps.intent;

import androidx.annotation.NonNull;

import com.mapsrahal.maps.MwmActivity;

import java.io.Serializable;

public interface MapTask extends Serializable
{
  boolean run(@NonNull MwmActivity target);
}
