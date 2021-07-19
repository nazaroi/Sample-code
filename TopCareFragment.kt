package developer.alexangan.ru.rewindapp.ui.customer

import android.animation.LayoutTransition
import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CompoundButton
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import developer.alexangan.ru.rewindapp.MainActivity
import developer.alexangan.ru.rewindapp.R
import developer.alexangan.ru.rewindapp.constants.DEV_MODE
import developer.alexangan.ru.rewindapp.constants.TESTING_EMAIL
import developer.alexangan.ru.rewindapp.data.repository.CustomerRepository
import developer.alexangan.ru.rewindapp.databinding.DialogErrorMessageBinding
import developer.alexangan.ru.rewindapp.databinding.FragmentTopCareBinding
import developer.alexangan.ru.rewindapp.ktx.launchWithHttpException
import developer.alexangan.ru.rewindapp.ktx.toGeneralApiResponse
import developer.alexangan.ru.rewindapp.ktx.toast
import developer.alexangan.ru.rewindapp.model.GeneralApiResponse
import developer.alexangan.ru.rewindapp.util.InjectorUtils
import developer.alexangan.ru.rewindapp.util.hideKeyboard
import developer.alexangan.ru.rewindapp.util.isEmailValid
import timber.log.Timber

class TopCareFragment : Fragment() {

    private lateinit var binding: FragmentTopCareBinding

    private val args: TopCareFragmentArgs by navArgs()

    private val onTokenInvalid
        get() = (requireActivity() as MainActivity).onTokenInvalid

    private val navController by lazy {
        findNavController()
    }

    private val isTablet
        get() = resources.getBoolean(R.bool.is_tablet)

    private val token: () -> String
        get() = (requireActivity() as MainActivity).token

    private val viewModel: TopCareViewModel by viewModels {
        val repository = InjectorUtils.getCustomerRepository()
        TopCareViewModelFactory(
            requireActivity().application,
            token,
            args.customerDetails.idCustomer,
            repository,
            onTokenInvalid
        )
    }

    private val referentInputText: String
        get() = binding.referentInputLayout.editText?.text?.trim().toString()

    private val mobileInputText: String
        get() = binding.mobileInputLayout.editText?.text?.trim().toString()

    private val mailInputText: String
        get() = binding.mailInputLayout.editText?.text?.trim().toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTopCareBinding.inflate(inflater, container, false)

        setupBackButton()
        setupLayoutTransitions()

        setupDetailSection()

        setupLandlineSection()
        setupLandlineProblemAutoCompleteTextView()

        setupInternetSection()
        setupInternetProblemAutoCompleteTextView()

        setupMobileSection()
        setupMobileProblemAutoCompleteTextView()

        setupSendButton()

