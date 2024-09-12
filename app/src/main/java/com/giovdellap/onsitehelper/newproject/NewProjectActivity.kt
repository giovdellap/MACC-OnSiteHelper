package com.giovdellap.onsitehelper.newproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.ActivityNewProjectBinding
import com.giovdellap.onsitehelper.model.NewProjectRequest
import com.giovdellap.onsitehelper.model.address
import com.giovdellap.onsitehelper.projects.ProjectsActivity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.launch

class NewProjectActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityNewProjectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backButton: Button = findViewById(R.id.backButton)
        val submitButton: Button = findViewById(R.id.submitButton)
        val titleForm: EditText = findViewById(R.id.titleEditText)
        val descriptionForm: EditText = findViewById(R.id.descriptionEditText)

        val context = this.applicationContext

        val sharedPreferences = this.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        val user = sharedPreferences.getString("email", "")
        var content = ""
        if (user != null) {
            content = user
        }

        backButton.setOnClickListener {
            val intent = Intent(this, ProjectsActivity::class.java)
            startActivity(intent)
        }

        submitButton.setOnClickListener {

            val title = titleForm.text.toString()
            val description = descriptionForm.text.toString()

            Log.d("TAG", title)
            Log.d("TAG", description)


            lifecycleScope.launch {
                val httpclient = HttpClient(CIO) {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 20000
                    }
                    install(ContentNegotiation) {
                        gson()
                    }

                }

                val url = address + "/newProject"
                val response = httpclient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(NewProjectRequest(content, title, description))
                }
                if(response.status.value == 200) {
                    val intent = Intent(context, ProjectsActivity::class.java)
                    startActivity(intent)
                }
                Log.d("TAG", response.status.value.toString())
            }
        }

    }
}