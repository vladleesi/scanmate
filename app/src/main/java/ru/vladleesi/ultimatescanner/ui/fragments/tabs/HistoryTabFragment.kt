package ru.vladleesi.ultimatescanner.ui.fragments.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
import ru.vladleesi.ultimatescanner.databinding.ActivityHistoryBinding
import ru.vladleesi.ultimatescanner.extensions.showToast
import ru.vladleesi.ultimatescanner.ui.adapter.HistoryListAdapter
import ru.vladleesi.ultimatescanner.ui.fragments.TabFragment
import java.lang.ref.WeakReference

class HistoryTabFragment : TabFragment() {

    override val pageTitleId: Int
        get() = R.string.page_title_history

    private lateinit var binding: ActivityHistoryBinding
    private val repo by lazy { AnalyzeRepo(WeakReference(context)) }
    private val adapter by lazy { HistoryListAdapter() }

    private val coroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            lifecycleScope.launch {
                showToast("ERROR: Can't load history list\n${throwable.message ?: throwable.toString()}")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_history, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = ActivityHistoryBinding.bind(view)

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

    companion object {
        const val TAG = "HistoryTabFragment"

        fun newInstance() = HistoryTabFragment().apply { arguments = Bundle() }
    }
}