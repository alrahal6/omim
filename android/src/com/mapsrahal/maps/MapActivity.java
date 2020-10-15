package com.mapsrahal.maps;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.mapsrahal.maps.activity.SelectorActivity;
import com.mapsrahal.maps.activity.ui.main.CargoStatePagerAdapter;
import com.mapsrahal.maps.activity.ui.main.ConfirmedListPagerAdapter;
import com.mapsrahal.maps.activity.ui.main.MatchingStatePagerAdapter;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.FindDriverApi;
import com.mapsrahal.maps.api.ParsedMwmRequest;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.api.UserMessageApi;
import com.mapsrahal.maps.base.BaseMwmFragmentActivity;
import com.mapsrahal.maps.bookmarks.data.FeatureId;
import com.mapsrahal.maps.bookmarks.data.MapObject;
import com.mapsrahal.maps.intent.MapTask;
import com.mapsrahal.maps.location.CompassData;
import com.mapsrahal.maps.location.LocationHelper;
import com.mapsrahal.maps.model.CallLog;
import com.mapsrahal.maps.model.FindDriver;
import com.mapsrahal.maps.model.GetMyHistory;
import com.mapsrahal.maps.model.IsValid;
import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.model.Post;
import com.mapsrahal.maps.model.Price;
import com.mapsrahal.maps.model.StatusUpdate;
import com.mapsrahal.maps.model.UserMessage;
import com.mapsrahal.maps.onboarding.OnboardingTip;
import com.mapsrahal.maps.routing.NavigationController;
import com.mapsrahal.maps.routing.RoutePointInfo;
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
import com.mapsrahal.maps.widget.menu.MyPositionButton;
import com.mapsrahal.util.Constants;
import com.mapsrahal.util.DateUtils;
import com.mapsrahal.util.PermissionsUtils;
import com.mapsrahal.util.SwipeButton;
import com.mapsrahal.util.SwipeButtonCustomItems;
import com.mapsrahal.util.UiUtils;
import com.mapsrahal.util.Utils;
import com.mapsrahal.util.sharing.TargetUtils;
import com.mapsrahal.util.statistics.AlohaHelper;
import com.mapsrahal.util.statistics.Statistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapsrahal.maps.activity.SelectorActivity.CAPTAIN_ANY;
import static com.mapsrahal.maps.activity.SelectorActivity.CAPTAIN_SHARE_ONLY;
import static com.mapsrahal.maps.activity.SelectorActivity.CAPTAIN_TAXI_ONLY;
import static com.mapsrahal.maps.activity.SelectorActivity.PASSENGER_ANY;
import static com.mapsrahal.maps.activity.SelectorActivity.PASSENGER_SHARE_ONLY;
import static com.mapsrahal.maps.activity.SelectorActivity.PASSENGER_TAXI_ONLY;
import static com.mapsrahal.maps.routing.RoutePointInfo.ROUTE_MARK_START;

