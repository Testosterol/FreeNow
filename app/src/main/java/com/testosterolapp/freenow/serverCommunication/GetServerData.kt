package com.testosterolapp.freenow.serverCommunication
import android.content.Context
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.testosterolapp.freenow.util.DbUtil.Companion.launchCoroutineForInsertDataIntoDatabase
import org.json.JSONException
import org.json.JSONObject

class GetServerData {

    /**
     * Method that requests data from server using Volley library and then launches the coroutine to
     * persist the data in room database
     */
    fun getServerData(context: Context?) {
        val strReq: StringRequest = object : StringRequest(Method.GET, Url.URL_DATA, Response.Listener { response: String? ->
                // server response
                var jsonObj: JSONObject? = null
                try {
                    jsonObj = JSONObject(response!!)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                launchCoroutineForInsertDataIntoDatabase(jsonObj!!, context!!)
            },
            Response.ErrorListener { error: VolleyError ->
                Log.e(TAG, "Failed to retrieve server data$error")
            }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                Log.i(TAG, "Status code from the server: " + response.statusCode)
                return super.parseNetworkResponse(response)
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
        }
        AppController.getInstance(context!!)!!.addToRequestQueue(strReq)
    }

    companion object {
        val TAG: String = GetServerData::class.java.simpleName
    }
}