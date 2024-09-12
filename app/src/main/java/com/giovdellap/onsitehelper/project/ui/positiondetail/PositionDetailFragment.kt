package com.giovdellap.onsitehelper.project.ui.positiondetail

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils.copy
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
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
import com.giovdellap.onsitehelper.databinding.FragmentPositionDetailBinding
import com.giovdellap.onsitehelper.model.NewPositionRequest
import com.giovdellap.onsitehelper.model.Position
import com.giovdellap.onsitehelper.model.address
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.io.File
import java.io.FileOutputStream


class PositionDetailFragment : Fragment() {

    private var _binding: FragmentPositionDetailBinding? = null
    private val binding get() = _binding!!
    var TAG: String = "POSITIONDETAILFRAGMENT"

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPositionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "OnviewCreated")

        val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        val pos_string = sharedPreferences.getString("current_position", "")
        val position: Position = Gson().fromJson<Position>(pos_string, Position::class.java)

        binding.titleTextView.text = position.title
        binding.descrTextView.text = position.description
        binding.latitudeTextView.text = "Latitude: " + position.latitude
        binding.longitudeTextView.text = "Longitude: " + position.longitude

        binding.backButton.setOnClickListener {
            Log.d(TAG, "OnviewCreated - cameraButton pressed")
            findNavController().navigate(R.id.action_Detail_to_List)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}