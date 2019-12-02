package com.mapsrahal.maps.editor;

import android.content.Intent;
import androidx.fragment.app.Fragment;

import com.mapsrahal.maps.base.BaseMwmFragmentActivity;
import com.mapsrahal.maps.editor.data.FeatureCategory;

public class FeatureCategoryActivity extends BaseMwmFragmentActivity implements FeatureCategoryFragment.FeatureCategoryListener
{
  public static final String EXTRA_FEATURE_CATEGORY = "FeatureCategory";

  @Override
  protected Class<? extends Fragment> getFragmentClass()
  {
    return FeatureCategoryFragment.class;
  }

  @Override
  public void onFeatureCategorySelected(FeatureCategory category)
  {
    Editor.createMapObject(category);
    final Intent intent = new Intent(this, EditorActivity.class);
    intent.putExtra(EXTRA_FEATURE_CATEGORY, category);
    intent.putExtra(EditorActivity.EXTRA_NEW_OBJECT, true);
    startActivity(intent);
  }
}
