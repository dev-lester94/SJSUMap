package com.example.sjsumap;

import java.util.ArrayList;

public class Service {

    //Service attributes

    private String mServiceName;
    private String mIcon;
    private ArrayList<String> mBuildingNames;

    //Constructor to create a service object

    public Service(String serviceName, String icon, ArrayList<String> buildingNames){
        mServiceName = serviceName;
        mIcon = icon;
        mBuildingNames = buildingNames;
    }


    //service getter methods to get service attributes
    public String getServiceName(){
        return mServiceName;
    }


    public String getIcon(){
        return mIcon;
    }

    public ArrayList<String> getBuildingNames(){
        return mBuildingNames;
    }




}
