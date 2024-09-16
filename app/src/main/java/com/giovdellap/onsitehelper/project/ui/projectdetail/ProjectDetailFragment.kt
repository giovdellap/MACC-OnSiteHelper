package com.giovdellap.onsitehelper.project.ui.projectdetail

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.database.DataSetObserver
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.FragmentProjectDetailBinding
import com.giovdellap.onsitehelper.model.AddEditorRequest
import com.giovdellap.onsitehelper.model.Project
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.concurrent.shared

class ProjectDetailFragment : Fragment() {
    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!
    var TAG: String = "PROJECTDETAILFRAGMENT"
    private var initialCount: Int = 0
    lateinit var project: Project

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)

        val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        val proj_string = sharedPreferences.getString("current_project", "")
        project = Gson().fromJson<Project>(proj_string, Project::class.java)
        val edited = sharedPreferences.getString("editor_deleted", "")
        if (edited == "true") {
            Log.d(TAG, "edited")
            val editor = sharedPreferences.getString("editor_name", "")
            Log.d(TAG, editor!!)
            lifecycleScope.launch {
                val httpclient = HttpClient(CIO) {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 30000
                    }
                    install(ContentNegotiation) {
                        gson()
                    }
                }
                val address = sharedPreferences.getString("address", "")
                val url = address + "/deleteEditor"
                val requestBody = AddEditorRequest(editor!!, project.id, project.author)
                Log.d(TAG, "delete pre-request")
                val response = httpclient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
                Log.d(TAG, "delete post-request")
                if(response.status.value == 200) {
                    Log.d(TAG, "delete request ok")
                    Log.d(TAG, project.editors.size.toString())
                    sharedPreferences.edit().putString("editor_deleted", "").apply()
                    sharedPreferences.edit().putString("editor_name", "").apply()
                }
                Log.d(TAG, response.status.value.toString())
            }
        }

        lifecycleScope.launch {
            val httpclient = HttpClient(CIO) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 30000
                }
                install(ContentNegotiation) {
                    gson()
                }
            }
            val address = sharedPreferences.getString("address", "")
            val url = address + "/getProject/" + project.author + "/" + project.id
            Log.d(TAG, "onCreateView pre-request")
            val response = httpclient.get(url)
            Log.d(TAG, "onCreateView post-request")
            if(response.status.value == 200) {
                project = response.body()
                Log.d(TAG, "onCreateView request ok")
                Log.d(TAG, project.editors.size.toString())
                sharedPreferences.edit().putString("current_project", Gson().toJson(project)).apply()
                //findNavController().navigate(R.id.action_ProjectDetail_to_ProjectDetail)
            }
            Log.d(TAG, response.status.value.toString())
        }

        binding.projectTitleTextView.text = project.title
        binding.projectDescriptionTextView.text = project.description

        var mListView: ListView= binding.editorsListView
        Log.d(TAG, ("pre-adapter " + project.editors.size.toString()))
        val arrayAdapter = EditorsAdapter(
            requireContext(),
            R.layout.list_editors, project.editors
        )

        arrayAdapter.setFragment(this)

        Log.d(TAG, "A")
        mListView.adapter = arrayAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "OnviewCreated")

        val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        val proj_string = sharedPreferences.getString("current_project", "")
        val project: Project = Gson().fromJson<Project>(proj_string, Project::class.java)

        binding.addEditorButton.setOnClickListener {
            val newEditor: String = binding.addEditorTextView.text.toString()
            if (newEditor != "") {

                val address = sharedPreferences.getString("address", "")
                lifecycleScope.launch {
                    val httpclient = HttpClient(CIO) {
                        install(HttpTimeout) {
                            requestTimeoutMillis = 30000
                        }
                        install(ContentNegotiation) {
                            gson()
                        }
                    }
                    val url = address + "/addEditor"
                    val requestBody = AddEditorRequest(newEditor, project.id, project.author)
                    val response = httpclient.post(url) {
                        contentType(ContentType.Application.Json)
                        setBody(requestBody)
                    }
                    if(response.status.value == 200) {
                        findNavController().navigate(R.id.action_ProjectDetail_to_ProjectDetail)
                    }
                    Log.d(TAG, response.status.value.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val fragment = this as Fragment
        Log.d(TAG, "onResume")
        val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        lifecycleScope.launch {
            val httpclient = HttpClient(CIO) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 30000
                }
                install(ContentNegotiation) {
                    gson()
                }
            }
            val address = sharedPreferences.getString("address", "")
            val url = address + "/getProject/" + project.author + "/" + project.id
            Log.d(TAG, "onResume pre-request")
            val response = httpclient.get(url)
            Log.d(TAG, "onResume post-request")
            if(response.status.value == 200) {
                project = response.body()
                Log.d(TAG, "onResume request ok")
                Log.d(TAG, project.editors.size.toString())
                sharedPreferences.edit().putString("current_project", Gson().toJson(project)).apply()
                val arrayAdapter = EditorsAdapter(
                    requireContext(),
                    R.layout.list_editors, project.editors
                )
                arrayAdapter.setFragment(fragment)                //findNavController().navigate(R.id.action_ProjectDetail_to_ProjectDetail)
                binding.editorsListView.adapter = arrayAdapter
            }
            Log.d(TAG, response.status.value.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}

class EditorsAdapter(
    private val mContext: Context,
    private val mLayoutResourceId: Int,
    editors: List<String>
) :
    ArrayAdapter<String>(mContext, mLayoutResourceId, editors) {
    private val editor: MutableList<String> = ArrayList(editors)
    private var allEditors: List<String> = ArrayList(editors)
    private var fragment: Fragment = Fragment()

    fun setFragment(fr: Fragment) {
        fragment = fr
    }

    override fun getCount(): Int {
        return editor.size
    }
    override fun getItem(position: Int): String {
        return editor[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Activity).layoutInflater
            convertView = inflater.inflate(mLayoutResourceId, parent, false)
        }
        try {
            val editor: String = getItem(position)
            val editorAutoCompleteView = convertView!!.findViewById<View>(R.id.editorCardTextView) as TextView
            editorAutoCompleteView.text = editor

            val deleteButton = convertView.findViewById<View>(R.id.deleteEditorButton) as Button
            deleteButton.setOnClickListener {
                Log.d("ADAPTER", ("deleteButton - " + editor))
                val sharedPreferences = mContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
                sharedPreferences.edit().putString("editor_deleted", "true").apply()
                sharedPreferences.edit().putString("editor_name", editor).apply()

                fragment.findNavController().navigate(R.id.action_ProjectDetail_to_ProjectDetail)

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }
}