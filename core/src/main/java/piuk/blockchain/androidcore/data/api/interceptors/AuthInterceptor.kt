package piuk.blockchain.androidcore.data.api.interceptors

import com.blockchain.api.NabuErrorStatusCodes
import com.blockchain.featureflag.FeatureFlag
import com.blockchain.nabu.NabuToken
import com.blockchain.nabu.datamanagers.NabuDataManager
import com.blockchain.network.interceptor.AuthenticateWithOfflineToken
import com.blockchain.network.interceptor.AuthenticationNotRequired
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.closeQuietly
import retrofit2.Invocation
import timber.log.Timber

class AuthInterceptor(
    private val nabuToken: Lazy<NabuToken>,
    private val nabuDataManager: Lazy<NabuDataManager>,
    private val authInterceptorFeatureFlag: FeatureFlag,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (!authInterceptorFeatureFlag.enabled.blockingGet()) {
            return chain.proceed(originalRequest)
        }

        val nabuToken = nabuToken.value
        val nabuDataManager = nabuDataManager.value

        val requestAnnotations = originalRequest.tag(Invocation::class.java)?.method()?.annotations.orEmpty()

        if (requestAnnotations.any { it is AuthenticationNotRequired }) {
            if (originalRequest.header("authorization") != null) {
                val url = originalRequest.url
                Timber.w("authorization header stripped on AuthenticationNotRequired call url: $url")
            }

            val request = originalRequest.newBuilder()
                .removeHeader("authorization")
                .build()
            return chain.proceed(request)
        } else if (requestAnnotations.any { it is AuthenticateWithOfflineToken }) {
            val offlineToken = nabuToken.fetchNabuToken().blockingGet()
            val request = originalRequest.newBuilder()
                .header("authorization", "Bearer ${offlineToken.token}")
                .build()
            return chain.proceed(request)
        }

        val offlineToken = try {
            nabuToken.fetchNabuToken().blockingGet()
        } catch (ex: Exception) {
            Timber.e("fetchNabuToken failed ${originalRequest.url} $ex")
            return chain.proceed(originalRequest)
        }

        val sessionToken = try {
            nabuDataManager.currentToken(offlineToken).blockingGet()
        } catch (ex: Exception) {
            Timber.e("currentToken failed ${originalRequest.url} $ex")
            return chain.proceed(originalRequest)
        }

        val request = originalRequest.newBuilder()
            .header("authorization", sessionToken.authHeader)
            .build()

        val response = chain.proceed(request)
        return if (response.code == NabuErrorStatusCodes.TokenExpired.code) {
            response.body?.closeQuietly()
            nabuDataManager.clearAccessToken()

            val sessionToken = nabuDataManager.refreshToken(offlineToken).blockingGet()
            val newRequest = request
                .newBuilder()
                .header("authorization", sessionToken.authHeader)
                .build()
            chain.proceed(newRequest)
        } else {
            response
        }
    }
}
