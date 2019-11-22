package com.example.mooddiary.ui.mymap;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.mooddiary.Database;
import com.example.mooddiary.LoginActivity;
import com.example.mooddiary.MapsActivity;
import com.example.mooddiary.MoodEvent;
import com.example.mooddiary.MoodList;
import com.example.mooddiary.R;
import com.example.mooddiary.ui.home.HomeViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * This is MyMapFragment which shows user's mood event map
 */
public class MyMapFragment extends Fragment {

    private MapView myMapMoodMapMap;
    private GoogleMap myMap;
    private ArrayList<MoodEvent> myMoodList = new ArrayList<>();
    private ProgressBar myMapLoadingProgress;
  
    /**
     * This creates the view for the user's mood event map.
     * @param inflater
     *      This is a LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container
     *      This can be null. If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState
     *      If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return
     *      Return the view for the fragment UI
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_map, container, false);
        //final TextView textView = root.findViewById(R.id.text_slideshow);

        myMapMoodMapMap = root.findViewById(R.id.mymap_map_map);
        myMapMoodMapMap.onCreate(savedInstanceState);
        myMapLoadingProgress = root.findViewById(R.id.myMap_loading_progress);

        myMapLoadingProgress.setVisibility(View.VISIBLE);

        DocumentReference docRef = Database.getUserMoodList(LoginActivity.userName);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                myMoodList = documentSnapshot.toObject(MoodList.class).getAllMoodList();
                myMapMoodMapMap.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        myMap = googleMap;
                        myMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(60,-100)));
                        myMapLoadingProgress.setVisibility(View.INVISIBLE);
                        for(MoodEvent m: myMoodList) {
                            System.out.println(m.getMood().getMood());
                            if(m.getLocation() != null) {
                                LatLng markPoint = getLocationLatLng(getContext(), m.getLocation());
                                if(markPoint != null) {
                                    myMap.addMarker(new MarkerOptions().position(markPoint).title(m.getMood().getMood()).icon(
                                            BitmapDescriptorFactory.fromResource(m.getMood().getMarker())));
                                }
                            }
                        }
                    }
                });
            }
        });


//        Intent intent = new Intent(getActivity(),MapsActivity.class);
//        intent.putExtra("map","mymap");
//        intent.putExtra("moodlist",viewModelFromHome.getMoodList().getAllMoodList());
//        startActivity(intent);


        return root;
    }

    /**
     * Getting the latitude and longitude of a location string by geocoder
     * May throw IOException if the locationName does not exist
     * @param context
     *      This activity
     * @param locationName
     *      This a location
     * @return
     *       Returns the LatLng of location
     */
    public LatLng getLocationLatLng(Context context, String locationName) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng l1 = null;

        try {
            address = coder.getFromLocationName(locationName, 5);
            if (address == null) {
                return null;
            }
            if (!(address.isEmpty())){
                Address location = address.get(0);
                l1 = new LatLng(location.getLatitude(), location.getLongitude());
            }
            else{
                Toast.makeText(getActivity(), locationName+" is not a valid address, please " +
                        "enter the correct address", Toast.LENGTH_SHORT).show();
                return null;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return l1;
    }

    @Override
    public void onResume() {
        super.onResume();
        myMapMoodMapMap.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        myMapMoodMapMap.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myMapMoodMapMap.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        myMapMoodMapMap.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        myMapMoodMapMap.onLowMemory();
    }
}