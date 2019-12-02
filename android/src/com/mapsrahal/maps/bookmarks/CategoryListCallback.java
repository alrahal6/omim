package com.mapsrahal.maps.bookmarks;

import androidx.annotation.NonNull;

import com.mapsrahal.maps.bookmarks.data.BookmarkCategory;

interface CategoryListCallback
{
  void onFooterClick();

  void onMoreOperationClick(@NonNull BookmarkCategory item);
}
