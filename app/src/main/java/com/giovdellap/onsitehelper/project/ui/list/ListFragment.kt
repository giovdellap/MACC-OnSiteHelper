package com.giovdellap.onsitehelper.project.ui.list

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.giovdellap.onsitehelper.model.Position
import com.giovdellap.onsitehelper.model.address
import com.google.gson.Gson

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        val us = sharedPreferences.getString("projectAuthor", "")
        val id_temp = sharedPreferences.getString("projectId", "")
        var user = ""
        var id = ""
        if (us != null) user = us
        if (id_temp != null) id = id_temp

        lifecycleScope.launch {
            val httpclient = HttpClient(CIO) {
                install(HttpTimeout) {
                    requestTimeoutMillis = 20000
                }
                install(ContentNegotiation) {
                    gson()
                }
            }

            val url = address + "/getPositions/" + user + "/" + id
            val response: PositionListResponse = httpclient.get(url).body()

            val positions = response.data

            val arrayAdapter: ArrayAdapter<*>

            var strings: ArrayList<String> = ArrayList()
            for (item in positions) {
                strings.add(item.title)
            }
            val projectsArray = strings.toTypedArray()
            Log.d("AO", Gson().toJson(projectsArray))


            // access the listView from xml file
            var mListView: ListView = binding.positionslistView
            arrayAdapter = ArrayAdapter(
                requireContext().applicationContext,
                android.R.layout.simple_list_item_1, projectsArray
            )
            mListView.adapter = arrayAdapter

            mListView.onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    // Get the selected item text from ListView
                    val selectedItem = parent.getItemAtPosition(position) as String
                    var selectedPos: Position = Position()
                    for (pos in positions) {
                        if (selectedItem == pos.title) {
                            selectedPos = pos
                        }
                    }
                    sharedPreferences.edit().putString("current_position", Gson().toJson(selectedPos)).apply()
                    if (selectedPos.title != "") {
                        Log.d("TAG", "project " + selectedPos.title)
                    }
                    findNavController().navigate(R.id.action_List_to_Detail)


                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}