package com.mycelium.wallet.activity.main.address


import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.mycelium.wallet.MbwManager
import com.mycelium.wallet.R
import com.mycelium.wallet.WalletApplication
import com.mycelium.wallet.databinding.AddressFragmentBindingImpl
import com.mycelium.wallet.databinding.AddressFragmentBtcBindingImpl
import com.mycelium.wapi.wallet.btc.AbstractBtcAccount
import com.mycelium.wapi.wallet.WalletAccount
import kotlinx.android.synthetic.main.address_fragment_label.*
import kotlinx.android.synthetic.main.address_fragment_qr.*

class AddressFragment : Fragment() {
    private val mbwManager = MbwManager.getInstance(WalletApplication.getInstance())
    private lateinit var viewModel: AddressFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        val viewModelProvider = ViewModelProviders.of(this)
        this.viewModel = viewModelProvider.get(
                if (accountSupportsMultipleBtcReceiveAddresses(mbwManager.selectedAccount)) {
                    AddressFragmentBtcModel::class.java
                } else {
                    AddressFragmentCoinsModel::class.java
                })
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding =
                if (accountSupportsMultipleBtcReceiveAddresses(mbwManager.selectedAccount)) {
                    DataBindingUtil.inflate<AddressFragmentBtcBindingImpl>(inflater, R.layout.address_fragment_btc,
                            container, false).also {
                        it.activity = activity
                        it.viewModel = viewModel as AddressFragmentBtcModel
                    }
                } else {
                    DataBindingUtil.inflate<AddressFragmentBindingImpl>(inflater, R.layout.address_fragment,
                            container, false).also {
                        it.activity = activity
                        it.viewModel = viewModel as AddressFragmentCoinsModel
                    }
                }
        binding.lifecycleOwner = this
        return binding.root
    }

    private fun accountSupportsMultipleBtcReceiveAddresses(account: WalletAccount<*>): Boolean =
            account is AbstractBtcAccount && account.availableAddressTypes.size > 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInitialized()) {
            viewModel.init()
        }

        ivQR.tapToCycleBrightness = false
        ivQR.qrCode = viewModel.getAddressString()

        val drawableForAccount = viewModel.getDrawableForAccount(resources)
        if (drawableForAccount != null) {
            ivAccountType.setImageDrawable(drawableForAccount)
        }
        viewModel.getAccountAddress().observe(this, Observer { newAddress ->
            if (newAddress != null) {
                ivQR.qrCode = viewModel.getAddressString()
            }
        })
    }
}
