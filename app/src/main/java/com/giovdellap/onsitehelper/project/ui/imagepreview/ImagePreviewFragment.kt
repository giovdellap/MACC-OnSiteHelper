package com.giovdellap.onsitehelper.project.ui.imagepreview

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils.copy
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.FragmentImagePreviewBinding
import com.giovdellap.onsitehelper.model.AddImageRequest
import com.giovdellap.onsitehelper.model.AddImageResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType

import io.ktor.serialization.gson.gson
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class ImagePreviewFragment : Fragment() {
    private var _binding: FragmentImagePreviewBinding? = null
    private val binding get() = _binding!!
    val TAG: String = "IMAGEPREVIEWFRAGMENT"
    var encodedImage: String = ""
    lateinit var bm: Bitmap


    lateinit var loc_uri: Uri


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("IMAGEPREVIEWFRAGMENT", "onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        val context = requireContext().applicationContext

        val sharedPreferences = context.getSharedPreferences("OnSiteHelper", MODE_PRIVATE)
        val address = sharedPreferences.getString("address", "")
        val imageServer = sharedPreferences.getString("imageServer", "")
        val uri_temp = sharedPreferences.getString("image_uri", "")
        if(uri_temp != null) loc_uri = Uri.parse(uri_temp)

        val previewImage: ImageView = binding.imageViewPreview


        lifecycleScope.launch {
            val fileType: String? = requireContext().applicationContext.contentResolver.getType(loc_uri)
            Log.d(TAG, "OnviewCreated - fileType")
            val fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
            Log.d(TAG, "OnviewCreated - fileExtension")
            val fileName = "temporary_file" + if (fileExtension != null) ".$fileExtension" else ""
            Log.d(TAG, "OnviewCreated - filename")

            var tempFile = File(requireContext().applicationContext.cacheDir, fileName)
            tempFile.createNewFile()
            Log.d(TAG, "OnviewCreated - tempFile created")
            try {
                val oStream = FileOutputStream(tempFile)
                val inputStream = requireContext().applicationContext.contentResolver.openInputStream(loc_uri)
                Log.d(TAG, "OnviewCreated - streamings setted")

                inputStream?.let {
                    copy(inputStream, oStream)
                }

                val bm_temp = BitmapFactory.decodeStream(FileInputStream(tempFile))
                bm = rotateBitmap(bm_temp, 90.toFloat())
                val baos = ByteArrayOutputStream()
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos) //bm is the bitmap object
                val b = baos.toByteArray()
                encodedImage = Base64.encodeToString(b, Base64.DEFAULT)

                oStream.flush()
                Log.d(TAG, "OnviewCreated - after flush")

                previewImage.setImageBitmap(bm)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            fun copy(source: InputStream, target: OutputStream) {
                val buf = ByteArray(8192)
                var length: Int
                while (source.read(buf).also { length = it } > 0) {
                    target.write(buf, 0, length)
                }
                Log.d(TAG, "OnviewCreated - IOEXCEPTION")

            }
        }

        binding.backbutton.setOnClickListener {
            Log.d(TAG, "onViewCreated - backButton pressed")
            findNavController().navigate(R.id.action_Preview_to_Camera)
        }


        binding.savebutton.setOnClickListener {
            Log.d(TAG, "savebutton pressed")
            lifecycleScope.launch {

                val url = imageServer + "/images/upload"
                //val response = httpclient.post(url) {
                //    contentType(ContentType.Application.Json)
                //    setBody(requestBody)
                //}

                val httpclient = HttpClient(CIO) {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 30000
                    }
                    install(ContentNegotiation) {
                        gson()
                    }
                }
                val response = httpclient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(AddImageRequest(encodedImage))
                }
                if(response.status.value == 200) {
                    Log.d(TAG, response.status.value.toString())
                    val imageBody: AddImageResponse = response.body<AddImageResponse>()
                    Log.d(TAG, imageBody.image)

                    sharedPreferences.edit().putString("imageId", imageBody.image).apply()
                    findNavController().navigate(R.id.action_Preview_to_NewPosition)
                }
            }
        }
    }

    fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )
    }
}
