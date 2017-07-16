package com.matthewkruk.diseasetracker;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, AddDialogFragment.Listener {

    private static final String TAG = MapActivity.class.getName();

    private DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mSchoolReference = mRootReference.child("schools");

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
    private List<School> updatedList = new ArrayList<School>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] areaSchoolNames = getResources().getStringArray(R.array.schools);
        final List<String> schoolNames = Arrays.asList(areaSchoolNames);

            for(String s : schoolNames){
                School school = new School();
                school.setSchoolName(s);
                school.setEvent(0);
                updatedList.add(school);
            }

        //populate database if empty
        mSchoolReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChildren()){

                        mSchoolReference.child("school").setValue(updatedList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Something went wrong!");
            }
        });
        setContentView(R.layout.activity_map);
        mapInit();
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
    public void onAdd(final String schoolName) {
        showProgressDialog();

        final Query schoolQuery = mSchoolReference.orderByChild("school").equalTo(schoolName);
        schoolQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    for(DataSnapshot dSnapshot : dataSnapshot.getChildren()){
                        long oldEvent = (long) dSnapshot.child("schools").child("school").child("event").getValue();
                        mSchoolReference.child("schools").child("school").child("event").setValue(oldEvent + 1);
                }


                updatedList = schoolList;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Couldn't find match in db", databaseError.toException());
            }
        });
                progressDialog.dismiss();
                updateMap(updatedList);
    }

    private void updateMap(List<School> entries) {
        final Map<String, Integer> schoolEvents = getSchoolEvents(entries);
        for(Map.Entry<String, Integer> entry : schoolEvents.entrySet()) {
            final CircleOptions options = new CircleOptions()
                    .center(SCHOOL_COORDS.get(entry.getKey()))
                    .radius((entry.getValue()) * 20) // TODO: Scale logarithmically
                    .fillColor(Color.BLUE);

            mMap.addCircle(options);
        }
    }

    private Map<String, Integer> getSchoolEvents(List<School> entries) {
        final Map<String, Integer> events = new HashMap<>();

        Collections.sort(entries, new Comparator<School>() {
            @Override
            public int compare(School lhs, School rhs) {
                return lhs.schoolName.compareTo(rhs.schoolName);
            }
        });

        int currentSchoolStart = 0;
        String currentSchoolName = entries.get(0).schoolName;

        final int size = entries.size();
        for (int i = 0; i < size; i++) {
            final School entry = entries.get(i);
            final String schoolName = entry.schoolName;
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

    private Observable<List<School>> loadSchools(){
        Firebase firebase = new Firebase("https://authentic-root-92816.firebaseio.com/");
        final GenericTypeIndicator<List<School>> typeIndicator = new GenericTypeIndicator<List<School>>();
            return RxFirebase
                    .observe(firebase.orderByKey())
                    .map(snapshot -> snapshot.getValue(typeIndicator));
    }
}
