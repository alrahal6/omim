package com.mapsrahal.maps;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.mapsrahal.maps.activity.ContactActivity;
import com.mapsrahal.maps.activity.MatchingListActivity;
import com.mapsrahal.maps.activity.MyRidesActivity;
import com.mapsrahal.maps.activity.ProfileActivity;
import com.mapsrahal.maps.activity.ui.main.MatchingStatePagerAdapter;
import com.mapsrahal.maps.api.ParsedMwmRequest;
import com.mapsrahal.maps.base.BaseMwmFragmentActivity;
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
import com.mapsrahal.maps.websocket.ServerConnection;
import com.mapsrahal.maps.websocket.WebSocketViewModel;
import com.mapsrahal.maps.widget.menu.BaseMenu;
import com.mapsrahal.maps.widget.menu.MyPositionButton;
import com.mapsrahal.util.Constants;
import com.mapsrahal.util.DateUtils;
import com.mapsrahal.util.PermissionsUtils;
import com.mapsrahal.util.SwipeButton;
import com.mapsrahal.util.SwipeButtonCustomItems;
import com.mapsrahal.util.UiUtils;
import com.mapsrahal.util.sharing.TargetUtils;
import com.mapsrahal.util.statistics.AlohaHelper;
import com.mapsrahal.util.statistics.Statistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Stack;

public class MapActivity extends BaseMwmFragmentActivity
                         implements View.OnTouchListener,
                                    MapRenderingListener,
                                    LocationHelper.UiCallback,
                                    RoutingController.Container,
                                    Framework.MapObjectListener,
                                    View.OnClickListener,
                                    NavigationButtonsAnimationController.OnTranslationChangedListener,
                                    AdapterView.OnItemSelectedListener,
                                    ServerConnection.ServerListener

