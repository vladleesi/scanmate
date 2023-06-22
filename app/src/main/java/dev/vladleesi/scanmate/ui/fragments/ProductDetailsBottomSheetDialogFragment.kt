package dev.vladleesi.scanmate.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.vladleesi.scanmate.Constants
import dev.vladleesi.scanmate.R
import dev.vladleesi.scanmate.data.remote.model.Product
import dev.vladleesi.scanmate.databinding.DialogProductDetailsBinding
import dev.vladleesi.scanmate.extensions.gone
import dev.vladleesi.scanmate.extensions.setByteArray
import dev.vladleesi.scanmate.ui.activity.Speecher

class ProductDetailsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: DialogProductDetailsBinding

    private val speecher by lazy { activity as? Speecher }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_product_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DialogProductDetailsBinding.bind(view)

        arguments?.getParcelable<Product>(Constants.PRODUCT)?.let { product ->
            binding.ivProductLogo.setByteArray(product.logo)
            binding.tvProductTitle.text = product.title

            val url = Uri.parse(product.url)
            if (url.scheme != null)
                binding.mbOpenProductStore.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, url))
                }
            else
                binding.mbOpenProductStore.gone()

            if (arguments?.getBoolean(Constants.VOICE_PRODUCT) == true)
                speecher?.voice(binding.tvProductTitle.text.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speecher?.stop()
    }

    companion object {
        const val TAG = "ProductDetailsBottomSheetDialogFragment"

        fun newInstance(product: Product?, voiceProduct: Boolean) =
            ProductDetailsBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Constants.PRODUCT, product)
                    putBoolean(Constants.VOICE_PRODUCT, voiceProduct)
                }
            }
    }
}
