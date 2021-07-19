package developer.alexangan.ru.rewindapp.ui.customer

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import developer.alexangan.ru.rewindapp.MainActivity
import developer.alexangan.ru.rewindapp.data.repository.CustomerRepository
import developer.alexangan.ru.rewindapp.databinding.FragmentAltuofiancoInvoiceBinding
import developer.alexangan.ru.rewindapp.ktx.launchWithHttpException
import developer.alexangan.ru.rewindapp.ktx.toGeneralApiResponse
import developer.alexangan.ru.rewindapp.model.AltuofiancoInvoiceDetails
import developer.alexangan.ru.rewindapp.model.GeneralApiResponse
import developer.alexangan.ru.rewindapp.util.InjectorUtils
import timber.log.Timber

class AltuofiancoInvoiceFragment : Fragment() {
    private lateinit var binding: FragmentAltuofiancoInvoiceBinding

    private val navController by lazy {
        findNavController()
    }

    private val args: AltuofiancoInvoiceFragmentArgs by navArgs()

    val onTokenInvalid
        get() = (requireActivity() as MainActivity).onTokenInvalid

    private val token: () -> String
        get() = (requireActivity() as MainActivity).token

    private val viewModel: AltuofiancoInvoiceViewModel by viewModels {
        val repository = InjectorUtils.getCustomerRepository()
        AltuofiancoInvoiceViewModelFactory(
            requireActivity().application,
            token,
            args.idInvoice,
            repository,
            onTokenInvalid
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAltuofiancoInvoiceBinding.inflate(inflater, container, false)

        setupBackButton()

        viewModel.invoice.observe(viewLifecycleOwner, Observer { item ->
            binding.item = item
        })

        return binding.root
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }
    }
}

private class AltuofiancoInvoiceViewModel(
    application: Application,
    private val token: () -> String,
    private val idInvoice: String,
    private val repository: CustomerRepository,
    private val onTokenInvalid: () -> Unit,
) : AndroidViewModel(application) {

    val app
        get() = getApplication<Application>()

    val invoice = MutableLiveData<AltuofiancoInvoiceDetails>()

    init {
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
            invoice.value = repository.getAltuofiancoInvoiceDetails(token(), idInvoice)[0]
        }
    }
}

private class AltuofiancoInvoiceViewModelFactory(
    private val application: Application,
    private val token: () -> String,
    private val idInvoice: String,
    private val repository: CustomerRepository,
    private val onTokenInvalid: () -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AltuofiancoInvoiceViewModel(
            application,
            token,
            idInvoice,
            repository,
            onTokenInvalid
        ) as T
    }
}

