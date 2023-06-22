package dev.vladleesi.scanmate.ui.fragments.tabs

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import dev.vladleesi.scanmate.R
import dev.vladleesi.scanmate.databinding.FragmentHistoryBinding
import dev.vladleesi.scanmate.extensions.showToast
import dev.vladleesi.scanmate.ui.adapter.HistoryListAdapter
import dev.vladleesi.scanmate.ui.fragments.TabFragment
import dev.vladleesi.scanmate.ui.viewmodel.HistoryViewModel

@AndroidEntryPoint
class HistoryTabFragment : TabFragment(R.layout.fragment_history) {

    private val binding by viewBinding(FragmentHistoryBinding::bind)

    private val viewModel by viewModels<HistoryViewModel>()

    private val adapter = HistoryListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initToolbar()

        loadHistory()

        binding.fabClearHistory.setOnClickListener {
            viewModel.clearHistory()
        }

        observeData()
    }

    private fun initToolbar() {
        binding.includeToolbar.tbToolbar.title = "История"
    }

    private fun loadHistory() {
        binding.rvHistoryList.adapter = adapter
        viewModel.loadHistory()
    }

    private fun observeData() {
        viewModel.clearLiveData.observe {
            adapter.clearData(notify = true)
            showToast("History have been cleared")
        }

        viewModel.historyLiveData.observe(adapter::setData)
    }

    companion object {
        fun newInstance() = HistoryTabFragment().apply {
            arguments = bundleOf()
        }
    }
}
