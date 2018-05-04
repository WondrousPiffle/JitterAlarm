package eli.me.jitteralarm;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

/**
 * Created by Eli on 1/7/2016.
 */
public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //NEED TO GRAB DETAILS FROM SHAREDPREFERENCES AND INTERPRET THE DATA TO SET A NEW ALARM TO TRIGGER AT THE CORRECT TIME.
        //REMEMBER TO CHECK IF THE ALARM IS SUPPOSE TO RUN ONLY ONCE OR IF IT NEEDS TO RUN MULTIPLE TIMES

        //NOTE THAT IT WORKS, JUST NEED TO DO THE RIGHT THINGS WITH THE DATA
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getBoolean("alarmRunning", false)) {
            String name = sp.getString("currentName", "");
            String description = sp.getString("currentDescription", "");
            long exactTriggerTime = sp.getLong("exactTriggerTime", 0);
            long offsetTriggerTime = sp.getLong("offsetTriggerTime", 0);
            boolean runOnce = sp.getBoolean("runOnce", true);
            long interval = sp.getLong("interval", 0);
            long currentRandomOffset = sp.getLong("currentRandomOffset", 0);
            if(runOnce){
                //Check if the offsetAlarm has expired. If so, send a notification and do nothing else
                if(offsetTriggerTime <= System.currentTimeMillis()){
                    long[] vibrate = new long[2];
                    vibrate[0] = 500;
                    vibrate[1] = 1000;
                    SoundPool pool = getSoundPool();
                    Random r = new Random();
                    int randomNumber = 0;
                    while(randomNumber == 0){
                        randomNumber = r.nextInt(122);
                    }
                    int rawResourceID = context.getResources().getIdentifier("a" + Integer.toString(randomNumber), "raw", context.getPackageName());
                    pool.load(context, rawResourceID, 0);
                    Uri randomSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + rawResourceID);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                            .setContentText(description)
                            .setSmallIcon(R.drawable.notificationicon)
                            .setLights(Color.GREEN, 500, 2000)
                            .setAutoCancel(true)
                            .setVibrate(vibrate)
                            .setSound(randomSound)
                            .setContentTitle(name);
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(1, builder.build());
                }
            } else {
                if(System.currentTimeMillis() >= offsetTriggerTime){
                    //Send notification and do nothing else.
                    playNotification(context, name, description);
                }
                if(System.currentTimeMillis() >= exactTriggerTime){
                    //send an intent to call the broadcast receiver in the MainActivity class so that the alarm will reset automatically.
                    //If needed, set a new alarm here that will run off that one when it triggers (put it back of track).

                    //Create a new alarm with the remaining time left, if multiple triggers have passed then do the math and start at the appropriate time.
                    //Need to open the MainActivity with an intent with the boolean extra FROM_BOOTUP when th alarm is done.

                    long exactTimeAtNextTrigger = exactTriggerTime;
                    while(exactTimeAtNextTrigger < System.currentTimeMillis()){
                        exactTimeAtNextTrigger += interval;
                    }
                    long offsetTimeAtNextTrigger = exactTimeAtNextTrigger + currentRandomOffset;

                    Intent i = new Intent(context, StartMainReceiver.class);

                    //Creates offset alarm structure
                    Intent i2 = new Intent(context, AlarmReciever.class);
                    intent.putExtra("name", name);
                    intent.putExtra("description", description);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 234324243, i2, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Creates new recursion alarm structure
                    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 000, i, PendingIntent.FLAG_UPDATE_CURRENT);

                    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                        manager.set(AlarmManager.RTC_WAKEUP, exactTimeAtNextTrigger, pendingIntent2);
                        manager.set(AlarmManager.RTC_WAKEUP, offsetTimeAtNextTrigger, pendingIntent);
                    } else {
                        manager.setExact(AlarmManager.RTC_WAKEUP, exactTimeAtNextTrigger, pendingIntent2);
                        manager.setExact(AlarmManager.RTC_WAKEUP, offsetTimeAtNextTrigger, pendingIntent);
                    }
                }
            }
        }
    }

    private void playNotification(Context context, String name, String description){
        long[] vibrate = new long[2];
        vibrate[0] = 500;
        vibrate[1] = 1000;
        SoundPool pool = getSoundPool();
        Random r = new Random();
        int randomNumber = 0;
        while(randomNumber == 0){
            randomNumber = r.nextInt(122);
        }
        int rawResourceID = context.getResources().getIdentifier("a" + Integer.toString(randomNumber), "raw", context.getPackageName());
        pool.load(context, rawResourceID, 0);
        Uri randomSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + rawResourceID);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentText(description)
                .setSmallIcon(R.drawable.notificationicon)
                .setLights(Color.GREEN, 500, 2000)
                .setAutoCancel(true)
                .setVibrate(vibrate)
                .setSound(randomSound)
                .setContentTitle(name);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    private SoundPool getSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .build();
            SoundPool sounds = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();
            return sounds;
        } else {
            SoundPool sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            return sounds;
        }
    }
}
