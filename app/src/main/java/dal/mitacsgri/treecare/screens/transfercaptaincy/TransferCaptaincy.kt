package dal.mitacsgri.treecare.screens.transfercaptaincy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.createFragmentViewWithStyle
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.fragment_transfer_captaincy.view.*
import kotlinx.android.synthetic.main.fragment_transfer_captaincy.view.recyclerView
import kotlinx.android.synthetic.main.fragment_transfer_captaincy.view.toolbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransferCaptaincy : Fragment() {
    private val mViewModel: TransferCaptaincyViewModel by viewModel()
    val args: TransferCaptaincyArgs by navArgs()
    lateinit var adapter: TransferCaptaincyRecyclerViewAdapter
    private lateinit var usersrv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.createFragmentViewWithStyle(
            activity, R.layout.fragment_transfer_captaincy, R.style.tournament_mode_theme
        )
        view.apply {
            Log.d("Test", "TeamName " + args.TeamName)

            toolbar.setOnClickListener {
                findNavController().navigateUp()
            }

            mViewModel.getUsersForTeam(args.TeamName).observe(viewLifecycleOwner, Observer {
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    adapter = TransferCaptaincyRecyclerViewAdapter(it, mViewModel)
                }
                adapter = recyclerView.adapter as TransferCaptaincyRecyclerViewAdapter
            })

            usersrv = findViewById(R.id.recyclerView)

            user_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })

            assignButton.setOnClickListener {
                MainActivity.playClickSound()
                if (!mViewModel.display.value!!) {
                    mViewModel.singleSelectionCheck().observe(viewLifecycleOwner, Observer {
                        it.toast(requireContext())
                        if (it.equals("You're not a captain anymore")) {
                            mViewModel.display.value = true
                            findNavController().navigate(R.id.action_transferCaptaincy2_to_teamsFragment2)
                            //findNavController().navigateUp()
                        }
                        else {
                            mViewModel.display.value = false
                        }
                    })
                }
            }
        }
        return view
    }
}