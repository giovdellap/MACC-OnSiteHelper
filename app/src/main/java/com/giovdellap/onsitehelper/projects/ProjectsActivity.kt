package com.giovdellap.onsitehelper.projects

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.AppBarConfiguration
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.ActivityProjectsBinding
import com.giovdellap.onsitehelper.model.ListRequest
import com.giovdellap.onsitehelper.model.ListResponse
import com.giovdellap.onsitehelper.model.Project
import com.giovdellap.onsitehelper.model.address
import com.giovdellap.onsitehelper.newproject.NewProjectActivity
import com.giovdellap.onsitehelper.project.ProjectActivity
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.gson.gson
import io.ktor.util.InternalAPI
import kotlinx.serialization.json.*

import kotlinx.coroutines.launch

class ProjectsActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProjectsBinding

    @OptIn(InternalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TAG", "Projects Activity")

        super.onCreate(savedInstanceState)

        binding = ActivityProjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var projects: List<Project> = emptyList()
        val context = this.applicationContext

        val sharedPreferences = context.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        val user = sharedPreferences.getString("email", "")
        var content = ""
        if (user != null) {
            content = user
        }

        Log.d("TAG", "prima di launch")
        lifecycleScope.launch {
            val httpclient = HttpClient(CIO) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 20000
                }
                install(ContentNegotiation) {
                    gson()
                }

            }

            val url = address + "/getProjects/" + content
            Log.d("TAGs", url)
            val response: ListResponse = httpclient.get(url).body()

            projects = response.data

            val arrayAdapter: ArrayAdapter<*>

            var strings: ArrayList<String> = ArrayList()
            for (item in projects) {
                strings.add(item.id + " - " + item.title)
            }
            val projectsArray = strings.toTypedArray()
            Log.d("AO", Gson().toJson(projectsArray))


            // access the listView from xml file
            var mListView: ListView= findViewById(R.id.listview)
            arrayAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1, projectsArray
            )
            mListView.adapter = arrayAdapter

            mListView.onItemClickListener = AdapterView.OnItemClickListener {
                    parent,view, position, id ->
                // Get the selected item text from ListView
                val selectedItem = parent.getItemAtPosition(position) as String
                var selectedProj: Project = Project(emptyList())
                for (proj in projects) {
                    var listName = proj.id + " - " + proj.title
                    if(selectedItem == listName) {
                        selectedProj = proj
                    }
                }
                if (selectedProj.title != "") {
                    val intent = Intent(context, ProjectActivity::class.java)

                    sharedPreferences.edit().putString("projectId", selectedProj.id).apply()
                    sharedPreferences.edit().putString("projectAuthor", selectedProj.author).apply()

                    startActivity(intent)
                }

            }

        }

        val newProjButton: Button = findViewById(R.id.newProjectButton)
        newProjButton.setOnClickListener {
            val intent = Intent(this, NewProjectActivity::class.java)
            startActivity(intent)
        }


        //val policy = StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

    }
}
