package com.example.srcwh

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import okhttp3.*
import org.jetbrains.anko.uiThread
import org.jetbrains.anko.doAsync
import java.io.IOException
import java.net.SocketTimeoutException

data class LoginResponse(
    @SerializedName("user")val user: LoginUser,
    @SerializedName("token")val token: String)

data class LoginUser (
    @SerializedName("id")val id: String,
    @SerializedName("firstName")val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("groupList")val groupList: ArrayList<String>,
    @SerializedName("hash")val hash: String)

class NetworkHandler {
    private val client = OkHttpClient()

    fun getBearer(token: String): String {
        return "Bearer $token"
    }

    fun postLogin(username: String, password: String, callback: (error: String?, response: LoginResponse?) -> Unit) {
        val jsonData = JsonObject()
        jsonData.addProperty("username", username)
        jsonData.addProperty("password", password)

        val json = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(json, jsonData.toString())
        val request = Request.Builder()
            .url(LOGIN_URL)
            .post(body)
            .build()

        doAsync {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful){
                    try {
                        val responseBody = response.body()?.string()
                        val responseJSON = Gson().fromJson(responseBody, LoginResponse::class.java)
                        Log.d("LOGIN", "Login is successful")
                        Log.d("LOGIN", responseBody)
                        uiThread { callback(null, responseJSON) }
                    } catch (e: IOException){
                        Log.e("LOGIN", e.toString())
                        uiThread { callback(GENERIC_ERROR, null) }
                    }
                } else {
                    uiThread { callback(LOGIN_ERROR, null) }
                }
            } catch (e: IOException){
                Log.e("LOGIN", e.toString())
                uiThread { callback(GENERIC_ERROR, null) }
            }
        }
    }

    fun postGroupUpdate(token: String, group: String, callback: (error: String?) -> Unit) {
        val jsonData = JsonObject()
        jsonData.addProperty("group", group)

        val json = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(json, jsonData.toString())
        val request = Request.Builder()
            .url(UPDATE_URL)
            .addHeader(AUTH_HEADER, getBearer(token))
            .post(body)
            .build()

        doAsync {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful){
                    uiThread { callback(null) }
                } else {
                    uiThread { callback(GENERIC_ERROR) }
                }
            } catch (e: IOException){
                Log.e("GROUP", e.toString())
                callback(GENERIC_ERROR)
            }
        }
    }
}