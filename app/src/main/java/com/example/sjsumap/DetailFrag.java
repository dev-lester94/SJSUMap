package com.example.sjsumap;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailFrag extends Fragment {

    //Building detail attributes
    private int mImageNameResID;
    private String mName;
    private String mAcro;
    private String mBuildingDesc;
    private String mServiceDesc;


    //Define TextView and ImageView from fragment_details.xml
    private ImageView mBuildingImgIv;
    private TextView mBuildingNameTv;
    private TextView mBuildingDescTv;
    private TextView mServiceDescTv;


    //Constructor to create the detail Fragment
    public DetailFrag(Building building, int imageNameResID) {

        mImageNameResID = imageNameResID;
        mName = building.getName();
        mAcro = building.getAcro();
        mBuildingDesc = building.getBuildingDesc();
        mServiceDesc = building.getServiceDesc();


    }

    //Default onCreate

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /*Inflate the detail Fragment with fragment_detail layout and defining and setting
    /views in the layout*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        Toolbar toolbar = v.findViewById(R.id.detailToolbar);
        toolbar.setTitle("Building Details");
        ///getActivity().setTitle("Building Details");

        mBuildingImgIv = (ImageView) v.findViewById(R.id.buildingImgIv);
        mBuildingNameTv = (TextView) v.findViewById(R.id.buildingNameTv);
        mBuildingDescTv = (TextView) v.findViewById(R.id.buildingDescTv);
        mServiceDescTv = (TextView) v.findViewById(R.id.serviceDescTv);

        mBuildingImgIv.setImageResource(mImageNameResID);
        mBuildingNameTv.setText(mName + " (" + mAcro + ")");
        mBuildingDescTv.setText(mBuildingDesc);
        mServiceDescTv.setText(mServiceDesc);

        return v;
    }



   /*Attach and detach the detail Fragment from MainActivity*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}