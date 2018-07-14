package calex.groov.receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Locale;

import calex.groov.R;
import calex.groov.activity.GroovActivity;
import calex.groov.constant.Keys;
import calex.groov.service.RecordDefaultSetService;
import calex.groov.service.UpdateAppWidgetService;

public class GroovAppWidgetProvider extends AppWidgetProvider {

  public static Intent newIntent(Context context, int repCount, int repsToDo) {
    Intent intent = new Intent(context, GroovAppWidgetProvider.class);
    intent.putExtra(Keys.COUNT, repCount);
    intent.putExtra(Keys.REPS, repsToDo);
    return intent;
  }

  public static void sendUpdate(Context context, int repsRecorded, int repsToday) {
    Intent intent = GroovAppWidgetProvider.newIntent(context, repsToday, repsRecorded);
    intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    int[] ids = AppWidgetManager.getInstance(context)
        .getAppWidgetIds(new ComponentName(context, GroovAppWidgetProvider.class));
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
    context.sendBroadcast(intent);
  }

  private int repCount;
  private int repsToDo;

  @Override
  public void onReceive(Context context, Intent intent) {
    repCount = intent.getIntExtra(Keys.COUNT, -1);
    repsToDo = intent.getIntExtra(Keys.REPS, -1);
    super.onReceive(context, intent);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    if (repCount == -1 || repsToDo == -1) {
      context.startService(UpdateAppWidgetService.newIntent(context));
      return;
    }

    for (int appWidgetId : appWidgetIds) {
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
      views.setOnClickPendingIntent(
          R.id.title, PendingIntent.getActivity(context, 0, GroovActivity.newIntent(context), 0));
      views.setTextViewText(
          R.id.count, String.format(Locale.getDefault(), "%d", repCount));
      views.setTextViewText(
          R.id.did_button, String.format(Locale.getDefault(), "+%d", repsToDo));
      views.setOnClickPendingIntent(
          R.id.did_button,
          PendingIntent.getService(context, 0, RecordDefaultSetService.newIntent(context), 0));
      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }
}
