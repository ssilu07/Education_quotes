package com.royal.edunotes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.royal.edunotes._activities.MainActivity;
import com.royal.edunotes._database.MyDatabase;
import com.royal.edunotes._models.QuoteModel;

import java.util.ArrayList;

public class VocabWidgetProvider extends AppWidgetProvider {

    private static final String DB_NAME = "life_quotes";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if ("com.royal.edunotes.WIDGET_REFRESH".equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(
                    new android.content.ComponentName(context, VocabWidgetProvider.class));
            onUpdate(context, manager, ids);
        }
    }

    private void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_vocab);

        String quoteText = getRandomQuote(context);
        views.setTextViewText(R.id.widget_quote, quoteText);
        views.setTextViewText(R.id.widget_author, "");

        Intent refreshIntent = new Intent("com.royal.edunotes.WIDGET_REFRESH");
        refreshIntent.setComponent(new android.content.ComponentName(context, VocabWidgetProvider.class));
        PendingIntent refreshPending = PendingIntent.getBroadcast(
                context, widgetId, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPending);

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(
                context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_quote, openPending);

        manager.updateAppWidget(widgetId, views);
    }

    private String getRandomQuote(Context context) {
        try {
            MyDatabase db = new MyDatabase(context, DB_NAME);
            ArrayList<QuoteModel> quotes = db.getPoses();
            if (quotes != null && !quotes.isEmpty()) {
                int index = (int) (Math.random() * quotes.size());
                return quotes.get(index).getQuote();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context.getString(R.string.app_name);
    }
}
