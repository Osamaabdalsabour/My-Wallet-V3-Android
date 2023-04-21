package com.blockchain.transactions.swap.selectsource

import com.blockchain.commonarch.presentation.mvi_v2.ModelState
import com.blockchain.data.DataResource
import com.blockchain.transactions.common.WithId
import com.blockchain.transactions.swap.CryptoAccountWithBalance

data class SelectSourceModelState(
    val accountListData: DataResource<List<WithId<CryptoAccountWithBalance>>> = DataResource.Loading
) : ModelState
