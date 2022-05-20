package piuk.blockchain.android.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import com.blockchain.commonarch.presentation.base.HostedBottomSheet
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.button.MinimalButton
import com.blockchain.componentlib.button.PrimaryButton
import com.blockchain.componentlib.sheets.BottomSheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import piuk.blockchain.android.R

class BottomSheetInformation : BottomSheetDialogFragment() {

    interface Host : HostedBottomSheet.Host {
        fun primaryButtonClicked()
        fun secondButtonClicked()
    }

    val host: Host by lazy {
        parentFragment as? Host ?: throw IllegalStateException(
            "Host fragment is not a BottomSheetInformation.Host"
        )
    }

    private val title by lazy { arguments?.getString(TITLE).orEmpty() }
    private val description by lazy { arguments?.getString(DESCRIPTION).orEmpty() }
    private val primaryCtaText by lazy { arguments?.getString(CTA_TEXT_PRIMARY).orEmpty() }
    private val secondaryCtaText by lazy { arguments?.getString(CTA_TEXT_SECONDARY) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireActivity())

        dialog.setContentView(
            ComposeView(requireContext()).apply {
                setContent {
                    BottomSheet(
                        title = title,
                        subtitle = description,
                        shouldShowHeaderDivider = false,
                        onCloseClick = { dismiss() },
                        imageResource = ImageResource.Local(R.drawable.ic_phone),
                        topButton = {
                            PrimaryButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = dimensionResource(R.dimen.standard_margin),
                                        end = dimensionResource(R.dimen.standard_margin)
                                    ),
                                text = primaryCtaText,
                                onClick = {
                                    host.primaryButtonClicked()
                                    super.dismiss()
                                }
                            )
                        },
                        bottomButton = {
                            secondaryCtaText?.let {
                                MinimalButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = dimensionResource(R.dimen.standard_margin),
                                            end = dimensionResource(R.dimen.standard_margin)
                                        ),
                                    text = it,
                                    onClick = {
                                        host.secondButtonClicked()
                                        super.dismiss()
                                    }
                                )
                            }
                        }
                    )
                }
            }
        )

        dialog.setOnShowListener {
            val d = it as BottomSheetDialog
            val layout =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from(layout).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    companion object {
        private const val TITLE = "title"
        private const val DESCRIPTION = "description"
        private const val CTA_TEXT_PRIMARY = "primary_cta_text"
        private const val CTA_TEXT_SECONDARY = "secondary_cta_text"

        fun newInstance(
            title: String,
            description: String,
            ctaPrimaryText: String,
            ctaSecondaryText: String? = null,
        ): BottomSheetInformation {
            return BottomSheetInformation().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                    putString(DESCRIPTION, description)
                    putString(CTA_TEXT_PRIMARY, ctaPrimaryText)
                    putString(CTA_TEXT_SECONDARY, ctaSecondaryText)
                }
            }
        }
    }
}
