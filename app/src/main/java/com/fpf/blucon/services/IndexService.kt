package com.fpf.blucon.services


import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fpf.blucon.MainActivity
import com.fpf.blucon.R
import com.fpf.blucon.cluster.ClusterManager
import com.fpf.blucon.data.devices.DeviceRepository
import com.fpf.blucon.di.DEVICE_EMBED_STORE
import com.fpf.blucon.errors.AppException
import com.fpf.blucon.index.IndexJobManager
import com.fpf.blucon.notifications.NotificationChannels
import com.fpf.blucon.notifications.showNotification
import com.fpf.blucon.utils.getTimeInMinutesAndSeconds
import com.fpf.smartscansdk.core.embeddings.FileEmbeddingStore
import com.fpf.smartscansdk.core.embeddings.TextEmbeddingProvider
import com.fpf.smartscansdk.core.processors.ProcessorResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class IndexService : Service(), KoinComponent {
    companion object {
        private const val NOTIFICATION_ID = 300
        private const val TAG = "IndexService"
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.Default)
    private val textEmbedder: TextEmbeddingProvider by inject()
    private val clusterManager: ClusterManager by inject()
    private val deviceEmbedStore: FileEmbeddingStore by inject(DEVICE_EMBED_STORE)
    private val deviceRepository: DeviceRepository by inject()
    private val sharedPrefs: SharedPreferences by inject()

    private val indexJobManager by lazy {
        IndexJobManager(
            application = application,
            textEmbedder = textEmbedder,
            deviceEmbedStore=deviceEmbedStore,
            deviceRepository=deviceRepository,
            clusterManager=clusterManager
        )
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
    }

    private fun startForegroundServiceNotification() {
        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationChannels.INDEX_SERVICE)
            .setContentTitle("Indexing devices")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(activityPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            try {
                val processorResult = indexJobManager.run()
                handleIndexResult(processorResult)

            }
            catch (e: Exception) {
                handleServiceError(e)
            }
            finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun handleIndexResult(processorResult: ProcessorResult){
        when(processorResult){
            is ProcessorResult.Success -> {
                val (minutes, seconds) = getTimeInMinutesAndSeconds(processorResult.timeElapsed)
                val indexCompleteTitle = "Index complete"
                val notificationText = "Total devices indexed: ${processorResult.totalProcessed}, Time: ${minutes}m ${seconds}s"
                showNotification(application, indexCompleteTitle, text = notificationText, channelId = NotificationChannels.INDEX_SERVICE, id=NOTIFICATION_ID + 1)

            }
            is ProcessorResult.Failure -> {
                val title = "Indexing error"
                val content = "An error occurred during indexing"
                showNotification(application, title, text=content, channelId = NotificationChannels.INDEX_SERVICE, id=NOTIFICATION_ID + 1)
            }
        }
    }
    private fun handleServiceError(e: Exception){
        Log.e(TAG, "Indexing service error", e)

        when(e) {
            is AppException.ClusterException ->  {
                val title = "Index error"
                val content = "An error occurred while grouping device names"
                showNotification(application, title, text = content, channelId = NotificationChannels.INDEX_SERVICE, id=NOTIFICATION_ID + 1)
            }
            is CancellationException -> {
                val cancelledTitle = "Index cancelled"
                showNotification(applicationContext, title=cancelledTitle, channelId = NotificationChannels.INDEX_SERVICE, id =NOTIFICATION_ID + 1)
            }

            else -> {
                val title = "Indexing error"
                val content = "An error occurred during indexing"
                showNotification(application, title, text=content, channelId = NotificationChannels.INDEX_SERVICE, id=NOTIFICATION_ID + 1)
            }
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}