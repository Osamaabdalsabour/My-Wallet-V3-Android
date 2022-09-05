package com.blockchain.addressverification.ui

import androidx.compose.ui.text.input.TextFieldValue
import com.blockchain.addressverification.domain.model.AutocompleteAddress
import com.blockchain.commonarch.presentation.mvi_v2.Intent

sealed class AddressVerificationIntent : Intent<AddressVerificationModelState> {

    data class MainLineInputChanged(val newInput: TextFieldValue) : AddressVerificationIntent() {
        override fun isValidFor(modelState: AddressVerificationModelState): Boolean =
            modelState.mainLineInput != newInput
    }

    data class ResultClicked(val result: AutocompleteAddress) : AddressVerificationIntent()

    data class ErrorWhileSaving(val error: AddressVerificationSavingError) : AddressVerificationIntent()

    object ManualOverrideClicked : AddressVerificationIntent()

    object ErrorHandled : AddressVerificationIntent()

    data class SecondLineInputChanged(val newInput: String) : AddressVerificationIntent()

    data class CityInputChanged(val newInput: String) : AddressVerificationIntent()

    data class PostCodeInputChanged(val newInput: String) : AddressVerificationIntent()

    object SaveClicked : AddressVerificationIntent()
}
