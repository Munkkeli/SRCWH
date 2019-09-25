package com.example.srcwh

import android.content.Context
import okhttp3.Response
import org.jetbrains.anko.doAsync

object DatabaseObj {
    private lateinit var dataBase: UserDatabase

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

    fun getUserData() : User{
        return dataBase.userDao().getAllUserData()
    }

    fun createNewUser(res: Response){

    }
}