package developer.alexangan.ru.rewindapp.ui.customer

import android.app.Application
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
import developer.alexangan.ru.rewindapp.data.repository.CustomerRepository
import developer.alexangan.ru.rewindapp.databinding.FragmentAltuofiancoInvoicesBinding
import developer.alexangan.ru.rewindapp.ktx.launchWithHttpException
import developer.alexangan.ru.rewindapp.ktx.toGeneralApiResponse
import developer.alexangan.ru.rewindapp.model.*
import developer.alexangan.ru.rewindapp.util.InjectorUtils
import developer.alexangan.ru.rewindapp.util.NavOptionsUtils
import timber.log.Timber

class AltuofiancoInvoicesFragment : Fragment() {
    private lateinit var binding: FragmentAltuofiancoInvoicesBinding

    private val navController by lazy {
        findNavController()
    }

    private val args: AltuofiancoInvoicesFragmentArgs by navArgs()

    val onTokenInvalid
        get() = (requireActivity() as MainActivity).onTokenInvalid

    private val token: () -> String
        get() = (requireActivity() as MainActivity).token

    private val viewModel: AltuofiancoInvoicesViewModel by viewModels {
        val repository = InjectorUtils.getCustomerRepository()
        AltuofiancoInvoicesViewModelFactory(
            requireActivity().application,
            token,
            args.idCustomer,
            repository,
            onTokenInvalid
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAltuofiancoInvoicesBinding.inflate(inflater, container, false)

        setupBackButton()
        setupRecyclerView()

        return binding.root
    }

    private fun showDetails(item: AltuofiancoInvoice) {
        val args = Bundle().apply {
            putString("idInvoice", item.id)
        }

        navController.navigate(
            R.id.altuofiancoInvoiceFragment,
            args,
            NavOptionsUtils.buildOpenCloseNavOptions()
        )
    }

    private fun setupRecyclerView() {
        binding.invoiceContainer.adapter = AltuofiancoInvoicesAdapter {
            showDetails(it)
        }.also { adapter ->
            viewModel.invoices.observe(viewLifecycleOwner, Observer {
                adapter.submitList(it)
            })
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            navController.popBackStack()
        }
    }
}

private class AltuofiancoInvoicesAdapter(private val onItemClick: (AltuofiancoInvoice) -> Unit) :
    ListAdapter<AltuofiancoInvoice, AltuofiancoInvoicesAdapter.AltuofiancoInvoiceViewHolder>(
        AltuofiancoInvoiceDiff
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AltuofiancoInvoiceViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        val binding: ViewDataBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.item_invoice,
            parent,
            false
        )

        return AltuofiancoInvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AltuofiancoInvoiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AltuofiancoInvoiceViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onItemClick.invoke(getItem(adapterPosition))
            }
        }

        fun bind(item: AltuofiancoInvoice) {
            binding.setVariable(BR.item, item)
            binding.executePendingBindings()
        }
    }
}

private class AltuofiancoInvoicesViewModel(
    application: Application,
    private val token: () -> String,
    private val idCustomer: Int,
    private val repository: CustomerRepository,
    private val onTokenInvalid: () -> Unit,
) : AndroidViewModel(application) {

    val app
        get() = getApplication<Application>()

    val invoices = MutableLiveData<List<AltuofiancoInvoice>>()

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
            invoices.value = repository.getAltuofiancoInvoices(token(), idCustomer).sortedWith(
                compareByDescending {it.date})
        }
    }
}

private class AltuofiancoInvoicesViewModelFactory(
    private val application: Application,
    private val token: () -> String,
    private val idCustomer: Int,
    private val repository: CustomerRepository,
    private val onTokenInvalid: () -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AltuofiancoInvoicesViewModel(
            application,
            token,
            idCustomer,
            repository,
            onTokenInvalid
        ) as T
    }
}