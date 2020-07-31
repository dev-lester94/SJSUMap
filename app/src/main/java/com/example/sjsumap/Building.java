package com.example.sjsumap;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Building {

    //Building attributes
    private String mId;
    private String mAcro;
    private String mName;
    private LatLng mCenter;
    private LatLng[] mOuter;
    private LatLng[] mInner;
    private String mBuildingDesc;
    private String mServiceDesc;
    private String mImg;

    //Constructor to create a Building object

    public Building(String _id, String acro, String name, LatLng center,
                    LatLng[] outer, LatLng[] inner,
                    String buildingDesc, String serviceDesc, String img)
    {
        mId = _id;
        mAcro = acro;
        mName = name;
        mCenter = center;

        mOuter = outer;
        mInner = inner;
        mBuildingDesc = buildingDesc;
        mServiceDesc = serviceDesc;
        mImg = img;
    }


    //Getter methods to return building attributes

    public String getId(){
        return mId;
    }

    public String getAcro(){
        return mAcro;
    }

    public String getName(){
        return mName;
    }

    public LatLng getCenter(){
        return mCenter;
    }

    public LatLng[] getOuter(){
        return mOuter;
    }

    public LatLng[] getInner(){
        return mInner;
    }

    public String getBuildingDesc(){
        return mBuildingDesc;
    }

    public String getServiceDesc(){
        return mServiceDesc;
    }

    public String getImg(){
        return mImg;
    }



}
