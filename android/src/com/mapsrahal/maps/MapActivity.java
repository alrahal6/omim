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
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.mapsrahal.maps.activity.ui.main.ConfirmedListPagerAdapter;
import com.mapsrahal.maps.activity.ui.main.MatchingStatePagerAdapter;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.FindDriverApi;
import com.mapsrahal.maps.api.ParsedMwmRequest;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.api.UserMessageApi;
import com.mapsrahal.maps.base.BaseMwmFragmentActivity;
import com.mapsrahal.maps.bookmarks.data.BookmarkManager;
import com.mapsrahal.maps.bookmarks.data.FeatureId;
import com.mapsrahal.maps.bookmarks.data.MapObject;
import com.mapsrahal.maps.intent.MapTask;
import com.mapsrahal.maps.location.CompassData;
import com.mapsrahal.maps.location.LocationHelper;
import com.mapsrahal.maps.model.FindDriver;
import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.model.Post;
import com.mapsrahal.maps.model.Price;
import com.mapsrahal.maps.model.StatusUpdate;
import com.mapsrahal.maps.model.UserMessage;
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

public class MapActivity extends BaseMwmFragmentActivity
                         implements View.OnTouchListener,
                                    MapRenderingListener,
                                    LocationHelper.UiCallback,
                                    RoutingController.Container,
                                    Framework.MapObjectListener,
                                    View.OnClickListener,
                                    NavigationButtonsAnimationController.OnTranslationChangedListener,
                                    AdapterView.OnItemSelectedListener,
                                    ServerConnection.ServerListener,
                                    MatchingStatePagerAdapter.MatchingSelectionListener,
                                    ConfirmedListPagerAdapter.ConfirmationSelectionListener

