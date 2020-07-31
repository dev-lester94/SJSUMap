package com.example.sjsumap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

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
import java.util.HashMap;

public class MainActivity extends  AppCompatActivity  implements OnMapReadyCallback, GoogleMap.OnPolygonClickListener, NavigationView.OnNavigationItemSelectedListener, ServiceFrag.ServiceFragListener, GoogleMap.OnMarkerClickListener, DirectionFrag.DirectionFragListener, SearchFrag.SearchFragListener {

    //MainActivity variables and definition

    private GoogleMap mMap;

    public static final String LOG_TAG = MainActivity.class.getName();

    private static final int Builder_LOADER_ID = 1;


    private HashMap<String, Building> mBuildings;

    private DetailFrag detailFrag;
    private ServiceFrag serviceFrag;


    private DrawerLayout drawer;

    private ArrayList<Marker> mMarkers = new ArrayList<Marker>();

    private InputMethodManager mgr;

    private AutoCompleteTextView searchAutoTv;

    private FloatingActionButton mFab;

    private DirectionFrag directionFrag;

    private SearchFrag searchFrag;


    /*onCreate and make sure to call getMapAsync on the mapFragment for the map
    * to be ready to load up on screen*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /* Overiding menu to add a refresh icon to refresh the map*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.refresh:
                resetMap();
                //finish();
                //startActivity(getIntent());
                //Toast.makeText(this,"refresh",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*When map is ready, get polygon data and draw it out. Load the services, searchBuilding, and
    * get direction functionality*/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sjsu = new LatLng(37.3352, -121.8811);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sjsu, 16));

        getPolygonData();
        drawPolygons();
        mMap.setOnPolygonClickListener(this);
        loadServices();
        searchForBuilding();
        getDirections();

        directionFrag = new DirectionFrag(mBuildings);


    }

    /*Read and extract polygon data from buildings JSON file and return a hashmap
    with buildingName and building object
    **/

    private void getPolygonData() {
        InputStream inputStream = getResources().openRawResource(R.raw.buildings);
        try {
            String jsonResponse = readFromStream(inputStream);
            mBuildings= extractBuildings(jsonResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*Read json file byte by byte and return a JSON string to be extracted
    * in extractBuildings method*/

    private String readFromStream(InputStream inputStream) throws IOException {
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


    /*Extract the JSON information and return the hashmap of building information */
    public static HashMap<String,Building> extractBuildings(String buildingJSON) {

        if (TextUtils.isEmpty(buildingJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding earthquakes to
        HashMap<String,Building> buildings = new HashMap<String, Building>();
        //Building building = new Building("Dudley MooreHead Hall");
        //buildings.put("DMH", building);

        // Try to parse the Building_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            JSONArray jsonRootArray = new JSONArray(buildingJSON);
            for(int i=0; i < jsonRootArray.length(); i++){
                JSONObject JSONObject = jsonRootArray.getJSONObject(i);
                String _id = JSONObject.optString("_id");
                //Log.i(LOG_TAG, _id);
                String acro = JSONObject.optString("acro");
                String name = JSONObject.optString("name");
                JSONObject centerJSONObject = JSONObject.optJSONObject("center");
                LatLng center = new LatLng(centerJSONObject.getDouble("lat")
                        ,centerJSONObject.getDouble("lng"));
                //Log.i(LOG_TAG, String.valueOf(center));

                JSONArray outerJSONArray = JSONObject.optJSONArray("outer");

                ArrayList<LatLng> outerTemp = new ArrayList<LatLng>();
                for(int j=0; j < outerJSONArray.length(); j++) {
                    JSONObject outerJSONObject = outerJSONArray.getJSONObject(j);
                    LatLng latlng = new LatLng(outerJSONObject.getDouble("lat")
                            ,outerJSONObject.getDouble("lng"));
                    outerTemp.add(latlng);
                }

                LatLng[] outer = new LatLng[outerTemp.size()];
                outer = outerTemp.toArray(outer);


                //Log.i("Outer 0", String.valueOf(outer.get(0)));
                //Log.i("Outer 1", String.valueOf(outer.get(1)));

                JSONArray innerJSONArray = JSONObject.optJSONArray("inner");
                ArrayList<LatLng> innerTemp = new ArrayList<LatLng>();
                for(int j=0; j < innerJSONArray.length(); j++) {
                    JSONObject innerJSONObject = innerJSONArray.getJSONObject(j);
                    LatLng latlng = new LatLng(innerJSONObject.getDouble("lat")
                            ,innerJSONObject.getDouble("lng"));
                    innerTemp.add(latlng);
                }

                LatLng[] inner = new LatLng[innerTemp.size()];
                inner = innerTemp.toArray(inner);


                /*if(inner!=null && inner.size() > 0) {
                    Log.i("i", String.valueOf(i));
                }*/

                String buildingDesc = JSONObject.optString("building desc");
                String serviceDesc = JSONObject.optString("service desc");
                String img = JSONObject.optString("img");

                Building building = new Building(_id,acro, name, center, outer,
                        inner, buildingDesc, serviceDesc,img);
                buildings.put(name,building);
                //Log.i(LOG_TAG, String.valueOf(img));


            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the Building JSON results", e);
        }

        // Return the list of earthquakes
        return buildings;
    }

    /*Draw the polygons on the screen */

    private void drawPolygons() {
        for (String buildingName : mBuildings.keySet()) {
            LatLng[] outer = mBuildings.get(buildingName).getOuter();

            Resources res = getResources();
            PolygonOptions polygonOptions = new PolygonOptions().add(outer).
                    fillColor(res.getColor(R.color.buildingBodyColor)).
                    strokeColor(res.getColor(R.color.buildingStrokeColor)).clickable(true);

            LatLng [] inner = mBuildings.get(buildingName).getInner();
            if(inner.length > 0){
                polygonOptions.addHole(Arrays.asList(inner));
            }

            Polygon polygon= mMap.addPolygon(polygonOptions);
            polygon.setTag(buildingName);
        }
    }


    /*Clicking a polygon will send you to view Building Details*/
    @Override
    public void onPolygonClick(Polygon polygon) {
        String clickedPolygonName = String.valueOf(polygon.getTag());
        Log.i("clickedPolygonName", clickedPolygonName);
        viewBuildingDetails(clickedPolygonName);

    }

    private void viewBuildingDetails(String buildingName) {
        Building clickedPolygonBuilding = mBuildings.get(buildingName);
        String[] removeJpg= clickedPolygonBuilding.getImg().split(".jpg");
        String imageName = removeJpg[0];
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        imageName = imageName.replaceAll(regex, replacement).toLowerCase();
        int imageNameResID = getResources().
                getIdentifier(imageName , "drawable", getPackageName());
        detailFrag = new DetailFrag(clickedPolygonBuilding, imageNameResID);
        getSupportFragmentManager().beginTransaction().
                replace(R.id.container, detailFrag).addToBackStack("detailFrag").commit();
    }

    /*Navigation drawer to provide easier navigation ways around the app
    * */

    public void loadServices(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.container);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            //Make sure the keyboard is down when you open the drawer
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(searchAutoTv.getWindowToken(), 0);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


    }

    /*Button to open the directions Fragment to get directions*/

    private void getDirections() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        //directionFrag = new DirectionFrag(mBuildings);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, directionFrag)
                        .addToBackStack("directionFrag").commit();
                //Toast.makeText(MainActivity.this, "clicked", Toast.LENGTH_LONG).show();
            }
        });
    }



    //search for a building by choosing a list of buildings in the autocomplete textview
    //Marker will be placed on map and gets center

    private void searchForBuilding() {
        searchAutoTv = findViewById(R.id.searchAutoTv);
        final String[] buildingNames = mBuildings.keySet().toArray(new String[mBuildings.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_list_item_1, buildingNames);
        searchAutoTv.setAdapter(adapter);

        searchAutoTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                resetMap();

                String buildingName = (String) adapterView.getItemAtPosition(i);

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(mBuildings.get(buildingName).getCenter())
                        .title(buildingName));

                searchAutoTv.setText("");

                mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(searchAutoTv.getWindowToken(), 0);


                mMap.moveCamera(CameraUpdateFactory.newLatLng(mBuildings.get(buildingName).getCenter()));
            }
        });
    }



    //close the drawer on backpress

    @Override
    public void onBackPressed()
    {

        ///setTitle("SJSU Map");
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

    }

    //Select a nvagiation item to go search up a building, know about the services
    //or get directions or open a link on the web brower to know more about SJSU

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        serviceFrag = new ServiceFrag();
        searchFrag = new SearchFrag(mBuildings);
        //directionFrag = new DirectionFrag(mBuildings);
        mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(searchAutoTv.getWindowToken(), 0);

        switch (item.getItemId()){
            case R.id.nav_service:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, serviceFrag)
                        .addToBackStack("serviceFrag").commit();

                break;
            case R.id.nav_search:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, searchFrag)
                        .addToBackStack("searchFrag").commit();
                break;

            case R.id.nav_direction:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, directionFrag)
                        .addToBackStack("directionFrag").commit();
                break;

            case R.id.nav_share:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.sjsu.edu/"));
                startActivity(intent);

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Show the infowindow when marker is clicked
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    //Reset the map to SJSU

    private void resetMap(){
        mMap.clear();
        LatLng sjsu = new LatLng(37.3352, -121.8811);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sjsu, 16));
        drawPolygons();
    }


    //Data sent form service fragment to add markers of buildings that provide
    //a service that was clicked
    @Override
    public void onServiceFragSent(Service service) {

        resetMap();

        //Log.i("serviceName", service.getServiceName());
        //Log.i("serviceIcon", service.getIcon());
        //mMap.clear();


        for(String buildingName: service.getBuildingNames()){
            //Log.i("buildingNames", buildingNames);
             Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(mBuildings.get(buildingName).getCenter())
                    .title(buildingName)
                    .icon(BitmapDescriptorFactory.fromResource(getResources().
                            getIdentifier(service.getIcon() ,
                                    "drawable", getPackageName()))));
            mMarkers.add(marker);

        }

    }



    /*Update the map with origin and destination with markers and the poyline to get from
    * the two points*/

    @Override
    public void onDirectionFragSent(Direction direction) {
        resetMap();

        Log.i("testing", "testing");

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(direction.getBounds(),120));

        mMap.addMarker(new MarkerOptions()
                .position(direction.getStartLocation())
                .title("Start Address: " + direction.getStartAddress()));

        mMap.addMarker(new MarkerOptions()
                .position(direction.getEndLocation())
                .title("End Address: " +  direction.getEndAdddress())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        mMap.addPolyline(new PolylineOptions().addAll(direction.getPolyLineList()));



    }

    /*Add a marker to map to denote the building that was search on the map*/

    @Override
    public void onSearchFragSent(LatLng buildingCenter, String buildingName) {
        resetMap();
        mMap.addMarker(new MarkerOptions()
                .position(buildingCenter)
                .title(buildingName));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(buildingCenter));

    }
}