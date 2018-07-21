package calex.groov.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import androidx.work.Worker;
import calex.groov.R;
import calex.groov.activity.GroovActivity;

public class ReminderWorker extends Worker {
  private static final int NOTIFICATION_ID = 0;
  private static final String REMINDERS_CHANNEL_ID = "reminders";

  @NonNull
  @Override
  public Result doWork() {
    Context context = getApplicationContext();
    NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager.getNotificationChannel(REMINDERS_CHANNEL_ID) == null) {
      NotificationChannel channel = new NotificationChannel(
          REMINDERS_CHANNEL_ID,
          context.getString(R.string.notification_channel_name),
          NotificationManager.IMPORTANCE_DEFAULT);
      notificationManager.createNotificationChannel(channel);
    }

    notificationManager.notify(
        NOTIFICATION_ID,
        new NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setLargeIcon(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.ic_launcher_foreground))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText(context.getString(R.string.reminder_notification_text))
            .setContentIntent(PendingIntent.getActivity(
                context, 0, GroovActivity.newIntent(context), 0))
            .setAutoCancel(true)
            .build());
    return Result.SUCCESS;
  }
}
