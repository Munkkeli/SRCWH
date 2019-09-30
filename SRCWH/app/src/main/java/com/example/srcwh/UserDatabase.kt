package com.example.srcwh

import android.content.Context
import androidx.room.*
import org.w3c.dom.Text
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
data class User(
    @PrimaryKey
    val metropoliaId: Long,
    val firstName: String?,
    val lastName: String?,
    var groupList: String,
    var currentGroup: String?,
    val hash : String,
    val token : String)


data class ClientUser(
    val metropoliaId: Long?,
    val firstName: String?,
    val lastName: String?,
    var groupList: Set<String>,
    var currentGroup: String?,
    val hash : String?,
    val token : String?
)

@Entity
data class Schedule(
    val start: String,
    val end: String,
    val locationList: String,
    val code: String,
    val name: String,
    val groupList: String,
    val teacherList: String,
    @PrimaryKey
    val id: String,
    val attended: String?
)

@Entity
data class AppSettings(
    @PrimaryKey
    val id: Int,
    val darkMode: Int
)

data class ClientSchedule(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val locationList: List<String>,
    val code: String,
    val name: String,
    val groupList: List<String>,
    val teacherList: List<String>,
    val id: String,
    val attended: String?
)

// technically this is enough, we can always just fetch the person and check for whatever it is that we need
// also on update we just take that same person, modify the data, and plug it back in here.
@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAllUserData(): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Update
    fun update(user: User): Int
}

@Dao
interface ScheduleDao{
    @Query("SELECT * FROM Schedule")
    fun getSchedule(): List<Schedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(lesson: Schedule)

    @Update
    fun update(lesson: Schedule)

    @Query("DELETE FROM Schedule")
    fun clearTable()
}

@Dao
interface SettingsDao{
    @Query("SELECT * FROM AppSettings")
    fun getSettings(): AppSettings

    @Update
    fun update(settings: AppSettings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(settings: AppSettings)
}


@Database(entities = [User::class, Schedule::class, AppSettings::class], version = 1)
abstract class UserDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun settingsDao(): SettingsDao

    companion object{
        private var sInstance: UserDatabase? = null
        @Synchronized
        fun get(context: Context): UserDatabase{
            if(sInstance == null){
                sInstance=
                    Room.databaseBuilder(context.applicationContext, UserDatabase::class.java, "userDB").allowMainThreadQueries().build()
            }
            return sInstance!!
        }

    }
}