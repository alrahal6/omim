package com.mapsrahal.maps.editor;

import androidx.annotation.NonNull;

import com.mapsrahal.maps.base.BaseMwmRecyclerFragment;

public class CuisineFragment extends BaseMwmRecyclerFragment<CuisineAdapter>
{
  private CuisineAdapter mAdapter;

  @NonNull
  @Override
  protected CuisineAdapter createAdapter()
  {
    mAdapter = new CuisineAdapter();
    return mAdapter;
  }

  @NonNull
  public String[] getCuisines()
  {
    return mAdapter.getCuisines();
  }

  public void setFilter(String filter)
  {
    mAdapter.setFilter(filter);
  }
}
