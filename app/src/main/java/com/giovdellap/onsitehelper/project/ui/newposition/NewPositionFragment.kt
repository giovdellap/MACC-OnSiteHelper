package com.giovdellap.onsitehelper.project.ui.newposition

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.FragmentNewBinding
import com.giovdellap.onsitehelper.model.NewPositionRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.launch


class NewPositionFragment : Fragment() {

    private var _binding: FragmentNewBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cameraButton.setOnClickListener {
            findNavController().navigate(R.id.action_NewPosition_to_Camera)
        }

        binding.submitButton.setOnClickListener {

            val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
            val user = sharedPreferences.getString("email", "")

            var projId: String = ""
            var projAuthor: String = ""
            val projId_temp = sharedPreferences.getString("projectId", "")
            val projAuthor_temp = sharedPreferences.getString("projectAuthor", "")
            if(projId_temp != null) projId = projId_temp
            if (projAuthor_temp != null) projAuthor = projAuthor_temp


            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()
            Log.d("TAG", "A")
            val aaa = ContextCompat.checkSelfPermission(requireContext().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            Log.d("TAG", aaa.toString())
            Log.d("TAG", PackageManager.PERMISSION_GRANTED.toString())
            if (ContextCompat.checkSelfPermission(requireContext().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("TAG", "B")
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext().applicationContext)
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    Log.d("TAG", "C")
                    var requestBody = NewPositionRequest(description, location?.latitude.toString(), location?.longitude.toString(), title, projId, projAuthor)
                    lifecycleScope.launch {
                        val httpclient = HttpClient(CIO) {
                            install(HttpTimeout) {
                                requestTimeoutMillis = 20000
                            }
                            install(ContentNegotiation) {
                                gson()
                            }
                        }

                        val url = "http://192.168.1.65:5001/macc/addPosition"
                        val response = httpclient.post(url) {
                            contentType(ContentType.Application.Json)
                            setBody(requestBody)
                        }
                        if(response.status.value == 200) {
                            findNavController().navigate(R.id.action_NewPosition_to_List)
                        }
                        Log.d("TAG", response.status.value.toString())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }.toTypedArray()
    }
}