        return binding.root
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }
    }

    private fun setupLayoutTransitions() {
        // without that animation doesn't work
        val layoutTransition = binding.animationLayout.layoutTransition
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun setupDetailSection() {
        binding.nameInputLayout.editText?.setText(args.customerDetails.name)
        binding.vatNumberInputLayout.editText?.setText(args.customerDetails.vatNumber)
        binding.referentInputLayout.editText?.setText(args.customerDetails.referent)
        binding.mobileInputLayout.editText?.setText(args.customerDetails.mobile)
        binding.mailInputLayout.editText?.setText(args.customerDetails.mail)
    }

    private fun setupLandlineSection() {
        binding.isLandlineSectionCollapsed = true

        binding.landlineCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { view, isChecked ->
            hideKeyboard(view)
            binding.isLandlineSectionCollapsed = !isChecked
        })
    }

    private fun setupInternetSection() {
        binding.isInternetSectionCollapsed = true

        binding.internetCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { view, isChecked ->
            hideKeyboard(view)
            binding.isInternetSectionCollapsed = !isChecked
        })
    }

    private fun setupMobileSection() {
        binding.isMobileSectionCollapsed = true

        binding.mobileCheckBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { view, isChecked ->
            hideKeyboard(view)
            binding.isMobileSectionCollapsed = !isChecked
        })
    }

    private val landlineProblemAutoCompleteTextView: AutoCompleteTextView
        get() = binding.landlineProblemTextInputLayout.editText as AutoCompleteTextView

    private lateinit var currLandlineProblem: LandlineProblem

    private val landlinePhonesInputText: String
        get() = binding.landlinePhonesTextInputLayout.editText?.text?.trim().toString()

    private val landlineMessageInputText: String
        get() = binding.landlineMessageTextInputLayout.editText?.text?.trim().toString()

    private fun setupLandlineProblemAutoCompleteTextView() {

        val items = LandlineProblem.values()

        ArrayAdapter(
            requireContext(),
            R.layout.item_blue_text_auto_complete,
            items.map { getString(it.problem).capitalize() }
        ).also { adapter ->
            landlineProblemAutoCompleteTextView.setAdapter(adapter)
        }

        landlineProblemAutoCompleteTextView.setText(
            getString(R.string.choose_one).capitalize(),
            false
        )

        landlineProblemAutoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                currLandlineProblem = LandlineProblem.of(items[position].problem)
            }
    }

    private val internetProblemAutoCompleteTextView: AutoCompleteTextView
        get() = binding.internetProblemTextInputLayout.editText as AutoCompleteTextView

    private lateinit var currInternetProblem: InternetProblem

    private val internetMessageInputText: String
        get() = binding.internetMessageTextInputLayout.editText?.text?.trim().toString()

    private fun setupInternetProblemAutoCompleteTextView() {

        val items = InternetProblem.values()

        ArrayAdapter(
            requireContext(),
            R.layout.item_blue_text_auto_complete,
            items.map { getString(it.problem).capitalize() }
        ).also { adapter ->
            internetProblemAutoCompleteTextView.setAdapter(adapter)
        }

        internetProblemAutoCompleteTextView.setText(
            getString(R.string.choose_one).capitalize(),
            false
        )

        internetProblemAutoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                currInternetProblem = InternetProblem.of(items[position].problem)
            }
    }

    private val mobileProblemAutoCompleteTextView: AutoCompleteTextView
        get() = binding.mobileProblemTextInputLayout.editText as AutoCompleteTextView

    private lateinit var currMobileProblem: MobileProblem

    private val mobilePhonesInputText: String
        get() = binding.mobilePhonesTextInputLayout.editText?.text?.trim().toString()

    private val mobileMessageInputText: String
        get() = binding.mobileMessageTextInputLayout.editText?.text?.trim().toString()

    private fun setupMobileProblemAutoCompleteTextView() {

        val items = MobileProblem.values()

        ArrayAdapter(
            requireContext(),
            R.layout.item_blue_text_auto_complete,
            items.map { getString(it.problem).capitalize() }
        ).also { adapter ->
            mobileProblemAutoCompleteTextView.setAdapter(adapter)
        }

        mobileProblemAutoCompleteTextView.setText(
            getString(R.string.choose_one).capitalize(),
            false
        )

        mobileProblemAutoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                currMobileProblem = MobileProblem.of(items[position].problem)
            }
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener { button ->

            button.isEnabled = false

            if (mailInputText.isNotEmpty() && !isEmailValid(mailInputText)) {
                val message =
                    getString(R.string.top_care_not_valid_referent_email_error).capitalize()
                showErrorDialog(message)
                button.isEnabled = true
                return@setOnClickListener
            }

            if (!binding.landlineCheckBox.isChecked &&
                !binding.internetCheckBox.isChecked &&
                !binding.mobileCheckBox.isChecked
            ) {
                val message =
                    getString(R.string.you_must_indicate_at_least_one_type_of_disservice).capitalize()
                showErrorDialog(message)
                button.isEnabled = true
                return@setOnClickListener
            }

            if (binding.landlineCheckBox.isChecked) {
                if (!::currLandlineProblem.isInitialized) {
                    val message =
                        getString(R.string.top_care_landline_problem_not_selected_error).capitalize()
                    showErrorDialog(message)
                    button.isEnabled = true
                    return@setOnClickListener
                }
            }

            if (binding.internetCheckBox.isChecked) {
                if (!::currInternetProblem.isInitialized) {
                    val message =
                        getString(R.string.top_care_internet_problem_not_selected_error).capitalize()
                    showErrorDialog(message)
                    button.isEnabled = true
                    return@setOnClickListener
                }
            }

            if (binding.mobileCheckBox.isChecked) {
                if (!::currMobileProblem.isInitialized) {
                    val message =
                        getString(R.string.top_care_mobile_problem_not_selected_error).capitalize()
                    showErrorDialog(message)
                    button.isEnabled = true
                    return@setOnClickListener
                }
            }

            viewModel.setCustomerAssurance(
                referentName = referentInputText,
                referentEmail = mailInputText,
                referentPhone = mobileInputText,
                landlineProblem =
                if (binding.landlineCheckBox.isChecked) getString(currLandlineProblem.problem) else "",
                landlinePhones = if (binding.landlineCheckBox.isChecked) landlinePhonesInputText else "",
                landlineMessage = if (binding.landlineCheckBox.isChecked) landlineMessageInputText else "",
                internetProblem =
                if (binding.internetCheckBox.isChecked) getString(currInternetProblem.problem) else "",
                internetMessage = if (binding.internetCheckBox.isChecked) internetMessageInputText else "",
                mobileProblem =
                if (binding.mobileCheckBox.isChecked) getString(currMobileProblem.problem) else "",
                mobilePhones = if (binding.mobileCheckBox.isChecked) mobilePhonesInputText else "",
                mobileMessage = if (binding.mobileCheckBox.isChecked) mobileMessageInputText else "",
            ) {
                context?.toast(getString(R.string.top_care_request_sent_successfully))
                hideKeyboard(button)
                navController.navigateUp()
            }
        }
    }

    private var errorDialog: AlertDialog? = null

    private fun showErrorDialog(message: String) {

        if (errorDialog != null) {
            return
        }

        val inflater = LayoutInflater.from(requireContext())

        val viewBinding = DialogErrorMessageBinding.inflate(inflater)
        viewBinding.message.text = message

        val inset = resources.getDimension(R.dimen.margin_16dp).toInt()

        errorDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(viewBinding.root)
            .setBackground(ResourcesCompat.getDrawable(resources, R.drawable.dialog_bg, null))
            .setBackgroundInsetTop(inset)
            .setBackgroundInsetBottom(inset)
            .setBackgroundInsetStart(inset)
            .setBackgroundInsetEnd(inset)
            .show().apply {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                viewBinding.button.setOnClickListener {
                    dismiss()
                }

                if (isTablet) {
                    window?.setLayout(
                        resources.getDimension(R.dimen.tablet_error_message_dialog_width).toInt(),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }

        errorDialog?.setOnDismissListener {
            errorDialog = null
        }
    }

    private enum class LandlineProblem(@StringRes val problem: Int) {
        NOT_WORKING(R.string.not_working),
        POOR_VOICE_QUALITY(R.string.poor_voice_quality),
        OTHER(R.string.other);

        companion object {
            @JvmStatic
            fun of(problem: Int): LandlineProblem {
                return when (problem) {
                    NOT_WORKING.problem -> NOT_WORKING
                    POOR_VOICE_QUALITY.problem -> POOR_VOICE_QUALITY
                    OTHER.problem -> OTHER
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    private enum class InternetProblem(@StringRes val problem: Int) {
        NOT_WORKING(R.string.not_working),
        OTHER(R.string.other);

        companion object {
            @JvmStatic
            fun of(problem: Int): InternetProblem {
                return when (problem) {
                    NOT_WORKING.problem -> NOT_WORKING
                    OTHER.problem -> OTHER
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    private enum class MobileProblem(@StringRes val problem: Int) {
        NOT_WORKING(R.string.not_working),
        POOR_VOICE_QUALITY(R.string.poor_voice_quality),
        NOT_WORKING_ABROAD(R.string.not_working_abroad),
        UNABLE_TO_NAVIGATE(R.string.unable_to_navigate),
        OTHER(R.string.other);

        companion object {
            @JvmStatic
            fun of(problem: Int): MobileProblem {
                return when (problem) {
                    NOT_WORKING.problem -> NOT_WORKING
                    POOR_VOICE_QUALITY.problem -> POOR_VOICE_QUALITY
                    NOT_WORKING_ABROAD.problem -> NOT_WORKING_ABROAD
                    UNABLE_TO_NAVIGATE.problem -> UNABLE_TO_NAVIGATE
                    OTHER.problem -> OTHER
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }
}

private class TopCareViewModel(
    application: Application,
    private val token: () -> String,
    private val idCustomer: Int,
    private val repository: CustomerRepository,
    private val onTokenInvalid: () -> Unit
) : AndroidViewModel(application) {

    val app
        get() = getApplication<Application>()

    fun setCustomerAssurance(
        referentName: String,
        referentEmail: String,
        referentPhone: String,
        landlineProblem: String,
        landlinePhones: String,
        landlineMessage: String,
        internetProblem: String,
        internetMessage: String,
        mobileProblem: String,
        mobilePhones: String,
        mobileMessage: String,
        onResponse: (GeneralApiResponse) -> Unit
    ) {
        viewModelScope.launchWithHttpException(exception = { he ->

            val gar = he.toGeneralApiResponse()

            Timber.i(gar.toString())

            when (GeneralApiResponse.Type.valueOfType(gar.type)) {
                GeneralApiResponse.Type.ERROR -> {
                    onTokenInvalid()
                }
                else -> {
                    throw he
                }
            }
        }) {
            repository.setCustomerAssurance(
                token(),
                idCustomer,
                problemDescription = "problemDescription",
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
                testingEmail = if (DEV_MODE) TESTING_EMAIL else null
            ).also(onResponse)
        }
    }
}

private class TopCareViewModelFactory(
    private val application: Application,
    private val token: () -> String,
    private val idCustomer: Int,
    private val repository: CustomerRepository,
    private val onTokenInvalid: () -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TopCareViewModel(
            application,
            token,
            idCustomer,
            repository,
            onTokenInvalid
        ) as T
    }
}