{

    public TextView tvDropOff,tvPickup,tvDistance,mDateTime,mRequiredSeats,mSetPickup,mSetDrop;
    private Button btRequest,mMore;
    private boolean mIsTabletLayout = false,isPickupSearch = true,isResultBySearch = false;
    private ImageButton mAddressToggle,mMainMenu;
    private ImageView mAddSeat,mRemoveSeat;
    private Date startingTime;
    private boolean mIsFullscreen;
    private MapObject tempLocation,fromLocation,toLocation;
    LinearLayout mllForm,mMnuForm;
    private int seatCount = 1;
    @Nullable
    private Dialog mLocationErrorDialog;
    //private DrawerLayout mDrawerLayout;
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
    private String receivedMessage;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private SwipeButton mSwipeButton;
    private Button mOpenGMap;
    private ProgressBar mProgressbar;

    private Boolean isOnWaytoCustomer = false;
    private Boolean isOnTrip = false;
    private String usrId;
    private LinearLayout mCustomerInfo, mAcceptBusyInfo, mSwipeLayout, mpayAndRating;

    private TextView mAmount, mTripTimer;
    private TextView mCustomerName;
    private TextView mCustomerPhone;
    private TextView mCustomerPickup;
    private TextView mCustomerDestination;
    private TextView mTripDistance;
    private Ringtone r;
    //MediaPlayer mediaPlayer;
    private CountDownTimer mCountDownTimer;

    private int requestResponse = 3;
    private static final int SEND_BUSY = 2;
    private static final int ACCEPT_REQUEST = 3;
    private static final int TRIP_CANCELLED = 5;
    private static final int REACHED_CUSTOMER = 11;
    private static final int TRIP_STARTED = 12;
    private static final int TRIP_COMPLETED = 13;
    private static final int DISTANCE_NOTIFY = 50;
    private Long tripStartTime;
    private String phoneNumber;

    private NetworkStateReceiver receiver;

    private final Gson gSon = new Gson();
    private static final String TAG = MapActivity.class.getSimpleName();

    private float base, km, mins;
    private int minDis;
    private ViewPager mViewPager;
    private SwipeButtonCustomItems swipeButtonSettings;
    private double distance, duration, price;
    private long startTime = 0;
    private final Handler timerHandler = new Handler();
    private final Handler tripRecordHandler = new Handler();
    private UserTripInfo userTripInfo;
    private Button mSendRequest;
    private Switch mSwitch;
    private String tripId;

    private ServerConnection mService;
    private WebSocketViewModel mViewModel;

    @Override
    protected void  onSafeCreate(Bundle savedInstanceState) {
        super.onSafeCreate(savedInstanceState);
        setContentView(R.layout.activity_my_map);
        Intent intent = getIntent();
        int message = intent.getIntExtra(PASSENGER_CAPTAIN_SELECTOR,1);
        mllForm = findViewById(R.id.ll_form);
        tvDropOff = findViewById(R.id.tv_dropoff);
        tvPickup = findViewById(R.id.tv_pickup);
        mSwitch = findViewById(R.id.switch2);
        if(MySharedPreference.getInstance(this).isCaptainOnline()) {
            mSwitch.setChecked(true);
        } else {
            mSwitch.setChecked(false);
        }
        mSendRequest = findViewById(R.id.send_request_test);
        mSendRequest.setOnClickListener(this);

        MatchingStatePagerAdapter matchingStateAdapter = new MatchingStatePagerAdapter(this, getSupportFragmentManager());
        mViewPager = findViewById(R.id.matching_list_vp);
        mViewPager.setAdapter(matchingStateAdapter);
        mViewPager.setVisibility(View.GONE);
        //mSwitch.setOnT
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
        Spinner spinner = findViewById(R.id.gender_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.select_gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        //mMore = findViewById(R.id.more);
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
        //Button buttonOpenBottomSheet = findViewById(R.id.more);
        /*buttonOpenBottomSheet.setOnClickListener(v -> {
            BottomSheetMoreSettings bottomSheet = new BottomSheetMoreSettings();
            bottomSheet.show(getSupportFragmentManager(), "MoreSetting");
        });*/
        //removeBookmark();
        //Log.d("MAP", "instance id new token is " + FirebaseInstanceId.getInstance().getToken());
        mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.class.getName());


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
                //break;
            /*default:
                break;*/
        }

        if(MySharedPreference.getInstance(this).isCaptainOnline()) {
            hideFromTo();
            onlineAsCaptain();
        }

        try {
            //Uri notificationRaw = Uri.parse("android.resource://" + this.getPackageName() + "/raw/driver_call.mp3");
            //Log.i(TAG,"Uri "+ notificationRaw);
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), notification);
            r = RingtoneManager.getRingtone(this, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mViewModel = ViewModelProviders.of(this).get(WebSocketViewModel.class);
        setObservers();
        //Bundle bundle = this.getArguments();
        //String s = (String) bundle.getSerializable("userTripInfo");
        //userTripInfo = gSon.fromJson(s, UserTripInfo.class);
        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
        //mapView = mapFragment.getView();

        mCustomerInfo = findViewById(R.id.customerInfo);
        mOpenGMap = findViewById(R.id.openGMap);
        mAcceptBusyInfo = findViewById(R.id.acceptBusyInfo);
        mTripTimer = findViewById(R.id.tripTimer);
        mSwipeLayout = findViewById(R.id.swipeLayout);
        mSwipeButton = findViewById(R.id.swipeButton);
        mCustomerName = findViewById(R.id.customerName);
        mCustomerPickup = findViewById(R.id.customerPickup);
        mCustomerPhone = findViewById(R.id.customerPhone);
        mTripDistance = findViewById(R.id.tripDistance);
        mCustomerDestination = findViewById(R.id.customerDestination);
        mProgressbar = findViewById(R.id.myProgress);
        mProgressbar.setVisibility(View.GONE);
        //View headerView = navigationView.getHeaderView(0);
        usrId = String.valueOf(MySharedPreference.getInstance(this).getUserId());
        //TextView mPhone = headerView.findViewById(R.id.dPhone);
        //mPhone.setText(MySharedPreference.getInstance(this).getPhoneNumber());
        mpayAndRating = findViewById(R.id.payAndRating);
        mpayAndRating.setVisibility(View.GONE);
        final RatingBar mRatingBar = findViewById(R.id.ratingBar);
        mAmount = findViewById(R.id.payAmount);
        Button mSendFeedback = findViewById(R.id.submitRating);
        //checkLocationPermission();

        mOpenGMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInGoogleMap();
            }
        });

        mSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpayAndRating.setVisibility(View.GONE);
                //if (mMap != null) {
                    //mMap.clear();
                //}
                // todo save rating
            }
        });

        mCustomerPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDriver();
            }
        });

        Button mRideStatus = findViewById(R.id.rideStatus);
        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //acceptRequest();
            }
        });

        Button mBusyResponse = findViewById(R.id.busyResponse);
        mBusyResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestResponse = 2;
                respondBusy();
            }
        });

        /*mSwipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeButtonPressed();
            }
        });*/
        //SwipeButton mSwipeButton = findViewById(R.id.my_swipe_button);

        swipeButtonSettings = new SwipeButtonCustomItems() {
            @Override
            public void onSwipeConfirm() {
                swipeButtonPressed();
                //Log.d("NEW_STUFF", "New swipe confirm callback");
            }
        };

        if (mSwipeButton != null) {
            mSwipeButton.setSwipeButtonCustomItems(swipeButtonSettings);
        }
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                connect();
            } else {
                disconnect();
            }
        });
    }

    private void setObservers() {
        mViewModel.getBinder().observe(this, myBinder -> {
            if (myBinder == null) {
                //Log.d(TAG, "onChanged: unbound from service");
            } else {
                //Log.d(TAG, "onChanged: bound to service.");
                mService = myBinder.getService();
                mService.registerListener(this);
                /*if(mService.isMessageReceived()) {
                    processMessage(mService.receivedMessage());
                    mService.setIsReceivedFalse();
                }*/
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        //Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void listMatch() {
        setFullscreen(!mIsFullscreen);

        //Intent intent = new Intent(this, MatchingListActivity.class);
        //startActivity(intent);
    }

    private void onlineAsCaptain() {
        mSwitch.setVisibility(View.VISIBLE);
        mSendRequest.setVisibility(View.VISIBLE);
        // todo online as captain
    }

    private void hideFromTo() {
        tvDropOff.setVisibility(View.GONE);
        tvPickup.setVisibility(View.GONE);
        mAddressToggle.setVisibility(View.GONE);
        mllForm.setVisibility(View.GONE);
        btRequest.setVisibility(View.GONE);
        mDateTime.setVisibility(View.GONE);
        //mMore.setVisibility(View.GONE);
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

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
            case R.id.send_request_test:
                sendMe();
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
        //Log.d("Map Activity Location", location.getLatitude()+"");
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

    /*@Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        super.onNavigationItemSelected(item);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }*/

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
    protected void onPause() {
        super.onPause();
        unBindMyService();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //if(mViewModel.getBinder() != null) {
            //unbindService(mViewModel.getServiceConnection());
        //}
        //disconnect();
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

    private void openInGoogleMap() {
        /*String url = "http://maps.google.com/maps?saddr=" + pickupLatLng.latitude + ","
                + pickupLatLng.longitude + "&daddr=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&mode=driving";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);*/
    }

    private void acceptRequest(UserTripInfo g) {
        try {
            //r.stop();
            mService.stopRingTone();
            send(ACCEPT_REQUEST, 0, 0, 0);
            mAcceptBusyInfo.setVisibility(View.GONE);
            mSwipeLayout.setVisibility(View.VISIBLE);
            mOpenGMap.setVisibility(View.VISIBLE);
            //mCustomerName.setText("");
            //mCustomerPhone.setText("");
            base = g.getBase();
            km = g.getKm();
            mins = g.getMins();
            minDis = g.getMinDis();
            //tripId = String.valueOf(g.getTripId());
            distance = g.getDistance();
            duration = g.getDuration();
            price = g.getPrice();
            /*if (mTimerRunning) {
                stopTimer();
            }*/
            // todo register accepted driver with trip id
            // tripId
            prepareGoToCustomer();
        } catch (Exception e) {
            Log.d(TAG, "Error accept request " + e.getMessage());
        }
    }



    private void prepareGoToCustomer() {
        //pickupLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        //destinationLatLng = new LatLng(g.getLat(), g.getLng());
        isOnWaytoCustomer = true;
        //getDirectionDistance();
    }

    private void reachedCustomer() {
        send(REACHED_CUSTOMER, 0, 0, 0);
        isOnWaytoCustomer = false;
        swipeButtonSettings.setActionConfirmText(getString(R.string.start_trip));
        mSwipeButton.setSwipeButtonCustomItems(swipeButtonSettings);
        //mSwipeButton.setText(R.string.start_trip);
    }

    private void send(int flag, double distance, double duration, double price) {
        try {
            //mService.sendMessage(flag, requestingPassenger, distance, duration, price);
            updateResponse(flag);
        } catch (Exception e) {
            Log.d(TAG, "Error sending message " + e.getMessage());
        }
    }

    private void updateResponse(int responseId) {
        requestResponse = responseId;
        //MyBase.getInstance(this).addToRequestQueue(updateIsOnReq);
    }

    private void sendMe() {
        //mDriverInfo.setVisibility(View.GONE);
        //mDriverPhone.setText("");
        //Log.i(TAG , "" +userTripInfo);

        try {
            UserTripInfo userTripInfo = new UserTripInfo(
                    27,
                    "912391525",
                    "dhayal");
            userTripInfo.setDriverId(25);
            mService.sendReq(userTripInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //startTimer();
    }





    private void respondBusy() {
        try {
            //r.stop();
            mService.stopRingTone();
            send(SEND_BUSY, 0, 0, 0);
            mCustomerInfo.setVisibility(View.GONE);
            mCustomerName.setText("");
            mCustomerPhone.setText("");
            //if (mTimerRunning) {
                //stopTimer();
            //}
        } catch (Exception e) {
            Log.d(TAG, "Error respond busy " + e.getMessage());
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
        if(MySharedPreference.getInstance(this).isCaptainOnline()) {
            connect();
        }


        String tripId = MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).getTripId();
        if(tripId != null) {
            this.tripId = tripId;
            // todo handle unclosed trip
            //Toast.makeText(this,"Last trip not ended properly",Toast.LENGTH_LONG).show();
            isOnWaytoCustomer = false;
            isOnTrip = true;
            price = 1;
            mCustomerInfo.setVisibility(View.VISIBLE);
            mAcceptBusyInfo.setVisibility(View.GONE);
            mSwipeLayout.setVisibility(View.VISIBLE);
            mSwipeButton.setText(getString(R.string.end_trip));
            //mCustomerPhone.setText(getString(R.string.customer_phone) + g.getPhone());
            // endTrip();
        }
    }

    /*private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Log.i(TAG, "onServiceConnected");
            ServerConnection.ServerConnectionBinder binder = (ServerConnection.ServerConnectionBinder) service;
            mService = binder.getService();
            //mService.registerListener(DriverMapsActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Log.i(TAG, "onServiceDisconnected");
            mService = null;
        }
    };*/

    private void swipeButtonPressed() {
        if (isOnWaytoCustomer) {
            reachedCustomer();
        } else if (!isOnTrip) {
            send(TRIP_STARTED, 0, 0, 0);
            startTrip();
        } else if (isOnTrip) {
            endTrip();
        }
    }

    private Long getCurrentTimestamp() {
        // Long timestamp = System.currentTimeMillis()/1000;
        //return timestamp;
        return System.currentTimeMillis() / 1000;
    }

    private void endTrip() {
        tripRecordHandler.removeCallbacks(recordTripRunnable);
        timerHandler.removeCallbacks(timerRunnable);
        MySharedPreference.getInstance(MapActivity.this).finishTrip();
        //NumberFormat format = NumberFormat.getCurrencyInstance();
        if (price == 0) {
            // todo get price from server
            long startedTime = MySharedPreference.getInstance(MapActivity.this).getStartTime();
            duration = (getCurrentTimestamp() - startedTime) / 60;
            price = base + (((distance < minDis) ? 0 : (distance - minDis)) * km) + (duration * mins);
            price = roundTwoDecimals(price);
            //Log.i(TAG,"Distance : "+distance);
            //Log.i(TAG,"Duration : "+duration);
            //Log.i(TAG,"Price : "+price);
            mOpenGMap.setVisibility(View.GONE);
            mAmount.setText(getString(R.string.collect_payment) + price + getString(R.string.sdg));
            mpayAndRating.setVisibility(View.VISIBLE);
            isOnTrip = false;
            mCustomerInfo.setVisibility(View.GONE);
            send(TRIP_COMPLETED, distance, duration, price);

            //MyBase.getInstance(MapActivity.this).addToRequestQueue(savePrice);
            //Long tripEndTime = getCurrentTimestamp();
            //duration = (tripEndTime - tripStartTime) / 60;
            //Log.i(TAG,"Duration : "+ duration);
            //distance = getDistance(pickupLatLng.latitude, pickupLatLng.longitude,
            //mLastLocation.getLatitude(), mLastLocation.getLongitude()) / 1000;
            //Log.i(TAG,"Distance : "+ distance);

            //((TextView) findViewById(R.id.text_result)).setText(format.format(result));
        } else {
            isOnTrip = false;
            mOpenGMap.setVisibility(View.GONE);
            mpayAndRating.setVisibility(View.VISIBLE);
            mCustomerInfo.setVisibility(View.GONE);
            if(price == 1) {
                mAmount.setText(getString(R.string.collect_payment) + " " + getString(R.string.sdg));
                updateResponse(TRIP_COMPLETED);
            } else {
                price = roundTwoDecimals(price);
                mAmount.setText(getString(R.string.collect_payment) + price + getString(R.string.sdg));
                send(TRIP_COMPLETED, distance, duration, price);
            }
        }
    }

    private double roundTwoDecimals(double d) {
        return new BigDecimal(d).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private final Runnable recordTripRunnable = new Runnable() {
        @Override
        public void run() {
            /*long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            mTripTimer.setText(String.format("%d:%02d", minutes, seconds));*/
            //double dis =
            //distance += getDistance(tempLatLng.latitude,tempLatLng.longitude,mLastLocation.getLatitude(),mLastLocation.getLongitude())/1000;
            //duration = (getCurrentTimestamp() - tripStartTime) / 60;
            //Log.i(TAG,"Distance : "+distance);
            //Log.i(TAG,"Duration : "+duration);
            MySharedPreference.getInstance(MapActivity.this).recordTrip(tripId,tripStartTime,(float) distance);
            //tempLatLng = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            tripRecordHandler.postDelayed(this, 120000);
        }
    };

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            mTripTimer.setText(String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };


    private void startTrip() {
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        tripStartTime = startTime / 1000;
        isOnTrip = true;
        //mMap.clear();
        //pickupLatLng = new LatLng(g.getLat(), g.getLng());
        //destinationLatLng = new LatLng(g.getDestLat(), g.getDestLng());
        swipeButtonSettings.setActionConfirmText(getString(R.string.end_trip));
        mSwipeButton.setSwipeButtonCustomItems(swipeButtonSettings);
        //mSwipeButton.setText(R.string.end_trip);
        mOpenGMap.setVisibility(View.VISIBLE);
        /*if (g.getDestLat() > 0) {
            //getDirectionDistance();
        } else {
            mOpenGMap.setVisibility(View.GONE);
        }*/
        if(price == 0) {
            //tempLatLng = pickupLatLng;
            distance = 0;
            tripRecordHandler.postDelayed(recordTripRunnable,0);
        }
    }


    private void callDriver() {
        //Log.i(TAG,"Call driver called");
        //Log.i(TAG,"Phone number : "+phoneNumber);
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission denied to make call", Toast.LENGTH_LONG).show();
            return;
        }
        this.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if(receivedMessage != null) {
            processMessage(receivedMessage);
            receivedMessage = null;
        }*/
        /*String msg = MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).getUserMessage();
        //Log.i(TAG,"Shared message : "+msg);
        if (msg != null) {
            processMessage(msg);
            MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userMessage(null);
        }*/

        final Intent intent = getMyIntent();
        if(MySharedPreference.getInstance(this).isCaptainOnline()) {
            bindMyService(intent);
        }
        //bindService(intent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private Intent getMyIntent() {
        Context context = getContext();
        return new Intent(context, ServerConnection.class);
    }

    @Override
    public void onNewMessage(String message) {
        //mMessageFromServer.setText(message);
        receivedMessage = message;
        //processMessage(message);
    }

    @Override
    public void onStatusChange(ServerConnection.ConnectionStatus status) {

        /*String statusMsg = (status == ServerConnection.ConnectionStatus.CONNECTED ?
                //getString(R.string.connected) : getString(R.string.disconnected));
        //mConnectionStatus.setText(statusMsg);
        //mSendMessageButton.setEnabled(status == ServerConnection.ConnectionStatus.CONNECTED);*/
    }

    private void connect() {
        //Log.i(TAG, "Main thread Id " + Thread.currentThread().getId());
        //mViewModel.setIsConnected(true);
        if(!isMyServiceRunning(ServerConnection.class)) {
            MySharedPreference.getInstance(this).setCaptainOnline(true);
            final Intent intent = getMyIntent();
            intent.setAction(Constants.STARTFOREGROUND_ACTION);
            ContextCompat.startForegroundService(getContext(), intent);
            bindMyService(intent);

        }

        //receiver = new NetworkStateReceiver();
        //IntentFilter filter1 = new IntentFilter();
        //filter1.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //registerReceiver(receiver, filter1);
        //LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(ServerConnection.ACTION_MSG_RECEIVED));
        //MyBase.getInstance(this).addToRequestQueue(insLoc);
    }

    private void bindMyService(Intent intent) {
        MySharedPreference.getInstance(this).setBind(true);
        bindService(intent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private void unBindMyService() {
        if(MySharedPreference.getInstance(this).isBinded()) {
            MySharedPreference.getInstance(this).setBind(false);
            unbindService(mViewModel.getServiceConnection());
        }
    }

    private Context getContext() {
        return MwmApplication.get().getApplicationContext();
    }

    private void disconnect() {
        //if(mFusedLocationClient != null){
        //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        //}
        //if (receiver != null) {
            //unregisterReceiver(receiver);
        //}
        Log.i(TAG, "Stop service called ");
        //if(isMyServiceRunning(ServerConnection.class)) {
        MySharedPreference.getInstance(this).setCaptainOnline(false);
        Intent stopIntent = getMyIntent();
        unBindMyService();
        stopIntent.setAction(Constants.STOPFOREGROUND_ACTION);
        ContextCompat.startForegroundService(getContext(), stopIntent);
        //startService(stopIntent);
        //stopService(new Intent(this, ServerConnection.class));
        Log.i(TAG, "Stop service called inside");
        //}
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private void setFullscreen(boolean isFullscreen) {
        mIsFullscreen = isFullscreen;
        if (isFullscreen) {
            mViewPager.setVisibility(View.VISIBLE);
            if (mNavAnimationController != null)
                mNavAnimationController.disappearZoomButtons();
            if (mNavMyPosition != null)
                mNavMyPosition.hide();
        } else {
            mViewPager.setVisibility(View.GONE);

            if (mNavAnimationController != null)
                mNavAnimationController.appearZoomButtons();
            if (mNavMyPosition != null)
                mNavMyPosition.show();
        }
    }

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
