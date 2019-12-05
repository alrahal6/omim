package com.mapsrahal.maps;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.material.navigation.NavigationView;
import com.mapsrahal.maps.activity.ChatActivity;
import com.mapsrahal.maps.activity.MatchingListActivity;
import com.mapsrahal.maps.activity.ProfileActivity;
import com.mapsrahal.maps.api.ParsedMwmRequest;
import com.mapsrahal.maps.bookmarks.data.MapObject;
import com.mapsrahal.maps.intent.MapTask;
import com.mapsrahal.maps.location.CompassData;
import com.mapsrahal.maps.location.LocationHelper;
import com.mapsrahal.maps.onboarding.OnboardingTip;
import com.mapsrahal.maps.routing.NavigationController;
import com.mapsrahal.maps.routing.RoutingController;
import com.mapsrahal.maps.routing.RoutingInfo;
import com.mapsrahal.maps.routing.RoutingPlanInplaceController;
import com.mapsrahal.maps.search.SearchActivity;
import com.mapsrahal.maps.search.SearchFilterController;
import com.mapsrahal.maps.sound.TtsPlayer;
import com.mapsrahal.maps.taxi.TaxiInfo;
import com.mapsrahal.maps.taxi.TaxiManager;
import com.mapsrahal.maps.widget.menu.MyPositionButton;
import com.mapsrahal.util.DateUtils;
import com.mapsrahal.util.PermissionsUtils;
import com.mapsrahal.util.UiUtils;
import com.mapsrahal.util.sharing.TargetUtils;
import com.mapsrahal.util.statistics.AlohaHelper;
import com.mapsrahal.util.statistics.Statistics;

import java.util.Date;
import java.util.Stack;

public class MapActivity extends AppCompatActivity
                         implements View.OnTouchListener,
                                    MapRenderingListener,
                                    LocationHelper.UiCallback,
                                    RoutingController.Container,
                                    Framework.MapObjectListener,
                                    View.OnClickListener,
                                    NavigationView.OnNavigationItemSelectedListener,
                                    NavigationButtonsAnimationController.OnTranslationChangedListener

