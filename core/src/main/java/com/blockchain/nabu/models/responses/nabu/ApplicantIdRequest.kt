package com.blockchain.nabu.models.responses.nabu

import kotlinx.serialization.Serializable

@Serializable
internal data class ApplicantIdRequest(
    val applicantId: String
)
