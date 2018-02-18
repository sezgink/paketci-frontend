package com.example.sezgink.paketciapp;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Parser;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CarrierActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap gmap;
    public RequestQueue queue;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrier);

        queue = Volley.newRequestQueue(this);

        Button buttonGet = findViewById(R.id.buttonGet);
        final TextView textView = findViewById(R.id.showDataText);
        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("Lets start now");
                /*
                String url = "https://api.myjson.com/bins/14imk1";
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                Log.d("Response", response.toString());
                                try {

                                    textView.setText(response.get("hello").toString());
                                    LatLng fy = new LatLng(40.7143528, -74.0059731);
                                    LatLng fy2 = new LatLng(40.7144528, -74.0059731);

                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(fy);
                                    gmap.addMarker(markerOptions);

                                    drawRoute(parseRoute(response.toString()));



                                    Log.d("2.d Marker","No problem");
                                    gmap.addMarker(new MarkerOptions().position(fy2));
                                    Log.d("2.d Marker","Created");

                                    //gmap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(40.7, -74.0)));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Error.Response", error.toString());
                            }
                        }
                );
                */
                String url2 = getUrl(new LatLng(41.077858,29.0138537),new LatLng(41.077858,28.0138537));
                StringRequest getRouteRequest = new StringRequest("https://paketciapi.herokuapp.com/api/courier/0",  new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Lol", "onResponse: ");
                        Log.d("Lol", response);
                        /*
                        ParserTask parserTask = new ParserTask();
                        parserTask.execute(response);
                        */
                        drawRoute(parseRoute(response));
                        addMarkers(response);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });



                // add it to the RequestQueue
                //queue.add(getRequest);
                queue.add(getRouteRequest);




            }
        });
        //https://api.myjson.com/bins/14imk1




        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);




    }
    private void addMarkers(String jsonObject) {
        JsonElement element = new JsonParser().parse(jsonObject);
        JsonObject jo = element.getAsJsonObject();
        Log.d("Marker","1");
        JsonObject curLocations = jo.getAsJsonObject("curLocation");
        Log.d("Marker","2");
        gmap.addMarker(new MarkerOptions().position(new LatLng(curLocations.get("lat").getAsDouble(),curLocations.get("long").getAsDouble())));
    }
    private List<LatLng> parseRoute(String jsonObject) {
        List<LatLng> list1 = new ArrayList<>();
        JsonElement element = new JsonParser().parse(jsonObject);
        JsonObject jo = element.getAsJsonObject();
        JsonArray routes = jo.getAsJsonArray("routes");
        jo = routes.get(0).getAsJsonObject().getAsJsonObject("startLoc");
        list1.add(new LatLng(jo.get("lat").getAsDouble(),jo.get("long").getAsDouble()));

        for(int i=0;i<routes.size();i++) {
            jo = routes.get(i).getAsJsonObject().getAsJsonObject("endLoc");
            list1.add(new LatLng(jo.get("lat").getAsDouble(),jo.get("long").getAsDouble()));
            Log.d("Parse", String.valueOf(i));
        }
        return list1;
    }

    private void drawRoute(List<LatLng> points) {
        gmap.clear();
        String theUrl;
        for (int i = 0; i<points.size()-1;i++) {
            theUrl = getUrl(points.get(i),points.get(i+1));
            StringRequest getRouteRequest = new StringRequest(theUrl,  new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("Lol", "onResponse: ");
                    ParserTask parserTask = new ParserTask();
                    parserTask.execute(response);
                    Log.d("Lol", "Returned route response ");

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            Log.d("Lol", "Ready to add request");
            queue.add(getRouteRequest);


        }
        if(points.size()>0)
        gmap.moveCamera(CameraUpdateFactory.newLatLng(points.get(0)));




    }


    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "AIzaSyDDMAGBe2tkSH8SJ4Vu1GRK7WaEKfSABrA" ;


        return url;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
        gmap.setMinZoomPreference(12);
        LatLng ny = new LatLng(41.077858,29.0138537);
        gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
        gmap.setMinZoomPreference(6);



    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                gmap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}
