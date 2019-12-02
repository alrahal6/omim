package com.mapsrahal.maps.downloader;

import androidx.annotation.NonNull;

import com.mapsrahal.util.statistics.Statistics;

public enum DownloaderPromoBannerStats
{
  NO_PROMO
      {
        @NonNull
        @Override
        public String getValue()
        {
          throw new UnsupportedOperationException("Unsupported here");
        }
      },
  CATALOG
      {
        @NonNull
        @Override
        public String getValue()
        {
          return Statistics.ParamValue.MAPSME_GUIDES;
        }
      },
  MEGAFON
      {
        @NonNull
        @Override
        public String getValue()
        {
          return Statistics.ParamValue.MEGAFON;
        }
      };

  @NonNull
  public abstract String getValue();
}
