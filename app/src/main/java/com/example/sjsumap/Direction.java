package com.example.sjsumap;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

public class Direction {

    //Direction attributes

    private LatLngBounds mBounds;
    private LatLng mStartLocation;
    private LatLng mEndLocation;
    private String mStartAddress;
    private String mEndAdddress;
    private ArrayList<LatLng> mPolyLineList;
    private ArrayList<String> mInstructions;

    //Constructor to create a Direction Object

    public Direction( LatLngBounds bounds, LatLng startLocation, LatLng endLocation,
                      String startAddress,String endAddress, ArrayList<LatLng> polyLineList,
                      ArrayList<String> instructions){

        mBounds = bounds;
        mStartLocation = startLocation;
        mEndLocation = endLocation;
        mStartAddress = startAddress;
        mEndAdddress = endAddress;
        mPolyLineList = polyLineList;
        mInstructions = instructions;

    }

    //Getter methods to return direction attributes

    public LatLngBounds getBounds() {
        return mBounds;
    }

    public LatLng getStartLocation() {
        return mStartLocation;
    }

    public LatLng getEndLocation() {
        return mEndLocation;
    }

    public String getStartAddress() {
        return mStartAddress;
    }

    public String getEndAdddress() {
        return mEndAdddress;
    }

    public ArrayList<LatLng> getPolyLineList() {
        return mPolyLineList;
    }

    public ArrayList<String> getInstructions() {
        return mInstructions;

    }

}

