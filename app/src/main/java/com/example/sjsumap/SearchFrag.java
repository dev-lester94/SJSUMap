package com.example.sjsumap;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFrag} factory method to
 * create an instance of this fragment.
 */
public class SearchFrag extends Fragment {

    //Service fragment variables and definition

    private HashMap<String, Building> mBuildings;
    private ListView mSearchLv;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> buildingNames;


    private SearchFragListener listener;

    //Interface to send back data to MainActivity
    public interface SearchFragListener{
        void onSearchFragSent(LatLng buildingCenter, String buildingName);
    }

    public SearchFrag(HashMap<String, Building> buildings){
        mBuildings = buildings;
    }


    //Default onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*Inflate the fragment with the fragment_direction layout and define the views
     in the layout. Build a list of the buildings on campus and sort them alphabetically
     and list them in a listview.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_search, container, false);

        Toolbar toolbar = v.findViewById(R.id.searchToolbar);
        toolbar.setTitle("Search");

        buildingNames = new ArrayList<>(mBuildings.keySet());
        Collections.sort(buildingNames);

        mSearchLv = v.findViewById(R.id.searchLv);

        mAdapter = new ArrayAdapter<String>
                (getActivity(),android.R.layout.simple_list_item_1, buildingNames);
        mSearchLv.setAdapter(mAdapter);


        //Clicking on a building will send you back to the MapActivity and send back
        //the neccesary information (building name and center coordinate) to place a marker on
        // its behalf on the Map
        mSearchLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String buildingName = (String) adapterView.getItemAtPosition(i);
                LatLng buildingCenter = mBuildings.get(buildingName).getCenter();
                listener.onSearchFragSent(buildingCenter, buildingName);

                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });







        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof SearchFragListener){
            listener = (SearchFragListener) context;
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