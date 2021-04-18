package ru.vladleesi.ultimatescanner.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.adapter.HistoryListAdapter
import ru.vladleesi.ultimatescanner.databinding.ActivityHistoryBinding
import ru.vladleesi.ultimatescanner.repository.AnalyzeRepo
import java.lang.ref.WeakReference

class HistoryActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHistoryBinding.inflate(layoutInflater) }
    private val repo by lazy { AnalyzeRepo(WeakReference(applicationContext)) }
    private val adapter by lazy { HistoryListAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.tbToolbar.navigationIcon =
            ContextCompat.getDrawable(baseContext, R.drawable.ic_baseline_arrow_back_24)
        binding.tbToolbar.setNavigationOnClickListener { onBackPressed() }
        binding.tbToolbar.title = "История"

        binding.rvHistoryList.adapter = adapter

        repo.getHistory().subscribe({ historyList ->
            Handler(Looper.getMainLooper()).post {
                historyList?.let { adapter.setData(historyList) }
            }
        }, {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    baseContext,
                    "ERROR: Can't load history list",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}