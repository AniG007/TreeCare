package dal.mitacsgri.treecare.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.consts.FCM_NOTIFICATION_CHANNEL_ID
import dal.mitacsgri.treecare.screens.MainActivity
import org.koin.core.KoinComponent

class MyFirebaseMessagingService: FirebaseMessagingService(), KoinComponent {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("Test", "Doken"+ p0)
    }

    //Called only when the app is in foreground
    override fun onMessageReceived(p0: RemoteMessage) {

        createNotificationChannel()
        Log.d("Received", p0.data.toString())

        val title = p0.notification?.title ?: ""
        val body = p0.notification?.body ?: ""

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, FCM_NOTIFICATION_CHANNEL_ID)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_notification))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                FCM_NOTIFICATION_CHANNEL_ID,
                "Push notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

}