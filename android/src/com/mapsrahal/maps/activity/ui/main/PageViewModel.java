package com.mapsrahal.maps.activity.ui.main;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.mapsrahal.maps.MwmApplication;
import com.mapsrahal.maps.MySharedPreference;
import com.mapsrahal.maps.api.ApiClient;
import com.mapsrahal.maps.api.PostApi;
import com.mapsrahal.maps.model.MatchingItem;
import com.mapsrahal.maps.model.Post;
import com.mapsrahal.maps.model.dao.MatchDao;
import com.mapsrahal.maps.model.db.MatchDatabase;
import com.mapsrahal.util.DateUtils;
import com.mapsrahal.util.Utils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PageViewModel extends AndroidViewModel {

    private double mMyTripDistance;
    ArrayList<MatchingItem> mMatchingList = new ArrayList<>();
    private static double ELEGIBLE_LIMIT = 1.4d;
    Context context = MwmApplication.get().getApplicationContext();
    private MatchDao matchDao;

    public PageViewModel(@NonNull Application application) {
        super(application);
        MatchDatabase database = MatchDatabase.getInstance(application);
        matchDao = database.matchDao();
    }

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            //return mMatchingList.get(input).getmText1();
            return "Available Soon!";
        }
    });

    private LiveData<LiveData<MatchingItem>> mList = Transformations.map(mIndex, new Function<Integer, LiveData<MatchingItem>>() {
        @Override
        public LiveData<MatchingItem> apply(Integer input) {
            return matchDao.getMatchList(input,input);
            //return "Viewing Page " + input;
        }
    });

    public LiveData<LiveData<MatchingItem>> getMatchItem() {
        //return matchDao.getMatchList(mIndex.getValue(),mIndex.getValue());
        return mList;
    }

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }

    // todo add more criteria
    public void createPost() {
        mMyTripDistance = Double.parseDouble(MySharedPreference.getInstance(context).getTripDistance().trim());
        PostApi postApi = ApiClient.getClient().create(PostApi.class);
        Post post = new Post(null, MySharedPreference.getInstance(context).getUserId(),
                MySharedPreference.getInstance(context).getFrmLat(),
                MySharedPreference.getInstance(context).getFrmLng(),
                MySharedPreference.getInstance(context).getToLat(),
                MySharedPreference.getInstance(context).getToLng(),
                mMyTripDistance,
                MySharedPreference.getInstance(context).getFrmAddress().trim(),
                MySharedPreference.getInstance(context).getToAddress().trim(),
                new Date(MySharedPreference.getInstance(context).getStartTime()),
                MySharedPreference.getInstance(context).getPhoneNumber(),0,0,"x",
                0.0,1,"x","");

        Call<List<Post>> call = postApi.createPost(post);

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                createMatchList(response.body());
            }
            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {

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
            //mMatchMaker.getMatchingList();
            if(MySharedPreference.getInstance(context).isCaptain()) {
                if (isCaptainEligible(mMyTripDistance, totDist, post.getSrcDistDiff(), post.getDestDistDiff(), post.getTripDistance())) {
                    // insert post
                    // todo get accurate distance and add
                    insert(new MatchingItem(post.getId(),post.getUserId(),
                            post.getSourceAddress(), post.getDestinationAddress(),
                            Double.toString(post.getTripDistance()), DateUtils.formatDateStr(post.getStartTime()), Double.toString(totDist), totDistTxt,
                            amount,extraDistance,mMyTripDistance,post.getSrcLat(),post.getSrcLng(),post.getDestLat(),post.getDestLng()
                            ,post.getSeats(),post.getDropDownVal(),post.getPrice(),post.getName()));
                    //insert(mMatchingList);
                }
            } else {
                if (isPassengerEligible(mMyTripDistance, totDist, post.getSrcDistDiff(), post.getDestDistDiff(), post.getTripDistance())) {
                    // insert post
                    // todo get accurate distance and add
                    insert(new MatchingItem(post.getId(),post.getUserId(),
                            post.getSourceAddress(), post.getDestinationAddress(),
                            Double.toString(post.getTripDistance()), DateUtils.formatDateStr(post.getStartTime()), Double.toString(totDist), totDistTxt,
                            amount,extraDistance,mMyTripDistance,post.getSrcLat(),post.getSrcLng(),post.getDestLat(),post.getDestLng()
                            ,post.getSeats(),post.getDropDownVal(),post.getPrice(),post.getName()));
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
}