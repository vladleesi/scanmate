package ru.vladleesi.ultimatescanner.ui.activity

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.vladleesi.ultimatescanner.Constants.BARCODE_MAP
import ru.vladleesi.ultimatescanner.Constants.CAPTURED_URI
import ru.vladleesi.ultimatescanner.R
import ru.vladleesi.ultimatescanner.data.remote.model.AnalyzeResultApi
import ru.vladleesi.ultimatescanner.data.remote.model.OcrData
import ru.vladleesi.ultimatescanner.data.remote.model.Product
import ru.vladleesi.ultimatescanner.data.repository.AnalyzeRepo
import ru.vladleesi.ultimatescanner.databinding.ActivityCaptureBinding
import ru.vladleesi.ultimatescanner.databinding.PopupTextToSpeechBinding
import ru.vladleesi.ultimatescanner.extensions.*
import ru.vladleesi.ultimatescanner.ui.fragments.ProductDetailsBottomSheetDialogFragment
import ru.vladleesi.ultimatescanner.ui.model.state.ResultState
import ru.vladleesi.ultimatescanner.ui.view.BoxView
import ru.vladleesi.ultimatescanner.utils.FileUtils
import java.lang.ref.WeakReference
import java.util.*

class CaptureActivity : AppCompatActivity(), TextToSpeech.OnInitListener, Speecher {

    private val binding by lazy { ActivityCaptureBinding.inflate(layoutInflater) }

    private val analyzeRepo by lazy { AnalyzeRepo(WeakReference(application)) }

    private val uri by lazy { intent.getParcelableExtra<Uri>(CAPTURED_URI) }

