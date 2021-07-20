package developer.alexangan.ru.rewindapp.model

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Altuofianco(
    @SerializedName("date_contract") val dateContract: String,
    @SerializedName("contract_type") val contractType: Float,
    @SerializedName("contract_code") val contractCode: String,
    @SerializedName("mail_ecommerce") val mailEcommerce: String,
    @SerializedName("points_collected") val pointsCollected: Int?,
    @SerializedName("points_used") val pointsUsed: Int?,
    @SerializedName("windecare_user") val windecareUser: String?,
    @SerializedName("windecare_password") val windecarePassword: String?,
    @SerializedName("points_availabe") val pointsAvailable: Int,
    @SerializedName("invoicesCount") val invoicesCount: Int,
    @SerializedName("pointsDetails") val pointsDetails: String?,
    @SerializedName("documentsCount") val documentsCount: Int,
    @SerializedName("campaign") val generateForms: Int,

    ) : Parcelable {
    companion object {
        const val ALTUOFIANCO_DATE_CONTRACT_PATTERN = "yyyy-MM-dd"
    }
}

object AltuofiancoDiff : DiffUtil.ItemCallback<Altuofianco>() {
    override fun areItemsTheSame(
        oldItem: Altuofianco,
        newItem: Altuofianco
    ): Boolean = oldItem.contractCode == newItem.contractCode

    override fun areContentsTheSame(
        oldItem: Altuofianco,
        newItem: Altuofianco
    ): Boolean = oldItem == newItem
}