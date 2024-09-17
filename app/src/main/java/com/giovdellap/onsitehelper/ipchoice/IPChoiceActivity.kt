package com.giovdellap.onsitehelper.ipchoice

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.ActivityIpchoiceBinding
import com.giovdellap.onsitehelper.signin.SignInActivity
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.launch
import android.widget.Toast
import io.ktor.client.statement.HttpResponse
import java.net.NoRouteToHostException

class IPChoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIpchoiceBinding
    val TAG: String = "IPCHOICEACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIpchoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val context = this.applicationContext

        val sharedPreferences = this.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        var ip = sharedPreferences.getString("ip", "")
        if (ip != null && ip != "") {
            lifecycleScope.launch {
                try {
                    val httpclient = HttpClient(CIO) {
                        install(HttpTimeout) {
                            requestTimeoutMillis = 20000
                        }
                        install(ContentNegotiation) {
                            gson()
                        }

                    }
                    var address = "http://" + ip + ":5001/macc"
                    val url = address + "/ping/"
                    Log.d(TAG, url)
                    val response = httpclient.get(url)
                    if (response.status.value == 200) {
                        val intent = Intent(context, SignInActivity::class.java)
                        startActivity(intent)
                    } else {
                        ip = ""
                        Toast.makeText(context, "Server problem. Please try again later", Toast.LENGTH_LONG).show()
                    }
                } catch (e1: NoRouteToHostException) {
                    ip = ""
                    Toast.makeText(context, "No route to host", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    ip = ""
                    Toast.makeText(context, "exception raised", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.ipButton.setOnClickListener {
            ip = binding.ipEditText.text.toString()
            if (ip != "") {
                lifecycleScope.launch {
                    val httpclient = HttpClient(CIO) {
                        install(HttpTimeout) {
                            requestTimeoutMillis = 20000
                        }
                        install(ContentNegotiation) {
                            gson()
                        }

                    }
                    var address = "http://" + ip + ":5001/macc"
                    val url = address + "/ping/"
                    Log.d(TAG, url)
                    val response = httpclient.get(url)

                    if (response.status.value == 200) {
                        Log.d("IPCHOICE", "200")
                        sharedPreferences.edit().putString("ip", ip).apply()
                        sharedPreferences.edit().putString("address", "http://" + ip + ":5001/macc").apply()
                        sharedPreferences.edit().putString("imageServer", "http://" + ip + ":9705").apply()
                        val intent = Intent(context, SignInActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, "IP not valid", Toast.LENGTH_SHORT).show()
                        ip = ""
                    }
                }
            }
        }



    }

}