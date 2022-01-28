package dal.mitacsgri.treecare.screens.jointeam

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.google.android.material.button.MaterialButton
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.extensions.toast
import dal.mitacsgri.treecare.screens.MainActivity
import kotlinx.android.synthetic.main.fragment_create_team.view.toolbar
import kotlinx.android.synthetic.main.fragment_join_team.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class JoinTeamFragment : Fragment() {

    private  val joinTeamViewModel: JoinTeamViewModel  by viewModel()
    val teamName= MutableLiveData<String>()
    var email = ""
    val uid = MutableLiveData<String>()

    val args: JoinTeamFragmentArgs by navArgs()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        teamName.value = args.teamName
    }
    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_join_team, container, false)
        view.apply {
            toolbar.setNavigationOnClickListener {

                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(windowToken, 0)

                findNavController().navigateUp()
            }

            AddTeamsButton.setOnClickListener {
                MainActivity.playClickSound()
                val emailId= playerEmailId.text
                //Log.d("Test","TeamName "+teamName.value)
                //Log.d("Test","OnButtonClick "+emailId)
                //Log.d("Test","OnButtonClick "+teamName)

                joinTeamViewModel.getUserEmail(emailId.toString(),args.teamName)
            }

            joinTeamViewModel.messageLiveData.observe(viewLifecycleOwner, Observer {
                it.toast(context)
                playerEmailId.setText("")
                //  findNavController().navigateUp()
            })

            joinTeamViewModel.messageLiveData2.observe(viewLifecycleOwner, Observer {
                it.toast(context)
                playerEmailId.setText("")
                //  findNavController().navigateUp()
            })

        }

        return view
    }

}
//                joinTeamViewModel.messageLiveData.observe(this@JoinTeamFragment, Observer {
//                    it.toast(context)
//                    playerEmailId.setText("")
//                    //  findNavController().navigateUp()
//                })

//                 joinTeamViewModel.getUserEmail(emailId.toString()).observe(this@JoinTeamFragment, Observer{
//
//                     if(it) {
//                         playerEmailId.setText("")
//                         //Toast.makeText(getApplicationContext(),"PlayerId does not exist", Toast.LENGTH_SHORT).show()
//                         joinTeamViewModel.messageLiveData.observe(this@JoinTeamFragment, Observer {
//                             it.toast(context)
//                         })
//                     }
//                     email = it.toString()
//                     Log.d("Test","itty"+email)
//                 })
//                if(email != "true") {
//                    joinTeamViewModel.getUserId(emailId.toString(), args.teamName)
//                        .observe(this@JoinTeamFragment, Observer {
//                            //Log.d("Test","it11"+it)
//                            if (it) {
//                                playerEmailId.setText("")
//                                //Toast.makeText(getApplicationContext(), "User was invited Successfully", Toast.LENGTH_SHORT).show()
//                                joinTeamViewModel.messageLiveData.observe(this@JoinTeamFragment, Observer {
//                                    it.toast(context)
//                                })
//                                email = "true"
//                            }
//                            else {
//                                playerEmailId.setText("")
//                                //Toast.makeText(getApplicationContext(), "PlayerId does not exist", Toast.LENGTH_SHORT).show()
//                                joinTeamViewModel.messageLiveData.observe(this@JoinTeamFragment, Observer {
//                                    it.toast(context)
//                                })
//                            }
//                        })
//                }
                /*joinTeamViewModel.valid.observe(this@JoinTeamFragment, Observer {
                    if(it){
                        playerEmailId.setText("")
                        Toast.makeText(getApplicationContext(),"User was invited Successfully", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        playerEmailId.setText("")
                        Toast.makeText(getApplicationContext(),"PlayerId does not exist", Toast.LENGTH_SHORT).show()
                    }
                })*/
                /*Log.d("Test","m "+m)
                if() {
                    playerEmailId.setText("")
                    Toast.makeText(
                        getApplicationContext(),
                        "User was invited Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    playerEmailId.setText("")
                    Toast.makeText(
                        getApplicationContext(),
                        "PlayerId does not exist",
                        Toast.LENGTH_SHORT
                    ).show()
                }*/
               /* if(joinTeamViewModel.getUserId(emailId.toString(),args.teamName)) {*/
                    /*joinTeamViewModel.valid.observe(this@JoinTeamFragment, Observer {*/
                        /*if (it) {
                            Log.d("Toast", it.toString())
                            playerEmailId.setText("")
                            Toast.makeText(
                                getApplicationContext(),
                                "User was invited Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            playerEmailId.setText("")
                            Toast.makeText(
                                getApplicationContext(),
                                "PlayerId does not exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }*/
                    /*})
                }*/

                    /*Log.d("Toast", it.toString())
                    if(it) {
                        Log.d("Toast","Test for Toast")
                        Log.d("Toast", it.toString())
                        playerEmailId.setText("")
                        //it.toast(context)
                        Toast.makeText(getApplicationContext(),"User was invited Successfully",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        playerEmailId.setText("")
                      //  it.toast(context)
                        Log.d("Toast","Email Id Does not exist")
                        Toast.makeText(getApplicationContext(),"PlayerId does not exist",Toast.LENGTH_SHORT).show()

                    }*/

                    //Log.d("TAG", "userID "+ it.toString())
                    /*joinTeamViewModel.userID.value = it.toString()
                    joinTeamViewModel.userID.notifyObserver()*/
                    //uid.notifyObserver()
                    //Log.d("TAG","uid"+ it.toString())





                /*joinTeamViewModel.sendInvite(joinTeamViewModel.userID.toString(),args.teamName).observe(this@JoinTeamFragment, Observer {
                    Log.d("TAG", "dataStorage "+ it.toString())
                })*/