{

    private TextView tvDropOff,tvPickup,tvDistance,mDateTime,mRequiredSeats,mSetPickup,mSetDrop;
    private TextView mAmount, mTripTimer,mCustomerName,mCustomerPhone,mCustomerPickup;
    private TextView mCustomerDestination,mTripDistance;
    private TextView mDriverName,mDriverPhone;
    private TextView mListCount, mListAmount,mCallingCaptain,mPriceText;
    private double tripPrice;
    private Button mCancelRequest,mFinishTrip,mStartTrip;

    private Button btRequest,mOpenGMap,mConfirmList;
    private SwipeButton mSwipeButton;
    private ImageButton mAddressToggle,mMainMenu;

    private ImageView mAddSeat,mRemoveSeat;
    private LinearLayout mNotificationCard,mStartTripLayout;
    private LinearLayout mDriverInfo,mllForm,mMnuForm,mConfirmLayout;
    private LinearLayout mCustomerInfo, mAcceptBusyInfo, mSwipeLayout, mpayAndRating,mPriceLayout;
    private ProgressBar mMyprogress;

    private boolean mIsTabletLayout = false,isPickupSearch = true,isResultBySearch = false;
    private boolean mIsFullscreen,mLocationErrorDialogAnnoying = false;
    private boolean mTimerRunning,isLaunchByDeepLink = false;
    private boolean isDriverAccepted = false,isDriverBusy = false;
    private boolean isRequestInProgress = false, isStartedCounter = false;
    private Boolean isOnWaytoCustomer = false;
    private Boolean isOnTrip = false;

    private int seatCount = 1;
    private static final int REQ_CODE_LOCATION_PERMISSION = 1;
    private int requestingPassenger = 0;
    private int requestResponse = 3;
    private static final String CONFIRMED_LIST_KEY = "confirmedlistkey";
    //private static final String CONFIRMED_LIST_KEY = "confirmedlistkey";
    private static final int SEND_BUSY = 2;
    private static final int ACCEPT_REQUEST = 3;
    private static final int TRIP_CANCELLED = 5;
    private static final int REACHED_CUSTOMER = 11;
    private static final int TRIP_STARTED = 12;
    private static final int TRIP_COMPLETED = 13;
    private static final int DISTANCE_NOTIFY = 50;
    private int minDis,requestCounter = 0,driverId = 0;
    private static final int NEW_REQUEST = 4;
    private static final int CANCEL_DRIVER = 5;
    private static final int END_TRIP = 13;
    private final int[] requestedDrivers = new int[10];
    private int mSelector;

    private static final String PASSENGER_CAPTAIN_SELECTOR = "passenger_captain_selector";
    private String receivedMessage,usrId;
    private String myDistance,mSourceAddress,mDestinationAddress,mAddressToggleStr;

    private Date startingTime;
    private MapObject tempLocation,fromLocation,toLocation;
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
    private Ringtone ringtone;
    //MediaPlayer mediaPlayer;
    private CountDownTimer mCountDownTimer;
    private static final long START_TIME_IN_MILLIS = 20000;
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
    private final Handler timerHandler = new Handler();
    private final Handler tripRecordHandler = new Handler();
    private UserTripInfo userTripInfo;
    private Switch mSwitch;
    private MatchingStatePagerAdapter mAdapter;
    private ConfirmedListPagerAdapter mConfirmedAdapter;
    private ServerConnection mService;
    private WebSocketViewModel mViewModel;
    private final Handler requestHandler = new Handler();
    ArrayList<UserMessage> confirmedUserList;

    Map<Integer,Integer> selectionList = new HashMap<>();
    private double totAmount = 0d;

    private void prepareForAll() {
        // show hide for all
        // validation for all
    }

    private void prepareList() {
        mListCount = findViewById(R.id.list_count);
        mListAmount = findViewById(R.id.list_amount);
        mConfirmList = findViewById(R.id.confirm_list);
        mConfirmList.setOnClickListener(this);
    }

    private void openInGoogleMap(double fromLat,double fromLng,double toLat, double toLng) {
        // todo revert back later
        /*Location l = LocationHelper.INSTANCE.getSavedLocation();
        fromLat = l.getLatitude();
        fromLng = l.getLongitude();*/
        String url = "http://maps.google.com/maps?saddr=" + fromLat + ","
                + fromLng + "&daddr=" + toLat + "," + toLng + "&mode=driving";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
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
    public void showInGMap(double fromLat,double fromLng,double toLat, double toLng) {
        openInGoogleMap(fromLat,fromLng,toLat,toLng);
    }

    private void showMap(double fromLat,double fromLng,double toLat, double toLng) {
        fromLocation = MapObject.createMapObject(FeatureId.EMPTY, MapObject.POI, "", "",
                fromLat, fromLng);
        toLocation = MapObject.createMapObject(FeatureId.EMPTY, MapObject.POI, "", "",
                toLat, toLng);
        RoutingController.get().setStartPoint(fromLocation);
        RoutingController.get().setEndPoint(toLocation);
    }

    @Override
    public void showInMap(double fromLat,double fromLng,double toLat, double toLng) {
        // openInGoogleMap(fromLat,fromLng,toLat,toLng);
        showMap(fromLat,fromLng,toLat,toLng);
    }

    @Override
    public void selectMatch(int position,boolean isAdd) {
        //MatchingItem matchingItems = mMatchingList.get(position);
        //matchingList[matchingCounter] = position;
        //Log.d(TAG,"List Id "+ mMatchingList.get(position).getmText1());
        //Log.d(TAG,"List Id "+ mMatchingList.get(position).getId());
        double amount =   Double.parseDouble(mMatchingList.get(position).getmAmount());
        //roundTwoDecimals(amount);
        // todo fetch seats
        //int totSeats = mMatchingList.get(position).getSeats();
        if(isAdd) {
            // todo add seats
            totAmount += amount;
            Log.d(TAG,"Id : "+ mMatchingList.get(position).getId());
            totAmount = roundTwoDecimals(totAmount);
            selectionList.put(position,mMatchingList.get(position).getId());
            //double distance = mMatchingList.get(position).mMyTripDistance;
            //matchingList[matchingCounter] = mMatchingList.get(position).getId();
            //matchingCounter++;
            mListCount.setText("Seats : " + selectionList.size());
            mListAmount.setText(totAmount+" SDG");
        } else {
            // todo remove seats
            totAmount -= amount;
            totAmount = roundTwoDecimals(totAmount);
            selectionList.remove(position);
            //matchingCounter--;
            mListCount.setText("Seats : " + selectionList.size());
            mListAmount.setText(totAmount+" SDG");
            //if(matchingList[mMatchingList.get(position).getId()] != 0) {
            //}
        }
        //mListAmount.setText("Distance : "+matchingCounter);
    }

    private void sendConfirmList() {
        // todo send confirm list to all
        //for (int i = 0; i < matchingCounter;i++) {
           // Log.d(TAG,"Confirmed List "+selectionList.);
        //}
        alertDialog();
        /*for (Map.Entry<Integer, Integer> entry : selectionList.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            Log.d(TAG, "Confirmed List Key : " + key);
            Log.d(TAG, "Confirmed List Value : " + value);
        }*/
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    sendConfirmation();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(MapActivity.this, "Request not Send", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private int getFlag() {
        return Constants.Notification.DRIVER_ACCEPTED;
    }

    private void sendConfirmation() {

        // selectionList
        List<UserMessage> userMessageList = new ArrayList<>();
        //Gson gson = new Gson();
        //String jsonString = gson.toJson(selectionList);
        //Log.d(TAG,"Json "+ jsonString);
        for (Map.Entry<Integer, Integer> entry : selectionList.entrySet()) {
            Integer position = entry.getKey();
            Integer value = entry.getValue();
            userMessage = new UserMessage(
                    MySharedPreference.getInstance(this).getUserId(),
                    mMatchingList.get(position).getUserId(),
                    getFlag(),mMatchingList.get(position).getId(),
                    0.0d,
                    mMatchingList.get(position).getmTripTime(),
                    mMatchingList.get(position).getmAmount(),
                    mMatchingList.get(position).getmPhone(),
                    mMatchingList.get(position).getmPhone(),
                    mMatchingList.get(position).getmText1(),
                    mMatchingList.get(position).getmText2(),
                    mMatchingList.get(position).getmPhone(),
                    mMatchingList.get(position).getfLat(),
                    mMatchingList.get(position).getfLng(),
                    mMatchingList.get(position).gettLat(),
                    mMatchingList.get(position).gettLng()
            );
            userMessageList.add(userMessage);
            Log.d(TAG, "Confirmed List Key : " + position);
            Log.d(TAG, "Confirmed List Value : " + value);
        }
        MySharedPreference.getInstance(this).addActiveProcess(Constants.ActiveProcess.CAPTAIN_HAVE_CONFIRMED_LIST);
        MySharedPreference.getInstance(this).putListConfirmed(CONFIRMED_LIST_KEY,userMessageList);
        UserMessageApi userMessageApi = ApiClient.getClient().create(UserMessageApi.class);
        Call<UserMessage> call = userMessageApi.sendConfirmation(userMessageList);

        call.enqueue(new Callback<UserMessage>() {
            @Override
            public void onResponse(Call<UserMessage> call, Response<UserMessage> response) {
                Toast.makeText(MapActivity.this, "Request Send Successfully ", Toast.LENGTH_LONG).show();
                displayConfirmedList();
            }

            @Override
            public void onFailure(Call<UserMessage> call, Throwable t) {
                //Toast.makeText(MapActivity.this, "Request Send Failed! ", Toast.LENGTH_LONG).show();
            }
        });
        //mNotificationCard.setVisibility(View.GONE);
    }

    /*private void sendMessage() {
        int temp = userMessage.getfUserId();
        userMessage.setfUserId(userMessage.gettUserId());
        userMessage.settUserId(temp);
        //userMessage.setmFlag(getFlag());
        UserMessageApi userMessageApi = ApiClient.getClient().create(UserMessageApi.class);
        Call<UserMessage> call = userMessageApi.sentMessage(userMessage);

        call.enqueue(new Callback<UserMessage>() {
            @Override
            public void onResponse(Call<UserMessage> call, Response<UserMessage> response) {
                Toast.makeText(MapActivity.this, "Request Send Successfully ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<UserMessage> call, Throwable t) {
                //Toast.makeText(MapActivity.this, "Request Send Failed! ", Toast.LENGTH_LONG).show();
            }
        });
        mNotificationCard.setVisibility(View.GONE);
    }*/

    private void alertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setMessage("Are you sure? Send Confirmation!").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    private void prepareForFrom() {
        hideExceptFromTo();
        showBtnRequest();
    }

    private void prepareForNone() {
        hideFromTo();
        onlineAsCaptain();
        initRingTone();
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
        btRequest.setVisibility(View.VISIBLE);
        showProgress(false);
    }

    private void hideFromTo() {
        tvDropOff.setVisibility(View.GONE);
        tvPickup.setVisibility(View.GONE);
        mAddressToggle.setVisibility(View.GONE);
        hideExceptFromTo();
    }

    private void initRingTone() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void  onSafeCreate(Bundle savedInstanceState) {
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
        if(MySharedPreference.getInstance(this).isCaptainOnline()) {
            mSwitch.setChecked(true);
        } else {
            mSwitch.setChecked(false);
        }
        //mSendRequest = findViewById(R.id.send_request_test);
        //mSendRequest.setOnClickListener(this);

        mViewPager = findViewById(R.id.matching_list_vp);
        mConfirmLayout = findViewById(R.id.confirm_layout);
        mStartTripLayout = findViewById(R.id.start_trip_layout);
        mStartTripLayout.setVisibility(View.GONE);
        //start_trip
        mFinishTrip = findViewById(R.id.finish_trip);
        mFinishTrip.setOnClickListener(this);
        mStartTrip = findViewById(R.id.start_trip);
        mStartTrip.setOnClickListener(this);
        //mViewPager.setAdapter(matchingStateAdapter);
        mViewPager.setVisibility(View.GONE);
        mConfirmLayout.setVisibility(View.GONE);
        userMessageApi = ApiClient.getClient().create(UserMessageApi.class);
        //matchingStateAdapter.createPost();
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
        mMapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentByTag(MapFragment.class.getName());

        View container = findViewById(R.id.map_fragment_container);
        //myView = findViewById(R.id.map_fragment_container);
        if (container != null) {
            container.setOnTouchListener(this);
        }
        adjustCompass(0);
        mNavigationController = new NavigationController(this);
        initNavigationButtons();
        //myPositionClick();
        requestCounter = 0;
        //requestedDrivers[requestCounter] = userTripInfo.getUserId();
        /*if(MySharedPreference.getInstance(this).isCaptainOnline()) {
            hideFromTo();
            onlineAsCaptain();
        }*/


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
        //Intent intent = getIntent();
        //mSelector = intent.getIntExtra(PASSENGER_CAPTAIN_SELECTOR,1);
        mSelector = MySharedPreference.getInstance(getApplicationContext()).getSelectorId();
        if(mSelector == CAPTAIN_SHARE_ONLY || mSelector == CAPTAIN_ANY) {
            prepareList();
        }

        switch (mSelector) {
            case PASSENGER_TAXI_ONLY:
                prepareForFrom();
                connect();
                break;
            case PASSENGER_SHARE_ONLY:
            case PASSENGER_ANY:
                prepareForAll();
                break;
            case CAPTAIN_TAXI_ONLY:
                prepareForNone();
                break;
        }

        ArrayAdapter<CharSequence> adapter;

        Spinner spinner = findViewById(R.id.gender_spinner);
        if(mSelector == CAPTAIN_ANY || mSelector == PASSENGER_ANY) {
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.select_cargo, android.R.layout.simple_spinner_item);
            // hide seats
            ImageView imageView = findViewById(R.id.remove_seat);
            imageView.setVisibility(View.GONE);
            TextView textView = findViewById(R.id.required_seats);
            textView.setVisibility(View.GONE);
            ImageView imageView1 = findViewById(R.id.add_seat);
            imageView1.setVisibility(View.GONE);
        } else {
            adapter = ArrayAdapter.createFromResource(this,
                    R.array.select_gender, android.R.layout.simple_spinner_item);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //mOpenGMap.setOnClickListener(v -> openInGoogleMap());

        mSendFeedback.setOnClickListener(view -> {
            mpayAndRating.setVisibility(View.GONE);
            // todo save rating
        });

        mCustomerPhone.setOnClickListener(v -> callDriver());

        Button mRideStatus = findViewById(R.id.rideStatus);
        mRideStatus.setOnClickListener(v -> acceptRequest());

        Button mBusyResponse = findViewById(R.id.busyResponse);
        mBusyResponse.setOnClickListener(v -> {
            requestResponse = 2;
            respondBusy();
        });

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
                Log.d(TAG, "onChanged: bound to service.");
                mService = myBinder.getService();
                mService.registerListener(this);
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
        if (RoutingController.get().isPlanning() || RoutingController.get().isNavigating()) {
            if (!UiUtils.isLandscape(this))
                mNavigationController.fadeOutSearchButtons();
        }
    }

    private boolean isValidateFrom() {
        showProgress(false);
        if(fromLocation != null) {
            return true;
        }
        return false;
    }

    private boolean isValidateFromAndTo() {
        showProgress(false);
        if(fromLocation != null && toLocation != null && tvDistance != null) {
            return true;
        }
        return false;
    }

    private void saveAndSearchPost() {
        if(isValidateFromAndTo()) {
            showProgress(true);
            MySharedPreference.getInstance(this).userTripInfo(fromLocation.getLat(),
                    fromLocation.getLon(),
                    toLocation.getLat(),
                    toLocation.getLon(), myDistance, mSourceAddress, mDestinationAddress,
                    startingTime);
            createPost();
        } else {
            Toast.makeText(this,"Please enter valid address",Toast.LENGTH_LONG).show();
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

    private void showProgress(boolean isTrue) {
        if(isTrue) {
            mMyprogress.setVisibility(View.VISIBLE);
        } else {
            mMyprogress.setVisibility(View.GONE);
        }
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
            case R.id.confirm_list:
                sendConfirmList();
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
                    getNearestDriver();
                } else {
                    saveAndSearchPost();
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
                showProgress(true);
                showSearch();
                break;
            case R.id.tv_dropoff:
                isPickupSearch = false;
                showProgress(true);
                showSearch();
                break;
            case R.id.set_pickup:
                showProgress(true);
                setPickup();
                break;
            case R.id.set_drop:
                showProgress(true);
                setDropoff();
                break;
            case R.id.cancelRequest:
                    cancelDriver();
                break;
            case R.id.finish_trip:
                finishConfirmedTrip();
                break;
            case R.id.start_trip:
                startConfirmedTrip();
                break;
        }
    }

    private void finishConfirmedTrip() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you want to cancel the current Trip?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // todo send trip finished message
                MySharedPreference.getInstance(MapActivity.this).addActiveProcess(0);
                reloadMe();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void startConfirmedTrip() {
        //  todo send trip started information to all list
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you want to start Trip and Inform users?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // todo send trip start message to all users
                //MySharedPreference.getInstance(MapActivity.this).addActiveProcess(0);
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void cancelRequest() {
        //ringtone.play();
        mCustomerInfo.setVisibility(View.VISIBLE);
        mSwipeLayout.setVisibility(View.GONE);
        mCustomerName.setText(R.string.passenger_cancel);
        mCustomerPickup.setText("");
        mCustomerDestination.setText("");
        //mCustomerPhone.setText("");
        mTripDistance.setText("");
        mAcceptBusyInfo.setVisibility(View.GONE);
        updateResponse(TRIP_CANCELLED);
        MyNotificationManager.getInstance(MapActivity.this).displayNotification("Request Cancelled", "Sorry! request cancelled by passenger");
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }
        if (mTimerRunning) {
            stopTimer();
        }
        cancelCall();
    }

    private void cancelDriver() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you want to cancel the current Trip?");
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
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
        showProgress(false);
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
        hideBtnRequest();
    }

    private void setDropoff() {
        //removeBookmark();
        toLocation = tempLocation;
        mDestinationAddress = toLocation.getTitle();
        tvDropOff.setText(mDestinationAddress);
        hideMenu();
        RoutingController.get().setEndPoint(toLocation);
        hideBtnRequest();
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
        if (rinfo != null) {
            myDistance = rinfo.distToTarget;
            String units = rinfo.distToTarget +" "+rinfo.targetUnits;
            tvDistance.setText(units);
            getPrice(myDistance,mSelector);
        }
        showBtnRequest();
    }

    private void getPrice(String myDistance,int mSelector) {
        PostApi postApi = ApiClient.getClient().create(PostApi.class);
        Price price = new Price(0.0,myDistance,mSelector);
        Call<Price> call = postApi.createPrice(price);
        call.enqueue(new Callback<Price>() {
            @Override
            public void onResponse(Call<Price> call, Response<Price> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MapActivity.this,"Sorry! Error in calculating Price",Toast.LENGTH_LONG).show();
                    return;
                }
                tripPrice = roundTwoDecimals(response.body().getPrice());
                mPriceLayout.setVisibility(View.VISIBLE);
                mPriceText.setText(""+tripPrice+" SDG");
            }

            @Override
            public void onFailure(Call<Price> call, Throwable t) {
                Toast.makeText(MapActivity.this,"Sorry! Error in calculating Price",Toast.LENGTH_LONG).show();
            }
        });
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
        //if (mService.isMessageReceived()) {
            //mService.setIsReceivedFalse();
            //processMessage(mService.receivedMessage());
            //receivedMessage = null;
        //}
        //unBindMyService();
    }

    @Override
    protected void onSafeDestroy() {
        super.onSafeDestroy();
        if(mSelector == PASSENGER_TAXI_ONLY) {
            disconnect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    private void acceptRequest() {
        try {
            ringtone.stop();
            //mService.stopRingTone();
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
            tripId = String.valueOf(g.getTripId());
            distance = g.getDistance();
            duration = g.getDuration();
            price = g.getPrice();
            if (mTimerRunning) {
                stopTimer();
            }
            // todo register accepted driver with trip id
            // tripId
            prepareGoToCustomer();
        } catch (Exception e) {
            Log.d(TAG, "Error accept request " + e.getMessage());
        }
    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(START_TIME_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Log.i(TAG,"Timer Started...");
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                //mService.stopRingTone();
                respondBusy();
                //Log.i(TAG,"Finished timer");
            }
        }.start();
        mTimerRunning = true;
    }

    private void stopTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        //Log.i(TAG,"Timer Stopped");
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
            mService.sendMessage(flag, requestingPassenger, distance, duration, price);
            updateResponse(flag);
        } catch (Exception e) {
            Log.d(TAG, "Error sending message " + e.getMessage());
        }
    }

    private void updateResponse(int responseId) {
        requestResponse = responseId;
        //MyBase.getInstance(this).addToRequestQueue(updateIsOnReq);
    }

    private void processNotification(String myNotification,boolean isFromNotify) {
        hideFromTo();
        mSwitch.setVisibility(View.GONE);
        TextView from = findViewById(R.id.n_textView);
        TextView to = findViewById(R.id.n_textView2);
        TextView stTime = findViewById(R.id.n_start_time);
        TextView urDistance = findViewById(R.id.n_your_distance);
        Button accept = findViewById(R.id.n_accept_request);
        TextView mTextView = findViewById(R.id.n_notification_title);
        LinearLayout llButton = findViewById(R.id.ll__button);
        int mFlag = 999;
        //if(isFromNotify) {
            userMessage = gSon.fromJson(myNotification, UserMessage.class);
            double tripId = userMessage.getTripId();
            //Button reject = findViewById(R.id.n_deny_request);
            from.setText(userMessage.getfAddress());
            to.setText(userMessage.gettAddress());
            stTime.setText(""+userMessage.getmTripTime());
            urDistance.setText(""+userMessage.getDistance());
            mFlag = userMessage.getmFlag();
        /*} else {
            from.setText(mSourceAddress);
            to.setText(mDestinationAddress);
            stTime.setText(myDistance);
            urDistance.setText(""+startingTime);
        }*/

        if(mFlag !=0 ) {
            mNotificationCard.setVisibility(View.VISIBLE);
        }
        switch(mFlag) {
            case Constants.Notification.PASSENGER_REQUEST:
                //n_notification_title
                //FCMListenerService.getFlagTitle(Constants.Notification.PASSENGER_REQUEST);
                mTextView.setText(getString(R.string.passenger_ride_request));
                break;
            case Constants.Notification.PASSENGER_ACCEPTED:
                //llButton.setVisibility(View.GONE);
                mTextView.setText(getString(R.string.passenger_accepted_request));
                break;
            case Constants.Notification.PASSENGER_CANCELLED:
                //llButton.setVisibility(View.GONE);
                mTextView.setText(getString(R.string.passenger_cancel));
                //mNotificationCard.setVisibility(View.VISIBLE);
                break;
            case Constants.Notification.DRIVER_INVITE:
                mTextView.setText(getString(R.string.captain_invitation));
                break;
            case Constants.Notification.DRIVER_ACCEPTED:
                //llButton.setVisibility(View.GONE);
                mTextView.setText(getString(R.string.captain_accepted));
                //mNotificationCard.setVisibility(View.VISIBLE);
                break;
            case Constants.Notification.DRIVER_REFUSED:
                //llButton.setVisibility(View.GONE);
                mTextView.setText(getString(R.string.captain_refused));
                //mNotificationCard.setVisibility(View.VISIBLE);
                break;
            case Constants.Notification.DRIVER_CANCELLED:
                //llButton.setVisibility(View.GONE);
                mTextView.setText(getString(R.string.captain_cancelled));
                //mNotificationCard.setVisibility(View.VISIBLE);
                break;
            case Constants.Notification.DRIVER_REACHED:
                //llButton.setVisibility(View.GONE);
                mTextView.setText(getString(R.string.captain_reached));
                //mNotificationCard.setVisibility(View.VISIBLE);
                break;
            case Constants.Notification.TRIP_STARTED:
                //llButton.setVisibility(View.GONE);
                mTextView.setText(getString(R.string.trip_started));
                //mNotificationCard.setVisibility(View.VISIBLE);
                break;
        }
        int acceptButtonFlag = Constants.Notification.DRIVER_ACCEPTED;
        int rejectButtonFlag = Constants.Notification.DRIVER_REFUSED;
        if(mSelector < 4) {
            acceptButtonFlag = Constants.Notification.PASSENGER_ACCEPTED;
            rejectButtonFlag = Constants.Notification.PASSENGER_REFUSED;
        }

        int finalAcceptButtonFlag = acceptButtonFlag;
        int finalMFlag = mFlag;
        accept.setOnClickListener(view -> {
            // todo update in server
            updateStatus(finalMFlag);
            //userMessage.setmFlag(finalAcceptButtonFlag);

        });

        /*int finalRejectButtonFlag = rejectButtonFlag;
        reject.setOnClickListener(view -> {
            userMessage.setmFlag(finalRejectButtonFlag);
        });*/
        //notification_req_res
    }

    private void updateStatus(int Status) {
        //PostApi postApi =
        PostApi postApi = ApiClient.getClient().create(PostApi.class);
        StatusUpdate statusUpdate = new StatusUpdate(MySharedPreference.getInstance(this).getUserId(),Status);

        Call<StatusUpdate> call = postApi.updateStatus(statusUpdate);

        call.enqueue(new Callback<StatusUpdate>() {
            @Override
            public void onResponse(Call<StatusUpdate> call, Response<StatusUpdate> response) {
                //Log.d(TAG,"Got response inside");
                if(!response.isSuccessful()) {
                    return;
                }
                //Log.d(TAG,"Got response success");
                MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userNotification(null);
                reloadMe();
            }

            @Override
            public void onFailure(Call<StatusUpdate> call, Throwable t) {

            }
        });
    }

    private void reloadMe() {
        Intent intent = new Intent(this,SelectorActivity.class);
        finish();
        startActivity(intent);
    }

    private void processMessage(String myMsg) {
        g = gSon.fromJson(myMsg, UserTripInfo.class);
        int flag = g.getMyFlag();
        requestingPassenger = g.getUserId();
        tripId = String.valueOf(g.getTripId());
        //Log.i(TAG,"request received");
        switch (flag) {
            case 4:
                ringtone.play();
                //mediaPlayer.start();
                mCustomerInfo.setVisibility(View.VISIBLE);
                mAcceptBusyInfo.setVisibility(View.VISIBLE);
                mSwipeButton.setText("Reached Customer");
                mSwipeLayout.setVisibility(View.GONE);
                mCustomerName.setText(getString(R.string.name) + g.getCustomerName());
                mCustomerPickup.setText(getString(R.string.pickup) + g.getPickupAddress());
                mCustomerDestination.setText(getString(R.string.destination) + g.getDestAddress());
                phoneNumber = "0" + g.getPhone();
                mCustomerPhone.setText(getString(R.string.customer_phone) + g.getPhone());
                mTripDistance.setText(getString(R.string.price) + g.getPrice() + getString(R.string.distance) + g.getDistance());
                if (!mTimerRunning) {
                    startTimer();
                }
                break;
            case 5:
                cancelRequest();
                /* //ringtone.play();
                mCustomerInfo.setVisibility(View.VISIBLE);
                mSwipeLayout.setVisibility(View.GONE);
                mCustomerName.setText(R.string.passenger_cancel);
                mCustomerPickup.setText("");
                mCustomerDestination.setText("");
                //mCustomerPhone.setText("");
                mTripDistance.setText("");
                mAcceptBusyInfo.setVisibility(View.GONE);
                updateResponse(TRIP_CANCELLED);
                MyNotificationManager.getInstance(MapActivity.this).displayNotification("Request Cancelled", "Sorry! request cancelled by passenger");
                if (ringtone.isPlaying()) {
                    ringtone.stop();
                }
                if (mTimerRunning) {
                    stopTimer();
                }
                cancelCall();*/
                break;
            /*case 13:
                mCustomerInfo.setVisibility(View.GONE);
                updateResponse(TRIP_COMPLETED);
                // todo display payment details
                break;*/
            case 3:
                //removeRequest();
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
                mDriverPhone.setText("Driver Phone: " + g.getPhone());
                mCallingCaptain.setText("Captain Found, on the way");
                MyNotificationManager.getInstance(MapActivity.this).displayNotification("Driver Found", "Driver Coming to you");
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
                    mCallingCaptain.setText("Sorry! No Drivers Found");
                    Toast.makeText(this,"Sorry! No drivers found",Toast.LENGTH_LONG).show();
                } else {
                    requestHandler.postDelayed(requestRunnable, 0);
                }
                //removeRequest();
                //requestHandler.removeCallbacks(requestRunnable);
                //requestHandler.postDelayed(requestRunnable, 0);
                //if (requestCounter < 9) {
                //getClosestDriver();
                //} else {
                //mRequest.setText("Sorry! Driver Not Found");
                //}
                break;
            case 11:
                mCallingCaptain.setText("Captain Reached!");
                MyNotificationManager.getInstance(MapActivity.this).displayNotification("Driver Reached", "Driver Reached your place");
                break;
            case 9:
                //Log.i(TAG," receiving driver current location"+ g);
                //LatLng newLocation;
                //double oldLat = oldLocation.latitude;
                //double oldLng = oldLocation.longitude;
                //double newLat = g.getLat();
                //double newLng = g.getLng();
                //LatLng newLocation = new LatLng(g.getLat(), g.getLng());
                //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                //float rotation = (float) SphericalUtil.computeHeading(oldLocation, newLocation);
                //rotateMarker(mDriverMarker, newLocation, rotation);
                //oldLocation = newLocation;
                //} else {
                //updateDriverLocMarker(newLocation);
                //}
                // break;
                break;
            case 6:
                MyNotificationManager.getInstance(MapActivity.this).displayNotification("Trip Canceled", "Trip Cancelled by driver");
                break;
            case 12:
                mCallingCaptain.setText("Trip Started!");
                mCancelRequest.setVisibility(View.GONE);
                mCallingCaptain.setVisibility(View.GONE);
                MyNotificationManager.getInstance(MapActivity.this).displayNotification("Trip Started", "Trip Started by driver");
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
                mAmount.setText("Pay Driver : " + g.getPhone() + " SDG");
                MyNotificationManager.getInstance(MapActivity.this).displayNotification("Trip Completed", "Trip Completed");
                // mCustomerInfo.setVisibility(View.GONE);
                // todo display payment details
                break;
            case 99:
                //userTripInfo.setDriverId(driverId);
                break;
        }
    }

    private void cancelCall() {
        final MediaPlayer[] player = {null};
        if (player[0] == null) {
            player[0] = MediaPlayer.create(this, R.raw.cancel_alarm);
            player[0].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (player[0] != null) {
                        player[0].release();
                        player[0] = null;
                    }
                }
            });
        }
        player[0].start();
    }

    private void respondBusy() {
        try {
            ringtone.stop();
            //mService.stopRingTone();
            send(SEND_BUSY, 0, 0, 0);
            mCustomerInfo.setVisibility(View.GONE);
            mCustomerName.setText("");
            mCustomerPhone.setText("");
            if (mTimerRunning) {
                stopTimer();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error respond busy " + e.getMessage());
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
        if(MySharedPreference.getInstance(this).isCaptainOnline()) {
            connect();
        }
        int activeProcess = MySharedPreference.getInstance(this).getActiveProcess();
        String msg = MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).getUserMessage();
        if(activeProcess == Constants.ActiveProcess.CAPTAIN_HAVE_CONFIRMED_LIST) {
            displayConfirmedList();
        }
        // todo remove later
        //MySharedPreference.getInstance(this).clearActiveProcess();
        //Log.i(TAG,"Shared message : "+msg);
        if (msg != null) {
            processMessage(msg);
            //if(activeProcess != Constants.ActiveProcess.PASSENGER_HAVE_ACTIVE_RIDE) {
                MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userMessage(null);
            //}
        }

        String notify =MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).getUserNotification();

        if (notify != null) {
            processNotification(notify,true);
            //MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userNotification(null);
        }

        /*String msg = MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).getUserMessage();
        //Log.i(TAG,"Shared message : "+msg);
        if (msg != null) {
            processMessage(msg);
            MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userMessage(null);
        }*/

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

        /*int activeProcessid = MySharedPreference.getInstance(this).getActiveProcess();
        if(activeProcessid == Constants.ActiveProcess.CAPTAIN_HAVE_CONFIRMED_LIST) {
            //displayConfirmedList();
        }*/
    }

    private void displayConfirmedList() {
        confirmedUserList = new ArrayList<>();
        confirmedUserList = MySharedPreference.getInstance(this).getListConfirmed(CONFIRMED_LIST_KEY);
        setFullscreen(true);
        hideFromTo();
        mSwitch.setVisibility(View.GONE);
        mConfirmLayout.setVisibility(View.GONE);
        mStartTripLayout.setVisibility(View.VISIBLE);
        mConfirmedAdapter = new ConfirmedListPagerAdapter(confirmedUserList,this,getSupportFragmentManager());
        mViewPager.setAdapter(mConfirmedAdapter);
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
        long timeMillis = System.currentTimeMillis();
        return TimeUnit.MILLISECONDS.toSeconds(timeMillis);
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
        if (g.getDestLat() > 0) {
            //getDirectionDistance();
        } else {
            mOpenGMap.setVisibility(View.GONE);
        }
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
        if(MySharedPreference.getInstance(this).isCaptainOnline() || mSelector == PASSENGER_TAXI_ONLY) {
            bindMyService(intent);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(ServerConnection.ACTION_MSG_RECEIVED));
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
            if(mSelector != PASSENGER_TAXI_ONLY) {
                MySharedPreference.getInstance(this).setCaptainOnline(true);
            }
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
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
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

    private double mMyTripDistance;
    ArrayList<MatchingItem> mMatchingList = new ArrayList<>();
    private static double ELEGIBLE_LIMIT = 1.4d;
    private UserMessage userMessage;
    private UserMessageApi userMessageApi;

    private void my() {
        setFullscreen(true);
        hideFromTo();
        mSwitch.setVisibility(View.GONE);
        mPriceLayout.setVisibility(View.GONE);
        //mConfirmLayout.setVisibility(View.GONE);
        mAdapter = new MatchingStatePagerAdapter(mMatchingList, this,getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
    }

    public void createPost() {
        mMyTripDistance = Double.parseDouble(MySharedPreference.getInstance(this).getTripDistance().trim());
        PostApi postApi = ApiClient.getClient().create(PostApi.class);
        Post post = new Post(null, MySharedPreference.getInstance(this).getUserId(),
                MySharedPreference.getInstance(this).getFrmLat(),
                MySharedPreference.getInstance(this).getFrmLng(),
                MySharedPreference.getInstance(this).getToLat(),
                MySharedPreference.getInstance(this).getToLng(),
                mMyTripDistance,
                MySharedPreference.getInstance(this).getFrmAddress().trim(),
                MySharedPreference.getInstance(this).getToAddress().trim(),
                new Date(MySharedPreference.getInstance(this).getStartTime()),
                MySharedPreference.getInstance(this).getPhoneNumber());
        post.setSelectorFlag(mSelector);

        Call<List<Post>> call = postApi.createPost(post);

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                showProgress(false);
                if(mSelector == PASSENGER_SHARE_ONLY || mSelector == PASSENGER_ANY) {
                    Toast.makeText(MapActivity.this,"Successfully sent your request",Toast.LENGTH_LONG).show();
                    Map<String, String> data = new HashMap<>();
                    data.put("fUserId",MySharedPreference.getInstance(MapActivity.this).getUserId()+"");
                    data.put("tUserId","0.0");
                    data.put("mFlag","1");
                    data.put("tripId","0.0");
                    data.put("distance",myDistance);
                    data.put("price","0.0");
                    data.put("mTripTime",""+startingTime);
                    data.put("phone","0.0");
                    data.put("name","0.0");
                    data.put("fAddress",mSourceAddress);
                    data.put("tAddress",mDestinationAddress);
                    data.put("note","0.0");
                    data.put("fLat","0.0");
                    data.put("fLng","0.0");
                    data.put("tLat","0.0");
                    data.put("tLng","0.0");
                    String usrNotification = gSon.toJson(data);
                    MySharedPreference.getInstance(MwmApplication.get().getApplicationContext()).userNotification(usrNotification);
                    MySharedPreference.getInstance(MapActivity.this).addActiveProcess(Constants.ActiveProcess.PASSENGER_HAVE_ACTIVE_RIDE);
                    processNotification(usrNotification,true);
                } else {
                    mMatchingList = new ArrayList<>();
                    createMatchList(response.body());
                    //matchingCounter = 0;
                    my();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Toast.makeText(MapActivity.this,"Some Error occurred! Try Later",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void createMatchList(List<Post> body) {
        for (Post post : body) {
            String totDistTxt = "";
            /*String totDistTxt = prepareRouteDistance(Utils.roundTwoDecimals(post.getSrcDistDiff()),
                    Utils.roundTwoDecimals(post.getTripDistance()),Utils.roundTwoDecimals(post.getDestDistDiff()));*/
            double totDist = Utils.roundTwoDecimals(post.getSrcDistDiff()+post.getTripDistance()+post.getDestDistDiff());
            double extra = 0;
            if(mMyTripDistance < totDist) {
                extra = totDist - mMyTripDistance;
            }

            String amount = "" + post.getTripDistance() * 2;
            String extraDistance = "" + Utils.roundTwoDecimals(extra);
            //Log.d(TAG,"Post Id : " +post.getId());
            //mMatchMaker.getMatchingList();
            if(MySharedPreference.getInstance(this).isCaptain()) {
                if (isCaptainEligible(mMyTripDistance, totDist, post.getSrcDistDiff(), post.getDestDistDiff(), post.getTripDistance())) {
                    // insert post
                    // todo get accurate distance and add
                    insert(new MatchingItem(post.getId(),post.getUserId(),
                            post.getSourceAddress(), post.getDestinationAddress(),
                            Double.toString(post.getTripDistance()), DateUtils.formatDateStr(post.getStartTime()), Double.toString(totDist), totDistTxt,
                            amount,extraDistance,mMyTripDistance,post.getSrcLat(),post.getSrcLng(),post.getDestLat(),post.getDestLng()));
                    //insert(mMatchingList);
                }
            } else {
                if (isPassengerEligible(mMyTripDistance, totDist, post.getSrcDistDiff(), post.getDestDistDiff(), post.getTripDistance())) {
                    // insert post
                    // todo get accurate distance and add
                    insert(new MatchingItem(post.getId(),post.getUserId(),
                            post.getSourceAddress(), post.getDestinationAddress(),
                            Double.toString(post.getTripDistance()), DateUtils.formatDateStr(post.getStartTime()), Double.toString(totDist), totDistTxt,
                            amount,extraDistance,mMyTripDistance,post.getSrcLat(),post.getSrcLng(),post.getDestLat(),post.getDestLng()));
                }
            }
        }
    }

    private boolean isCaptainEligible(double mMyTripDistance, double totDist, double srcDistDiff,
                                      double destDistDiff, double tripDistance) {
        // my trip distance is greater than my distance
        if (mMyTripDistance >= totDist) {
            return true;
        } else {
            // my trip distance is less than my distance
            // so i have to travel more as a captain
            double percentage = getPercentage(mMyTripDistance,totDist);
            if(percentage > ELEGIBLE_LIMIT)
                return false;
            return true;
        }
    }

    // todo check later
    private boolean isPassengerEligible(double mMyTripDistance, double totDist, double srcDistDiff,
                                        double destDistDiff, double tripDistance) {
        // my trip distance is greater than my distance
        if (mMyTripDistance <= totDist) {
            return true;
            // totDist
        } else {
            double percentage = getPercentage(totDist,mMyTripDistance);
            if(percentage > ELEGIBLE_LIMIT)
                return false;
            return true;
        }
    }

    private double getPercentage(double a,double b) {
        return ((b * 100d) / a)/100d;
    }

    private void insert(MatchingItem matchingItem) {
        mMatchingList.add(matchingItem);
        //matchDao.insert(matchingItem);
    }

    /*private String getNotIn() {
        StringBuilder argsBuilder = new StringBuilder();
        argsBuilder.append("(");
        final int argsCount = requestedDrivers.length;
        for (int i = 0; i < argsCount; i++) {
            argsBuilder.append(requestedDrivers[i]);
            if (i < argsCount - 1) {
                argsBuilder.append(",");
            }
        }
        argsBuilder.append(")");
        return argsBuilder.toString();
    }*/

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

    List<FindDriver> mNearestDriver = new ArrayList<>();
    int listSize = 0;
    int listCurrent = 0;

    private void sendMe() {
        try {
            /*UserTripInfo userTripInfo = new UserTripInfo(
                    27,
                    "912391525",
                    "dhayal");
            userTripInfo.setDriverId(25);*/
            mService.sendReq(userTripInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getNearestDriver() {
        showProgress(true);
        //String notIn = getNotIn();
        Log.d(TAG,"User Id"+ MySharedPreference.getInstance(this).getUserId());
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
                        showProgress(false);
                        removeRequest();
                        mNearestDriver = new ArrayList<>();
                        mNearestDriver = response.body();
                        listCurrent = 0;
                        listSize = mNearestDriver.size();
                        Log.d(TAG, "Sizze " + mNearestDriver.size());
                        if(listSize > 0) {
                            requestHandler.postDelayed(requestRunnable, 0);
                        } else {
                            Toast.makeText(MapActivity.this, "Sorry! No Drivers found! Try Later", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<FindDriver>> call, Throwable t) {
                    Toast.makeText(MapActivity.this, "Sorry! No Drivers found! Try Later", Toast.LENGTH_LONG).show();
                }

            });
        } else {
            Toast.makeText(this,"Please enter valid address",Toast.LENGTH_LONG).show();
        }
    }

    /*private void cancelCall() {

    }*/

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
                    mDriverPhone.setText("Driver Phone: " + g.getPhone());
                    mCallingCaptain.setText("Captain Found! On the way");
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification("Driver Found", "Driver Coming to you");
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
                        Toast.makeText(MapActivity.this,"Sorry! No drivers found",Toast.LENGTH_LONG).show();
                    } else {
                        requestHandler.postDelayed(requestRunnable, 0);
                    }
                    //removeRequest();
                    //requestHandler.removeCallbacks(requestRunnable);
                    //requestHandler.postDelayed(requestRunnable, 0);
                    //if (requestCounter < 9) {
                    //getClosestDriver();
                    //} else {
                    //mRequest.setText("Sorry! Driver Not Found");
                    //}
                    break;
                case 5:
                    cancelRequest();
                    /* //ringtone.play();
                    mCustomerInfo.setVisibility(View.VISIBLE);
                    mSwipeLayout.setVisibility(View.GONE);
                    mCustomerName.setText(R.string.passenger_cancel);
                    mCustomerPickup.setText("");
                    mCustomerDestination.setText("");
                    //mCustomerPhone.setText("");
                    mTripDistance.setText("");
                    mAcceptBusyInfo.setVisibility(View.GONE);
                    updateResponse(TRIP_CANCELLED);
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification("Request Cancelled", "Sorry! request cancelled by passenger");
                    if (ringtone.isPlaying()) {
                        ringtone.stop();
                    }
                    if (mTimerRunning) {
                        stopTimer();
                    }
                    cancelCall();*/
                    break;
                case 11:
                    mCallingCaptain.setText("Captain Reached!");
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification("Driver Reached", "Driver Reached your place");
                    break;
                case 9:
                    //Log.i(TAG," receiving driver current location"+ g);
                    //LatLng newLocation;
                    //double oldLat = oldLocation.latitude;
                    //double oldLng = oldLocation.longitude;
                    //double newLat = g.getLat();
                    //double newLng = g.getLng();
                    //LatLng newLocation = new LatLng(g.getLat(), g.getLng());
                    //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    //float rotation = (float) SphericalUtil.computeHeading(oldLocation, newLocation);
                    //rotateMarker(mDriverMarker, newLocation, rotation);
                    //oldLocation = newLocation;
                    //} else {
                    //updateDriverLocMarker(newLocation);
                    //}
                    // break;
                    break;
                case 6:
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification("Trip Canceled", "Trip Cancelled by driver");
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
                    mCallingCaptain.setText("Trip Started!");
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification("Trip Started", "Trip Started by driver");
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
                    mAmount.setText("Pay Driver : " + g.getPhone() + " SDG");
                    MyNotificationManager.getInstance(MapActivity.this).displayNotification("Trip Completed", "Trip Completed");
                    // mCustomerInfo.setVisibility(View.GONE);
                    // todo display payment details
                    break;
                case 99:
                    //userTripInfo.setDriverId(driverId);
                    break;
            }
        }
    };

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
                            MySharedPreference.getInstance(getApplicationContext()).getUserName()
                            );
                    double dLat = driverList.getLat();
                    double dLng = driverList.getLng();
                    driverId = dId;
                    Log.d(TAG,"driver id "+ driverId);
                    userTripInfo.setDriverId(driverId);
                    //addMarker(new LatLng(dLat, dLng));
                    btRequest.setVisibility(View.GONE);
                    mCancelRequest.setVisibility(View.VISIBLE);
                    mCallingCaptain.setVisibility(View.VISIBLE);
                    //requestedDrivers[++requestCounter] = driverId;
                    isRequestInProgress = true;
                    if (!isDriverAccepted) {
                        //mNearestDriver.remove(driverList);
                        Log.d(TAG,"sending request");
                        //i.remove();
                        userTripInfo.setMyFlag(NEW_REQUEST);
                        sendMe();
                        isDriverBusy = false;
                    }
                    //requestHandler.postDelayed(requestRunnable, 25000);
                    /*try {
                        Thread.sleep(25000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                } else {
                    isRequestInProgress = false;
                    requestCounter = 9;
                    removeRequest();
                    Toast.makeText(MapActivity.this, "Sorry! No drivers found", Toast.LENGTH_LONG).show();
                }
            //}
        }
    };

    private void removeRequest() {
        requestHandler.removeCallbacks(requestRunnable);
    }
    /*
    private final Runnable requestRunnable = new Runnable() {
        @Override
        public void run() {
            final String pLat = Double.toString(userTripInfo.getLat());
            final String pLng = Double.toString(userTripInfo.getLng());
            //Log.i(TAG,"lat "+ pLat);
            final String notIn = getNotIn();

            Map<String, String> params = new HashMap();
            params.put("passengerId", Integer.toString(userTripInfo.getUserId()));
            params.put("tripId", Double.toString(userTripInfo.getTripId()));
            params.put("lat", pLat);
            params.put("car", userTripInfo.getCar());
            params.put("lng", pLng);
            params.put("notIn", notIn);

            JSONObject parameters = new JSONObject(params);
            bringBackDriver();
            //Log.i(TAG,"json "+ parameters);
            //Toast.makeText(getApplicationContext(),"param : "+parameters, Toast.LENGTH_LONG).show();
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST,
                    MyUrl.URL_GET_NEAR_DRIVER, parameters, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        jsonArray = response.getJSONArray("nearDrivers");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject driverList;
                            driverList = jsonArray.getJSONObject(i);
                            int dId = Integer.parseInt(driverList.getString("driverId"));
                            if (dId > 0) {
                                erasePolylines();
                                double dLat = Double.parseDouble(driverList.getString("dLat"));
                                double dLng = Double.parseDouble(driverList.getString("dLng"));
                                //String destDistance = String.format("%.2f",distanceToDestination());
                                //getRouteToMarker(dLat,dLng);
                                //oldLocation = new LatLng(dLat, dLng);
                                mDriverMarker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(dLat, dLng)).title("your driver").flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.car_top)));
                                drawPolylines(dLat, dLng);
                                driverId = Integer.parseInt(driverList.getString("driverId"));
                                userTripInfo.setDriverId(driverId);
                                //addMarker(new LatLng(dLat, dLng));
                                mCancelRequest.setVisibility(View.VISIBLE);
                                requestedDrivers[++requestCounter] = driverId;
                                isRequestInProgress = true;
                                if(!isDriverAccepted) {
                                    userTripInfo.setMyFlag(NEW_REQUEST);
                                    sendMe();
                                    isDriverBusy = false;
                                }
                                //Log.i(TAG,"request thread id : " +Thread.currentThread().getId());
                                //Thread.sleep(25000);
                                //if(isDriverAccepted) {
                                //isDriverBusy = false;
                                //getClosestDriver();
                                //} //else {
                                //removeRequest();
                                //}
                                requestHandler.postDelayed(requestRunnable, 25000);
                            } else {
                                isRequestInProgress = false;
                                if (mDriverMarker != null) {
                                    mDriverMarker.remove();
                                }
                                erasePolylines();
                                requestCounter = 9;
                                //mRequest.setText("Sorry! Driver Not Found");
                                removeRequest();
                                //removeRequest();
                                Toast.makeText(mContext, "Sorry! No drivers found", Toast.LENGTH_LONG).show();
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } //catch (InterruptedException e) {
                    //e.printStackTrace();
                    //}
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //error.printStackTrace();
                    Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                    //TODO: handle failure
                }
            });
            MyBase.getInstance(mContext).addToRequestQueue(jsonRequest);
        }
    };*/

}