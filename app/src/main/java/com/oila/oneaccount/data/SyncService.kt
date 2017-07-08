package com.oila.oneaccount.data

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.os.IBinder

import javax.inject.Inject

import timber.log.Timber
import com.oila.oneaccount.OneApplication
import com.oila.oneaccount.util.extension.isNetworkConnected
import com.oila.oneaccount.util.extension.toggleAndroidComponent
import rx.Subscription

class SyncService : Service() {
    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, SyncService::class.java)
        }
    }

    @Inject lateinit var dataManager: DataManager

    var subscription: Subscription? = null

    override fun onCreate() {
        super.onCreate()
        (applicationContext as OneApplication)
                .applicationComponent
                .inject(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.i("Starting sync...")

        if (!isNetworkConnected()) {
            Timber.i("Sync canceled, connection not available")
            toggleAndroidComponent(SyncOnConnectionAvailable::class.java, true)
            stopSelf(startId)
            return Service.START_NOT_STICKY
        }

        subscription?.unsubscribe()
//        subscription = dataManager.syncRibots()
//                .subscribeOn(Schedulers.io())
//                .subscribeBy(
//                    onCompleted = {
//                        Timber.i("Synced successfully!")
//                        stopSelf(startId)
//                    },
//                    onError = {
//                        Timber.w(it, "Error syncing.")
//                        stopSelf(startId)
//                    }
//                )

        return Service.START_STICKY
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    class SyncOnConnectionAvailable : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == CONNECTIVITY_ACTION && context.isNetworkConnected()) {
                Timber.i("Connection is now available, triggering sync...")
                context.toggleAndroidComponent(SyncOnConnectionAvailable::class.java, false)
                context.startService(getStartIntent(context))
            }
        }
    }
}
