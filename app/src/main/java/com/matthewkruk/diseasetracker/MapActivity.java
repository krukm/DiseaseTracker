package com.matthewkruk.diseasetracker;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, AddDialogFragment.Listener {

    private static final String TAG = MapActivity.class.getName();
    private static final String KEY_SCHOOL_NAME = "schoolName";
    private static final String KEY_IMMUNIZABLE = "immunizable";

    private static final Map<String, LatLng> SCHOOL_COORDS = new HashMap<>();

    {
        SCHOOL_COORDS.put("Abbot", new LatLng(42.294235, -83.785226));
        SCHOOL_COORDS.put("Angell", new LatLng(42.274521, -83.728093));
        SCHOOL_COORDS.put("Ann Arbor Open At Mack", new LatLng(42.287296, -83.759778));
        SCHOOL_COORDS.put("Ann Arbor Technological High", new LatLng(42.24753, -83.721136));
        SCHOOL_COORDS.put("Bach Elementary", new LatLng(42.27623, -83.75478));
        SCHOOL_COORDS.put("Burns Park Elementary", new LatLng(42.265967, -83.730246));
        SCHOOL_COORDS.put("Carpenter", new LatLng(42.248636, -83.675365));
        SCHOOL_COORDS.put("Clague Middle", new LatLng(42.311307, -83.704414));
        SCHOOL_COORDS.put("Clifford E. Bryant Comm.", new LatLng(42.232498, -83.715524));
        SCHOOL_COORDS.put("Community High", new LatLng(42.28404, -83.74456));
        SCHOOL_COORDS.put("Dicken Elementary", new LatLng(42.26251, -83.776388));
        SCHOOL_COORDS.put("Eberwhite", new LatLng(42.271993, -83.764636));
        SCHOOL_COORDS.put("Forsythe Middle", new LatLng(42.296348, -83.767199));
        SCHOOL_COORDS.put("Haisley Elementary", new LatLng(42.289847, -83.775951));
        SCHOOL_COORDS.put("Huron High", new LatLng(42.28165, -83.703301));
        SCHOOL_COORDS.put("John Allen", new LatLng(42.251161, -83.711167));
        SCHOOL_COORDS.put("Lakewood Elementary", new LatLng(42.276698, -83.790432));
        SCHOOL_COORDS.put("Logan Elementary", new LatLng(42.312546, -83.709277));
        SCHOOL_COORDS.put("Martin Luther King Elem.", new LatLng(42.284898, -83.686343));
        SCHOOL_COORDS.put("Mary D. Mitchell", new LatLng(42.236999, -83.693087));
        SCHOOL_COORDS.put("Northside Elementary", new LatLng(42.298826, -83.7335));
        SCHOOL_COORDS.put("Pattengill", new LatLng(42.255154, -83.720463));
        SCHOOL_COORDS.put("Pioneer High", new LatLng(42.262136, -83.75439));
        SCHOOL_COORDS.put("Pittsfield", new LatLng(42.250033, -83.691592));
        SCHOOL_COORDS.put("Roberto Clemente Center", new LatLng(42.20198, -83.673071));
        SCHOOL_COORDS.put("Scarlett Middle", new LatLng(42.235347, -83.692642));
        SCHOOL_COORDS.put("Skyline High", new LatLng(42.305002, -83.77799));
        SCHOOL_COORDS.put("Slauson Middle", new LatLng(42.279294, -83.761425));
        SCHOOL_COORDS.put("Tappan Middle", new LatLng(42.262175, -83.717147));
        SCHOOL_COORDS.put("Thurston Elementary", new LatLng(42.306422, -83.701583));
        SCHOOL_COORDS.put("Uriah H. Lawton", new LatLng(42.253525, -83.763911));
        SCHOOL_COORDS.put("Wines Elementary", new LatLng(42.298311, -83.769476));
    }

    private GoogleMap mMap;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapInit();
        startUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapInit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add: {
                AddDialogFragment.newInstance(this).show(
                        getSupportFragmentManager(),
                        AddDialogFragment.TAG);

                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void mapInit() {
        FragmentManager fm = getSupportFragmentManager();
        // Try to obtain the map from the SupportMapFragment.
        SupportMapFragment fragment = ((SupportMapFragment) fm.findFragmentById(R.id.map));
        // Do a null check to confirm that we have not already instantiated the map.
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
        }
        fragment.getMapAsync(this);
        // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
    }

    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
    }

    @Override
    public void onAdd(String schoolName, boolean immunizable) {
        showProgressDialog();

        ParseObject entry = new ParseObject("Entry");
        entry.put(KEY_SCHOOL_NAME, schoolName);
        entry.put(KEY_IMMUNIZABLE, immunizable);
        entry.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                startUpdate();
            }
        });
    }

    private void startUpdate() {
        showProgressDialog();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Entry");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                progressDialog.dismiss();
                updateMap(parseObjects);
            }
        });
    }

    private void updateMap(List<ParseObject> entries) {
        final Map<String, Integer> schoolEvents = getSchoolEvents(entries);
        for(Map.Entry<String, Integer> entry : schoolEvents.entrySet()) {
            final CircleOptions options = new CircleOptions()
                    .center(SCHOOL_COORDS.get(entry.getKey()))
                    .radius((entry.getValue()) * 20) // TODO: Scale logarithmically
                    .fillColor(Color.BLUE);

            mMap.addCircle(options);
        }
    }

    private Map<String, Integer> getSchoolEvents(List<ParseObject> entries) {
        final Map<String, Integer> events = new HashMap<>();

        Collections.sort(entries, new Comparator<ParseObject>() {
            @Override
            public int compare(ParseObject lhs, ParseObject rhs) {
                return lhs.getString(KEY_SCHOOL_NAME).compareTo(rhs.getString(KEY_SCHOOL_NAME));
            }
        });

        int currentSchoolStart = 0;
        String currentSchoolName = entries.get(0).getString(KEY_SCHOOL_NAME);

        final int size = entries.size();
        for (int i = 0; i < size; i++) {
            final ParseObject entry = entries.get(i);
            final String schoolName = entry.getString(KEY_SCHOOL_NAME);
            boolean newSchool = !schoolName.equals(currentSchoolName);
            if (newSchool || (i == size - 1)) {
                events.put(currentSchoolName, i - currentSchoolStart);
                currentSchoolName = schoolName;
                currentSchoolStart = i;
            }
        }

        return events;
    }

    private void showProgressDialog() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = ProgressDialog.show(this, "Loading", "Downloading...");
        }
    }
}
