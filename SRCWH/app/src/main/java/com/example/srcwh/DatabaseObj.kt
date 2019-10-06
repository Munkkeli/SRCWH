package com.example.srcwh

import android.content.Context
import android.util.Log
import org.jetbrains.anko.doAsync
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId.systemDefault
import java.time.ZoneOffset
import java.time.ZonedDateTime

object DatabaseObj {
    private var dataBase: UserDatabase? = null
    // user is the userdata clientside, stored in a variable that can be reached easily app-wide
    lateinit var user: ClientUser

    val isConnected
        get() = dataBase != null

    fun initDatabaseConnection(context: Context){
        dataBase = UserDatabase.get(context)
    }

    fun addUserToDatabase(_user: User){
        user = parseToClientUser(_user)
        doAsync {
            dataBase!!.userDao().insert(_user)
        }
    }

    fun addSettingsToDatabase(settings: AppSettings){
        dataBase!!.settingsDao().insert(settings)
    }

    fun getUserData() : ClientUser?{
        val user = dataBase!!.userDao().getAllUserData()
        if (user != null){
            return parseToClientUser(user)
        }
        return user
    }

    fun getSettingsData(): AppSettings?{
        return dataBase!!.settingsDao().getSettings()
    }

    fun updateUserdata(): Int{
        return dataBase!!.userDao().update(parseToDatabaseUser(user))
    }

    fun updateSettingsData(state: Int){
        Log.d("APP_SETTINGS", "updating darkmode settings witht the value of $state")
        dataBase!!.settingsDao().update(AppSettings(1, state))
    }

    fun addScheduleToDatabase(lesson: ScheduleResponse){
        // first convert the lesson into a proper format
        // then insert into database
        dataBase!!.scheduleDao().insert(parseToDatabaseSchedule(lesson))
    }

    fun getSchedule(): List<ClientSchedule>?{
        // get schedule from database
        // convert it into client schedule
        val temp = dataBase!!.scheduleDao().getSchedule()
        if (temp.count() > 0){
            val scheduleList: MutableList<ClientSchedule> = mutableListOf()
            for(element in temp){
                scheduleList.add(parseToClientSchedule(element))
                //scheduleList.add(parseToClientSchedule(element))
            }
            return scheduleList
        }
        else return null
    }

    fun clearSchedule(){
        dataBase!!.scheduleDao().clearTable()
    }

    fun clearAllData(){
        dataBase!!.clearAllTables()

    }

    // since class conversion are sort of limited in kotlin, I wrote these ghetto ass function to work
    // as a replacement

    private fun parseToClientUser(user: User): ClientUser{
       return ClientUser(user.metropoliaId, user.firstName, user.lastName, user.groupList.split(",").toSet(), user.currentGroup, user.hash, user.token)
    }

    private fun parseToDatabaseUser(user: ClientUser): User{
        return User(user.metropoliaId!!, user.firstName, user.lastName, user.groupList.joinToString(","), user.currentGroup, user.hash!!, user.token!!)
    }

    private fun convertFromUTCToLocal(dateString: String): ZonedDateTime {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
            .atOffset(ZoneOffset.UTC)
            .atZoneSameInstant(systemDefault())
    }

    private fun parseToClientSchedule(schedule: Schedule): ClientSchedule {
        return ClientSchedule(
            start = convertFromUTCToLocal(schedule.start),
            end = convertFromUTCToLocal(schedule.end),
            locationList = schedule.locationList.split(",").toList(),
            address = schedule.address,
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
            address = schedule.address,
            code = schedule.code,
            name = schedule.name,
            groupList = schedule.groupList.joinToString(","),
            teacherList = schedule.teacherList.joinToString(","),
            id = schedule.id,
            attended = schedule.attended
        )
    }


}