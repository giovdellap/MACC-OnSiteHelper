package com.giovdellap.onsitehelper.projects

import android.app.Activity
import android.content.Intent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.AppBarConfiguration
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.ActivityProjectsBinding
import com.giovdellap.onsitehelper.model.DeleteProjectRequest
import com.giovdellap.onsitehelper.model.ListRequest
import com.giovdellap.onsitehelper.model.ListResponse
import com.giovdellap.onsitehelper.model.Project
import com.giovdellap.onsitehelper.newproject.NewProjectActivity
import com.giovdellap.onsitehelper.project.ProjectActivity
import com.giovdellap.onsitehelper.signin.SignInActivity
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

    private lateinit var binding: ActivityProjectsBinding
    lateinit var address: String
    lateinit var user: String
    private val TAG: String = "PROJECTSACTIVITY"
    var projects: List<Project> = emptyList()

    @OptIn(InternalAPI::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TAG", "Projects Activity")

        super.onCreate(savedInstanceState)

        binding = ActivityProjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var projects: List<Project> = emptyList()
        val context = this as Activity

        val sharedPreferences = context.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        user = sharedPreferences.getString("email", "")!!
        address = sharedPreferences.getString("address", "")!!

        val edited = sharedPreferences.getString("project_deleted", "")
        if (edited == "true") {
            Log.d(TAG, "project deleted")
            val proj_deleted = sharedPreferences.getString("project_id", "")
            Log.d(TAG, proj_deleted!!)
            lifecycleScope.launch {
                val httpclient = HttpClient(CIO) {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 30000
                    }
                    install(ContentNegotiation) {
                        gson()
                    }
                }
                val url = address + "/deleteProject"
                val requestBody = DeleteProjectRequest(user, proj_deleted)
                Log.d(TAG, "delete pre-request")
                val response = httpclient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
                Log.d(TAG, "delete post-request")
                if(response.status.value == 200) {
                    Log.d(TAG, "delete request ok")
                    sharedPreferences.edit().putString("project_deleted", "").apply()
                    sharedPreferences.edit().putString("project_id", "").apply()
                }
                Log.d(TAG, response.status.value.toString())
            }
        }

        lifecycleScope.launch {
            val httpclient = HttpClient(CIO) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 20000
                }
                install(ContentNegotiation) {
                    gson()
                }
            }

            val url = address + "/getProjects/" + user
            Log.d("TAGs", url)
            val response: ListResponse = httpclient.get(url).body()

            projects = response.data
            Log.d(TAG, "A")
            // access the listView from xml file
            var mListView: ListView= findViewById(R.id.listview)
            Log.d(TAG, "B")

            val arrayAdapter = ProjectsAdapter(
                context,
                R.layout.list_editors, projects
            )
            Log.d(TAG, "C")

            arrayAdapter.setCon(context)
            Log.d(TAG, "D")

            mListView.adapter = arrayAdapter
            Log.d(TAG, "E")

        }

        val newProjButton: Button = findViewById(R.id.newProjectButton)
        newProjButton.setOnClickListener {
            val intent = Intent(this, NewProjectActivity::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            sharedPreferences.edit().putString("email", "").apply()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val context = this as Activity
        lifecycleScope.launch {
            val httpclient = HttpClient(CIO) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 20000
                }
                install(ContentNegotiation) {
                    gson()
                }
            }

            val url = address + "/getProjects/" + user
            Log.d("TAGs", url)
            val response: ListResponse = httpclient.get(url).body()

            projects = response.data
            Log.d(TAG, "A")
            // access the listView from xml file
            var mListView: ListView= findViewById(R.id.listview)
            Log.d(TAG, "B")

            val arrayAdapter = ProjectsAdapter(
                context,
                R.layout.list_editors, projects
            )
            Log.d(TAG, "C")

            arrayAdapter.setCon(context)
            Log.d(TAG, "D")

            mListView.adapter = arrayAdapter
            Log.d(TAG, "E")

        }
    }
}

class ProjectsAdapter(
    private val mContext: Context,
    private val mLayoutResourceId: Int,
    projects: List<Project>
) :
    ArrayAdapter<Project>(mContext, mLayoutResourceId, projects) {
    private val proj: MutableList<Project> = ArrayList(projects)
    private var allProjects: List<Project> = ArrayList(projects)
    private lateinit var con: Activity

    fun setCon(arg: Activity) {
        con = arg
    }

    override fun getCount(): Int {
        return proj.size
    }
    override fun getItem(position: Int): Project {
        return proj[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            Log.d("ADAPTER", "PRIMA")
            val inflater = con.layoutInflater
            Log.d("ADAPTER", "IN MEZZO")
            convertView = inflater.inflate(mLayoutResourceId, parent, false)
            Log.d("ADAPTER", "DOPO")

        }
        try {
            val proj: Project = getItem(position)
            val editorAutoCompleteView = convertView!!.findViewById<View>(R.id.editorCardTextView) as TextView
            editorAutoCompleteView.text = proj.id + " - " + proj.title
            val sharedPreferences = mContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)

            val deleteButton = convertView.findViewById<View>(R.id.deleteEditorButton) as Button
            deleteButton.setOnClickListener {
                Log.d("ADAPTER", ("deleteButton - " + proj.id))
                sharedPreferences.edit().putString("project_deleted", "true").apply()
                sharedPreferences.edit().putString("project_id", proj.id).apply()

                val intent = Intent(con, ProjectsActivity::class.java)
                con.startActivity(intent)

            }

            convertView.setOnClickListener {
                sharedPreferences.edit().putString("current_project", Gson().toJson(proj)).apply()
                sharedPreferences.edit().putString("projectId", proj.id).apply()
                sharedPreferences.edit().putString("projectAuthor", proj.author).apply()

                val intent = Intent(con, ProjectActivity::class.java)
                con.startActivity(intent)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }
}
