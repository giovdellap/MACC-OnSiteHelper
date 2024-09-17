package com.giovdellap.onsitehelper.project.ui.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils.copy
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.FragmentCameraBinding
import com.giovdellap.onsitehelper.model.AddImageRequest
import com.giovdellap.onsitehelper.model.AddImageResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType

import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.io.InputStream
import java.io.OutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    lateinit var loc_uri: Uri


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("CAMERAFRAGMENT", "onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CAMERAFRAGMENT", "onViewCreated")

        val context = requireContext().applicationContext

        binding.backButton.text = "<"

        binding.backButton.setOnClickListener {
            Log.d("CAMERAFRAGMENT", "onViewCreated - backButton pressed")
            findNavController().navigate(R.id.action_Camera_to_NewPosition)
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext().applicationContext)
        val imageCapture = ImageCapture.Builder()
            .build()


        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)
                Log.d("CAMERAFRAGMENT", "onViewCreated - cameraProvider binded")

            } catch(exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext().applicationContext))
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.takePhotoButton.setOnClickListener {

            Log.d("CAMERAFRAGMENT", "onViewCreated - takephotobutton pressed")

            val name = SimpleDateFormat("", Locale.ITALIAN)
                .format(System.currentTimeMillis())

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
            Log.d("CAMERAFRAGMENT", "onViewCreated - contentvalues created")

            val contentResolver = requireContext().applicationContext.getContentResolver();
            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions
                .Builder(contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues)
                .build()
            Log.d("CAMERAFRAGMENT", "outputoptions created")

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext().applicationContext),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults){

                        val msg = "Photo capture succeeded: ${output.savedUri}"
                        val savedUri = output.savedUri
                        if(savedUri != null) loc_uri = savedUri
                        val sharedPreferences = requireContext().applicationContext.getSharedPreferences("OnSiteHelper", Context.MODE_PRIVATE)
                        sharedPreferences.edit().putString("image_uri", loc_uri.toString()).apply()
                        Log.d("CAMERAFRAGMENT", loc_uri.toString())
                        Toast.makeText(requireContext().applicationContext, msg, Toast.LENGTH_SHORT).show()
                        Log.d("CAMERAFRAGMENT", "onViewCreated - onImageSaved")
                        findNavController().navigate(R.id.action_Camera_to_Preview)

                    }
                }
            )
        }
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
