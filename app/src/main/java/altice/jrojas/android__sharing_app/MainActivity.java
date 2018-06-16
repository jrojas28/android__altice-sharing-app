package altice.jrojas.android__sharing_app;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import altice.jrojas.android__sharing_app.activities.ArticleFeedFragment;
import altice.jrojas.android__sharing_app.activities.NewArticleFragment;
import altice.jrojas.android__sharing_app.activities.SignInFragment;
import altice.jrojas.android__sharing_app.classes.Article;
import altice.jrojas.android__sharing_app.classes.TabAdapter;
import altice.jrojas.android__sharing_app.classes.User;

public class MainActivity extends AppCompatActivity
        implements ArticleFeedFragment.OnFragmentInteractionListener,
        NewArticleFragment.OnFragmentInteractionListener,
        SignInFragment.OnFragmentInteractionListener {
    //Request Code Constants
    final private int FINE_LOCATION_PERMISSION_REQUEST = 1;
    //Log Constants
    final private String TAG = "MAIN_ACTIVITY";
    //Primitive Variables
    private boolean ignoreLocationPermission = false;
    //Firebase Variables
    private FirebaseFirestore database;
    private FirebaseFirestoreSettings databaseSettings;
    //Tabs & Pager
    private TabLayout mainTabs;
    private ViewPager mainViewPager;
    private TabAdapter mainViewPagerAdapter;
    //Activity Variables
    private Location currentUserLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Criteria locationCriteria;
    private Looper locationLooper;
    private User loggedUser;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseFirestore.getInstance();
        databaseSettings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        database.setFirestoreSettings(databaseSettings);
        initializeLocationVariables();
        updateUI();
        if (!ignoreLocationPermission) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, FINE_LOCATION_PERMISSION_REQUEST);
            } else {
                Log.wtf(TAG, "Requesting Location");
                locationManager.requestSingleUpdate(locationCriteria, locationListener, locationLooper);
            }
        }
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            Log.wtf(TAG, "Logged In as " + firebaseUser.getEmail());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment f : fragments) {
                f.onActivityResult(requestCode, resultCode, data);
            }
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST:
                //If this happens, the permission has been granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestSingleUpdate(locationCriteria, locationListener, locationLooper);
                }
                //Otherwise, we have 2 situations.
                else {
                    //The first situation is that the user didn't understand the message and he didn't click
                    //DON'T SHOW AGAIN. In this case, we'll give a better explanation and try once more.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                    }
                    //Otherwise, we can stop relying on the feature, since the user already clicked
                    //never show again.
                    else {
                        ignoreLocationPermission = true;
                    }
                }
                break;
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.wtf(TAG, uri.toString());
        /**
         * This is REQUIRED for Fragments to work.
         * This basically helps with Fragment interaction, allowing the master view
         * (in this case, the main view) to know when certain actions happen within the view.
         * Since our example just shows articles, and we don't need to know if the article has
         * been swiped or clicked, no code is necessary to handle this.
         */
    }

    /**
     * -------------------------------------------------------------------------------------------------------
     *                                          Custom Functions
     * -------------------------------------------------------------------------------------------------------
     */

    /**
     * initializeLocationVariables -- This function is intended to initialize the location related variables.
     */
    public void initializeLocationVariables() {
        //The first step, above all, is checking if the user said NO forever
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Since the user said NOT to check for the permissiona gain, we'll have to stop relying on it.
            ignoreLocationPermission = true;
            Log.wtf(TAG, "Location will be ignored");
        }
        //If we have the permission OR we can still request it, we'll initialize the variables.
        else {
            Log.wtf(TAG, "Listening to Location");
            //First of all, initialize the Location Listener.
            //In the meantime, all it'll do is log the changes.
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    currentUserLocation = location;
                    Log.wtf(TAG, "Location Change - LAT:" + location.getLatitude() + " LONG: " + location.getLongitude());
                    //Here is where we'll finally get the Data for the location.
                    //In the meantime, we'll juse use a Geocoder to determine the city and such
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && addresses.size() > 0) {
                            for (Address address : addresses) {
                                Log.wtf(TAG, address.getLocality());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.wtf(TAG, "Status Change: " + String.valueOf(status));
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.wtf(TAG, "Provider Enabled: " + provider);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.wtf(TAG, "Provider Disabled: " + provider);
                }
            };
            //Second, create the criteria for the Manager.
            //This will allow us to avoid wasting bateries or time
            //By specifying exactly what we want from the location Manager.
            locationCriteria = new Criteria();
            locationCriteria.setAccuracy(Criteria.ACCURACY_FINE);
            locationCriteria.setPowerRequirement(Criteria.POWER_LOW);
            locationCriteria.setAltitudeRequired(false);
            locationCriteria.setBearingRequired(false);
            locationCriteria.setSpeedRequired(false);
            locationCriteria.setCostAllowed(true);
            locationCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
            locationCriteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
            //Third, initialize the location Manager.
            //For this, we'll obtain the Location service from the Context class.
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //Finally, initialize the looper as null.
            //This looper will later be passed on to the locationManager,
            //which will use it to produce results.
            locationLooper = null;
            Log.wtf(TAG, "Location Data Initialized");
        }
    }

    public void initializeFragments(int page) {
        mainViewPagerAdapter = new TabAdapter(getSupportFragmentManager());
        mainViewPager = findViewById(R.id.main_pager);
        mainViewPager.setAdapter(mainViewPagerAdapter);
        mainViewPager.setCurrentItem(page);
        mainTabs = findViewById(R.id.main_tabs);
        mainTabs.setupWithViewPager(mainViewPager);
    }

    public void updateUI() {
        initializeFragments(0);
    }

    public void updateUI(int page) {
        initializeFragments(page);
    }

    public Location getCurrentLocation() {
        if (!ignoreLocationPermission) {
            Log.wtf(TAG, "Not ignoring permission...");
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestSingleUpdate(locationCriteria, locationListener, locationLooper);
                Log.wtf(TAG, "Returning Last Known Location");
                return currentUserLocation;
            }
        }
        return null;
    }

    public void shareArticle(Article article, ImageView articleImageView) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent shareIntent;
        Bitmap bitmap = ((BitmapDrawable) articleImageView.getDrawable()).getBitmap();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/PicToShare.jpeg";
        OutputStream out = null;
        File file=new File(path);
        try {
            File cachePath = new File(getApplicationContext().getCacheDir(), "images");
            cachePath.mkdirs(); // don't forget to make the directory
            FileOutputStream stream = new FileOutputStream(cachePath + "/image.jpeg"); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File imagePath = new File(getApplicationContext().getCacheDir(), "images");
        File newFile = new File(imagePath, "image.jpeg");
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.altice.jrojas.android__sharing_app.fileprovider", newFile);

        if (contentUri != null) {
            shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT,article.getTitle() + "\n\n" + article.getDescription());
            startActivity(Intent.createChooser(shareIntent, "Seleccione una aplicacion para compartir el articulo"));
        }
        else {
            Toast.makeText(getApplicationContext(), "No ha sido posible compartir el articulo", Toast.LENGTH_SHORT).show();
        }
    }
}
