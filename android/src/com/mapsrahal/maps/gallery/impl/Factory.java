package com.mapsrahal.maps.gallery.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapsrahal.maps.R;
import com.mapsrahal.maps.discovery.LocalExpert;
import com.mapsrahal.maps.gallery.Constants;
import com.mapsrahal.maps.gallery.GalleryAdapter;
import com.mapsrahal.maps.gallery.ItemSelectedListener;
import com.mapsrahal.maps.gallery.Items;
import com.mapsrahal.maps.promo.PromoCityGallery;
import com.mapsrahal.maps.promo.PromoEntity;
import com.mapsrahal.maps.search.SearchResult;
import com.mapsrahal.maps.widget.placepage.PlacePageView;
import com.mapsrahal.util.statistics.GalleryPlacement;
import com.mapsrahal.util.statistics.GalleryState;
import com.mapsrahal.util.statistics.GalleryType;
import com.mapsrahal.util.statistics.Statistics;

import java.util.List;

import static com.mapsrahal.util.statistics.GalleryState.OFFLINE;
import static com.mapsrahal.util.statistics.GalleryState.ONLINE;
import static com.mapsrahal.util.statistics.GalleryType.LOCAL_EXPERTS;

public class Factory
{
  @NonNull
  public static GalleryAdapter createSearchBasedAdapter(@NonNull SearchResult[] results,
                                                        @Nullable ItemSelectedListener<Items
                                                            .SearchItem> listener,
                                                        @NonNull GalleryType type,
                                                        @NonNull GalleryPlacement placement,
                                                        @Nullable Items.MoreSearchItem item)
  {
    trackProductGalleryShownOrError(results, type, OFFLINE, placement);
    return new GalleryAdapter<>(new SearchBasedAdapterStrategy(results, item, listener));
  }

  @NonNull
  public static GalleryAdapter createSearchBasedLoadingAdapter()
  {
    return new GalleryAdapter<>(new SimpleLoadingAdapterStrategy(null));
  }

  @NonNull
  public static GalleryAdapter createSearchBasedErrorAdapter()
  {
    return new GalleryAdapter<>(new SimpleErrorAdapterStrategy(null));
  }

  @NonNull
  public static GalleryAdapter createHotelAdapter(@NonNull SearchResult[] results,
                                                  @Nullable ItemSelectedListener<Items
                                                      .SearchItem> listener,
                                                  @NonNull GalleryType type,
                                                  @NonNull GalleryPlacement placement)
  {
    trackProductGalleryShownOrError(results, type, OFFLINE, placement);
    return new GalleryAdapter<>(new HotelAdapterStrategy(results, listener));
  }

  @NonNull
  public static GalleryAdapter createLocalExpertsAdapter(@NonNull LocalExpert[] experts,
                                                         @Nullable String expertsUrl,
                                                         @Nullable ItemSelectedListener<Items
                                                             .LocalExpertItem> listener,
                                                         @NonNull GalleryPlacement placement)
  {
    trackProductGalleryShownOrError(experts, LOCAL_EXPERTS, ONLINE, placement);
    return new GalleryAdapter<>(new LocalExpertsAdapterStrategy(experts, expertsUrl, listener));
  }

  @NonNull
  public static GalleryAdapter createLocalExpertsLoadingAdapter()
  {
    return new GalleryAdapter<>(new LocalExpertsLoadingAdapterStrategy(null));
  }

  @NonNull
  public static GalleryAdapter createLocalExpertsErrorAdapter()
  {
    return new GalleryAdapter<>(new LocalExpertsErrorAdapterStrategy(null));
  }

  @NonNull
  public static GalleryAdapter createCatalogPromoAdapter(@NonNull Context context,
                                                         @NonNull PromoCityGallery gallery,
                                                         @Nullable String url,
                                                         @Nullable ItemSelectedListener<PromoEntity> listener,
                                                         @NonNull GalleryPlacement placement)
  {
    @SuppressWarnings("ConstantConditions")
    PromoEntity item = new PromoEntity(Constants.TYPE_MORE,
                                       context.getString(R.string.placepage_more_button),
                                       null, url, null, null);
    List<PromoEntity> entities = PlacePageView.toEntities(gallery);
    CatalogPromoAdapterStrategy strategy = new CatalogPromoAdapterStrategy(entities,
                                                                           item,
                                                                           listener);
    trackProductGalleryShownOrError(gallery.getItems(), GalleryType.PROMO, ONLINE, placement);
    return new GalleryAdapter<>(strategy);
  }

  @NonNull
  public static GalleryAdapter createCatalogPromoLoadingAdapter()
  {
    CatalogPromoLoadingAdapterStrategy strategy = new CatalogPromoLoadingAdapterStrategy(null, null);
    return new GalleryAdapter<>(strategy);
  }

  @NonNull
  public static GalleryAdapter createCatalogPromoErrorAdapter(@Nullable ItemSelectedListener<Items.Item> listener)
  {
    return new GalleryAdapter<>(new CatalogPromoErrorAdapterStrategy(listener));
  }

  private static <Product> void trackProductGalleryShownOrError(@NonNull Product[] products,
                                                                @NonNull GalleryType type,
                                                                @NonNull GalleryState state,
                                                                @NonNull GalleryPlacement placement)
  {
    if (products.length == 0)
      Statistics.INSTANCE.trackGalleryError(type, placement, Statistics.ParamValue.NO_PRODUCTS);
    else
      Statistics.INSTANCE.trackGalleryShown(type, state, placement, products.length);
  }
}
