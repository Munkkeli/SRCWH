package com.example.srcwh

import android.content.Context
import okhttp3.Response
import org.jetbrains.anko.doAsync
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

object DatabaseObj {
    private lateinit var dataBase: UserDatabase
    // user is the userdata clientside, stored in a variable that can be reached easily app-wide
    lateinit var user: ClientUser

    fun initDatabaseConnection(context: Context){
        dataBase = UserDatabase.get(context)
    }

    fun addUserToDatabase(_user: User){
        user = parseToClientUser(_user)
        doAsync {
            dataBase.userDao().insert(_user)
        }

    }


    fun getUserData() : ClientUser?{
        val user = dataBase.userDao().getAllUserData()
        if (user != null){
            return parseToClientUser(user)
        }
        return user
    }

    //later an update function here...?

    fun addScheduleToDatabase(lesson: ScheduleResponse){
        // first convert the lesson into a proper format
        // then insert into database
        dataBase.scheduleDao().insert(parseToDatabaseSchedule(lesson))
    }

    fun getSchedule(): List<ClientSchedule>?{
        // get schedule from database
        // convert it into client schedule
        val temp = dataBase.scheduleDao().getSchedule()
        if(temp.count() > 0){
            val scheduleList: MutableList<ClientSchedule> = mutableListOf()
            for(element in temp){
                scheduleList.add(parseToClientSchedule(element))
            }
            return scheduleList
        }
        else return null

    }

    fun clearData(){
        dataBase.clearAllTables()
    }

    // since class conversion are sort of limited in kotlin, I wrote these ghetto ass function to work
    // as a replacement

    private fun parseToClientUser(user: User): ClientUser{
       return ClientUser(user.metropoliaId, user.firstName, user.lastName, user.groupList.split(",").toSet(), user.currentGroup, user.hash, user.token)
    }

    private fun parseToDatabaseUser(user: ClientUser): User{
        return User(user.metropoliaId!!, user.firstName, user.lastName, user.groupList.joinToString(","), user.currentGroup, user.hash!!, user.token!!)
    }

    private fun parseToClientSchedule(schedule: Schedule): ClientSchedule{
        return ClientSchedule(
            start = LocalDateTime.parse(schedule.start, DateTimeFormatter.ISO_DATE_TIME),
            end = LocalDateTime.parse(schedule.end, DateTimeFormatter.ISO_DATE_TIME),
            locationList = schedule.locationList.split(",").toList(),
            code = schedule.code,
            name = schedule.name,
            groupList = schedule.groupList.split(",").toList(),
            teacherList = schedule.teacherList.split(",").toList(),
            id = schedule.id,
            attended = schedule.attended
        )
    }

    private fun parseToDatabaseSchedule(schedule: ScheduleResponse): Schedule{
        return Schedule(
            start = schedule.start,
            end = schedule.end,
            locationList = schedule.locationList.joinToString(","),
            code = schedule.code,
            name = schedule.name,
            groupList = schedule.groupList.joinToString(","),
            teacherList = schedule.teacherList.joinToString(","),
            id = schedule.id,
            attended = schedule.attended
        )
    }


}