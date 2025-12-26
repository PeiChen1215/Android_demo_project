package com.example.android_development.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.app.PendingIntent;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.NotificationCompat;
import com.example.android_development.security.Auth;
import com.example.android_development.util.Constants;
import android.util.Log;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.example.android_development.model.Product;
import java.util.List;

/**
 * 后台库存监控前台服务。
 *
 * <p>服务启动后会进入前台（startForeground），并通过定时任务周期性检查低库存商品：
 * 当存在低库存时，向具备权限的用户发送系统通知提醒。</p>
 *
 * <p>注意：Android 13+ 需要 {@code POST_NOTIFICATIONS} 通知权限；
 * 本服务也会通过 {@link Auth#hasPermission(Context, String)} 判断当前登录角色是否应接收提醒。</p>
 */
public class InventoryMonitorService extends Service {

    private ScheduledExecutorService scheduler;
    private com.example.android_development.services.InventoryService inventoryService;

    private static final String CHANNEL_ID = "inventory_monitor_channel_v2";
    private static final int NOTIF_ID = 1001;

    @Override
    /**
     * Service 创建：初始化业务服务对象与调度器，并创建通知通道。
     */
    public void onCreate() {
        super.onCreate();
        inventoryService = new com.example.android_development.services.InventoryService(this);
        // 仅创建通知通道，实际 startForeground 移至 onStartCommand
        createNotificationChannel();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * 创建通知通道（Android O+）。
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "库存监控", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    @Override
    /**
     * Service 启动：
     * <p>1）构建前台常驻通知并进入前台；2）在进入前台后启动定时任务进行低库存检查与通知推送。</p>
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            // 点击通知打开主界面
            Intent open = new Intent(this, com.example.android_development.activities.MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("库存监控运行中")
                    .setContentText("后台监控库存预警")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setOngoing(true)
                    .setContentIntent(pi)
                    .build();

            startForeground(NOTIF_ID, notif);
            // (测试通知已移除)
        } catch (Exception e) {
            e.printStackTrace();
            // 无法进入前台则停止服务，避免反复崩溃
            stopSelf();
            return START_NOT_STICKY;
        }

        // 启动定时任务（在服务成功进入前台后启动）
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                List<Product> low = inventoryService.getLowStockAlerts();
                if (low != null && !low.isEmpty()) {
                    String content = "共有 " + low.size() + " 件商品库存不足";
                    Intent open = new Intent(this, com.example.android_development.activities.MainActivity.class);
                    PendingIntent pi = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    boolean canNotify = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        canNotify = (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED);
                    }
                    boolean isRecipient = Auth.hasPermission(this, Constants.PERM_RUN_INVENTORY);
                    Log.d("InventoryMonitorService", "Low-stock notify: canNotify=" + canNotify + ", isRecipient=" + isRecipient + ", count=" + low.size());
                    if (canNotify && isRecipient) {
                        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                                .setContentTitle("低库存预警")
                                .setContentText(content)
                                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(pi)
                                .build();
                        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        if (nm != null) nm.notify((int) System.currentTimeMillis(), n);
                    } else {
                        Log.w("InventoryMonitorService", "POST_NOTIFICATIONS not granted — skipping low-stock notification");
                    }
                }
            } catch (Exception e) {
                Log.e("InventoryMonitorService", "Error during scheduled check", e);
                e.printStackTrace();
            }
        }, 0, 15, TimeUnit.MINUTES);

        return START_STICKY;
    }

    @Override
    /**
     * Service 销毁：停止定时任务并退出前台。
     */
    public void onDestroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    /**
     * 本服务不提供绑定能力。
     */
    public IBinder onBind(Intent intent) {
        return null;
    }
}
