package com.bignerdranch.android.photogallery;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by cody on 6/7/2018.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class jobservice extends JobService {

    private static final String TAG = "PollService";
    PollTask mcurrenttask;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mcurrenttask = new PollTask();
        mcurrenttask.execute(jobParameters);
        return true;     }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mcurrenttask != null) {
            mcurrenttask.cancel(true);
        }
        return true;

    }

    private class PollTask extends AsyncTask<JobParameters,Void ,List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(JobParameters... params) {
            JobParameters jobParams = params[0];

Log.i(TAG,"polling");
            String query = QueryPreferences.getStoredQuery(jobservice.this);

            List<GalleryItem> items;
           //int page = 1;
            if (query == null) {
                items = new FlickrFetchr().fetchRecentPhotos();
            } else {
                items = new FlickrFetchr().searchPhotos(query);
            }

            jobFinished(jobParams, false);
            return items;
        }
        @Override
        protected void onPostExecute(List<GalleryItem>items){
            if (items.size() == 0) {
                return;
            }

            String lastResultId = QueryPreferences.getLastResultId(jobservice.this);

            String resultId = items.get(0).getId();
            if (resultId.equals(lastResultId)) {
                Log.i(TAG, "Got an old result: " + resultId);
            } else {
                Log.i(TAG, "Got a new result: " + resultId);
               // Resources resources = getResources();
                Intent i = PhotoGalleryActivity.newIntent(jobservice.this);
                PendingIntent pi = PendingIntent.getActivity(jobservice.this, 0, i, 0);



                Notification notification = new NotificationCompat.Builder(jobservice.this)
                        .setTicker(getString(R.string.new_pictures_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(getString(R.string.new_pictures_title))
                        .setContentText(getString(R.string.new_pictures_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(jobservice.this);
                notificationManager.notify(0, notification);
            }
            QueryPreferences.setLastResultId(jobservice.this, resultId);

        }

    }
}