    private lateinit var tts: TextToSpeech
    private val ttsParams by lazy {
        Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f)
        }
    }

    private val handler = CoroutineExceptionHandler { _, throwable ->
        lifecycleScope.launch {
            showToast("ERROR: ${throwable.message ?: throwable.toString()}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        makeStatusBarTransparent()

        binding.ivBack.setOnClickListener { onBackPressed() }

        // TODO: А не в этом ли дело с неверными боксами?
        val bitmap = BitmapFactory.decodeFile(uri?.let { FileUtils.getPathFrom(baseContext, it) })

        binding.ivCapturedImage.setImageBitmap(bitmap)
//        bitmap?.let { runDetector(FirebaseVisionImage.fromBitmap(it)) }

        val barcodeMap =
            intent.getSerializableExtra(BARCODE_MAP) as? HashMap<String, String>
        barcodeMap?.let {
            GlobalScope.launch(handler) {
                analyzeRepo.saveToHistory(it)
            }
        }

        tts = TextToSpeech(baseContext, this)
        uri?.let {
            GlobalScope.launch(handler) {
                val state = analyzeRepo.analyze(it, barcodeMap)
                lifecycleScope.launch {
                    binding.pbProgress.gone()
                    when (state) {
                        is ResultState.Success<AnalyzeResultApi> -> {
                            processData(state.data)
                            showToast("Success")
                        }
                        is ResultState.Loading -> showToast("Loading")
                        is ResultState.Error -> showToast("ERROR: Sending photo have not been complete")
                    }
                }
            }
        }
    }

    private fun processData(data: AnalyzeResultApi?) {
        drawBoxes(data?.ocr_data)

        data?.products?.getOrNull(0)?.let { product ->
            binding.ivProductLogo.setByteArray(product.logo)
            binding.cvProductLogoCard.visible()
            binding.cvProductLogoCard.setOnClickListener {
                openProductDetails(product, false)
            }
            openProductDetails(product, true)
        }
    }

    private fun openProductDetails(product: Product, voiceProduct: Boolean) {
        ProductDetailsBottomSheetDialogFragment.newInstance(product, voiceProduct).show(
            supportFragmentManager,
            ProductDetailsBottomSheetDialogFragment.TAG
        )
    }

    private fun drawBoxes(ocrData: OcrData?) {
        ocrData?.bbox?.forEachIndexed { index, ocrBox ->
            val text = ocrData.text?.get(index)
            drawBox(ocrBox, text)
        }
        initGlobalTextSpeaker(ocrData)
    }

    private fun initGlobalTextSpeaker(ocrData: OcrData?) {
        if (ocrData?.text?.isNotEmpty() == true) {
            binding.fabVoice.visible()
            binding.fabVoice.setOnClickListener {
                tts.setOnUtteranceProgressListener(GlobalUtteranceProgressListener())
                if (tts.isSpeaking) {
                    tts.stop()
                    binding.fabVoice.setImageDrawable(getDrawableCompat(R.drawable.ic_baseline_play_arrow_24))
                } else {
                    binding.fabVoice.setImageDrawable(getDrawableCompat(R.drawable.ic_baseline_stop_24))
                    voiceText(ocrData.text.toString())
                }
            }
        }
    }

    private fun drawBox(ocrBox: List<List<Float>>, text: String?): View {
        return BoxView(this)
            .apply {
                rect(ocrBox)
                background =
                    AppCompatResources.getDrawable(baseContext, R.drawable.background_corner_white)
                alpha = 0.3f
            }
            .also { view ->
                val lp = LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(ocrBox[0][0].toInt(), ocrBox[1][1].toInt(), 0, 0)
                binding.flCaptureContent.addView(view, lp)
                view.setOnClickListener {
                    view.showPopup(getPopupTextToSpeech(text))
                }
            }
    }

    private fun getPopupTextToSpeech(text: String?): View {
        return layoutInflater.inflate(
            R.layout.popup_text_to_speech,
            binding.flCaptureContent,
            false
        ).apply {
            val viewBinding = PopupTextToSpeechBinding.bind(this)
            viewBinding.tvText.text = text

            viewBinding.mbTextCopy.setOnClickListener {
                viewBinding.mbTextCopy.text = "Copied"
                viewBinding.mbTextCopy.icon = getDrawableCompat(R.drawable.ic_baseline_check_24)
                viewBinding.mbTextCopy.isEnabled = false
                viewBinding.tvText.copyToClipboard()
            }

            viewBinding.mbTextToSpeech.setOnClickListener {
                tts.setOnUtteranceProgressListener(PopupUtteranceProgressListener(viewBinding))

                if (tts.isSpeaking) {
                    tts.stop()
                    viewBinding.mbTextToSpeech.text = getString(R.string.tv_play)
                    viewBinding.mbTextToSpeech.icon =
                        getDrawableCompat(R.drawable.ic_baseline_play_arrow_24)
                } else {
                    viewBinding.mbTextToSpeech.text = getString(R.string.tv_stop)
                    voiceText(text)
                }
            }
        }
    }

    override fun voiceText(text: String?) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, ttsParams, "")
    }

    override fun stop() {
        if (::tts.isInitialized) {
            tts.stopWithChecking()
        }
    }

    private fun TextToSpeech.stopWithChecking() {
        if (tts.isSpeaking) tts.stop()
    }

    override fun onInit(status: Int) {
        var isInitSuccess = false
        if (status == TextToSpeech.SUCCESS) {
            if (tts.isLanguageAvailable(Locale(Locale.getDefault().language))
                == TextToSpeech.LANG_AVAILABLE
            ) {
                tts.language = Locale(Locale.getDefault().language)
            } else {
                tts.language = Locale.US
            }
            tts.setPitch(1.3f)
            tts.setSpeechRate(0.9f)
            isInitSuccess = true
        } else if (status == TextToSpeech.ERROR) {
        }
        showToast("TTS init: $isInitSuccess\n Status: $status")
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::tts.isInitialized) {
            tts.stopWithChecking()
            tts.shutdown()
        }
    }

    private inner class PopupUtteranceProgressListener(private val viewBinding: PopupTextToSpeechBinding) :
        UtteranceProgressListener() {

        override fun onStart(utteranceId: String?) {
            viewBinding.mbTextToSpeech.icon =
                getDrawableCompat(R.drawable.ic_baseline_stop_24)
        }

        override fun onDone(utteranceId: String?) {
            viewBinding.mbTextToSpeech.icon =
                getDrawableCompat(R.drawable.ic_baseline_play_arrow_24)
            viewBinding.mbTextToSpeech.text = getString(R.string.tv_play)
        }

        override fun onError(utteranceId: String?) {
            viewBinding.mbTextToSpeech.icon =
                getDrawableCompat(R.drawable.ic_baseline_play_arrow_24)
            viewBinding.mbTextToSpeech.text = getString(R.string.tv_play)
        }
    }

    private inner class GlobalUtteranceProgressListener : UtteranceProgressListener() {

        override fun onStart(utteranceId: String?) {
            binding.fabVoice.setImageDrawable(
                ContextCompat.getDrawable(
                    baseContext,
                    R.drawable.ic_baseline_stop_24
                )
            )
        }

        override fun onDone(utteranceId: String?) {
            binding.fabVoice.setImageDrawable(
                ContextCompat.getDrawable(
                    baseContext,
                    R.drawable.ic_baseline_play_arrow_24
                )
            )
        }

        override fun onError(utteranceId: String?) {
            binding.fabVoice.setImageDrawable(
                ContextCompat.getDrawable(
                    baseContext,
                    R.drawable.ic_baseline_play_arrow_24
                )
            )
        }
    }

    private companion object {
        private const val TAG = "CaptureActivity"
    }
}