public class MapActivity extends BaseMwmFragmentActivity
        implements View.OnTouchListener,
        MapRenderingListener,
        LocationHelper.UiCallback,
        RoutingController.Container,
        Framework.MapObjectListener,
        View.OnClickListener,
        ServerConnection.ServerListener,
        NavigationButtonsAnimationController.OnTranslationChangedListener,
        AdapterView.OnItemSelectedListener,
        MatchingStatePagerAdapter.MatchingSelectionListener,
        ConfirmedListPagerAdapter.ConfirmationSelectionListener,
        CargoStatePagerAdapter.CargoSelectionListener {

    private TextView tvDropOff, tvPickup, tvDistance, mDateTime, mRequiredSeats, mSetPickup, mSetDrop;
    private TextView mAmount, mTripTimer, mCustomerName, mCustomerPhone, mCustomerPickup;
    private TextView mCustomerDestination, mTripDistance;
    private TextView mDriverName, mDriverPhone;
    private TextView mListCount, mListAmount, mCallingCaptain, mPriceText;
    private double tripPrice = 0.0d;
    private double tripSeatPrice = 0.0d;
    private Button mCancelRequest, mFinishTrip, mStartTrip;
    private int mCancelId;

    private Button btRequest, mOpenGMap, mConfirmList;
    private SwipeButton mSwipeButton;
    private ImageButton mAddressToggle, mMainMenu,mCloseDest;

    private ImageView mAddSeat, mRemoveSeat, mCloseList, mCloseNotification;
    private LinearLayout mNotificationCard, mStartTripLayout;
    private LinearLayout mDriverInfo, mllForm, mMnuForm, mConfirmLayout;
    private LinearLayout mCustomerInfo, mAcceptBusyInfo, mSwipeLayout, mpayAndRating, mPriceLayout;
    private ProgressBar mMyprogress;

    private boolean mIsTabletLayout = false, isPickupSearch = true, isResultBySearch = false;
    private boolean mIsFullscreen, mLocationErrorDialogAnnoying = false;
    private boolean mTimerRunning, isLaunchByDeepLink = false;
    private boolean isDriverAccepted = false, isDriverBusy = false;
    private boolean isRequestInProgress = false, isStartedCounter = false;
    private Boolean isOnWaytoCustomer = false;
    private Boolean isOnTrip = false, isOnRequestBtn = false, isCaptainInitialised = false;

    private String genderCargoTxt = "";

    private int seatCount = 1;
    private int genderCargoId = 0;
    private static final int REQ_CODE_LOCATION_PERMISSION = 1;
    private int requestingPassenger = 0;
    private int requestResponse = 3;
    private static final String CONFIRMED_LIST_KEY = "confirmedlistkey";
    //private static final String CONFIRMED_LIST_KEY = "confirmedlistkey";
    public static final int SEND_BUSY = 2;
    public static final int ACCEPT_REQUEST = 3;
    public static final int TRIP_CANCELLED = 5;
    public static final int REACHED_CUSTOMER = 11;
    public static final int TRIP_STARTED = 12;
    public static final int TRIP_COMPLETED = 13;
    public static final int DISTANCE_NOTIFY = 50;
    private int minDis, requestCounter = 0, driverId = 0;
    public static final int NEW_REQUEST = 4;
    public static final int CANCEL_DRIVER = 5;
    public static final int END_TRIP = 13;
    private final int[] requestedDrivers = new int[10];
    private int mSelector;

    private static final String PASSENGER_CAPTAIN_SELECTOR = "passenger_captain_selector";
    private String receivedMessage, usrId;
    private String myDistance = "0", mSourceAddress, mDestinationAddress, mAddressToggleStr;

    private Date startingTime;
    private MapObject tempLocation, fromLocation, toLocation;
    @Nullable
    private Dialog mLocationErrorDialog;
    @Nullable
    private MapFragment mMapFragment;
    @Nullable
    private SearchFilterController mFilterController;
    @Nullable
    private MyPositionButton mNavMyPosition;
    @Nullable
    private NavigationButtonsAnimationController mNavAnimationController;
    private RoutingPlanInplaceController mRoutingPlanInplaceController;
    @NonNull
    private final View.OnClickListener mOnMyPositionClickListener = new MapActivity.CurrentPositionClickListener();
    // Map tasks that we run AFTER rendering initialized
    private final Stack<MapTask> mTasks = new Stack<>();
    @SuppressWarnings("NullableProblems")
    @NonNull
    private NavigationController mNavigationController;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private ProgressBar mProgressbar;
    private boolean isConfirmationTimerOn = false;
    private CountDownTimer mCountDownTimer, mTimeOutTimer;
    private static final long START_TIME_IN_MILLIS = 20000;
    private static final long START_TIME_OUT_IN_MILLIS = 300000;
    private Long tripStartTime;
    private String phoneNumber;
    private NetworkStateReceiver receiver;
    private UserTripInfo g;
    private final Gson gSon = new Gson();
    private static final String TAG = MapActivity.class.getSimpleName();
    private String tripId;
    private float base, km, mins;
    private ViewPager mViewPager;
    private SwipeButtonCustomItems swipeButtonSettings;
    private double distance, duration, price;
    private long startTime = 0;
    private boolean isSelectorFree = true;
    private final Handler timerHandler = new Handler();
    private final Handler tripRecordHandler = new Handler();
    private UserTripInfo userTripInfo;
    private SwitchCompat mSwitch;
    private CargoStatePagerAdapter mCargoAdapter;
    private MatchingStatePagerAdapter mAdapter;
    private ConfirmedListPagerAdapter mConfirmedAdapter;
    private ServerConnection mService;
    private WebSocketViewModel mViewModel;
    private final Handler requestHandler = new Handler();
    ArrayList<UserMessage> confirmedUserList;

    Map<Integer, Integer> selectionList = new HashMap<>();
    private double totAmount = 0d;
    private Button mPlanTrip;

    // preparations

    private void prepareForAll() {
    }

    private void prepareList() {
        mListCount = findViewById(R.id.list_count);
        mListAmount = findViewById(R.id.list_amount);
        mConfirmList = findViewById(R.id.confirm_list);
        mConfirmList.setOnClickListener(this);
    }

    private void prepareForFrom() {
        hideExceptFromTo();
        showBtnRequest();
    }

    private void prepareForNone() {
        isOnRequestBtn = true;
        //hideFromTo();
        hideFrom();
        onlineAsCaptain();
    }

    private void openInGoogleMap(double fromLat, double fromLng, double toLat, double toLng) {
        String url = "http://maps.google.com/maps?saddr=" + fromLat + ","
                + fromLng + "&daddr=" + toLat + "," + toLng + "&mode=driving";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        //startActivity(intent);
        try {
            startActivity(intent);
        } catch(Exception e) {
            Toast.makeText(this,"Please install Google Map",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void goToLocation(double toLat, double toLng) {
        //double toLat, double toLng
        fromLocation = LocationHelper.INSTANCE.getMyPosition();
        toLocation = MapObject.createMapObject(FeatureId.EMPTY, MapObject.POI, "", "",
                toLat, toLng);
        RoutingController.get().setStartPoint(fromLocation);
        RoutingController.get().setEndPoint(toLocation);
    }

    @Override
    public void callMatch(int position) {
        phoneNumber = "0" + mMatchingList.get(position).getmPhone();
        calledMatch(phoneNumber, mMatchingList.get(position).getId());
        callDriver();
    }

    @Override
    public void callConfirmed(String phone) {
        phoneNumber = phone;
        callDriver();
    }

    private void calledMatch(String phone, int tripId) {
        PostApi postApi = ApiClient.getClient().create(PostApi.class);
        CallLog callLog = new CallLog(
                MySharedPreference.getInstance(this).getUserId(), phone, tripId
        );
        Call<CallLog> call = postApi.callLog(callLog);
        call.enqueue(new Callback<CallLog>() {
            @Override
            public void onResponse(Call<CallLog> call, Response<CallLog> response) {

            }

            @Override
            public void onFailure(Call<CallLog> call, Throwable t) {

            }
        });
    }

    @Override
    public void routeInMap(double fromLat, double fromLng, double toLat, double toLng) {
        openInGoogleMap(fromLat, fromLng, toLat, toLng);
    }

    @Override
    public void showInGMap(double fromLat, double fromLng, double toLat, double toLng) {
        openInGoogleMap(fromLat, fromLng, toLat, toLng);
    }

    private void showMap(double fromLat, double fromLng, double toLat, double toLng) {
        fromLocation = MapObject.createMapObject(FeatureId.EMPTY, MapObject.POI, "", "",
                fromLat, fromLng);
        toLocation = MapObject.createMapObject(FeatureId.EMPTY, MapObject.POI, "", "",
                toLat, toLng);
        RoutingController.get().setStartPoint(fromLocation);
        RoutingController.get().setEndPoint(toLocation);
    }

    @Override
    public void showInMap(double fromLat, double fromLng, double toLat, double toLng) {
        // openInGoogleMap(fromLat,fromLng,toLat,toLng);
        showMap(fromLat, fromLng, toLat, toLng);
    }

    private void cancelConfirmedRequest(int cancelId) {
        mCancelId = cancelId;
        alertDialogCancel();
    }

    private void alertDialogCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setMessage(getString(R.string.sure_cancel)).setPositiveButton(getString(R.string.yes), cancelDialogClickListener)
                .setNegativeButton(getString(R.string.no), cancelDialogClickListener).show();
    }

    DialogInterface.OnClickListener cancelDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //sendConfirmation();
                    if (confirmedUserList.size() > 0) {
                        sendUserMessage(confirmedUserList, Constants.Notification.DRIVER_CANCELLED);
                        //reloadMe();
                    }
                    // cancelMe();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(MapActivity.this, getString(R.string.req_not_send), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private int getFlag() {
        return Constants.Notification.DRIVER_ACCEPTED;
    }

    private void alertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setMessage(getString(R.string.sure_send_conf)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();

    }

    private void onlineAsCaptain() {
        mSwitch.setVisibility(View.VISIBLE);
    }

    private void hideExceptFromTo() {
        mllForm.setVisibility(View.GONE);
        hideBtnRequest();
        mDateTime.setVisibility(View.GONE);
    }

    private void hideBtnRequest() {
        btRequest.setVisibility(View.GONE);
    }

    private void showBtnRequest() {
        showHideRequest(!isOnRequestBtn);
    }

    private void showHideRequest(boolean showHide) {
        if (showHide) {
            if(Double.parseDouble(myDistance) > 1) {
                btRequest.setVisibility(View.VISIBLE);
            }
        } else {
            btRequest.setVisibility(View.GONE);
        }
        showProgress(false);
    }

    private void hideFromTo() {
        tvDropOff.setVisibility(View.GONE);
        tvPickup.setVisibility(View.GONE);
        mAddressToggle.setVisibility(View.GONE);
        hideExceptFromTo();
    }

    private void hideFrom() {
        //tvDropOff.setVisibility(View.GONE);
        tvDropOff.setText(R.string.set_destination);
        tvPickup.setVisibility(View.GONE);
        mAddressToggle.setVisibility(View.GONE);
        mCloseDest.setVisibility(View.VISIBLE);
        hideExceptFromTo();
    }

    @Override
    protected void onSafeCreate(Bundle savedInstanceState) {
        super.onSafeCreate(savedInstanceState);
        setContentView(R.layout.activity_my_map);

        mDriverInfo = findViewById(R.id.driverInfo);
        mNotificationCard = findViewById(R.id.notification_req_res);
        mMyprogress = findViewById(R.id.myProgress);
        //mDriverName = (TextView) view.findViewById(R.id.driverName);
        mDriverPhone = findViewById(R.id.driverPhone);
        mDriverPhone.setOnClickListener(this);
        mDriverPhone.setVisibility(View.GONE);
        mCancelRequest = findViewById(R.id.cancelRequest);
        mCallingCaptain = findViewById(R.id.callingCaptain);
        mCancelRequest.setOnClickListener(this);
        mllForm = findViewById(R.id.ll_form);
        tvDropOff = findViewById(R.id.tv_dropoff);
        tvPickup = findViewById(R.id.tv_pickup);
        mSwitch = findViewById(R.id.switch2);
        mViewPager = findViewById(R.id.matching_list_vp);
        mConfirmLayout = findViewById(R.id.confirm_layout);
        mStartTripLayout = findViewById(R.id.start_trip_layout);
        mStartTripLayout.setVisibility(View.GONE);
        mFinishTrip = findViewById(R.id.finish_trip);
        mFinishTrip.setOnClickListener(this);
        mStartTrip = findViewById(R.id.start_trip);
        mStartTrip.setOnClickListener(this);
        mPlanTrip = findViewById(R.id.plan_trip);
        mPlanTrip.setOnClickListener(this);
        mViewPager.setVisibility(View.GONE);
        mConfirmLayout.setVisibility(View.GONE);
        userMessageApi = ApiClient.getClient().create(UserMessageApi.class);
        mCloseList = findViewById(R.id.closable_l);
        mCloseList.setOnClickListener(this);
        mCloseNotification = findViewById(R.id.closable_n);
        mCloseNotification.setOnClickListener(this);
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
        btRequest = findViewById(R.id.bt_request);
        btRequest.setOnClickListener(this);
        mAddressToggle = findViewById(R.id.addressToggle);
        mAddressToggle.setOnClickListener(this);
        mCloseDest = findViewById(R.id.closeDest);
        mCloseDest.setOnClickListener(this);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        mpPhone = headerView.findViewById(R.id.pPhone);
        mpPhone.setText(MySharedPreference.getInstance(this).getPhoneNumber());
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mMainMenu = findViewById(R.id.mainMenu);
        mMainMenu.setOnClickListener(this);
        mMapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentByTag(MapFragment.class.getName());

        View container = findViewById(R.id.map_fragment_container);
        if (container != null) {
            container.setOnTouchListener(this);
        }
        adjustCompass(0);
        mNavigationController = new NavigationController(this);
        initNavigationButtons();
        requestCounter = 0;
        mViewModel = ViewModelProviders.of(this).get(WebSocketViewModel.class);
        setObservers();
        mPriceLayout = findViewById(R.id.ll_form_price);
        mPriceText = findViewById(R.id.tv_price);
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
        usrId = String.valueOf(MySharedPreference.getInstance(this).getUserId());
        mpayAndRating = findViewById(R.id.payAndRating);
        mpayAndRating.setVisibility(View.GONE);
        final RatingBar mRatingBar = findViewById(R.id.ratingBar);
        mAmount = findViewById(R.id.payAmount);
        Button mSendFeedback = findViewById(R.id.submitRating);
        mSelector = MySharedPreference.getInstance(this).getSelectorId();
        if(MySharedPreference.getInstance(this).isCaptainOnline()) {
            mSwitch.setChecked(true);
            mSelector = CAPTAIN_TAXI_ONLY;
        }
        if (mSelector == CAPTAIN_SHARE_ONLY || mSelector == CAPTAIN_ANY) {
            prepareList();
        }

        switch (mSelector) {
            case PASSENGER_TAXI_ONLY:
                prepareForAll();
                mDateTime.setVisibility(View.GONE);
                connect();
                break;
            case PASSENGER_SHARE_ONLY:
            case PASSENGER_ANY:
                prepareForAll();
                break;
            case CAPTAIN_TAXI_ONLY:
                prepareForNone();
                isCaptainInitialised = true;
                break;
        }

        ArrayAdapter<CharSequence> adapter;
        Spinner spinner = findViewById(R.id.gender_spinner);

        ImageView imageView = findViewById(R.id.remove_seat);
        TextView textView = findViewById(R.id.required_seats);
        ImageView imageView1 = findViewById(R.id.add_seat);
        if (mSelector == CAPTAIN_ANY || mSelector == PASSENGER_ANY) {
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.select_cargo, android.R.layout.simple_spinner_item);
            // hide seats
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            imageView1.setVisibility(View.GONE);
        } else if (mSelector == PASSENGER_TAXI_ONLY) {
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.select_vehicle, android.R.layout.simple_spinner_item);
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            imageView1.setVisibility(View.GONE);
        } else {
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.select_gender, android.R.layout.simple_spinner_item);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        mOpenGMap.setOnClickListener(this);

        mSendFeedback.setOnClickListener(view -> {
            mpayAndRating.setVisibility(View.GONE);
            // todo save rating
        });

        mCustomerPhone.setOnClickListener(v -> callDriver());
        //mSwitch.setVisibility(View.VISIBLE);
        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                connect();
            } else {
                disconnect();
            }
        });
    }

    private void connect() {
        //Log.i(TAG, "Main thread Id " + Thread.currentThread().getId());
        //mViewModel.setIsConnected(true);
        try {
            if (!isMyServiceRunning(ServerConnection.class)) {
                final Intent intent = getMyIntent();
                intent.setAction(Constants.STARTFOREGROUND_ACTION);
                ContextCompat.startForegroundService(getContext(), intent);
                if (mSelector != PASSENGER_TAXI_ONLY) {
                    MySharedPreference.getInstance(this).setCaptainOnline(true);
                }
                bindMyService(intent);
            }
        } catch (Exception e) {

        }
    }

    private Context getContext() {
        //return MwmApplication.get().getApplicationContext();
        return MapActivity.this;
    }

    private void disconnect() {
        //Log.i(TAG, "Stop service called ");
        try {
            //if(isMyServiceRunning(ServerConnection.class)) {
            Intent stopIntent = getMyIntent();
            unBindMyService();
            stopIntent.setAction(Constants.STOPFOREGROUND_ACTION);
            ContextCompat.startForegroundService(getContext(), stopIntent);
            //MySharedPreference.getInstance(this).setCaptainOnline(false);
            //Log.i(TAG, "Stop service called inside");
        } catch (Exception e) {

        }
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

    private Intent getMyIntent() {
        Context context = getContext();
        return new Intent(context, ServerConnection.class);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        genderCargoTxt = text;
        genderCargoId = position;
    }

    private boolean isValidateFrom() {
        showProgress(false);
        return fromLocation != null;
    }

    private boolean isValidateFromAndTo() {
        showProgress(false);
        return fromLocation != null && toLocation != null && tvDistance != null;
    }

    private void saveAndSearchPost() {
        if (isValidateFromAndTo()) {
            showProgress(true);
            MySharedPreference.getInstance(this).userTripInfo(fromLocation.getLat(),
                    fromLocation.getLon(),
                    toLocation.getLat(),
                    toLocation.getLon(), myDistance, mSourceAddress, mDestinationAddress,
                    startingTime);
            clearForNewTrip();
            createPost();
            isOnRequestBtn = true;
        } else {
            Toast.makeText(this, getString(R.string.enter_valid_address), Toast.LENGTH_LONG).show();
        }
    }

    private void showProgress(boolean isTrue) {
        if (isTrue) {
            mMyprogress.setVisibility(View.VISIBLE);
        } else {
            mMyprogress.setVisibility(View.GONE);
        }
    }

    private void alertDialogCloseMe() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setMessage(getString(R.string.sure_cancel)).setPositiveButton(getString(R.string.yes),
                closeMeDialogClickListener)
                .setNegativeButton(getString(R.string.no), closeMeDialogClickListener).show();

    }

    DialogInterface.OnClickListener closeMeDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    cancelTrip();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //Toast.makeText(MapActivity.this, getString(R.string.error_occured), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void cancelTrip() {
        //todo write close logic
        //String notify = MySharedPreference.getInstance(this).getUserNotification();
        //userMessage = gSon.fromJson(notify, UserMessage.class);
        GetMyHistory getMyHistory = new GetMyHistory(
                MySharedPreference.getInstance(this).getUserId(),
                MySharedPreference.getInstance(this).getPhoneNumber()
        );
        Call<IsValid> call = userMessageApi.sendPassengerCancelled(getMyHistory);
        call.enqueue(new Callback<IsValid>() {
            @Override
            public void onResponse(Call<IsValid> call, Response<IsValid> response) {
                if(response.body().getFlag() == 1) {
                    closeMyNotification(true);
                    reloadMe();
                } else {
                    Toast.makeText(MapActivity.this,"Sorry!,Unable to cancel now",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<IsValid> call, Throwable t) {
                Toast.makeText(MapActivity.this,"Sorry! Unable to connect",Toast.LENGTH_LONG).show();
            }
        });
        //reloadMe();
    }

    private void closeMyNotification(boolean isClose) {
        if(isClose) {
            mCloseNotification.setVisibility(View.GONE);
            mCloseList.setVisibility(View.GONE);
            MySharedPreference.getInstance(MapActivity.this).addActiveProcess(0);
            MySharedPreference.getInstance(MapActivity.this).userNotification(null);
            //reloadMe();
        }
    }

    // save trip time
    private boolean isTripTimeLesser() {
        long tripTime = MySharedPreference.getInstance(this).getStartTime();
        Date dt = DateUtils.timeMinusFifteen(new Date());
        //Date dt = new Date();
        return tripTime <= dt.getTime();
    }

    private int cancelFlag = Constants.Notification.PASSENGER_CANCELLED;
    private void closeList() {
        // todo check condition
        cancelFlag = Constants.Notification.DRIVER_CANCELLED;
        alertDialogCloseMe();
        /*if (isTripTimeLesser()) {
            alertDialogCloseMe();
        } else {
            Toast.makeText(MapActivity.this, this.getString(R.string.still_trip_valid), Toast.LENGTH_LONG).show();
        }*/
        //MySharedPreference.getInstance(MapActivity.this).addActiveProcess(0);
        //reloadMe();
    }

    private void closeNotification() {
        // todo check condition
        cancelFlag = Constants.Notification.PASSENGER_CANCELLED;
        alertDialogCloseMe();
        /*if (isTripTimeLesser() && !MySharedPreference.getInstance(this).getStartStatus()) {
            alertDialogCloseMe();
        } else {
            Toast.makeText(MapActivity.this, this.getString(R.string.still_trip_valid), Toast.LENGTH_LONG).show();
        }*/
        //MySharedPreference.getInstance(MapActivity.this).addActiveProcess(0);
        //reloadMe();
    }

    private void wstest() {
        //mService.sendMessage(4,1,1.1,1.2,1.1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_zoom_in:
                MapFragment.nativeScalePlus();
                break;
            case R.id.nav_zoom_out:
                MapFragment.nativeScaleMinus();
                break;
            case R.id.closable_l:
                //closeList();
                break;
            case R.id.closable_n:
                closeNotification();
                break;
            case R.id.addressToggle:
                toggleAddress();
                break;
            case R.id.closeDest:
                closeDestAddress();
                break;
            case R.id.confirm_list:
                if (selectionList.size() > 0) {
                    sendConfirmList();
                } else {
                    Toast.makeText(this, getString(R.string.add_seats), Toast.LENGTH_LONG).show();
                }
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
                hideBtnRequest();
                showProgress(true);
                if (mSelector == PASSENGER_TAXI_ONLY) {
                    if(isValidateFrom()) {
                        showConfirmDialog();
                    } else {
                        Toast.makeText(this,getString(R.string.enter_valid_address),Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(isValidateFromAndTo()) {
                        showConfirmDialog();
                    } else {
                        Toast.makeText(this,getString(R.string.enter_valid_address),Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.date_time:
                dateTime();
                break;
            //case R.id.send_request_test:
            //sendMe();
            //getNearestDriver();
            //break;
            case R.id.add_seat:
                if (seatCount < 4) {
                    seatCount++;
                    setSeat();
                    //getCalculatedPrice();
                } else {
                    Toast.makeText(this, getString(R.string.max_4_seats), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.remove_seat:
                if (seatCount > 1) {
                    seatCount--;
                    setSeat();
                    //getCalculatedPrice();
                }
                break;
            case R.id.tv_pickup:
                isPickupSearch = true;
                showProgress(true);
                showHideRequest(true);
                showSearch();
                break;
            case R.id.tv_dropoff:
                isPickupSearch = false;
                showProgress(true);
                showHideRequest(true);
                showSearch();
                break;
            case R.id.set_pickup:
                //showProgress(true);
                myDistance = "0";
                showProgress(true);
                //showBtnRequest();
                //if()
                //if(toLocation != null) {
                showHideRequest(false);
                //}
                setPickup();
                break;
            case R.id.set_drop:
                myDistance = "0";
                showProgress(true);
                //if(fromLocation != null) {
                showHideRequest(false);
                //}
                setDropoff();
                break;
            case R.id.cancelRequest:
                cancelDriver();
            break;
            case R.id.finish_trip:
                cancelConfirmedRequest(1);
                break;
            case R.id.start_trip:
                if (MySharedPreference.getInstance(this).getStartStatus()) {
                    finishConfirmedTrip();
                } else {
                    startConfirmedTrip();
                }
                break;
            case R.id.plan_trip:
                planConfirmedTrip();
                break;
            case R.id.driverPhone:
                callDriver();
                break;
            case R.id.openGMap:
                if (g.getLat() > 0) {
                    try {
                        MapObject m = LocationHelper.INSTANCE.getMyPosition();
                        openInGoogleMap(m.getLat(), m.getLon(), g.getLat(), g.getLng());
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private EditText mEtComments;

    private MapObject getMapObj(double lat, double lng) {
        return MapObject.createMapObject(FeatureId.EMPTY, MapObject.POI, "", "",
                lat, lng);
    }

    private void clearForNewTrip() {
        MySharedPreference.getInstance(this).clearStartStatus();
    }

    private void turnStartToFinish() {
        // todo
        mStartTrip.setText("Finish Trip");
        mCloseList.setVisibility(View.GONE);
        mFinishTrip.setVisibility(View.GONE);
        MySharedPreference.getInstance(this).addStartStatus();
    }

    private void startConfirmedTrip() {
        //  todo send trip started information to all list
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.inform_start_trip));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // todo send trip start message to all users
                if (confirmedUserList.size() > 0) {

                    sendUserMessage(confirmedUserList, Constants.Notification.TRIP_STARTED);
                }
                //MySharedPreference.getInstance(MapActivity.this).addActiveProcess(0);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setSeat() {
        mRequiredSeats.setText("" + seatCount);
    }

    private void toggleAddress() {
        if (toLocation != null) {
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
            //RoutingController.get().swapPoints();
            mAddressToggle.animate().rotation(mAddressToggle.getRotation() + 360).start();
        }
    }

    private void closeDestAddress() {
        if (toLocation != null) {
            toLocation = null;
            tvDropOff.setText(R.string.set_destination);
        }
    }

    @Override
    public void onMapObjectActivated(MapObject object) {
        if (MapObject.isOfType(MapObject.API_POINT, object)) {
            final ParsedMwmRequest request = ParsedMwmRequest.getCurrentRequest();
            if (request == null)
                return;

            request.setPointData(object.getLat(), object.getLon(), object.getTitle(), object.getApiId());
            object.setSubtitle(request.getCallerName(MwmApplication.get()).toString());
        }
        tempLocation = object;
        if (!isResultBySearch) {
            if (!isOnRequestBtn) {
                showMenu();
            }
            if(mSelector == CAPTAIN_TAXI_ONLY) {
                showToMenu();
            }
        } else {
            isResultBySearch = false;
            if (isPickupSearch) {
                setPickup();
            } else {
                setDropoff();
            }
        }
        showProgress(false);
    }

    private void showMenu() {
        mMnuForm.setVisibility(View.VISIBLE);
    }

    private void showToMenu() {
        mMnuForm.setVisibility(View.VISIBLE);
        mSetPickup.setVisibility(View.GONE);
        mSetDrop.setText(R.string.set_destination);
    }

    private void hideMenu() {
        mMnuForm.setVisibility(View.GONE);
    }

    private void setPickup() {
        //addTestBookMark();
        myDistance = "0";
        //Log.d(TAG,"my distance - "+myDistance);
        fromLocation = tempLocation;
        mSourceAddress = fromLocation.getTitle();
        tvPickup.setText(mSourceAddress);
        hideMenu();
        hideBtnRequest();
        RoutingController.get().setStartPoint(fromLocation);
    }

    private void setDropoff() {
        myDistance = "0";
        //Log.d(TAG,"my distance - "+myDistance);
        toLocation = tempLocation;
        mDestinationAddress = toLocation.getTitle();
        tvDropOff.setText(mDestinationAddress);
        hideMenu();
        hideBtnRequest();
        RoutingController.get().setEndPoint(toLocation);
    }

    @Override
    public void updateBuildProgress(int progress, int router) {
        final RoutingInfo rinfo = RoutingController.get().getCachedRoutingInfo();
        if (rinfo != null) {
            myDistance = rinfo.distToTarget;
            String units = rinfo.distToTarget + " " + rinfo.targetUnits;
            tvDistance.setText(units);
            showBtnRequest();
        }
    }

    private void getPrice(String myDistance, int mSelector) {
        double t = Double.parseDouble(myDistance);
        //Log.d(TAG,"my distance - "+t);
        if(t > 1) {
            PostApi postApi = ApiClient.getClient().create(PostApi.class);
            Price price = new Price(0.0, myDistance, mSelector);
            Call<Price> call = postApi.createPrice(price);
            call.enqueue(new Callback<Price>() {
                @Override
                public void onResponse(Call<Price> call, Response<Price> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(MapActivity.this, getString(R.string.error_calc_price), Toast.LENGTH_LONG).show();
                        return;
                    }
                    tripPrice = roundTwoDecimals(response.body().getPrice());
                    if (!isOnRequestBtn) {
                        getCalculatedPrice();
                    }
                }

                @Override
                public void onFailure(Call<Price> call, Throwable t) {
                    Toast.makeText(MapActivity.this, getString(R.string.error_calc_price), Toast.LENGTH_LONG).show();
                }
            });
        }
        showBtnRequest();
    }

    private void getCalculatedPrice() {
        if (tripPrice > 0.0d) {
            tripSeatPrice = roundTwoDecimals(tripPrice * seatCount);
            final String s = "" + tripSeatPrice + getString(R.string.sdg);
            mPriceText.setText(s);
        }
        if (mSelector == PASSENGER_TAXI_ONLY) {
            if (isValidateFrom()) {
                showConfirmDialog();
            } else {
                Toast.makeText(MapActivity.this, getString(R.string.enter_valid_address), Toast.LENGTH_LONG).show();
            }
        } else {
            if (isValidateFromAndTo()) {
                showConfirmDialog();
            } else {
                Toast.makeText(MapActivity.this, getString(R.string.enter_valid_address), Toast.LENGTH_LONG).show();
            }
        }
        showProgress(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Framework.nativeSetMapObjectListener(this);
        RoutingController.get().attach(this);
        if (MapFragment.nativeIsEngineCreated()) {
            LocationHelper.INSTANCE.attach(this);
        }
    }

    @Override
    protected void onSafeDestroy() {
        super.onSafeDestroy();
        if(mSelector == PASSENGER_TAXI_ONLY) {
            disconnect();
        }
        removePointsAndRoute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Framework.nativeRemoveMapObjectListener();
        LocationHelper.INSTANCE.detach(!isFinishing());
        RoutingController.get().detach();
    }

    private void removePointsAndRoute() {
        if (fromLocation != null) {
            Framework.nativeRemoveRoutePoint(ROUTE_MARK_START, 0);
        }
        if (toLocation != null) {
            Framework.nativeRemoveRoutePoint(RoutePointInfo.ROUTE_MARK_FINISH, 0);
        }
        Framework.nativeRemoveRoute();
    }

    private void dateTime() {
        new SingleDateAndTimePickerDialog.Builder(this)
                .bottomSheet()
                .minDateRange(new Date())
                .displayListener(picker -> {
                })
                .title(getString(R.string.sel_date_time))
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

    private void prepareGoToCustomer() {
        isOnWaytoCustomer = true;
    }

    private void updateResponse(int responseId) {
        requestResponse = responseId;
    }

    //private int cancelFlag = 0;

    private void alertDialogCancelPassenger() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setMessage(getString(R.string.sure_cancel)).setPositiveButton(getString(R.string.yes), cancelPassengerDialogClickListener)
                .setNegativeButton(getString(R.string.no), cancelPassengerDialogClickListener).show();
    }

    DialogInterface.OnClickListener cancelPassengerDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    updateStatus(cancelFlag);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(MapActivity.this, getString(R.string.req_not_send), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void reloadMe() {
        Intent intent = new Intent(this, SelectorActivity.class);
        finish();
        startActivity(intent);
    }

    private void displayConf() {
        int activeProcess = MySharedPreference.getInstance(this).getActiveProcess();
        if (activeProcess == Constants.ActiveProcess.CAPTAIN_HAVE_CONFIRMED_LIST) {
            isOnRequestBtn = true;
            displayConfirmedList();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mMapFragment == null) {
            Bundle args = new Bundle();
            args.putBoolean(MapFragment.ARG_LAUNCH_BY_DEEP_LINK, isLaunchByDeepLink);
            mMapFragment = (MapFragment) MapFragment.instantiate(this, MapFragment.class.getName(), args);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_fragment_container, mMapFragment, MapFragment.class.getName())
                    .commit();
        } else {
            mMapFragment.getView().setBackgroundColor(Color.WHITE);
        }
        displayConf();
        String msg = MySharedPreference.getInstance(this).getUserMessage();
        if (msg != null) {
            isOnRequestBtn = true;
            isSelectorFree = false;
            //processMessage(msg);
            MySharedPreference.getInstance(this).userMessage(null);
        }

        String notify = MySharedPreference.getInstance(this).getUserNotification();
        if (notify != null) {
            isSelectorFree = false;
            processNotification(notify, true);
        }

        String tripId = MySharedPreference.getInstance(this).getTripId();
        if (tripId != null) {
            this.tripId = tripId;
            isSelectorFree = false;
            // todo handle unclosed trip
            //Toast.makeText(this,"Last trip not ended properly",Toast.LENGTH_LONG).show();
            isOnWaytoCustomer = false;
            isOnTrip = true;
            price = 1;
            mCustomerInfo.setVisibility(View.VISIBLE);
            mAcceptBusyInfo.setVisibility(View.GONE);
            mSwipeLayout.setVisibility(View.VISIBLE);
            mSwipeButton.setText(getString(R.string.end_trip));
        }
        if (isSelectorFree) {
            switch (mSelector) {
                case PASSENGER_SHARE_ONLY:
                case PASSENGER_ANY:
                    prepareForAll();
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int captRes = MySharedPreference.getInstance(this).getCaptRespId();
        if(captRes == ACCEPT_REQUEST) {
            Toast.makeText(this,"Captain Accepted Test",Toast.LENGTH_LONG).show();
            clearRequest();
        }
        if(captRes == SEND_BUSY) {
            Toast.makeText(this,"Captain Busy Test",Toast.LENGTH_LONG).show();
            clearRequest();
        }
        final Intent intent = getMyIntent();
        if(MySharedPreference.getInstance(this).isCaptainOnline() || mSelector == PASSENGER_TAXI_ONLY) {
            bindMyService(intent);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(ServerConnection.ACTION_MSG_RECEIVED));
        }
    }

    private void clearRequest() {
        MySharedPreference.getInstance(this).setCaptRespId(0);
    }

    private void displayConfirmedList() {
        confirmedUserList = new ArrayList<>();
        confirmedUserList = MySharedPreference.getInstance(this).getListConfirmed(CONFIRMED_LIST_KEY);
        setFullscreen(true);
        hideFromTo();
        mSwitch.setVisibility(View.GONE);
        mConfirmLayout.setVisibility(View.GONE);
        mStartTripLayout.setVisibility(View.VISIBLE);
        if (MySharedPreference.getInstance(this).getStartStatus()) {
            mStartTrip.setText("Finish Trip");
            mFinishTrip.setVisibility(View.GONE);
        }
        mConfirmedAdapter = new ConfirmedListPagerAdapter(confirmedUserList, this, getSupportFragmentManager());
        mViewPager.setAdapter(mConfirmedAdapter);
    }

    private Long getCurrentTimestamp() {
        long timeMillis = System.currentTimeMillis();
        return TimeUnit.MILLISECONDS.toSeconds(timeMillis);
    }

    private double roundTwoDecimals(double d) {
        return new BigDecimal(d).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private final Runnable recordTripRunnable = new Runnable() {
        @Override
        public void run() {
            MySharedPreference.getInstance(MapActivity.this).recordTrip(tripId, tripStartTime, (float) distance);
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

    private void callDriver() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.denied_make_call), Toast.LENGTH_LONG).show();
            return;
        }
        this.startActivity(intent);
    }

    private int addedSeats = 0;

    @Override
    public void selectMatch(int position, boolean isAdd) {
        double amount = mMatchingList.get(position).getPrice();
        if (isAdd) {
            totAmount += amount;
            addedSeats += mMatchingList.get(position).getSeats();
            //Log.d(TAG,"Seats : "+ mMatchingList.get(position).getSeats());
            totAmount = roundTwoDecimals(totAmount);
            selectionList.put(position, mMatchingList.get(position).getId());
            mListCount.setText(getString(R.string.seats) + addedSeats);
            mListAmount.setText(totAmount + getString(R.string.sdg));
        } else {
            totAmount -= amount;
            addedSeats -= mMatchingList.get(position).getSeats();
            totAmount = roundTwoDecimals(totAmount);
            selectionList.remove(position);
            mListCount.setText(getString(R.string.seats) + addedSeats);
            mListAmount.setText(totAmount + getString(R.string.sdg));
        }
    }

    private List<UserMessage> listWithMyPhone(List<UserMessage> userMessageList) {
        List<UserMessage> userMessageAll = new ArrayList<>();
        for (UserMessage userMessage : userMessageList) {
            userMessage.setPhone(MySharedPreference.getInstance(this).getPhoneNumber());
            userMessage.setName(MySharedPreference.getInstance(this).getUserName());
            userMessageAll.add(userMessage);
        }
        return userMessageList;
    }

    private void sendConfirmList() {
        alertDialog();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //checkBeforeConfirm();
                    sendConfirmation();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(MapActivity.this, getString(R.string.req_not_send), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void showUserAlreadyConfirmed() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    dialog.dismiss();
                    break;
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setMessage("Sorry! The User you have chosen confirmed by other Captain!").setPositiveButton("Dismiss", dialogClickListener)
                .show();
    }

    private void checkBeforeConfirm(List<UserMessage> userMessageList, List<UserMessage> userMessageListSave) {
        // todo api
        Call<IsValid> call;
        call = userMessageApi.checkBeforeConfirm(userMessageList);
        UserMessageApi userMessageApi = ApiClient.getClient().create(UserMessageApi.class);
        call.enqueue(new Callback<IsValid>() {
            @Override
            public void onResponse(Call<IsValid> call, Response<IsValid> response) {
                if (response.body().getFlag() == 1) {
                    sendUserMessage(userMessageList, Constants.Notification.DRIVER_ACCEPTED);
                    MySharedPreference.getInstance(MapActivity.this).addActiveProcess(Constants.ActiveProcess.CAPTAIN_HAVE_CONFIRMED_LIST);
                    MySharedPreference.getInstance(MapActivity.this).putListConfirmed(CONFIRMED_LIST_KEY, userMessageListSave);
                    timeOutClose();
                    displayConf();
                    Toast.makeText(MapActivity.this, getString(R.string.success_sent_req), Toast.LENGTH_LONG).show();
                } else {
                    showUserAlreadyConfirmed();
                }
            }

            @Override
            public void onFailure(Call<IsValid> call, Throwable t) {
                showUserAlreadyConfirmed();
            }
        });
    }

    private void sendConfirmation() {
        List<UserMessage> userMessageList = new ArrayList<>();
        List<UserMessage> userMessageListM = new ArrayList<>();
        UserMessage userMessage1;
        for (Map.Entry<Integer, Integer> entry : selectionList.entrySet()) {
            Integer position = entry.getKey();
            Integer value = entry.getValue();
            userMessage = new UserMessage(
                    MySharedPreference.getInstance(this).getUserId(),
                    mMatchingList.get(position).getUserId(),
                    getFlag(), mMatchingList.get(position).getId(),
                    Double.valueOf(mMatchingList.get(position).getmTotDistTxt()),
                    String.valueOf(mMatchingList.get(position).getPrice()),
                    mMatchingList.get(position).getmTripTime(),
                    mMatchingList.get(position).getmPhone(),
                    mMatchingList.get(position).getName(),
                    mMatchingList.get(position).getmText1(),
                    mMatchingList.get(position).getmText2(),
                    mMatchingList.get(position).getDropDownVal(),
                    mMatchingList.get(position).getfLat(),
                    mMatchingList.get(position).getfLng(),
                    mMatchingList.get(position).gettLat(),
                    mMatchingList.get(position).gettLng()
            );
            userMessage1 = new UserMessage(
                    MySharedPreference.getInstance(this).getUserId(),
                    mMatchingList.get(position).getUserId(),
                    getFlag(), mMatchingList.get(position).getId(),
                    Double.valueOf(mMatchingList.get(position).getmTotDistTxt()),
                    String.valueOf(mMatchingList.get(position).getPrice()),
                    mMatchingList.get(position).getmTripTime(),
                    MySharedPreference.getInstance(this).getPhoneNumber(),
                    MySharedPreference.getInstance(this).getUserName(),
                    mMatchingList.get(position).getmText1(),
                    mMatchingList.get(position).getmText2(),
                    mMatchingList.get(position).getDropDownVal(),
                    mMatchingList.get(position).getfLat(),
                    mMatchingList.get(position).getfLng(),
                    mMatchingList.get(position).gettLat(),
                    mMatchingList.get(position).gettLng()
            );
            userMessageList.add(userMessage);
            userMessageListM.add(userMessage1);
        }
        checkBeforeConfirm(userMessageListM, userMessageList);
    }

    private void sendUserMessage(List<UserMessage> userMessageList, int flag) {
        Call<UserMessage> call;
        boolean isCloseNotify = false;
        boolean isConfirmed = false;
        UserMessageApi userMessageApi = ApiClient.getClient().create(UserMessageApi.class);
        switch (flag) {
            case Constants.Notification.DRIVER_ACCEPTED:
                call = userMessageApi.sendConfirmation(userMessageList);
                break;
            case Constants.Notification.TRIP_STARTED:
                isConfirmed = true;
                call = userMessageApi.sendTripStarted(listWithMyPhone(userMessageList));
                break;
            case Constants.Notification.TRIP_COMPLETED:
                call = userMessageApi.sendTripCompleted(listWithMyPhone(userMessageList));
                isCloseNotify = true;
                break;
            case Constants.Notification.DRIVER_CANCELLED:
                call = userMessageApi.sendTripCancelled(listWithMyPhone(userMessageList));
                isCloseNotify = true;
                break;
            default:
                return;
        }
        boolean finalIsCloseNotify = isCloseNotify;
        boolean finalIsConfirmed = isConfirmed;
        call.enqueue(new Callback<UserMessage>() {
            @Override
            public void onResponse(Call<UserMessage> call, Response<UserMessage> response) {
                Toast.makeText(MapActivity.this, getString(R.string.success_sent_req), Toast.LENGTH_LONG).show();
                displayConfirmedList();
                if(finalIsCloseNotify) {
                    closeMyNotification(true);
                    reloadMe();
                }

                if(finalIsConfirmed) {
                    turnStartToFinish();
                }

            }

            @Override
            public void onFailure(Call<UserMessage> call, Throwable t) {
                //Toast.makeText(MapActivity.this, "Request Send Failed! ", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showConfirmDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View view = layoutInflater.inflate(R.layout.confirm_layout, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Are You Sure? Confirm?");
        //alertDialog.setIcon("Icon id here");
        alertDialog.setCancelable(false);
        final TextView cFrom = view.findViewById(R.id.c_from);
        cFrom.setText(mSourceAddress);
        final TextView cTo = view.findViewById(R.id.c_to);
        cTo.setText(mDestinationAddress);
        final TextView cDist = view.findViewById(R.id.c_distance);
        cDist.setText(myDistance + " KM");
        final TextView cAmount = view.findViewById(R.id.c_amount);
        cAmount.setText(tripSeatPrice + " SDG");
        mEtComments = view.findViewById(R.id.c_note);

        final TextView cTime = view.findViewById(R.id.c_time);
        cTime.setText(DateUtils.formatDateCustom(startingTime));
        final TextView cSeats = view.findViewById(R.id.c_seats);
        cSeats.setText(seatCount + "");
        final TextView cGender = view.findViewById(R.id.c_gender);
        cGender.setText(genderCargoTxt);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mSelector == PASSENGER_TAXI_ONLY) {
                    getNearestDriver();
                } else {
                    saveAndSearchPost();
                }
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                showBtnRequest();
            }
        });
        alertDialog.setView(view);
        alertDialog.show();
    }

    private void finishConfirmedTrip() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.sure_finish_current));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // todo send trip finished message
                //cancelRequest()
                if (confirmedUserList.size() > 0) {
                    sendUserMessage(confirmedUserList, Constants.Notification.TRIP_COMPLETED);
                }
                //MySharedPreference.getInstance(MapActivity.this).addActiveProcess(0);
                //reloadMe();
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void planConfirmedTrip() {
        showProgress(true);
        removePointsAndRoute();
        int size = confirmedUserList.size();
        fromLocation = LocationHelper.INSTANCE.getMyPosition();
        RoutingController.get().setStartPoint(fromLocation);
        MapObject m = getMapObj(confirmedUserList.get(0).getfLat(), confirmedUserList.get(0).getfLng());
        MapObject m1 = getMapObj(confirmedUserList.get(0).gettLat(), confirmedUserList.get(0).gettLng());
        switch (size) {
            case 2:
                MapObject m2 = getMapObj(confirmedUserList.get(1).getfLat(), confirmedUserList.get(1).getfLng());
                MapObject m3 = getMapObj(confirmedUserList.get(1).gettLat(), confirmedUserList.get(1).gettLng());
                RoutingController.get().setEndPoint(m3);
                RoutingController.get().addStop(m);
                RoutingController.get().addStop(m2);
                break;
            case 3:
                MapObject mx = getMapObj(confirmedUserList.get(1).getfLat(), confirmedUserList.get(1).getfLng());
                MapObject m4 = getMapObj(confirmedUserList.get(2).getfLat(), confirmedUserList.get(2).getfLng());
                MapObject m5 = getMapObj(confirmedUserList.get(2).gettLat(), confirmedUserList.get(2).gettLng());
                RoutingController.get().setEndPoint(m5);
                RoutingController.get().addStop(m);
                RoutingController.get().addStop(mx);
                RoutingController.get().addStop(m4);
                break;
            case 4:
                MapObject m1x = getMapObj(confirmedUserList.get(1).getfLat(), confirmedUserList.get(1).getfLng());
                MapObject m2x = getMapObj(confirmedUserList.get(2).getfLat(), confirmedUserList.get(2).getfLng());
                MapObject m7 = getMapObj(confirmedUserList.get(3).gettLat(), confirmedUserList.get(3).gettLng());
                RoutingController.get().setEndPoint(m7);
                RoutingController.get().addStop(m);
                RoutingController.get().addStop(m1x);
                RoutingController.get().addStop(m2x);
                break;
            case 1:
            default:
                RoutingController.get().setEndPoint(m1);
                RoutingController.get().addStop(m);
                break;
        }
        showProgress(false);
    }

    private void timeOutClose() {
        // todo close
        setFullscreen(false);
        mMatchingList = new ArrayList<>();
        if (isConfirmationTimerOn) {
            stopTimeOutTimer();
        }
    }

    private void startTimeOutTimer() {
        mTimeOutTimer = new CountDownTimer(START_TIME_OUT_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Log.i(TAG,"Timer Started...");
            }

            @Override
            public void onFinish() {
                isConfirmationTimerOn = false;
                timeOutClose();
                //Log.i(TAG,"Finished timer");
            }
        }.start();
        isConfirmationTimerOn = true;
    }

    private void stopTimeOutTimer() {
        mTimeOutTimer.cancel();
        isConfirmationTimerOn = false;
        //Log.i(TAG,"Timer Stopped");
    }


    private void updateStatus(int Status) {
        //PostApi postApi =
        PostApi postApi = ApiClient.getClient().create(PostApi.class);
        StatusUpdate statusUpdate = new StatusUpdate(MySharedPreference.getInstance(this).getUserId(), Status);

        Call<StatusUpdate> call = postApi.updateStatus(statusUpdate);

        call.enqueue(new Callback<StatusUpdate>() {
            @Override
            public void onResponse(Call<StatusUpdate> call, Response<StatusUpdate> response) {
                //Log.d(TAG,"Got response inside");
                if (!response.isSuccessful()) {
                    return;
                }
                //Log.d(TAG,"Got response success");
                MySharedPreference.getInstance(MapActivity.this).userNotification(null);
                reloadMe();
            }

            @Override
            public void onFailure(Call<StatusUpdate> call, Throwable t) {

            }
        });
    }

    private void processNotification(String myNotification, boolean isFromNotify) {
        boolean isCloseNotify = false;
        isOnRequestBtn = true;
        hideFromTo();
        mSwitch.setVisibility(View.GONE);
        mPriceText.setVisibility(View.GONE);
        TextView from = findViewById(R.id.n_textView);
        TextView to = findViewById(R.id.n_textView2);
        TextView stTime = findViewById(R.id.n_start_time);
        TextView urDistance = findViewById(R.id.n_your_distance);
        TextView urPrice = findViewById(R.id.n_trip_amount);
        Button accept = findViewById(R.id.n_accept_request);

        ProgressBar pb = findViewById(R.id.waiting_progress);
        TextView wtv = findViewById(R.id.waiting_for_c);

        TextView mTextView = findViewById(R.id.n_notification_title);
        TextView stPhone = findViewById(R.id.n_user_phone);
        LinearLayout llButton = findViewById(R.id.ll__button);
        TextView stName = findViewById(R.id.n_user_name);
        int mFlag = 999;
        //if(isFromNotify) {
        userMessage = gSon.fromJson(myNotification, UserMessage.class);
        double tripId = userMessage.getTripId();
        //Button reject = findViewById(R.id.n_deny_request);
        from.setText(userMessage.getfAddress());
        to.setText(userMessage.gettAddress());
        stTime.setText("" + userMessage.getmTripTime());
        urDistance.setText("Distance : " + userMessage.getDistance() + " KM");
        urPrice.setText("Amount " + userMessage.getPrice() + " SDG");
        mFlag = userMessage.getmFlag();

        if (mFlag != 0) {
            mNotificationCard.setVisibility(View.VISIBLE);
        }
        switch (mFlag) {
            case Constants.Notification.PASSENGER_REQUEST:
                mTextView.setText(getString(R.string.passenger_ride_request));
                break;
            case Constants.Notification.PASSENGER_ACCEPTED:
                mTextView.setText(getString(R.string.passenger_accepted_request));
                break;
            case Constants.Notification.PASSENGER_CANCELLED:
                mCloseNotification.setVisibility(View.GONE);
                isCloseNotify = true;
                mTextView.setText(getString(R.string.passenger_cancel));
                break;
            case Constants.Notification.DRIVER_INVITE:
                mTextView.setText(getString(R.string.captain_invitation));
                break;
            case Constants.Notification.DRIVER_ACCEPTED:
                // todo show captain name and phone
                pb.setVisibility(View.GONE);
                wtv.setVisibility(View.GONE);
                phoneNumber = "0" + userMessage.getPhone();
                stName.setText("Captain : " + userMessage.getName());
                stPhone.setText("Phone : " + phoneNumber);
                mTextView.setText(getString(R.string.captain_accepted));
                stName.setVisibility(View.VISIBLE);
                stPhone.setVisibility(View.VISIBLE);
                break;
            case Constants.Notification.DRIVER_REFUSED:
                isCloseNotify = true;
                mTextView.setText(getString(R.string.captain_refused));
                break;
            case Constants.Notification.DRIVER_CANCELLED:
                isCloseNotify = true;
                pb.setVisibility(View.GONE);
                wtv.setVisibility(View.GONE);
                mTextView.setText(getString(R.string.captain_cancelled));
                accept.setVisibility(View.GONE);
                break;
            case Constants.Notification.TRIP_COMPLETED:
                isCloseNotify = true;
                pb.setVisibility(View.GONE);
                wtv.setVisibility(View.GONE);
                phoneNumber = "0" + userMessage.getPhone();
                stName.setText("Captain : " + userMessage.getName());
                stPhone.setText("Phone : " + phoneNumber);
                mTextView.setText(getString(R.string.trip_completed));
                stName.setVisibility(View.VISIBLE);
                stPhone.setVisibility(View.VISIBLE);
                accept.setVisibility(View.GONE);
                break;
            case Constants.Notification.DRIVER_REACHED:
                mTextView.setText(getString(R.string.captain_reached));
                break;
            case Constants.Notification.TRIP_STARTED:
                pb.setVisibility(View.GONE);
                wtv.setVisibility(View.GONE);
                phoneNumber = "0" + userMessage.getPhone();
                stName.setText("Captain : " + userMessage.getName());
                stPhone.setText("Phone : " + phoneNumber);
                mTextView.setText(getString(R.string.trip_started));
                stName.setVisibility(View.VISIBLE);
                stPhone.setVisibility(View.VISIBLE);
                break;
        }
        int acceptButtonFlag = Constants.Notification.DRIVER_ACCEPTED;
        int rejectButtonFlag = Constants.Notification.DRIVER_REFUSED;
        if (mSelector < 4) {
            acceptButtonFlag = Constants.Notification.PASSENGER_ACCEPTED;
            rejectButtonFlag = Constants.Notification.PASSENGER_REFUSED;
        }

        int finalAcceptButtonFlag = acceptButtonFlag;
        int finalMFlag = mFlag;
        /* accept.setOnClickListener(view -> {
            // todo update in server
            // cancelFlag = finalMFlag;
            //alertDialogCancelPassenger();
            //updateStatus(finalMFlag);
            //userMessage.setmFlag(finalAcceptButtonFlag);

        });*/
        stPhone.setOnClickListener(view -> callDriver());

        closeMyNotification(isCloseNotify);
        /*int finalRejectButtonFlag = rejectButtonFlag;
        reject.setOnClickListener(view -> {
            userMessage.setmFlag(finalRejectButtonFlag);
        });*/
        //notification_req_res
    }


    private void setFullscreen(boolean isFullscreen) {
        mIsFullscreen = isFullscreen;
        if (isFullscreen) {
            mViewPager.setVisibility(View.VISIBLE);
            mConfirmLayout.setVisibility(View.VISIBLE);
            if (mNavAnimationController != null)
                mNavAnimationController.disappearZoomButtons();
            if (mNavMyPosition != null)
                mNavMyPosition.hide();
        } else {
            mViewPager.setVisibility(View.GONE);
            mConfirmLayout.setVisibility(View.GONE);
            if (mNavAnimationController != null)
                mNavAnimationController.appearZoomButtons();
            if (mNavMyPosition != null)
                mNavMyPosition.show();
        }
    }

    private double mMyTripDistance;
    ArrayList<MatchingItem> mMatchingList = new ArrayList<>();
    private static double ELEGIBLE_LIMIT = 1.4d;
    private UserMessage userMessage;
    private UserMessageApi userMessageApi;

    private void my() {
        if (mMatchingList.size() > 0) {
            setFullscreen(true);
            hideFromTo();
            mSwitch.setVisibility(View.GONE);
            //mPriceLayout.setVisibility(View.GONE);
            //mConfirmLayout.setVisibility(View.GONE);
            mAdapter = new MatchingStatePagerAdapter(mMatchingList, this, getSupportFragmentManager());
            mViewPager.setAdapter(mAdapter);
            startTimeOutTimer();
        } else {
            Toast.makeText(this, getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
        }
    }

    private int getSelector(int selector) {
        if (selector == CAPTAIN_SHARE_ONLY) {
            return PASSENGER_SHARE_ONLY;
        }
        return selector;
    }

    public void createPost() {
        mMyTripDistance = Double.parseDouble(MySharedPreference.getInstance(this).getTripDistance().trim());
        PostApi postApi = ApiClient.getClient().create(PostApi.class);
        //int sel = getSelector(mSelector);
        Post post = new Post(null, MySharedPreference.getInstance(this).getUserId(),
                MySharedPreference.getInstance(this).getFrmLat(),
                MySharedPreference.getInstance(this).getFrmLng(),
                MySharedPreference.getInstance(this).getToLat(),
                MySharedPreference.getInstance(this).getToLng(),
                mMyTripDistance,
                MySharedPreference.getInstance(this).getFrmAddress().trim(),
                MySharedPreference.getInstance(this).getToAddress().trim(),
                new Date(MySharedPreference.getInstance(this).getStartTime()),
                MySharedPreference.getInstance(this).getPhoneNumber(), seatCount, genderCargoId,
                genderCargoTxt, tripSeatPrice, mSelector,
                MySharedPreference.getInstance(MapActivity.this).getUserName(), mEtComments.getText().toString());
        //post.setSelectorFlag(mSelector);

        Call<List<Post>> call = postApi.createPost(post);

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                //Log.d(TAG,response.body().size()+"Size response");
                showProgress(false);
                //|| mSelector == PASSENGER_ANY
                if (mSelector == PASSENGER_SHARE_ONLY || mSelector == PASSENGER_ANY) {
                    Toast.makeText(MapActivity.this, getString(R.string.success_sent_req), Toast.LENGTH_LONG).show();
                    Map<String, String> data = new HashMap<>();
                    data.put("fUserId", MySharedPreference.getInstance(MapActivity.this).getUserId() + "");
                    data.put("tUserId", "0.0");
                    data.put("mFlag", "1");
                    data.put("tripId", "0.0");
                    data.put("distance", myDistance);
                    data.put("price", "" + tripSeatPrice);
                    data.put("mTripTime", "" + startingTime);
                    data.put("phone", post.getPhone());
                    data.put("name", post.getName());
                    data.put("fAddress", mSourceAddress);
                    data.put("tAddress", mDestinationAddress);
                    data.put("note", "0.0");
                    data.put("fLat", "0.0");
                    data.put("fLng", "0.0");
                    data.put("tLat", "0.0");
                    data.put("tLng", "0.0");
                    String usrNotification = gSon.toJson(data);
                    MySharedPreference.getInstance(MapActivity.this).userNotification(usrNotification);
                    MySharedPreference.getInstance(MapActivity.this).addActiveProcess(Constants.ActiveProcess.PASSENGER_HAVE_ACTIVE_RIDE);
                    processNotification(usrNotification, true);
                } else {
                    mMatchingList = new ArrayList<>();
                    createMatchList(response.body());
                    if (mSelector == CAPTAIN_ANY || mSelector == PASSENGER_ANY) {
                        cargo();
                    } else {
                        my();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(MapActivity.this, "failure : " + t.getMessage(), Toast.LENGTH_LONG).show();
                //Toast.makeText(MapActivity.this,getString(R.string.error_occured),Toast.LENGTH_LONG).show();
            }
        });
    }

    public void createMatchList(List<Post> body) {
        for (Post post : body) {
            double totDist = Utils.roundTwoDecimals(post.getSrcDistDiff() + post.getTripDistance() + post.getDestDistDiff());
            String amount = "";
            String extraDistance = "";
            insert(new MatchingItem(post.getId(), post.getUserId(),
                    post.getSourceAddress(), post.getDestinationAddress(),
                    DateUtils.formatDateStrGmt(post.getStartTime()), Double.toString(post.getTripDistance()),
                    Double.toString(totDist), extraDistance, post.getPhone(),
                    amount, mMyTripDistance, post.getSrcLat(), post.getSrcLng(), post.getDestLat(), post.getDestLng()
                    , post.getSeats(), post.getDropDownVal(), post.getPrice(), post.getName()));
        }
    }

    private void insert(MatchingItem matchingItem) {
        mMatchingList.add(matchingItem);
        //matchDao.insert(matchingItem);
    }

    // Common Code
    private double getPercentage(double a, double b) {
        return ((b * 100d) / a) / 100d;
    }

    // Cargo
    private void cargo() {
        if (mMatchingList.size() > 0) {
            setFullscreen(true);
            hideFromTo();
            mSwitch.setVisibility(View.GONE);
            //mPriceLayout.setVisibility(View.GONE);
            mConfirmLayout.setVisibility(View.GONE);
            mCargoAdapter = new CargoStatePagerAdapter(mMatchingList, this, getSupportFragmentManager());
            mViewPager.setAdapter(mCargoAdapter);
        } else {
            Toast.makeText(this, getString(R.string.no_record_found), Toast.LENGTH_LONG).show();
        }
    }

    //________________________empty methods
    @Override
    public void onRoutingFinish() {
    }

    @Override
    public void showNavigation(boolean show) {
    }

    @Override
    public void showDownloader(boolean openDownloaded) {
    }

    @Override
    public void updateMenu() {
    }

    @Override
    public void onTaxiInfoReceived(@NonNull TaxiInfo info) {
    }

    @Override
    public void onTaxiError(@NonNull TaxiManager.ErrorCode code) {
    }

    @Override
    public void onNavigationCancelled() {
    }

    @Override
    public void onNavigationStarted() {
    }

    @Override
    public void onAddedStop() {
    }

    @Override
    public void onRemovedStop() {
    }

    @Override
    public void onDrivingOptionsWarning() {
    }

    @Override
    public boolean isSubwayEnabled() {
        return false;
    }

    @Override
    public void onCommonBuildError(int lastResultCode, @NonNull String[] lastMissingMaps) {
    }

    @Override
    public void onDrivingOptionsBuildError() {
    }

    @Override
    public void onStartRouteBuilding() {
    }

    //_______________________ Almost never change methods

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

    private boolean showAddStartOrFinishFrame(@NonNull RoutingController controller, boolean showFrame) {
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

        if (myPosition != null && !controller.hasEndPoint()) {
            showAddFinishFrame();
            if (showFrame)
                showLineFrame();
            return true;
        }
        if (!controller.hasStartPoint()) {
            showAddStartFrame();
            if (showFrame)
                showLineFrame();
            return true;
        }
        if (!controller.hasEndPoint()) {
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

    private void showLocationNotFoundDialog() {
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
        if (show) {
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

    private void listMatch() {
        setFullscreen(!mIsFullscreen);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    void adjustCompass(int offsetY) {
        if (mMapFragment == null || !mMapFragment.isAdded())
            return;

        int resultOffset = offsetY;
        mMapFragment.setupCompass(resultOffset, true);
        CompassData compass = LocationHelper.INSTANCE.getCompassData();
        if (compass != null)
            MapFragment.nativeCompassUpdated(compass.getMagneticNorth(), compass.getTrueNorth(), true);
    }

    private void initNavigationButtons() {
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
        if (RoutingController.get().isPlanning() || RoutingController.get().isNavigating()) {
            if (!UiUtils.isLandscape(this))
                mNavigationController.fadeOutSearchButtons();
        }
    }

    private class CurrentPositionClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Statistics.INSTANCE.trackEvent(Statistics.EventName.TOOLBAR_MY_POSITION);
            AlohaHelper.logClick(AlohaHelper.TOOLBAR_MY_POSITION);

            if (!PermissionsUtils.isLocationGranted()) {
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


    List<FindDriver> mNearestDriver = new ArrayList<>();
    int listSize = 0;
    int listCurrent = 0;

    private void getNearestDriver() {
        showProgress(true);
        //String notIn = getNotIn();
        //Log.d(TAG,"User Id"+ MySharedPreference.getInstance(this).getUserId());
        if(isValidateFrom()) {
            double lat = fromLocation.getLat();
            double lng = fromLocation.getLon();
            FindDriver findDriver = new FindDriver(
                    MySharedPreference.getInstance(this).getUserId(), lat, lng,
                    0
            );
            FindDriverApi findDriverApi = ApiClient.getClient().create(FindDriverApi.class);
            Call<List<FindDriver>> call = findDriverApi.findDriver(findDriver);

            call.enqueue(new Callback<List<FindDriver>>() {
                @Override
                public void onResponse(Call<List<FindDriver>> call, Response<List<FindDriver>> response) {
                    if (response.isSuccessful()) {
                        isOnRequestBtn = true;
                        showProgress(false);
                        removeRequest();
                        mNearestDriver = new ArrayList<>();
                        mNearestDriver = response.body();
                        listCurrent = 0;
                        listSize = mNearestDriver.size();
                        //Log.d(TAG, "Sizze " + mNearestDriver.size());
                        if(listSize > 0) {
                            requestHandler.postDelayed(requestRunnable, 0);
                        } else {
                            Toast.makeText(MapActivity.this, getString(R.string.no_driver_found), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<FindDriver>> call, Throwable t) {
                    Toast.makeText(MapActivity.this, "Sorry! No Drivers found! Try Later", Toast.LENGTH_LONG).show();
                }

            });
        } else {
            Toast.makeText(this,getString(R.string.enter_valid_address),Toast.LENGTH_LONG).show();
        }
    }

    private final Runnable requestRunnable = new Runnable() {
        @Override
        public void run() {
            //Iterator<FindDriver> i = mNearestDriver.iterator();
            //Log.d(TAG,"Sizze "+ mNearestDriver.size());
            //for (FindDriver driverList : mNearestDriver) {
            //while (i.hasNext()) {
            FindDriver driverList = mNearestDriver.get(listCurrent);
            int dId = driverList.getUserId();
            if (dId > 0) {
                userTripInfo = new UserTripInfo(MySharedPreference.getInstance(MapActivity.this).getUserId(),
                        MySharedPreference.getInstance(getApplicationContext()).getPhoneNumber(),
                        MySharedPreference.getInstance(getApplicationContext()).getUserName(),
                        mSourceAddress,mDestinationAddress,fromLocation.getLat(),fromLocation.getLon(),
                        tripSeatPrice,Double.parseDouble(myDistance)
                );
                //double dLat = driverList.getLat();
                //double dLng = driverList.getLng();
                driverId = dId;
                //Log.d(TAG,"driver id "+ driverId);
                userTripInfo.setDriverId(driverId);
                //addMarker(new LatLng(dLat, dLng));
                btRequest.setVisibility(View.GONE);
                mCancelRequest.setVisibility(View.VISIBLE);
                mCallingCaptain.setVisibility(View.VISIBLE);
                //requestedDrivers[++requestCounter] = driverId;
                isRequestInProgress = true;
                if (!isDriverAccepted) {
                    //mNearestDriver.remove(driverList);
                    //Log.d(TAG,"sending request");
                    //i.remove();
                    userTripInfo.setMyFlag(NEW_REQUEST);
                    sendMe();
                    isDriverBusy = false;
                }
            } else {
                isRequestInProgress = false;
                requestCounter = 9;
                removeRequest();
                mCallingCaptain.setText("Sorry! No Captain found, please try later");
                Toast.makeText(MapActivity.this, "Sorry! No Captain found", Toast.LENGTH_LONG).show();
            }
            //}
        }
    };

    private void sendMe() {
        try {
            Log.d(TAG,"sending request...");
            mService.sendReq(userTripInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeRequest() {
        requestHandler.removeCallbacks(requestRunnable);
    }

    private void cancelDriver() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.sure_cancel_current));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userTripInfo.setMyFlag(CANCEL_DRIVER);
                // todo handle below code for different cancel
                if (isRequestInProgress && !isDriverAccepted) {
                    sendMe();
                    isRequestInProgress = false;
                }

                if (isDriverAccepted) {
                    sendMe();
                    isDriverAccepted = false;
                }

                removeRequest();
                bringBackDriver();
                //MyBase.getInstance(mContext).addToRequestQueue(updateIsOnReq);
                //iPassengerMapsActivity.setRating(userTripInfo.getTripId(), 1.2f);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void bringBackDriver() {
        if(driverId > 0) {
            // send cancel to previous request
            if(!isDriverBusy) {
                userTripInfo.setMyFlag(CANCEL_DRIVER);
                sendMe();
            }

            // todo bring driver back online
            //MyBase.getInstance(mContext).addToRequestQueue(bringBackDriver);
        }
    }

    private void bindMyService(Intent intent) {
        MySharedPreference.getInstance(this).setBind(true);
        bindService(intent, mViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }

    private void unBindMyService() {
        if(MySharedPreference.getInstance(this).isBinded()) {
            MySharedPreference.getInstance(this).setBind(false);
            unbindService(mViewModel.getServiceConnection());
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        }
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.i(TAG,"Success! Message received from server");
            String myMsg = intent.getStringExtra("MyDriverMessage");
            UserTripInfo g = gSon.fromJson(myMsg, UserTripInfo.class);
            int flag = g.getMyFlag();

            if(isDriverAccepted && flag == 3) {
                flag = 99;
            }

            //if (mTimerRunning) {
            //stopTimer();
            //}
            switch (flag) {
                case 3:
                    removeRequest();
                    // mCancelRequest.setVisibility(View.GONE);
                    mDriverInfo.setVisibility(View.VISIBLE);
                    //mDriverName.setText("Driver Phone: "+g.getPhone());
                    //Log.i(TAG,g.getUserId() + " D - " +g.getDriverId());
                    g.setMyFlag(9);
                    try {
                        mService.sendMe(""+g);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mDriverPhone.setVisibility(View.VISIBLE);
                    mDriverPhone.setText(getString(R.string.captain_phone) + g.getPhone());
                    mCallingCaptain.setText(getString(R.string.captain_on_way));
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification(getString(R.string.captain_found), getString(R.string.captain_on_way));
                    //mRequest.setText("Driver Found, Coming to you");
                    phoneNumber = "0" + g.getPhone();
                    isDriverAccepted = true;
                    isRequestInProgress = false;
                    //erasePolylines();
                    //if(mMap != null) {
                    //mMap.clear();
                    //}
                    break;
                case 2:
                    isDriverBusy = true;
                    isRequestInProgress = false;
                    listCurrent++;
                    if(listCurrent >= listSize) {
                        removeRequest();
                        Toast.makeText(MapActivity.this,getString(R.string.no_driver_found),Toast.LENGTH_LONG).show();
                    } else {
                        requestHandler.postDelayed(requestRunnable, 0);
                    }
                    break;
                case 5:
                    cancelRequest();
                    break;
                case 11:
                    mCallingCaptain.setText(getString(R.string.captain_reached));
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification(getString(R.string.captain_reached), getString(R.string.captain_reached));
                    break;
                case 9:
                    break;
                case 6:
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification(getString(R.string.trip_cancelled), getString(R.string.captain_cancel_trip));
                    //mDriverPhone.setVisibility(View.VISIBLE);
                    //mCustomerInfo.setVisibility(View.VISIBLE);
                    //mCustomerName.setText("");
                    //mCustomerPickup.setText("");
                    //mCustomerDestination.setText("");
                    //mCustomerPhone.setText("Sorry! Passenger Canceled the carPriceArray");
                    //mTripDistance.setText("");
                    break;
                case 12:
                    mCancelRequest.setVisibility(View.GONE);
                    mCallingCaptain.setText(getString(R.string.trip_started));
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification(getString(R.string.trip_started), getString(R.string.trip_started));
                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    //mCustomerInfo.setVisibility(View.VISIBLE);
                    //mCustomerName.setText("Trip Started");
                    break;
                case 13:
                    //onEndtrip(Double.valueOf(g.getPhone()));
                    timerHandler.removeCallbacks(timerRunnable);
                    mDriverInfo.setVisibility(View.GONE);
                    mpayAndRating.setVisibility(View.VISIBLE);
                    mAmount.setText(getString(R.string.pay_driver) + g.getPhone() + getString(R.string.sdg));
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification(getString(R.string.trip_completed), getString(R.string.trip_completed));
                    // mCustomerInfo.setVisibility(View.GONE);
                    // todo display payment details
                    break;
                case 99:
                    //userTripInfo.setDriverId(driverId);
                    break;
            }
        }
    };

    private void cancelRequest() {
        //ringtone.play();
        /*mCustomerInfo.setVisibility(View.VISIBLE);
        mSwipeLayout.setVisibility(View.GONE);
        mCustomerName.setText(R.string.passenger_cancel);
        mCustomerPickup.setText("");
        mCustomerDestination.setText("");
        //mCustomerPhone.setText("");
        mTripDistance.setText("");
        mAcceptBusyInfo.setVisibility(View.GONE);
        updateResponse(TRIP_CANCELLED);
        MyNotificationManager.getInstance(MapActivity.this).displayNotification(getString(R.string.req_cancelled), getString(R.string.req_cancel_by_pas));
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (mTimerRunning) {
            stopTimer();
        }
        cancelCall();*/
    }

    private void setObservers() {
        mViewModel.getBinder().observe(this, myBinder -> {
            if (myBinder == null) {
                //Log.d(TAG, "onChanged: unbound from service");
            } else {
                //Log.d(TAG, "onChanged: bound to service.");
                mService = myBinder.getService();
                mService.registerListener(this);
            }
        });
    }

    @Override
    public void onNewMessage(String myFlag) {

    }

    @Override
    public void onStatusChange(ServerConnection.ConnectionStatus status) {

    }
}