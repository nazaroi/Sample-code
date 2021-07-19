package developer.alexangan.ru.rewindapp.data.repository

import developer.alexangan.ru.rewindapp.constants.TESTING_EMAIL
import developer.alexangan.ru.rewindapp.data.api.TopCareApi
import developer.alexangan.ru.rewindapp.model.ApiResultResponse
import developer.alexangan.ru.rewindapp.model.TopCare
import timber.log.Timber

class TopCareRepository private constructor(private val api: TopCareApi) {

    suspend fun getTopCare(vatNumber: String): TopCare {

        buildString {
            append("vatNumber: $vatNumber")
        }.also { msg ->
            Timber.i(msg)
        }

        return api.getTopCare(vatNumber).also {
            Timber.i(it.toString())
        }
    }

    suspend fun setCustomerTopCare(
        vatNumber: String,
        validationToken: String,
        referentName: String = "",
        referentEmail: String = "",
        referentPhone: String = "",
        landlineProblem: String = "",
        landlinePhones: String = "",
        landlineMessage: String = "",
        internetProblem: String = "",
        internetMessage: String = "",
        mobileProblem: String = "",
        mobilePhones: String = "",
        mobileMessage: String = "",
        testingEmail: String = TESTING_EMAIL,
    ): ApiResultResponse {

        buildString {
            append("vatNumber: $vatNumber, ")
            append("validationToken: $validationToken, ")
            append("referentName: $referentName, ")
            append("referentEmail: $referentEmail, ")
            append("referentPhone: $referentPhone, ")
            append("landlineProblem: $landlineProblem, ")
            append("landlinePhones: $landlinePhones, ")
            append("landlineMessage: $landlineMessage, ")
            append("internetProblem: $internetProblem, ")
            append("internetMessage: $internetMessage, ")
            append("mobileProblem: $mobileProblem, ")
            append("mobilePhones: $mobilePhones, ")
            append("mobileMessage: $mobileMessage, ")
            append("testingEmail: $testingEmail")
        }.also { msg ->
            Timber.i(msg)
        }

        return api.setCustomerTopCare(
            vatNumber,
            validationToken,
            referentName,
            referentEmail,
            referentPhone,
            landlineProblem,
            landlinePhones,
            landlineMessage,
            internetProblem,
            internetMessage,
            mobileProblem,
            mobilePhones,
            mobileMessage,
            testingEmail
        ).also {
            Timber.i(it.toString())
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TopCareRepository? = null

        fun getInstance(api: TopCareApi): TopCareRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: TopCareRepository(
                api
            ).also { INSTANCE = it }
        }
    }
}