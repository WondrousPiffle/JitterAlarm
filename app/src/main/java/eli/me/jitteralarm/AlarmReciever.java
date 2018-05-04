package eli.me.jitteralarm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

/**
 * Created by Eli on 12/29/2015.
 */
public class AlarmReciever extends BroadcastReceiver {

    private String name;
    private String description;

    @Override
    public void onReceive(Context context, Intent intent) {
        name = "" + intent.getStringExtra("name");
        description = "" + intent.getStringExtra("description");
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
