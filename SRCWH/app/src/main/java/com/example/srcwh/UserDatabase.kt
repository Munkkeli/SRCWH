package com.example.srcwh

import android.content.Context
import androidx.room.*

@Entity
data class User(
    @PrimaryKey
    val metropoliaID: Long?,
    val firstName: String?,
    val lastName: String?,
    val groupList:String?,
    val hash : Long?,
    val token : Long?) {}


// technically this is enough, we can always just fetch the person and check for whatever it is that we need
// also on update we just take that same person, modify the data, and plug it back in here.
@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAllUserData(): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Update
    fun update(user: User)
}

@Database(entities = [User::class], version = 1)
abstract class UserDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao

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