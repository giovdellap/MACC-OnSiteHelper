package com.giovdellap.onsitehelper.newproject

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
import java.util.Locale
import java.util.Objects

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
        val address = sharedPreferences.getString("address", "")
        var content = ""
        if (user != null) {
            content = user
        }

        backButton.text = "<"

        backButton.setOnClickListener {
            val intent = Intent(this, ProjectsActivity::class.java)
            startActivity(intent)
        }

        binding.speechButtonProj.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            // on below line we are passing language model
            // and model free form in our intent
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // on below line we are passing our
            // language as a default language.
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            // on below line we are specifying a prompt
            // message as speak to text on below line.
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            // on below line we are specifying a try catch block.
            // in this block we are calling a start activity
            // for result method and passing our result code.
            try {
                startActivityForResult(intent, 1)
            } catch (e: Exception) {
                // on below line we are displaying error message in toast
                Toast.makeText(this, "speech to text", Toast.LENGTH_SHORT).show()
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // in this method we are checking request
        // code with our result code.
        if (requestCode == 1) {
            // on below line we are checking if result code is ok
            Log.d("NEWPOSITIONFRAGMENT", resultCode.toString())
            if (resultCode == RESULT_OK && data != null) {

                // in that case we are extracting the
                // data from our array list
                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>

                // on below line we are setting data
                // to our output text view.
                binding.descriptionEditText.setText(
                    Objects.requireNonNull(res)[0]
                )
            }
        }
    }
}