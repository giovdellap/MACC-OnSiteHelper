package com.giovdellap.onsitehelper.project.ui.list

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.model.PositionListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.launch
import com.giovdellap.onsitehelper.databinding.FragmentListBinding
import com.giovdellap.onsitehelper.model.DeletePositionRequest
import com.giovdellap.onsitehelper.model.Position
import com.google.gson.Gson
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType


class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val TAG: String = "LISTFRAGMENT"

    lateinit var author: String
    lateinit var projId: String
    lateinit var address: String
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var positions: List<Position> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)

        val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        author = sharedPreferences.getString("projectAuthor", "")!!
        address = sharedPreferences.getString("address", "")!!
        projId = sharedPreferences.getString("projectId", "")!!
        val fr = this as Fragment
        val edited = sharedPreferences.getString("position_deleted", "")
        if (edited == "true") {
            Log.d(TAG, "edited")
            val pos_deleted = sharedPreferences.getString("position_id", "")
            Log.d(TAG, pos_deleted!!)
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
                val url = address + "/deletePosition"
                val requestBody = DeletePositionRequest(pos_deleted, author, projId)
                Log.d(TAG, "delete pre-request")
                val response = httpclient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
                Log.d(TAG, "delete post-request")
                if(response.status.value == 200) {
                    Log.d(TAG, "delete request ok")
                    sharedPreferences.edit().putString("position_deleted", "").apply()
                    sharedPreferences.edit().putString("position_id", "").apply()
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
            val url = address + "/getPositions/" + author + "/" + projId
            Log.d(TAG, url)
            Log.d(TAG, "onCreateView pre-request")
            val response: PositionListResponse = httpclient.get(url).body()
            Log.d(TAG, "onCreateView post-request")
            Log.d(TAG, Gson().toJson(response).toString())
            positions = response.data
            val arrayAdapter = PositionsAdapter(requireContext(), R.layout.list_editors, positions)
            Log.d(TAG, "adapter 1")
            arrayAdapter.setFragment(fr)
            Log.d(TAG, "adapter 2")
            binding.positionslistView.adapter = arrayAdapter
            Log.d(TAG, "adapter 3")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        author = sharedPreferences.getString("projectAuthor", "")!!
        address = sharedPreferences.getString("address", "")!!
        projId = sharedPreferences.getString("projectId", "")!!
        val fr = this as Fragment

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
            val url = address + "/getPositions/" + author + "/" + projId
            Log.d(TAG, "onCreateView pre-request")
            val response: PositionListResponse = httpclient.get(url).body()
            Log.d(TAG, "onCreateView post-request")
            positions = response.data
            val arrayAdapter = PositionsAdapter(requireContext(), R.layout.list_editors, positions)
            arrayAdapter.setFragment(fr)
            binding.positionslistView.adapter = arrayAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}

class PositionsAdapter(
    private val mContext: Context,
    private val mLayoutResourceId: Int,
    positions: List<Position>
) :
    ArrayAdapter<Position>(mContext, mLayoutResourceId, positions) {
    private val pos: MutableList<Position> = ArrayList(positions)
    private var allPositions: List<Position> = ArrayList(positions)
    private var fragment: Fragment = Fragment()

    fun setFragment(fr: Fragment) {
        fragment = fr
    }

    override fun getCount(): Int {
        return pos.size
    }
    override fun getItem(position: Int): Position {
        return pos[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Activity).layoutInflater
            convertView = inflater.inflate(mLayoutResourceId, parent, false)
        }
        try {
            val pos: Position = getItem(position)
            val editorAutoCompleteView = convertView!!.findViewById<View>(R.id.editorCardTextView) as TextView
            editorAutoCompleteView.text = pos.id + " - " + pos.title
            val sharedPreferences = mContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)

            val deleteButton = convertView.findViewById<View>(R.id.deleteEditorButton) as Button
            deleteButton.setOnClickListener {
                Log.d("ADAPTER", ("deleteButton - " + pos.id))
                sharedPreferences.edit().putString("position_deleted", "true").apply()
                sharedPreferences.edit().putString("position_id", pos.id).apply()

                fragment.findNavController().navigate(R.id.action_List_to_List)
            }

            convertView.setOnClickListener {
                sharedPreferences.edit().putString("current_position", Gson().toJson(pos)).apply()
                sharedPreferences.edit().putString("prevFragment", "LIST").apply()
                Log.d("TAG", "project " + pos.title)
                fragment.findNavController().navigate(R.id.action_List_to_Detail)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }
}