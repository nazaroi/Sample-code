package developer.alexangan.ru.rewindapp.ui.customer

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import developer.alexangan.ru.rewindapp.BR
import developer.alexangan.ru.rewindapp.MainActivity
import developer.alexangan.ru.rewindapp.R
import developer.alexangan.ru.rewindapp.constants.PDF_MIME_TYPE
import developer.alexangan.ru.rewindapp.data.repository.CustomerRepository
import developer.alexangan.ru.rewindapp.databinding.FragmentCustomerDocumentsBinding
import developer.alexangan.ru.rewindapp.ktx.launchWithHttpException
import developer.alexangan.ru.rewindapp.ktx.toGeneralApiResponse
import developer.alexangan.ru.rewindapp.ktx.toast
import developer.alexangan.ru.rewindapp.model.CustomerDocument
import developer.alexangan.ru.rewindapp.model.CustomerDocumentDiff
import developer.alexangan.ru.rewindapp.model.GeneralApiResponse
import developer.alexangan.ru.rewindapp.util.FileUtils
import developer.alexangan.ru.rewindapp.util.InjectorUtils
import timber.log.Timber


class CustomerDocumentsFragment : Fragment() {

    private lateinit var binding: FragmentCustomerDocumentsBinding

    private val onTokenInvalid
        get() = (requireActivity() as MainActivity).onTokenInvalid

    private val args: CustomerDocumentsFragmentArgs by navArgs()

    private val navController by lazy {
        findNavController()
    }

    private val onNoResult = { gar: GeneralApiResponse ->
        binding.layoutNoResultFound.message = gar.text
    }

    private val viewModel: CustomerDocumentsViewModel by viewModels {
        val repository = InjectorUtils.getCustomerRepository()
        CustomerDocumentsViewModelFactory(
            requireActivity().application,
            token,
            args.idCustomer,
            repository,
            onNoResult,
            onTokenInvalid
        )
    }

    private val token: () -> String
        get() = (requireActivity() as MainActivity).token

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomerDocumentsBinding.inflate(inflater, container, false)

        binding.recyclerView.adapter = CustomerDocumentAdapter { item ->
            downloadOrShow(item)
        }.also { adapter ->
            viewModel.documents.observe(viewLifecycleOwner, Observer { items ->

                binding.noResult = items.isEmpty()

                if (items.isNotEmpty()) {
                    adapter.submitList(items.reversed())
                }
            })
        }

        binding.backButton.setOnClickListener {
            navController.navigateUp()
        }

        return binding.root
    }

    private fun downloadOrShow(item: CustomerDocument) {
        val mimeType =
            FileUtils.getMimeType(requireActivity().application, Uri.parse(item.link))

        val intent = Intent(Intent.ACTION_VIEW)

        if (mimeType == PDF_MIME_TYPE) {
            intent.setDataAndType(Uri.parse(item.link), PDF_MIME_TYPE)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        } else {
            intent.data = Uri.parse(item.link)
        }

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            context?.startActivity(intent)
        }
    }
}

private class CustomerDocumentAdapter(private val onItemClick: (CustomerDocument) -> Unit) :
    ListAdapter<CustomerDocument, CustomerDocumentAdapter.CustomerDocumentViewHolder>(
        CustomerDocumentDiff
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomerDocumentViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        val binding: ViewDataBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_customer_document,
            parent,
            false
        )

        return CustomerDocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerDocumentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CustomerDocumentViewHolder(
        private val binding: ViewDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onItemClick.invoke(getItem(adapterPosition))
            }
        }

        fun bind(item: CustomerDocument) {
            binding.setVariable(BR.item, item)
            binding.executePendingBindings()
        }
    }
}

private class CustomerDocumentsViewModel(
    application: Application,
    private val token: () -> String,
    private val idCustomer: Int,
    private val repository: CustomerRepository,
    private val onNoResult: (GeneralApiResponse) -> Unit,
    private val onTokenInvalid: () -> Unit,
) : AndroidViewModel(application) {

    val app
        get() = getApplication<Application>()

    val documents = MutableLiveData<List<CustomerDocument>>()

    init {
        viewModelScope.launchWithHttpException(exception = { he ->

            val gar = he.toGeneralApiResponse()

            Timber.i(gar.toString())

            when (GeneralApiResponse.Type.valueOfType(gar.type)) {
                GeneralApiResponse.Type.ERROR -> {
                    onTokenInvalid()
                }
                GeneralApiResponse.Type.WARNING -> {
                    documents.value = listOf()
                    app.toast(gar.text)
                    onNoResult(gar)
                }
                else -> {
                    throw he
                }
            }
        }) {
            documents.value = repository.getCustomerDocuments(token(), idCustomer)
        }
    }
}

private class CustomerDocumentsViewModelFactory(
    private val application: Application,
    private val token: () -> String,
    private val idCustomer: Int,
    private val repository: CustomerRepository,
    private val onNoResult: (GeneralApiResponse) -> Unit,
    private val onTokenInvalid: () -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CustomerDocumentsViewModel(
            application,
            token,
            idCustomer,
            repository,
            onNoResult,
            onTokenInvalid
        ) as T
    }
}