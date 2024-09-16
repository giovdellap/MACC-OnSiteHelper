package com.giovdellap.onsitehelper.project.ui.map

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.FragmentMapBinding
import com.giovdellap.onsitehelper.model.Position
import com.giovdellap.onsitehelper.model.PositionListResponse
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.launch
import io.ktor.client.call.body



class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("TAG", "A")

        val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        val address = sharedPreferences.getString("address", "")
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
            Log.d("TAG", "B")

            val positions = response.data
            Log.d("TAG", positions[0].title)

            val mapFragment = childFragmentManager.findFragmentById(
                R.id.map_fragment
            ) as? SupportMapFragment
            Log.d("TAG", mapFragment?.id.toString())
            mapFragment?.getMapAsync { googleMap ->
                Log.d("TAG", "D")

                for (pos in positions) {
                    Log.d("TAG", "E")

                    val latlng = LatLng(pos.latitude.toDouble(), pos.longitude.toDouble())
                    googleMap.addMarker(
                        MarkerOptions()
                            .title(pos.id + " - " + pos.title)
                            .position(latlng)
                    )
                }
                googleMap.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { marker ->
                    var selectedPos: Position = Position()
                    for (pos in positions) {
                        if (marker.title == (pos.id + " - " + pos.title)) selectedPos = pos
                    }
                    if (selectedPos.title != "") {
                        Log.d("TAG", "project " + selectedPos.title)
                        sharedPreferences.edit().putString("current_position", Gson().toJson(selectedPos)).apply()
                        sharedPreferences.edit().putString("prevFragment", "MAP").apply()
                        findNavController().navigate(R.id.action_Map_to_Detail)
                    }
                    true

                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}