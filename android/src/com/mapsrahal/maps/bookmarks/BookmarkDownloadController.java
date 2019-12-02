package com.mapsrahal.maps.bookmarks;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.mapsrahal.maps.base.Detachable;
import com.mapsrahal.maps.base.Savable;

public interface BookmarkDownloadController extends Detachable<BookmarkDownloadCallback>,
                                                    Savable<Bundle>
{
  boolean downloadBookmark(@NonNull String url);
  void retryDownloadBookmark();
}
