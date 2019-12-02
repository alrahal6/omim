package com.mapsrahal.util.sharing;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.mapsrahal.maps.Framework;
import com.mapsrahal.maps.R;
import com.mapsrahal.maps.bookmarks.data.MapObject;
import com.mapsrahal.maps.widget.placepage.Sponsored;

import java.util.Arrays;

class MapObjectShareable extends BookmarkInfoShareable<MapObject>
{
  MapObjectShareable(Activity context, @NonNull MapObject mapObject, @Nullable Sponsored sponsored)
  {
    super(context, mapObject, sponsored);

    if (MapObject.isOfType(MapObject.MY_POSITION, mapObject))
    {
      setSubject(R.string.my_position_share_email_subject);
      String text = makeMyPositionEmailBodyContent();
      setText(text);
    }
  }

  @NonNull
  private String makeMyPositionEmailBodyContent()
  {
    return getActivity().getString(R.string.my_position_share_email,
                                   Framework.nativeGetAddress(getProvider().getLat(),
                                                              getProvider().getLon()),
                                   getGeoUrl(), getHttpUrl());
  }

  @NonNull
  @Override
  protected Iterable<String> getEmailBodyContent()
  {
    return Arrays.asList(getProvider().getName(), getProvider().getSubtitle(), getProvider().getAddress(),
                         getGeoUrl(), getHttpUrl());
  }
}
