package com.example.ch10_notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ch10_notification.databinding.ActivityMainBinding
import android.Manifest


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val PERMISSION_REQUEST_CODE = 101 // 임의의 정수 값, 고유해야 함

        // Android 13 이상에서 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 현재 POST_NOTIFICATIONS 권한이 부여되었는지 확인
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                // 권한이 없으면 사용자에게 권한을 요청
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE)
            }
        }

        // ViewBinding을 사용하여 레이아웃에 접근
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 'notificationButton' 클릭 시 알림 생성 및 표시
        binding.notificationButton.setOnClickListener {

            // NotificationManager 객체를 가져옴
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // 알림 빌더 생성
            val builder = createNotificationBuilder(notificationManager)

            // 알림 설정
            setNotification(builder)

            // 답장 작업에 사용할 PendingIntent 생성
            val replyPendingIntent = createReplyPendingIntent()

            // RemoteInput 객체 생성 (사용자가 답장을 입력할 수 있는 필드)
            val remoteInput = createRemoteInput()

            // 답장 작업을 포함한 알림 액션 생성
            val action = createNotificationAction(replyPendingIntent, remoteInput)

            // 알림에 액션 추가
            builder.addAction(action)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }

    /**
     * 알림 빌더를 생성하는 메서드.
     * NotificationChannel을 생성하고, API 26 이상에서는 채널을 통해 알림을 보냄.
     */
    private fun createNotificationBuilder(notificationManager: NotificationManager): NotificationCompat.Builder {
        val builder: NotificationCompat.Builder

        // API Level 26 이상인 경우 채널 사용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT // 채널 중요도를 설정
            ).apply {
                description = getString(R.string.channel_desc)
                setShowBadge(true)
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(uri, audioAttributes)
                enableVibration(true)
            }
            // 알림 채널을 NotificationManager에 등록
            notificationManager.createNotificationChannel(channel)

            // 알림 빌더에 채널 ID 추가
            builder = NotificationCompat.Builder(this, CHANNEL_ID)
        } else {
            builder = NotificationCompat.Builder(this)
        }

        return builder
    }

    /**
     * 알림 빌더에 기본 알림 설정을 적용하는 메서드.
     */
    private fun setNotification(builder: NotificationCompat.Builder) {
        builder.run {
            setSmallIcon(R.drawable.small)
            setWhen(System.currentTimeMillis())
            setContentTitle(getString(R.string.noti_content_title))
            setContentText(getString(R.string.noti_content_text))
            setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.big))
        }
    }

    /**
     * 사용자가 알림에서 답장을 보낼 때 사용되는 PendingIntent를 생성하는 메서드.
     */
    private fun createReplyPendingIntent(): PendingIntent {
        // 알림에 답장을 보낼 때 실행될 브로드캐스트 리시버를 정의
        val replyIntent = Intent(this, ReplyReceiver::class.java)
        return PendingIntent.getBroadcast(
            this,
            30,
            replyIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * RemoteInput 객체를 생성하여 사용자가 답장을 입력할 수 있는 텍스트 필드를 제공하는 메서드.
     */
    private fun createRemoteInput(): RemoteInput {
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(getString(R.string.noti_reply_label))
            build()
        }
        return remoteInput
    }

    /**
     * 알림에 액션(답장 기능)을 추가하는 메서드.
     * 사용자가 입력한 내용을 RemoteInput을 통해 전달받을 수 있음.
     */
    private fun createNotificationAction(
        replyPendingIntent: PendingIntent,
        remoteInput: RemoteInput
    ): NotificationCompat.Action {
        return NotificationCompat.Action.Builder(
            R.drawable.send,
            getString(R.string.noti_action_title),
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()
    }
}