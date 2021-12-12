package ru.vladleesi.ultimatescanner.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
import ru.vladleesi.ultimatescanner.databinding.FragmentHistoryBinding
import ru.vladleesi.ultimatescanner.extensions.showToast
import ru.vladleesi.ultimatescanner.ui.adapter.HistoryListAdapter
import java.lang.ref.WeakReference

class HistoryActivity : AppCompatActivity() {

    private val binding by lazy { FragmentHistoryBinding.inflate(layoutInflater) }
    private val repo by lazy { AnalyzeRepo(WeakReference(applicationContext)) }
    private val adapter by lazy { HistoryListAdapter() }

    private val coroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            lifecycleScope.launch {
                showToast("ERROR: Can't load history list\n${throwable.message ?: throwable.toString()}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initToolbar()

        loadHistory()

        binding.fabClearHistory.setOnClickListener {
            clearHistory()
        }
    }

    private fun clearHistory() {
        GlobalScope.launch(coroutineExceptionHandler) {
            repo.clearHistory()
            lifecycleScope.launch {
                adapter.clearData(notify = true)
                showToast("History have been cleared")
            }
        }
    }

    private fun initToolbar() {
        binding.includeToolbar.tbToolbar.navigationIcon =
            ContextCompat.getDrawable(baseContext, R.drawable.ic_baseline_arrow_back_24)
        binding.includeToolbar.tbToolbar.setNavigationOnClickListener { onBackPressed() }
        binding.includeToolbar.tbToolbar.title = "История"
    }

    private fun loadHistory() {
        binding.rvHistoryList.adapter = adapter

        GlobalScope.launch(coroutineExceptionHandler) {
            repo.getHistory()?.let { historyList ->
                lifecycleScope.launch {
                    adapter.setData(historyList)
                }
            }
        }
    }
}