{

    public TextView tvDropOff,tvPickup,tvDistance,mDateTime,mRequiredSeats,mSetPickup,mSetDrop;
    public TextView mpPhone;
    private Button btRequest,mMore;
    private boolean mIsTabletLayout = false,isPickupSearch = true,isResultBySearch = false;
    private ImageButton mAddressToggle,mMainMenu;
    private ImageView mAddSeat,mRemoveSeat;
    private Date startingTime;
    private MapObject tempLocation,fromLocation,toLocation;
    LinearLayout mllForm,mMnuForm;
    private int seatCount = 1;
    @Nullable
    private Dialog mLocationErrorDialog;
    private DrawerLayout mDrawerLayout;
    private boolean mLocationErrorDialogAnnoying = false;
    @Nullable
    private MapFragment mMapFragment;
    @Nullable
    private SearchFilterController mFilterController;
    private static final int REQ_CODE_LOCATION_PERMISSION = 1;
    boolean isLaunchByDeepLink = false;
    private String myDistance,mSourceAddress,mDestinationAddress,mAddressToggleStr;
    @Nullable
    private MyPositionButton mNavMyPosition;
    @Nullable
    private NavigationButtonsAnimationController mNavAnimationController;
    //private View myView;
    private RoutingPlanInplaceController mRoutingPlanInplaceController;
    @NonNull
    private final View.OnClickListener mOnMyPositionClickListener = new MapActivity.CurrentPositionClickListener();
    // Map tasks that we run AFTER rendering initialized
    private final Stack<MapTask> mTasks = new Stack<>();
    @SuppressWarnings("NullableProblems")
    @NonNull
    private NavigationController mNavigationController;
    private static final String PASSENGER_CAPTAIN_SELECTOR = "passenger_captain_selector";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_map);
        Intent intent = getIntent();
        int message = intent.getIntExtra(PASSENGER_CAPTAIN_SELECTOR,1);
        mllForm = findViewById(R.id.ll_form);
        tvDropOff = findViewById(R.id.tv_dropoff);
        tvPickup = findViewById(R.id.tv_pickup);
        tvPickup.setOnClickListener(this);
        tvDropOff.setOnClickListener(this);
        mDateTime = findViewById(R.id.date_time);
        startingTime = DateUtils.timePlusFifteen(new Date());
        mDateTime.setText(DateUtils.formatDate(startingTime));
        mDateTime.setOnClickListener(this);
        tvDistance = findViewById(R.id.tv_distance);
        mAddSeat = findViewById(R.id.add_seat);
        mAddSeat.setOnClickListener(this);
        mMnuForm = findViewById(R.id.mnu_form);
        mSetPickup = findViewById(R.id.set_pickup);
        mSetDrop = findViewById(R.id.set_drop);
        mSetPickup.setOnClickListener(this);
        mSetDrop.setOnClickListener(this);
        mRemoveSeat = findViewById(R.id.remove_seat);
        mRemoveSeat.setOnClickListener(this);
        mRequiredSeats = findViewById(R.id.required_seats);
        mMore = findViewById(R.id.more);
        btRequest = findViewById(R.id.bt_request);
        btRequest.setOnClickListener(this);
        mAddressToggle = findViewById(R.id.addressToggle);
        mAddressToggle.setOnClickListener(this);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        mpPhone = headerView.findViewById(R.id.pPhone);
        mpPhone.setText(MySharedPreference.getInstance(getApplicationContext()).getPhoneNumber());
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mMainMenu = findViewById(R.id.mainMenu);
        mMainMenu.setOnClickListener(this);
        Button buttonOpenBottomSheet = findViewById(R.id.more);
        buttonOpenBottomSheet.setOnClickListener(v -> {
            BottomSheetMoreSettings bottomSheet = new BottomSheetMoreSettings();
            bottomSheet.show(getSupportFragmentManager(), "MoreSetting");
        });
        //removeBookmark();
        //Log.d("MAP", "instance id new token is " + FirebaseInstanceId.getInstance().getToken());
        mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.class.getName());
        if (mMapFragment == null)
        {
            Bundle args = new Bundle();
            args.putBoolean(MapFragment.ARG_LAUNCH_BY_DEEP_LINK, isLaunchByDeepLink);
            mMapFragment = (MapFragment) MapFragment.instantiate(this, MapFragment.class.getName(), args);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_fragment_container, mMapFragment, MapFragment.class.getName())
                    .commit();
        }

        View container = findViewById(R.id.map_fragment_container);
        //myView = findViewById(R.id.map_fragment_container);
        if (container != null)
        {
            container.setOnTouchListener(this);
        }
        adjustCompass(0);
        mNavigationController = new NavigationController(this);
        initNavigationButtons();
        //myPositionClick();
        switch (message) {
            case 4:
                hideFromTo();
                onlineAsCaptain();
                break;
            /*default:
                break;*/
        }
    }

    private void listMatch() {
        Intent intent = new Intent(this, MatchingListActivity.class);
        startActivity(intent);
    }

    private void onlineAsCaptain() {
        // todo online as captain
    }

    private void hideFromTo() {
        tvDropOff.setVisibility(View.GONE);
        tvPickup.setVisibility(View.GONE);
        mAddressToggle.setVisibility(View.GONE);
        mllForm.setVisibility(View.GONE);
        btRequest.setVisibility(View.GONE);
        mMore.setVisibility(View.GONE);
    }

    void adjustCompass(int offsetY)
    {
        if (mMapFragment == null || !mMapFragment.isAdded())
            return;

        int resultOffset = offsetY;
        mMapFragment.setupCompass(resultOffset, true);
        CompassData compass = LocationHelper.INSTANCE.getCompassData();
        if (compass != null)
            MapFragment.nativeCompassUpdated(compass.getMagneticNorth(), compass.getTrueNorth(), true);
    }

    private void initNavigationButtons()
    {
        View frame = findViewById(R.id.navigation_buttons);
        if (frame == null)
            return;

        View zoomIn = frame.findViewById(R.id.nav_zoom_in);
        zoomIn.setOnClickListener(this);
        View zoomOut = frame.findViewById(R.id.nav_zoom_out);
        zoomOut.setOnClickListener(this);
        View myPosition = frame.findViewById(R.id.my_position);
        mNavMyPosition = new MyPositionButton(myPosition, mOnMyPositionClickListener);
        //initToggleMapLayerController(frame);
        View openSubsScreenBtnContainer = frame.findViewById(R.id.subs_screen_btn_container);
        boolean hasOnBoardingView = OnboardingTip.get() != null
                && MwmApplication.from(this).isFirstLaunch();
        mNavAnimationController = new NavigationButtonsAnimationController(
                zoomIn, zoomOut, myPosition, getWindow().getDecorView().getRootView(), this,
                hasOnBoardingView ? openSubsScreenBtnContainer : null);
        /*mNavAnimationController = new NavigationButtonsAnimationController(
                zoomIn, zoomOut, myPosition, getWindow().getDecorView().getRootView(), this);
    */
    }


    @Override
    public void onRenderingCreated() {
        //checkMeasurementSystem();
        LocationHelper.INSTANCE.attach(this);
        //setTvPickupText();
        myPositionClick();
    }

    @Override
    public void onRenderingRestored() {
        hideMenu();
        //runTasks();
        //setTvPickupText();
    }

    @Override
    public void onRenderingInitializationFinished() {
        //runTasks();
    }

    public boolean isMapAttached() {
        return mMapFragment != null && mMapFragment.isAdded();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        return mMapFragment != null && mMapFragment.onTouch(view, event);
    }

    @Override
    public void onTranslationChanged(float translation) {
        mNavigationController.updateSearchButtonsTranslation(translation);
    }

    @Override
    public void onFadeInZoomButtons() {
        if (RoutingController.get().isPlanning() || RoutingController.get().isNavigating())
            mNavigationController.fadeInSearchButtons();
    }

    @Override
    public void onFadeOutZoomButtons() {
        if (RoutingController.get().isPlanning() || RoutingController.get().isNavigating())
        {
            if (!UiUtils.isLandscape(this))
                mNavigationController.fadeOutSearchButtons();
        }
    }

    private void saveAndSearchPost() {
        MySharedPreference.getInstance(this).userTripInfo(fromLocation.getLat(),
                fromLocation.getLon(),
                toLocation.getLat(),
                toLocation.getLon(),myDistance,mSourceAddress,mDestinationAddress,
                startingTime);
        listMatch();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.nav_zoom_in:
                MapFragment.nativeScalePlus();
                break;
            case R.id.nav_zoom_out:
                MapFragment.nativeScaleMinus();
                break;
            case R.id.addressToggle:
                toggleAddress();
                break;
            case R.id.mainMenu:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                //mMainMenu.animate().rotation(mMainMenu.getRotation()+360).start();
                break;
            case R.id.bt_request:
                //createPost();
                saveAndSearchPost();
                break;
            case R.id.date_time:
                dateTime();
                break;
            case R.id.add_seat:
                if(seatCount < 4) {
                    seatCount++;
                    setSeat();
                } else {
                    Toast.makeText(this, "Now we allow only 4 seats", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.remove_seat:
                if(seatCount > 1) {
                    seatCount--;
                    setSeat();
                }
                break;
            case R.id.tv_pickup:
                isPickupSearch = true;
                showSearch();
                break;
            case R.id.tv_dropoff:
                isPickupSearch = false;
                showSearch();
                break;
            case R.id.set_pickup:
                setPickup();
                break;
            case R.id.set_drop:
                setDropoff();
                break;
        }
    }

    private void setSeat() {
        mRequiredSeats.setText(""+seatCount);
    }

    private void toggleAddress() {
        if(toLocation != null) {
            mAddressToggleStr = mSourceAddress;
            mSourceAddress = mDestinationAddress;
            mDestinationAddress = mAddressToggleStr;
            tempLocation = fromLocation;
            fromLocation = toLocation;
            toLocation = tempLocation;
            String text1 = tvPickup.getText().toString();
            String text2 = tvDropOff.getText().toString();
            tvPickup.setText(text2);
            tvDropOff.setText(text1);
            mAddressToggle.animate().rotation(mAddressToggle.getRotation() + 360).start();
        }
    }

    @Override
    public void onMapObjectActivated(MapObject object) {
        if (MapObject.isOfType(MapObject.API_POINT, object))
        {
            final ParsedMwmRequest request = ParsedMwmRequest.getCurrentRequest();
            if (request == null)
                return;

            request.setPointData(object.getLat(), object.getLon(), object.getTitle(), object.getApiId());
            object.setSubtitle(request.getCallerName(MwmApplication.get()).toString());
        }
        tempLocation = object;
        if(!isResultBySearch) {
            showMenu();
        } else {
            isResultBySearch = false;
            if(isPickupSearch) {
                setPickup();
            } else {
                setDropoff();
            }
        }
    }

    private void showMenu() {
        mMnuForm.setVisibility(View.VISIBLE);
    }

    private void hideMenu() {
        mMnuForm.setVisibility(View.GONE);
    }

    private void setPickup() {
        //addTestBookMark();
        fromLocation = tempLocation;
        mSourceAddress = fromLocation.getTitle();
        tvPickup.setText(mSourceAddress);
        hideMenu();
        RoutingController.get().setStartPoint(fromLocation);
    }

    private void setDropoff() {
        //removeBookmark();
        toLocation = tempLocation;
        mDestinationAddress = toLocation.getTitle();
        tvDropOff.setText(mDestinationAddress);
        hideMenu();
        RoutingController.get().setEndPoint(toLocation);
    }

    /*private void addTestBookMark() {
        //BookmarkManager.INSTANCE.addNewBookmark(15.619435,15.619435);
        BookmarkManager.INSTANCE.addNewBookmark(15.548887,32.566284);
        BookmarkManager.INSTANCE.addNewBookmark(15.570659,32.564159);
        BookmarkManager.INSTANCE.addNewBookmark(15.642770,32.574570);
    }*/

    private void removeBookmark() {
        Framework.nativeDeleteBookmarkFromMapObject();
    }

    @Override
    public void onDismiss(boolean switchFullScreenMode) { }

    @Override
    public FragmentActivity getActivity() {
        return this;
    }

    @Nullable
    Fragment getFragment(Class<? extends Fragment> clazz) {
        if (!mIsTabletLayout)
            throw new IllegalStateException("Must be called for tablets only!");

        return getSupportFragmentManager().findFragmentByTag(clazz.getName());
    }

    private void showAddFinishFrame() {
        mRoutingPlanInplaceController.showAddFinishFrame();
        return;
    }

    private void adjustBottomWidgets(int offsetY) {
        if (mMapFragment == null || !mMapFragment.isAdded())
            return;
        mMapFragment.setupRuler(offsetY, false);
    }

    private void showLineFrame() {
        adjustBottomWidgets(0);
    }

    private void showAddStartFrame() {
        mRoutingPlanInplaceController.showAddStartFrame();
        return;
    }

    private void showLocationErrorDialog(@NonNull final Intent intent) {
        if (mLocationErrorDialog != null && mLocationErrorDialog.isShowing())
            return;

        mLocationErrorDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.enable_location_services)
                .setMessage(R.string.location_is_disabled_long_text)
                .setNegativeButton(R.string.close, (dialog, which) -> mLocationErrorDialogAnnoying = true)
                .setOnCancelListener(dialog -> mLocationErrorDialogAnnoying = true)
                .setPositiveButton(R.string.connection_settings, (dialog, which) -> startActivity(intent)).show();
    }

    private boolean showAddStartOrFinishFrame(@NonNull RoutingController controller,
                                              boolean showFrame)
    {
        // S - start, F - finish, L - my position
        // -S-F-L -> Start
        // -S-F+L -> Finish
        // -S+F-L -> Start
        // -S+F+L -> Start + Use
        // +S-F-L -> Finish
        // +S-F+L -> Finish
        // +S+F-L -> Hide
        // +S+F+L -> Hide

        MapObject myPosition = LocationHelper.INSTANCE.getMyPosition();

        if (myPosition != null && !controller.hasEndPoint())
        {
            showAddFinishFrame();
            if (showFrame)
                showLineFrame();
            return true;
        }
        if (!controller.hasStartPoint())
        {
            showAddStartFrame();
            if (showFrame)
                showLineFrame();
            return true;
        }
        if (!controller.hasEndPoint())
        {
            showAddFinishFrame();
            if (showFrame)
                showLineFrame();
            return true;
        }

        return false;
    }

    @Override
    public void onMyPositionModeChanged(int newMode) {
        if (mNavMyPosition != null)
            mNavMyPosition.update(newMode);

        RoutingController controller = RoutingController.get();
        if (controller.isPlanning())
            showAddStartOrFinishFrame(controller, true);
    }

    @Override
    public void onLocationUpdated(@NonNull Location location) {
        if (!RoutingController.get().isNavigating())
            return;
        mNavigationController.update(Framework.nativeGetRouteFollowingInfo());
        TtsPlayer.INSTANCE.playTurnNotifications(getApplicationContext());
    }

    @Override
    public void onCompassUpdated(@NonNull CompassData compass) {
        MapFragment.nativeCompassUpdated(compass.getMagneticNorth(), compass.getTrueNorth(), false);
        mNavigationController.updateNorth(compass.getNorth());
    }

    @Override
    public void onLocationError() {
        if (mLocationErrorDialogAnnoying)
            return;

        Intent intent = TargetUtils.makeAppSettingsLocationIntent(getApplicationContext());
        if (intent == null)
            return;
        showLocationErrorDialog(intent);
    }

    @Override
    public void onLocationNotFound() {
        showLocationNotFoundDialog();
    }

    @Override
    public void showSearch() {
        isResultBySearch = true;
        SearchActivity.start(this, "", null, null);
    }

    private void showLocationNotFoundDialog()
    {
        String message = String.format("%s\n\n%s", getString(R.string.current_location_unknown_message),
                getString(R.string.current_location_unknown_title));

        DialogInterface.OnClickListener stopClickListener = (dialog, which) ->
                LocationHelper.INSTANCE.setStopLocationUpdateByUser(true);

        DialogInterface.OnClickListener continueClickListener = (dialog, which) ->
        {
            if (!LocationHelper.INSTANCE.isActive())
                LocationHelper.INSTANCE.start();
            LocationHelper.INSTANCE.switchToNextMode();
        };

        new AlertDialog.Builder(this)
                .setMessage(message)
                .setNegativeButton(R.string.current_location_unknown_stop_button, stopClickListener)
                .setPositiveButton(R.string.current_location_unknown_continue_button, continueClickListener)
                .setCancelable(false)
                .show();
    }

    @Override
    public void showRoutePlan(boolean show, @Nullable Runnable completionListener) {
        if(show) {
            if (completionListener != null)
                completionListener.run();
        }
    }

    @Override
    public void onBuiltRoute() {
        if (!RoutingController.get().isPlanning())
            return;
        mNavigationController.resetSearchWheel();
    }


    @Override
    public void updateBuildProgress(int progress, int router) {
        // @todo
        //mRoutingPlanInplaceController.updateBuildProgress(progress, router);
        final RoutingInfo rinfo = RoutingController.get().getCachedRoutingInfo();
        if (rinfo != null)
        {
            myDistance = rinfo.distToTarget;
            String units = rinfo.distToTarget +" "+rinfo.targetUnits;
            tvDistance.setText(units);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_settings:
                break;
            case R.id.nav_trip_history:
                break;
            case R.id.nav_chat:
                intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_profile:
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_vehicle:
                //intent = new Intent(this, ProfileActivity.class);
                //startActivity(intent);
                break;
            //case R.id.nav_share:
                //Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                //break;
            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private class CurrentPositionClickListener implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Statistics.INSTANCE.trackEvent(Statistics.EventName.TOOLBAR_MY_POSITION);
            AlohaHelper.logClick(AlohaHelper.TOOLBAR_MY_POSITION);

            if (!PermissionsUtils.isLocationGranted())
            {
                if (PermissionsUtils.isLocationExplanationNeeded(MapActivity.this))
                    PermissionsUtils.requestLocationPermission(MapActivity.this, REQ_CODE_LOCATION_PERMISSION);
                else
                    Toast.makeText(MapActivity.this, R.string.enable_location_services, Toast.LENGTH_SHORT)
                            .show();
                return;
            }
            myPositionClick();
        }
    }

    private void myPositionClick() {
        mLocationErrorDialogAnnoying = false;
        LocationHelper.INSTANCE.setStopLocationUpdateByUser(false);
        LocationHelper.INSTANCE.switchToNextMode();
        LocationHelper.INSTANCE.restart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //SearchEngine.INSTANCE.addListener(this);
        Framework.nativeSetMapObjectListener(this);
        //BookmarkManager.INSTANCE.addLoadingListener(this);
        //BookmarkManager.INSTANCE.addCatalogListener(this);
        RoutingController.get().attach(this);
        if (MapFragment.nativeIsEngineCreated())
            LocationHelper.INSTANCE.attach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //SearchEngine.INSTANCE.removeListener(this);
        Framework.nativeRemoveMapObjectListener();
        //BookmarkManager.INSTANCE.removeLoadingListener(this);
        //BookmarkManager.INSTANCE.removeCatalogListener(this);
        LocationHelper.INSTANCE.detach(!isFinishing());
        RoutingController.get().detach();
    }

    private void dateTime() {
        new SingleDateAndTimePickerDialog.Builder(this)
                .bottomSheet()
                .minDateRange(new Date())
                .displayListener(picker -> { })
                .title("Select Date and Time")
                .listener(date -> {
                    startingTime = date;
                    mDateTime.setText(DateUtils.formatDateStr(date));
                }).display();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*private String formatDate(Date date) {
        final long FIFTEEN_MINUTE_IN_MILLIS = 60000 * 15;//millisecs
        long curTimeInMs = date.getTime();
        Date afterAddingMins = new Date(curTimeInMs + FIFTEEN_MINUTE_IN_MILLIS);
        String pattern = "d MMM-HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(afterAddingMins);
    }*/

    //________________________empty methods
    @Override
    public void onRoutingFinish() { }

    @Override
    public void showNavigation(boolean show) { }

    @Override
    public void showDownloader(boolean openDownloaded) { }

    @Override
    public void updateMenu() { }

    @Override
    public void onTaxiInfoReceived(@NonNull TaxiInfo info) { }

    @Override
    public void onTaxiError(@NonNull TaxiManager.ErrorCode code) { }

    @Override
    public void onNavigationCancelled() { }

    @Override
    public void onNavigationStarted() { }

    @Override
    public void onAddedStop() { }

    @Override
    public void onRemovedStop() { }

    @Override
    public void onDrivingOptionsWarning() { }

    @Override
    public boolean isSubwayEnabled() { return false; }

    @Override
    public void onCommonBuildError(int lastResultCode, @NonNull String[] lastMissingMaps) { }

    @Override
    public void onDrivingOptionsBuildError() { }

    @Override
    public void onStartRouteBuilding() { }

}
