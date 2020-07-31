package com.example.sjsumap;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class ServiceFrag extends Fragment {

    //Service fragment variables and definition

    private String[] services = {"Academic & Adminstrative",
            "Arts & Performance",
            "Athletic & Recreation",
            "Dining",
            "Emergency Resources",
            "Housing",
            "Libraries",
            "Parking",
            "Printing"
    };

    private ArrayList<String> mServices= new ArrayList<String>(Arrays.asList(services));

    private ArrayList<Service> mServiceList;

    private ServiceFragListener listener;

    public interface ServiceFragListener{
        void onServiceFragSent(Service service);
    }




    public ServiceFrag() {
        // Required empty public constructor
    }


    //Default onCreate

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /*Inflate the fragment with the fragment_service layout and define the views
    in the layout.
      */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        InputStream inputStream = getResources().openRawResource(R.raw.services);
        try {
            String jsonResponse = readFromStream(inputStream);
            mServiceList= extractService(jsonResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }

        View v = inflater.inflate(R.layout.fragment_service, container, false);
        Toolbar toolbar = v.findViewById(R.id.serviceToolbar);
        toolbar.setTitle("Services");



        //Create listview to grab show the list of services the campus provides
        ListView serviceListView = v.findViewById(R.id.serviceList);
        ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                mServices);
        serviceListView.setAdapter(adapter);

        //Clicking on service will send back the arraylist of buildings that provide
        //a particular service

        serviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String serviceName = (String) adapterView.getItemAtPosition(i);
                FragmentManager fm = getFragmentManager();

                switch(serviceName) {
                    case "Academic & Adminstrative":
                        listener.onServiceFragSent(mServiceList.get(0));
                        break;
                    case "Arts & Performance":
                        listener.onServiceFragSent(mServiceList.get(1));
                        break;
                    case "Athletic & Recreation":
                        listener.onServiceFragSent(mServiceList.get(2));
                        break;
                    case "Dining":
                        listener.onServiceFragSent(mServiceList.get(3));
                        break;
                    case "Emergency Resources":
                        listener.onServiceFragSent(mServiceList.get(4));
                        break;
                    case "Housing":
                        listener.onServiceFragSent(mServiceList.get(5));
                        break;
                    case "Libraries":
                        listener.onServiceFragSent(mServiceList.get(6));
                        break;
                    case "Parking":
                        listener.onServiceFragSent(mServiceList.get(7));
                        break;
                    case "Printing":
                        listener.onServiceFragSent(mServiceList.get(8));
                        break;
                    default :
                        break;
                }

                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("SJSU Map");

                //Toast.makeText(getActivity(),serviceName, Toast.LENGTH_LONG).show();
                fm.popBackStack();
            }
        });


        return v;
    }

    //Read and parse the service json file and return an arraylist that holds
    //a list of buildings that provide a particular service
    private ArrayList<Service> extractService(String serviceJSON) {

        ArrayList<Service> serviceList= new ArrayList<Service>();

        try {

            JSONObject jsonRootObject = new JSONObject(serviceJSON);
            Iterator<String> keys= jsonRootObject.keys();
            ArrayList<String> buildingNames;

            while (keys.hasNext())
            {
                buildingNames = new ArrayList<String>();

                String serviceName = (String)keys.next();
                //Log.i("keyValue", serviceName);

                JSONObject JSONObject = jsonRootObject.getJSONObject(serviceName);
                String icon = JSONObject.optString("icon");
                JSONArray buildingsJSONArray = JSONObject.optJSONArray("buildings");


                for(int j=0; j < buildingsJSONArray.length(); j++) {
                    buildingNames.add(buildingsJSONArray.getString(j));
                }

                Service service = new Service(serviceName, icon, buildingNames);
                serviceList.add(service);

            }




        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the Building JSON results", e);
        }

        return serviceList;
    }

    //Read  the JSON file byte by byte and return the JSON file string to be extracted in
    // extractService

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /*Attach and detach the service  Fragment from MainActivity
    Make sure to get the context for the listener object*/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ServiceFragListener){
            listener = (ServiceFragListener) context;
        }else{
            throw new RuntimeException(context.toString()
            + " must implement ServiceFragListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}