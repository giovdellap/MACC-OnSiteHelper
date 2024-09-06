package com.giovdellap.onsitehelper.projects

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.giovdellap.onsitehelper.R
import com.giovdellap.onsitehelper.databinding.ActivityProjectsBinding

class ProjectsActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityProjectsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TAG", "Projects Activity")

        super.onCreate(savedInstanceState)

        binding = ActivityProjectsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val arrayAdapter: ArrayAdapter<*>
        val projects = arrayOf(
            "Virat Kohli", "Rohit Sharma", "Steve Smith",
            "Kane Williamson", "Ross Taylor"
        )

        // access the listView from xml file
        var mListView = findViewById<ListView>(R.id.listview)
        arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, projects)
        mListView.adapter = arrayAdapter
    }

}