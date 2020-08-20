package com.example.sjsumap;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class DirectionFrag extends Fragment {

    //Direction fragment variables and definition
    private HashMap<String, Building>mBuildings;
    private AutoCompleteTextView mOrigin;
    private AutoCompleteTextView mDestination;
    private String[] buildingNames;
    private ArrayAdapter<String> adapter;
    private ImageButton mOriginClear;
    private ImageButton mDestinationClear;
    private RadioGroup mMode;
    private Button mbtnOk;
    private ListView mDirectionList;
    private ArrayAdapter<String> directionAdapter;
    private String[] emptyArray = {};
    private ArrayList<String> emptyDirectionList = new ArrayList<>(Arrays.asList(emptyArray));

    private static String Direction_REQUEST_URL="";

    private static final int Direction_LOADER_ID = 2;

    private DirectionFragListener listener;

    private boolean mTwoPane;

    public void isTwoPane(boolean twoPane) {
        mTwoPane = twoPane;
    }

    //Interface to send back data to MainActivity
    public interface DirectionFragListener{
        void onDirectionFragSent(Direction direction);
    }

    //Constructor to create a Direction Fragment
    public DirectionFrag(HashMap<String, Building> buildings) {
        mBuildings = buildings;
        buildingNames = mBuildings.keySet().toArray(new String[mBuildings.size()]);

    }

    //Defined loader to get directions from Origin to destinaton by doing
    //a HTTP GET request with the Direction_REQUEST_URL
    private LoaderManager.LoaderCallbacks<Direction> getDirectionListener
            = new LoaderManager.LoaderCallbacks<Direction>() {

        @Override
        public Loader<Direction> onCreateLoader(int i, Bundle bundle) {
            //Log.i("Direction_REQUEST_URL",Direction_REQUEST_URL);
            return new DirectionLoader(getActivity(), Direction_REQUEST_URL);


        }


        /*
        * Once the results come back, we check if there was is a valid direction
        * from Origin to destination. If no, toast to use that the direction was not found
        * of if yes, toast to the user that the direction is finished and list
        * down the directions in a listview and update the map in MainActivity
         */
        @Override
        public void onLoadFinished(Loader<Direction> loader, Direction direction) {

            //Log.i("Finished", "finished");
            if(direction == null){
                Toast.makeText(getActivity(), "Getting Direction: Not found", Toast.LENGTH_LONG).show();
                getActivity().getLoaderManager().destroyLoader(Direction_LOADER_ID);
                return;
            }
            Toast.makeText(getActivity(), "Getting Direction: Finished", Toast.LENGTH_LONG).show();



            ArrayList<String> directionsList = direction.getInstructions();
            directionAdapter.clear();
            directionAdapter.notifyDataSetChanged();

            directionAdapter.addAll(directionsList);
            getActivity().getLoaderManager().destroyLoader(Direction_LOADER_ID);

            listener.onDirectionFragSent(direction);

            if(!mTwoPane) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragTransaction = fm.beginTransaction();
                fragTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fm.popBackStack();
            }
        }

        @Override
        public void onLoaderReset(Loader<Direction> loader) {

        }
    };

    //Default onCreate
    //testing
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*Inflate the fragment with the fragment_direction layout and define the views
      in the layout.
      */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        adapter = new ArrayAdapter<String>
                (getActivity(),android.R.layout.simple_list_item_1, buildingNames);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_direction, container, false);
        Toolbar toolbar = v.findViewById(R.id.serviceToolbar);
        toolbar.setTitle("Get Directions");


        mOrigin = v.findViewById(R.id.origin);
        mOrigin.setAdapter(adapter);

        mDirectionList = v.findViewById(R.id.directionList);

        directionAdapter = new ArrayAdapter<String>
                (getActivity(),android.R.layout.simple_list_item_1, emptyDirectionList);
        mDirectionList.setAdapter(directionAdapter);

        //.mDirectionList.addHeaderView(mDirectionTv);


        mDestination = v.findViewById(R.id.destination);
        mDestination.setAdapter(adapter);

        mOriginClear = v.findViewById(R.id.origin_clear);
        mDestinationClear = v.findViewById(R.id.destination_clear);


        /*Set on Clicklisteners to clear the origin and destination address
        autcompltetextview*/
        mOriginClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOrigin.setText("");
            }
        });

        mDestinationClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDestination.setText("");
            }
        });

        mMode = v.findViewById(R.id.mode);

        mbtnOk = v.findViewById(R.id.btnOk);

        /*Set an onclicklistener when user wants to get directions from origin
        to destination. Checks to see if the autocompletextviews are empty and if yes
        tell the user to enter the origin and destination. Check to see if the origin
        or destination is a building on campus or not. If it is on campus, use the center
        coordinates in mBuildings as the lat and longitude and if not get results back from
        the Geocoder library. Create the request URL for directions and start the loader
         */
        mbtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(mDestination.getWindowToken(), 0);


                if(TextUtils.isEmpty(mOrigin.getText())){
                    Toast.makeText(getActivity(), "Enter origin address", Toast.LENGTH_LONG).show();
                    return;
                }

                if(TextUtils.isEmpty(mDestination.getText())){
                    Toast.makeText(getActivity(), "Enter destination address", Toast.LENGTH_LONG).show();
                    return;
                }

                String mode;
                int id = mMode.getCheckedRadioButtonId();
                if(id == R.id.walking){
                    mode= "walking";
                    //Toast.makeText(getActivity(), "walking", Toast.LENGTH_LONG).show();
                }else if(id == R.id.driving){
                    mode = "driving";
                    //Toast.makeText(getActivity(), "driving", Toast.LENGTH_LONG).show();
                }else{
                    mode = "transit";
                    //Toast.makeText(getActivity(), "transit", Toast.LENGTH_LONG).show();
                }

                LatLng originLatLng;
                String origin;


                //String originName = mOrigin.getText().toString();
                //originLatLng = mBuildings.get(mOrigin.getText().toString()).getCenter();
                if(mBuildings.get(mOrigin.getText().toString()) != null){
                    originLatLng = mBuildings.get(mOrigin.getText().toString()).getCenter();
                    origin = originLatLng.latitude + "," + originLatLng.longitude;
                }else{
                    Geocoder coder = new Geocoder(getActivity());
                    try {
                        List<Address> originAddress = coder.getFromLocationName(mOrigin.getText().toString(),4) ;
                        if(originAddress.size() == 0){
                            Toast.makeText(getActivity(), "Invalid Origin adddress", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //Address location = originAddress.get(0);
                        origin = originAddress.get(0).getLatitude() +"," + originAddress.get(0).getLongitude();
                        Log.i("origin",origin);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "Invalid Origin adddress", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                //Log.i("origin", origin);

                LatLng destinationLatLng;
                String destination;

                if(mBuildings.get(mDestination.getText().toString()) != null){
                    destinationLatLng = mBuildings.get(mDestination.getText().toString()).getCenter();
                    destination = destinationLatLng.latitude + "," + destinationLatLng.longitude;
                }else{
                    Geocoder coder = new Geocoder(getActivity());
                    try {
                        List<Address> destinationAddress = coder.getFromLocationName(mDestination.getText().toString(),4) ;
                        //Address location = originAddress.get(0);
                        if(destinationAddress.size() == 0){
                            Toast.makeText(getActivity(), "Invalid Destination adddress", Toast.LENGTH_LONG).show();
                            return;
                        }
                        destination = destinationAddress.get(0).getLatitude() +"," + destinationAddress.get(0).getLongitude();
                        //Log.i("origin",origin);

                    } catch (IOException e) {
                        Toast.makeText(getActivity(), "Invalid Destinaton adddress", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        return;
                    }
                }
                //Log.i("destination", destination);

                //Log.i("mode", mode);

                String google_key = getString(R.string.google_map_key);
                //Log.i("google_key", google_key);

                Direction_REQUEST_URL = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin="+ origin +
                        "&destination=" + destination +
                        "&mode="+ mode +
                        "&key="+ google_key;

                //Log.i("Direction_REQUEST_URL",Direction_REQUEST_URL);

                Toast.makeText(getActivity(), "Getting Direction: In Progress", Toast.LENGTH_LONG).show();


                LoaderManager loaderManager = getActivity().getLoaderManager();
                loaderManager.initLoader(Direction_LOADER_ID, null, getDirectionListener);
                //getActivity().getLoaderManager().destroyLoader(Direction_LOADER_ID);
            }
        });

        return v;
    }

    /*Attach and detach the detail Fragment from MainActivity
     Make sure to get the context for the listener object*/


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof DirectionFrag.DirectionFragListener){
            listener = (DirectionFragListener) context;
        }else{
            throw new RuntimeException(context.toString()
                    + " must implement DirectionFragListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}