package developer.alexangan.ru.rewindapp.model

import com.google.gson.annotations.SerializedName

data class TopCare(
    @SerializedName("text") val text: String,
    @SerializedName("result") val result: Int,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("validationToken") val validationToken: String,
)