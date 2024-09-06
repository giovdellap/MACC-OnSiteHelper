package com.giovdellap.onsitehelper

import android.content.Intent
import android.credentials.CredentialManager
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.commit
import com.giovdellap.onsitehelper.databinding.ActivityMainBinding
import com.giovdellap.onsitehelper.signin.SignInActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TAG", "main oncreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Log.d("TAG", "main before switch")

        val serviceIntent = Intent(this, APIService::class.java)
        startService(serviceIntent)

        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

}