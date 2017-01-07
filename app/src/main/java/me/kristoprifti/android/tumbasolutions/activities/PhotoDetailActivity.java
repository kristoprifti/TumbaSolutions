package me.kristoprifti.android.tumbasolutions.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.kristoprifti.android.tumbasolutions.models.Picture;
import me.kristoprifti.android.tumbasolutions.R;

public class PhotoDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PARM_ID = "photo_details";
    private static final String TAG = "PhotoDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: starts");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Picture photo = intent.getParcelableExtra(EXTRA_PARM_ID);

        if(photo != null){
            ImageView photoImage = (ImageView) findViewById(R.id.pictureImageView);
            ImageView weatherIcon = (ImageView) findViewById(R.id.weatherIcon);
            TextView pictureDetails = (TextView) findViewById(R.id.pictureDetails);

            Picasso.with(this)
                    .load(photo.getPictureUrl())
                    .noPlaceholder()
                    .error(R.mipmap.ic_launcher)
                    .into(photoImage);
            Picasso.with(this)
                    .load(photo.getWeatherImageUri())
                    .noPlaceholder()
                    .error(R.mipmap.ic_launcher)
                    .into(weatherIcon);

            pictureDetails.setText(photo.getTitle());
            pictureDetails.append("\n");
            pictureDetails.append(getString(R.string.latitude) + photo.getLatitude());
            pictureDetails.append("\n");
            pictureDetails.append(getString(R.string.longitude) + photo.getLongitude());
            pictureDetails.append("\n");
            pictureDetails.append(getString(R.string.address) + photo.getAddressLocation());
            pictureDetails.append("\n");
            pictureDetails.append(photo.getWeatherCondition());
        }
        Log.d(TAG, "onCreate: ends");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
