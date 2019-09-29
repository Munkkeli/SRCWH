package com.example.srcwh

import android.content.Context
import okhttp3.Response
import org.jetbrains.anko.doAsync
import java.util.concurrent.ConcurrentLinkedDeque

object DatabaseObj {
    private lateinit var dataBase: UserDatabase
    // user is the userdata clientside, stored in a variable that can be reached easily app-wide
    lateinit var user: ClientUser

    fun initDatabaseConnection(context: Context){
        dataBase = UserDatabase.get(context)
    }

    fun addUserToDatabase(user: User){
        // when adding a user, we need to change the user groups into one string
        // ---> the database cannot hold arrays. we reverse the trick when getting the data out.
        doAsync {
            dataBase.userDao().insert(user)
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
}