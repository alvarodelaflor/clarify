package es.clarify.clarify.Notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.ExecutionException;

import es.clarify.clarify.R;
import es.clarify.clarify.ShoppingCart.ShoppingCart;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sented = remoteMessage.getData().get("sented");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = firebaseUser.getUid();

        if (firebaseUser != null && sented.equals(uid)) {
            try {
                sendNotification(remoteMessage);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) throws ExecutionException, InterruptedException {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String photo = remoteMessage.getData().get("photo");

        Intent intent = new Intent(this, ShoppingCart.class);
        intent.putExtra("goToShare", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,this.getPackageName())
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_prueba_2)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setColor(this.getColor(R.color.fui_bgFacebook))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setContentInfo("¡Oye!" + body)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body));

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(this.getPackageName());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    this.getPackageName(),
                    "Clarify",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[] { 400, 200, 400, 100 });
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        FutureTarget<Bitmap> futureTarget = Glide.with(this)
                .asBitmap()
                .load(photo)
                .submit();

        Bitmap bitmap = futureTarget.get();
        Bitmap bitmapRounded = getCircleBitmap(bitmap);
        builder.setLargeIcon(bitmapRounded);

        Glide.with(this).clear(futureTarget);

        notificationManager.notify(1,builder.build());
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

//        bitmap.recycle();

        return output;
    }
}
