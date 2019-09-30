package com.example.srcwh

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import okhttp3.*
import org.jetbrains.anko.uiThread
import org.jetbrains.anko.doAsync
import java.io.IOException

data class LoginResponse(
    @SerializedName("user") val user: LoginUser,
    @SerializedName("token") val token: String
)

data class LoginUser(
    @SerializedName("id") val id: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("groupList") val groupList: ArrayList<String>,
    @SerializedName("hash") val hash: String
)

enum class AttendError {
    GENERIC,
    LESSON,
    LOCATION,
    POSITION,
    UPDATE,
}

data class AttendResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("requiresUpdate") val requiresUpdate: Boolean,
    @SerializedName("lesson") val lesson: ScheduleResponse?,
    @SerializedName("location") val location: String,
    @SerializedName("existing") val existing: String?,
    @SerializedName("valid") val valid: AttendResponseValid
)

data class AttendResponseValid(
    @SerializedName("lesson") val lesson: Boolean,
    @SerializedName("location") val location: Boolean,
    @SerializedName("position") val position: Boolean
)

typealias AttendCallback = (error: AttendError?, location: String?, lesson: ScheduleResponse?) -> Unit

data class ScheduleResponse(
    @SerializedName("start") val start: String,
    @SerializedName("end") val end: String,
    @SerializedName("locationList") val locationList: ArrayList<String>,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("groupList") val groupList: ArrayList<String>,
    @SerializedName("teacherList") val teacherList: ArrayList<String>,
    @SerializedName("id") val id: String,
    @SerializedName("attended") val attended: String?
)

class NetworkHandler {
    private val client = OkHttpClient()

    fun getBearer(token: String): String {
        return "Bearer $token"
    }

    fun postLogin(
        username: String,
        password: String,
        callback: (error: String?, response: LoginResponse?) -> Unit
    ) {
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
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body()?.string()
                        val responseJSON = Gson().fromJson(responseBody, LoginResponse::class.java)
                        Log.d("LOGIN", "Login is successful")
                        Log.d("LOGIN", responseBody)
                        uiThread { callback(null, responseJSON) }
                    } catch (e: IOException) {
                        Log.e("LOGIN", e.toString())
                        uiThread { callback(GENERIC_ERROR, null) }
                    }
                } else {
                    uiThread { callback(LOGIN_ERROR, null) }
                }
            } catch (e: IOException) {
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
                if (response.isSuccessful) {
                    uiThread { callback(null) }
                } else {
                    uiThread { callback(GENERIC_ERROR) }
                }
            } catch (e: IOException) {
                Log.e("GROUP", e.toString())
                callback(GENERIC_ERROR)
            }
        }
    }

    fun postAttend(
        token: String,
        slabId: String,
        coordinates: Coordinates,
        confirmUpdate: Boolean,
        confirmOverride: Boolean,
        callback: AttendCallback
    ) {
        val jsonData = JsonObject()
        jsonData.addProperty("slab", slabId)
        jsonData.addProperty("confirmUpdate", confirmUpdate)
        jsonData.addProperty("confirmOverride", confirmOverride)

        val jsonDataCoordinates = JsonObject()
        jsonDataCoordinates.addProperty("x", coordinates.longitude)
        jsonDataCoordinates.addProperty("y", coordinates.latitude)
        jsonData.add("coordinates", jsonDataCoordinates)

        Log.d("TEST", jsonData.toString())

        val json = MediaType.parse("application/json; charset=utf-8")
        val body = RequestBody.create(json, jsonData.toString())
        val request = Request.Builder()
            .url(ATTEND_URL)
            .addHeader(AUTH_HEADER, getBearer(token))
            .post(body)
            .build()

        doAsync {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    val responseJSON = Gson().fromJson(responseBody, AttendResponse::class.java)
                    val lesson = responseJSON.lesson
                    val location = responseJSON.location

                    Log.d("TEST", responseBody)

                    fun cb(error: AttendError?, location: String?, lesson: ScheduleResponse?) {
                        uiThread {
                            callback(error, location, lesson)
                        }
                    }

                    when {
                        responseJSON.success -> cb(null, location, lesson)
                        responseJSON.requiresUpdate -> cb(AttendError.UPDATE, location, lesson)
                        !responseJSON.valid.lesson -> cb(AttendError.LESSON, location, lesson)
                        !responseJSON.valid.location -> cb(AttendError.LOCATION, location, lesson)
                        !responseJSON.valid.position -> cb(AttendError.POSITION, location, lesson)
                        else -> cb(AttendError.GENERIC, location, lesson)
                    }
                } else {
                    uiThread { callback(AttendError.GENERIC, null, null) }
                }
            } catch (e: IOException) {
                Log.e("ATTEND", e.toString())
                callback(AttendError.GENERIC, null, null)
            }
        }
    }

    fun getSchedule(callback: () -> Unit) {
        try {
            doAsync {
                val request = Request.Builder()
                    .url(SCHEDULE_URL)
                    .header("Authorization", "Bearer ${DatabaseObj.user.token!!}")
                    .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("SCHEDULE", e.toString())
                    }

                    override fun onResponse(call: Call, response: Response) {
                        // cast the response into proper format
                        val responseBody = response.body()?.string()
                        val responseJSON =
                            Gson().fromJson(responseBody, Array<ScheduleResponse>::class.java)
                        for (lesson in responseJSON) {
                            DatabaseObj.addScheduleToDatabase(lesson)
                        }

                        for (e in DatabaseObj.getSchedule()!!) {
                            println(e)
                        }


                        uiThread { callback() }
                    }
                })

            }

        } catch (e: IOException) {
            Log.e("SCHEDULE", e.toString())

        }
    }
}