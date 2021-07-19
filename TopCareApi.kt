package developer.alexangan.ru.rewindapp.data.api

import developer.alexangan.ru.rewindapp.model.ApiResultResponse
import developer.alexangan.ru.rewindapp.model.TopCare
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TopCareApi {

    @FormUrlEncoded
    @POST("topcare")
    suspend fun getTopCare(
        @Field("partita_iva") vatNumber: String
    ): TopCare

    @FormUrlEncoded
    @POST("topcare/setCustomerTopCare")
    suspend fun setCustomerTopCare(
        @Field("partita_iva") vatNumber: String,
        @Field("validationToken") validationToken: String,
        @Field("referent_name") referentName: String,
        @Field("referent_email") referentEmail: String,
        @Field("referent_phone") referentPhone: String,
        @Field("landline_problem") landlineProblem: String,
        @Field("landline_phones") landlinePhones: String,
        @Field("landline_message") landlineMessage: String,
        @Field("internet_problem") internetProblem: String,
        @Field("internet_message") internetMessage: String,
        @Field("mobile_problem") mobileProblem: String,
        @Field("mobile_phones") mobilePhones: String,
        @Field("mobile_message") mobileMessage: String,
        @Field("testing_email") testingEmail: String?
    ): ApiResultResponse
}