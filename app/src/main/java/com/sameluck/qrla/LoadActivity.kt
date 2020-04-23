package com.sameluck.qrla

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import bolts.AppLinks
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.facebook.FacebookSdk
import com.facebook.applinks.AppLinkData
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_load.*

class LoadActivity : AppCompatActivity() {

    private var userCountry: String = "na"
    private var isStart = false
    private var fb: String? = ""
    private val sp = App.instance.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        FacebookSdk.sdkInitialize(this)

        val ed = sp.edit()
        myRef = FirebaseDatabase.getInstance().getReference()

        startProgressBar()

        var getUserCountry: () -> Unit = {
            val telephonyManager =
                App.instance.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            Log.d(get(), "Country: " + telephonyManager.simCountryIso.toLowerCase())
            userCountry = telephonyManager.simCountryIso.toLowerCase()
        }

        getUserCountry()

        setSubscribeToTopic()

        var msg = sp.getString("fb", "")
        Log.d(get(), "is Deep: ${sp.getBoolean("deepLinkRecieved", false)}")

        if (msg.isNullOrEmpty()) getDPFromFB(ed)

        if (!sp.getBoolean("deepLinkRecieved", false)){
            myRef.child("state").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val value = dataSnapshot.getValue(Boolean::class.java)
                    Log.d("LoadActivity", "Value state is: $value")
                    if (value != null) {
                        with(ed) {
                            putBoolean("state", value)
                            apply()
                        }
                    }
                    if (!isStart && value!!) {
                        myRef.child("AcceptCountry").addValueEventListener(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                val value = dataSnapshot.getValue(String::class.java)
                                Log.d("LoadActivity", "Value AcceptCountry is: $value")
                                if (value != null) {
                                    with(ed) {
                                        putString("country", value)
                                        apply()
                                    }
                                }
                                Log.d("LoadActivity", "Value userCounry is: " + userCountry)
                                if (!isStart) {
                                    nextActivity(value!!.contains(userCountry, true))
                                    isStart = true
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                Log.w("LoadActivity", "Failed to read value.", error.toException())
                            }
                        })
                    } else if (!isStart) {
                        nextActivity(isStart)
                        isStart = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("LoadActivity", "Failed to read value.", error.toException())
                }
            })
        }  else {
            nextActivity(true)
            isStart = true
        }
    }

    private fun getDPFromFB(ed: SharedPreferences.Editor) {
        AppLinkData.fetchDeferredAppLinkData(
            this,
            object : AppLinkData.CompletionHandler {
                override fun onDeferredAppLinkDataFetched(appLinkData: AppLinkData?) {
                    val uri = if (appLinkData != null) {
                        appLinkData.targetUri
                    } else {
                        AppLinks.getTargetUrlFromInboundIntent(applicationContext, intent)
                    }
                    var value = ""
                    if (uri != null) {
                        var count = 1
                        value = uri.pathSegments.joinToString("&") {
                            "subid${count++}=$it"
                        }
                    }
                    fb = value
                    Log.d("LoadActivity", "fb: " + fb)
                    if (fb != "") {
                        with(ed) {
                            putString("fb", fb)
                            putBoolean("deepLinkRecieved", true)
                            apply()
                        }
                    } else {
                        val referrerClient =
                            InstallReferrerClient.newBuilder(this@LoadActivity).build()
                        referrerClient.startConnection(object : InstallReferrerStateListener {
                            override fun onInstallReferrerServiceDisconnected() {
                            }

                            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                                    try {
                                        var count = 1
                                        val refs = referrerClient.installReferrer.installReferrer
                                            .split("&").joinToString("&") {
                                                "subid${count++}=${it.split("=")[1]}"
                                            }

                                        fb = refs
                                        Log.d("LoadActivity", "fb: " + fb)
                                        if (fb != "") {
                                            with(ed) {
                                                putString("fb", fb)
                                                apply()
                                            }
                                        }

                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        })
                    }
                }
            }
        )
    }

    private fun nextActivity(isWViewNext: Boolean) {
        var nameActivity = if (isWViewNext) LastActivity::class.java else StartActivity::class.java
        startActivity(
            Intent(
                this@LoadActivity,
                nameActivity
            )
        )
    }


    fun setSubscribeToTopic() {
        FirebaseMessaging.getInstance().apply {
            subscribeToTopic("pharaon")
                .addOnSuccessListener { println("!!!") }
            subscribeToTopic("country_" + userCountry)
                .addOnSuccessListener { println("!!!") }
        }
    }

    private fun startProgressBar() {
        Thread(Runnable {
            var progress = 0
            while (progress < 100) {

                Thread.sleep(500)

                progress.also {
                    progress_bar_h.setProgress(it)
                    progress = it + 5
                }
            }
            if (!isStart) {
                nextActivity(false)
                isStart = true
            }
        }).start()
    }

    private inline fun <reified T> T.get() = T::class.java.simpleName
}