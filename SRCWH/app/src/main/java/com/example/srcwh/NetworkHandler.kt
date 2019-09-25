package com.example.srcwh

import com.google.gson.GsonBuilder
import okhttp3.*
import org.jetbrains.anko.doAsync
import java.io.IOException

class NetworkHandler() {

    private val client = OkHttpClient()

    fun postLogin(username: String, password: String) {
        //first build the fortm
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(LOGIN_URL)
            .post(formBody)
            .build()

        try{
            doAsync {
                val response = client.newCall(request).execute()
                println("KIKKEL post begins")
                if(response.isSuccessful){
                    println("KIKKEL response code: " + response.code())
                    response.code()
                    try {
                        println("KIKKEL 1")
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        println("KIKKEL 2")
                        val responseJSON = gson.fromJson(response.body().toString(), loginResponse::class.java)
                        println("KIKKEL " + responseJSON)
                       // DatabaseObj.createNewUser(response)
                    }catch (e: IOException){
                        println("KIKKEL something went wrong in casting " + e.toString())}
                }
            }
        }catch (e: IOException){
            println("KIKKEL something went wrong with the login post")
        }
    }
}

data class loginResponse(val user: loginUser, val token: Long)
data class loginUser (val id: Long, val firstname: String, val lastName: String, val groupList: ArrayList<String>, val hash: Long)