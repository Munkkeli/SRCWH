package com.example.srcwh

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import okhttp3.*
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.io.IOException

class NetworkHandler() {

    private val client = OkHttpClient()

    fun postLogin(username: String, password: String) {

        val jsonData = JsonObject()
        jsonData.addProperty("username", username)
        jsonData.addProperty("password", password)
        val JSON = MediaType.parse("application/json; charset=utf-8")
        val b = RequestBody.create(JSON, jsonData.toString())
        val request = Request.Builder()
            .url(LOGIN_URL)
            .post(b)
            .build()

        try{
            doAsync {
                val response = client.newCall(request).execute()
                println("KIKKEL post begins")
                println("KIKKEL " + response.body()?.string())
                if(response.isSuccessful){
                    println("KIKKEL response code: " + response.code())
                    try {
                        //val gson = GsonBuilder().setPrettyPrinting().create()
                        // the problem currently is, that the gsonbuilder isn't casting the response into loginResponse succesfully.
                        // the token isn't getting printed
                        val responseJSON = Gson().fromJson(response.body()?.string(), loginResponse::class.java)
                        println("KIKKEL " + responseJSON.token)
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

data class loginResponse(
    @SerializedName("user")val user: loginUser,
    @SerializedName("token")val token: String)
data class loginUser (
    @SerializedName("id")val id: String,
    @SerializedName("firstname")val firstname: String,
    @SerializedName("lastname") val lastName: String,
    @SerializedName("groupList")val groupList: ArrayList<String>,
    @SerializedName("hash")val hash: String)