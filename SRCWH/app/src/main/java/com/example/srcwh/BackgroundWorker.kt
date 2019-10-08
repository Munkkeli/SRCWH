package com.example.srcwh

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.Duration

class BackgroundWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters){

    override fun doWork(): Result {
        // we need to get the schedule, then check if attending class
        val networkHandler = NetworkHandler()
        networkHandler.getSchedule { startNotificationSendProcess() }
        return Result.success()
    }


    private fun startNotificationSendProcess(){
        // get the schedule from database object
        val schedule = DatabaseObj.getSchedule()
        Log.d("WORKER", "data fetched :-)")

        // if there is a schedule
        if(schedule != null){

            for(lesson in schedule){

                // check if the lesson is ongoing
                when(determineLessonStatus(lesson)){
                    LessonState.ONGOING -> {

                        // check if it has already been notified and the time is right
                        val notifState = DatabaseObj.checkNotification(lesson.id)

                        when(notifState){
                            null ->{
                                // the notification state is null, which means that there is no database
                                // entry for the current lesson --> means that notification has not been sent
                                if(checkLessonTime(lesson)) createNotification(lesson)
                            }
                            true -> {
                                // the notification has already been sent
                                return
                            }
                        }
                    }
                    else -> return
                }

            }
        }




        // check with lesson id if the notification has already been sent
     //   val sent = DatabaseObj.checkNotification()
    }

    private fun determineLessonStatus(lesson: ClientSchedule): LessonState{
        val time = Controller.time
        return when {
            lesson.attended != null -> LessonState.ATTENDED
            lesson.start.isBefore(time) && lesson.end.isAfter(time) -> LessonState.ONGOING
            lesson.end.isBefore(time) -> LessonState.MISSED
            else -> LessonState.UPCOMING
        }
    }

    private fun checkLessonTime(lesson: ClientSchedule): Boolean{
        val time = Controller.time
        val d = Duration.between(lesson.start, time).toMinutes()

        return (d >= 30 && time.isBefore(lesson.end))

    }

    private fun createNotification(lesson: ClientSchedule){
        Log.d("WORKER", "creating a notification!")

        val testID = 6969

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent : PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val notificationText = applicationContext.getString(R.string.notification_text, lesson.name, lesson.address )
        val notificationTitle = applicationContext.getString(R.string.notification_title)
        val builder = NotificationCompat.Builder(applicationContext, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(notificationTitle)
            .setContentText(lesson.name)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)){
            notify(testID, builder.build())
        }

        DatabaseObj.insertNotification(Notification(lesson.id, true))
    }
}