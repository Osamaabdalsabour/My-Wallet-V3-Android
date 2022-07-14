package info.blockchain.wallet.payload.data

import info.blockchain.wallet.bip44.HDAccount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
This class is used for iOS and Web only.
 */
@Serializable
data class AddressCache(
    @SerialName("receiveAccount")
    val receiveAccount: String,
    @SerialName("changeAccount")
    val changeAccount: String
) {
    companion object {
        fun setCachedXPubs(account: HDAccount): AddressCache {
            return AddressCache(
                receiveAccount = account.receive.xpub,
                changeAccount = account.change.xpub
            )
        }
    }
}
