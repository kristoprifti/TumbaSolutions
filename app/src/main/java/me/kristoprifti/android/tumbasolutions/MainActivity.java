package me.kristoprifti.android.tumbasolutions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ALBUM_NAME = "/TumbaSolutions";
    private static final int TAKE_PICTURE = 100;
    private static final int MY_PERMISSIONS_REQUEST = 1;
    private File newPicture;
    private String imageFileName;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private PictureAdapter pictureAdapter;
    private ArrayList<Picture> savedImages;
    private TextView emptyTextView;
    private GPSTracker gpsTracker;
    private Realm realm;
    private static final String WEATHER_API_URL = "http://api.worldweatheronline.com/premium/v1/weather.ashx?key=92090fe2558649c6b6b93054170601";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Obtain a Realm instance
        realm = Realm.getDefaultInstance();

        checkPermissions();

        try {
            createDirectory();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        savedImages = new ArrayList<>();
        getSavedPictures(savedImages);

        emptyTextView = (TextView) findViewById(R.id.empty_view);
        if (savedImages.size() == 0)
            emptyTextView.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.picturesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);

        pictureAdapter = new PictureAdapter(savedImages, this);
        recyclerView.setAdapter(pictureAdapter);
        pictureAdapter.setOnItemClickListener(onItemClickListener);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Here, thisActivity is the current activity
                if (ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MainActivity.this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    gpsTracker = new GPSTracker(MainActivity.this);
                    if(gpsTracker.canGetLocation()){
                        takePhoto();
                    }else{
                        showSettingsAlertForLocation();
                    }
                } else {
                    showSnackbarSettings(view);
                }
                Log.d(TAG, "fab onClick: ends");
            }
        });
        Log.d(TAG, "onCreate: ends");
    }

    PictureAdapter.OnItemClickListener onItemClickListener = new PictureAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            Intent transitionIntent = new Intent(MainActivity.this, PhotoDetailActivity.class);
            transitionIntent.putExtra(PhotoDetailActivity.EXTRA_PARM_ID, savedImages.get(position));
            ImageView placeImage = (ImageView) v.findViewById(R.id.pictureImageView);
            Pair<View, String> imagePair = Pair.create((View) placeImage, "tImage");
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, imagePair);
            ActivityCompat.startActivity(MainActivity.this, transitionIntent, options.toBundle());
        }
    };

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: starts");
        super.onResume();
        Log.d(TAG, "onResume: ends");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    gpsTracker = new GPSTracker(this);
                    getWeatherConditions();
                }
                break;
            default:
        }
    }

    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permissions");
            ActivityCompat.requestPermissions(this, new String[]{CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        }
    }

    private void createDirectory() throws FileNotFoundException {
        Log.d(TAG, "createDirectory: starts");
        File f = new File(Environment.getExternalStorageDirectory(), ALBUM_NAME);
        if (!f.exists() && !f.isDirectory()) {
            f.mkdirs();
        }
    }

    private String createUri(double latitude, double longitude){
        Log.d(TAG, "createUri starts");

        return Uri.parse(WEATHER_API_URL).buildUpon()
                .appendQueryParameter("format", "json")
                .appendQueryParameter("q", latitude + "," + longitude).build().toString();
    }

    private void createPictureObject(String jsonInString) throws JSONException {
        JSONObject weatherObject = new JSONObject(jsonInString);

        String observationTime = weatherObject.getJSONObject("data").getJSONArray("current_condition").getJSONObject(0).getString("observation_time");
        String temperatureCelsius = weatherObject.getJSONObject("data").getJSONArray("current_condition").getJSONObject(0).getString("temp_C");
        final String weatherIconUrl = weatherObject.getJSONObject("data").getJSONArray("current_condition").getJSONObject(0).getJSONArray("weatherIconUrl").getJSONObject(0).getString("value");
        String weatherDescription = weatherObject.getJSONObject("data").getJSONArray("current_condition").getJSONObject(0).getJSONArray("weatherDesc").getJSONObject(0).getString("value");

        final String weatherConditions = "Observation Time: " + observationTime + "\nTemperature in Celsius: " + temperatureCelsius + "\nWeather Description: " + weatherDescription;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //save to realm
                realm.beginTransaction();
                final Picture newPictureObject = realm.createObject(Picture.class); // Create a new object
                newPictureObject.setTitle(imageFileName);
                newPictureObject.setPictureUrl(Uri.fromFile(newPicture).toString());
                newPictureObject.setLatitude(getLatitude());
                newPictureObject.setLongitude(getLongitude());
                newPictureObject.setAddressLocation(getAddressLocation());
                newPictureObject.setWeatherCondition(weatherConditions);
                newPictureObject.setWeatherImageUri(weatherIconUrl);
                realm.commitTransaction();
                savedImages.add(newPictureObject);
                if (emptyTextView.getVisibility() == View.VISIBLE)
                    emptyTextView.setVisibility(View.INVISIBLE);
                pictureAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, newPictureObject.toString(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "run: " + newPictureObject.toString());
            }
        });
    }

    private void getWeatherConditions(){
        String baseUri = createUri(getLatitude(), getLongitude());

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(baseUri)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, "Failed to get the weather data!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String rawData = response.body().string();
                try {
                    createPictureObject(rawData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private double getLatitude(){
        return gpsTracker.getLatitude();
    }

    private double getLongitude(){
        return gpsTracker.getLongitude();
    }

    private String getAddressLocation(){
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            List<Address> addressList = geocoder.getFromLocation(getLatitude(), getLongitude(), 1);

            if(addressList != null) {
                Address returnedAddress = addressList.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("Address:\n");
                for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                //Toast.makeText(getApplicationContext(), strReturnedAddress, Toast.LENGTH_LONG).show();
                return strReturnedAddress.toString();
            }
            else{
                //Toast.makeText(getApplicationContext(), "No Address Returned", Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<Picture> getSavedPictures(ArrayList<Picture> savedPictures) {
        File[] listFile;

        File file = new File(Environment.getExternalStorageDirectory(), ALBUM_NAME);

        if (file.isDirectory()) {
            listFile = file.listFiles();
            for (File picture : listFile) {
                // Build the query looking at all users:
                RealmQuery<Picture> query = realm.where(Picture.class);
                // Add query conditions:
                query.equalTo("title", picture.getName());
                // Execute the query:
                RealmResults<Picture> result1 = query.findAll();

                Picture currentPicture = new Picture();
                currentPicture.setTitle(result1.get(0).getTitle());
                currentPicture.setPictureUrl(result1.get(0).getPictureUrl());
                currentPicture.setWeatherImageUri(result1.get(0).getWeatherImageUri());
                currentPicture.setWeatherCondition(result1.get(0).getWeatherCondition());
                currentPicture.setAddressLocation(result1.get(0).getAddressLocation());
                currentPicture.setLatitude(result1.get(0).getLatitude());
                currentPicture.setLongitude(result1.get(0).getLongitude());
                savedPictures.add(currentPicture);
            }
        }

        return savedPictures;
    }

    private void showSettingsAlertForLocation(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    private void showSnackbarSettings(View view) {
        Snackbar.make(view, "This app can't take pictures unless you...", Snackbar.LENGTH_INDEFINITE)
                .setAction("Grant Access", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Snackbar onClick: starts");
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, CAMERA) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, WRITE_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, ACCESS_FINE_LOCATION) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, ACCESS_COARSE_LOCATION)) {
                            Log.d(TAG, "Snackbar onClick: calling requestPermissions");
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION},
                                    MY_PERMISSIONS_REQUEST);
                        } else {
                            // The user has permanently denied the permission, so take them to the settings
                            Log.d(TAG, "Snackbar onClick: launching settings");
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                            Log.d(TAG, "Snackbar onClick: Intent Uri is " + uri.toString());
                            intent.setData(uri);
                            MainActivity.this.startActivity(intent);
                        }
                        Log.d(TAG, "Snackbar onClick: ends");
                    }
                })
                .show();
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create an image file name
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = JPEG_FILE_PREFIX + timeStamp + JPEG_FILE_SUFFIX;
        newPicture = new File(Environment.getExternalStorageDirectory() + ALBUM_NAME, imageFileName);
        //In android nougat the photo cannot be passed with intent but a content provider instead
        //https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en
        Uri photoURI = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", newPicture);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, TAKE_PICTURE);
    }
}
