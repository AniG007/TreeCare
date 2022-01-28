package dal.mitacsgri.treecare.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
//import com.crashlytics.android.Crashlytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.messaging.FirebaseMessaging
import dal.mitacsgri.treecare.consts.CHALLENGER_MODE
import dal.mitacsgri.treecare.consts.STARTER_MODE
import dal.mitacsgri.treecare.consts.TOURNAMENT_MODE
import dal.mitacsgri.treecare.extensions.default
import dal.mitacsgri.treecare.extensions.getMapFormattedDate
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.repository.FirestoreRepository
import dal.mitacsgri.treecare.repository.SharedPreferencesRepository
import dal.mitacsgri.treecare.repository.StepCountRepository
import dal.mitacsgri.treecare.screens.dialog.logindataloading.LoginDataLoadingDialog
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import java.util.*

class MainViewModel(
    private val sharedPrefsRepository: SharedPreferencesRepository,
    private val stepCountRepository: StepCountRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    private val loadingDialog = LoginDataLoadingDialog()
    private lateinit var mActivity: Activity

    private var RC_SIGN_IN = 1000
    private val RC_GOOGLE_FIT_PERMISSIONS = 4

    val isLoginDone = MutableLiveData<Boolean>().default(false)

    val userFirstName =  MutableLiveData<String>()


    var firstLoginTime: Long
        set(value) {
            sharedPrefsRepository.firstLoginTime = value
        }
        get() = sharedPrefsRepository.firstLoginTime

    var lastLoginTime: Long
        set(value) {
            sharedPrefsRepository.lastLoginTime = value
        }
        get() = sharedPrefsRepository.lastLoginTime

    fun hasInstructionsDisplayed(mode: Int) =
        when(mode) {
            STARTER_MODE -> sharedPrefsRepository.starterModeInstructionsDisplayed
            CHALLENGER_MODE -> sharedPrefsRepository.challengerModeInstructionsDisplayed
            TOURNAMENT_MODE -> sharedPrefsRepository.tournamentModeInstructionsDisplayed
            else -> true
        }

    fun setInstructionsDisplayed(mode: Int, value: Boolean) {
        when(mode) {
            STARTER_MODE -> sharedPrefsRepository.starterModeInstructionsDisplayed = value
            CHALLENGER_MODE -> sharedPrefsRepository.challengerModeInstructionsDisplayed = value
            TOURNAMENT_MODE -> sharedPrefsRepository.tournamentModeInstructionsDisplayed = value
        }
    }

    fun setGameMode(mode: Int) {
        sharedPrefsRepository.gameMode = mode
    }

    fun setCrashlyticsUserIdentifiers() {
        val user = sharedPrefsRepository.user
        val crashlytics = FirebaseCrashlytics.getInstance()

//        Crashlytics.setUserEmail(user.email)
//        Crashlytics.setUserName(user.name)
//        Crashlytics.setUserIdentifier(user.uid)
        crashlytics.setUserId(user.uid) //After removal of Fabric SDK for crashlytics
    }

    fun subscribeToFCMDailyGoalNotification(context: Context) {
        FirebaseMessaging.getInstance().subscribeToTopic("DailyGoal")
            .addOnSuccessListener {
                Log.d("Subscribed", "FCM notifications")
            }
    }

    fun startLoginAndConfiguration(activity: FragmentActivity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("628888141862-lmblquvs5s3gl9rmshvag3sin348kaam.apps.googleusercontent.com"/*Web application type client ID*/)
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun onSignInResult(requestCode: Int, resultCode: Int, data: Intent?, activity: Activity) {
        Log.d("Test","Inside onSignInResult")
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_SIGN_IN -> {
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                        .addOnSuccessListener {
                            val account = GoogleSignIn.getLastSignedInAccount(activity)!!
                            mActivity = activity

                            firebaseAuthWithGoogle(account) {
                                val fitnessOptions = FitnessOptions.builder()
                                    .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
                                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                                    .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
                                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                                    .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                                    .build()

                                if (!GoogleSignIn.hasPermissions(
                                        GoogleSignIn.getLastSignedInAccount(mActivity), fitnessOptions)
                                ) {
                                    Log.d("FitAPI","User has not granted permission")
                                    GoogleSignIn.requestPermissions(
                                        mActivity,
                                        RC_GOOGLE_FIT_PERMISSIONS,
                                        GoogleSignIn.getLastSignedInAccount(mActivity),
                                        fitnessOptions)
                                } else {
                                    Log.d("FitAPI", "permissions exist")
                                    subscribeToRecordSteps(account) {
                                        sharedPrefsRepository.isLoginDone = true
                                        isLoginDone.value = true
                                    }
                                }

                                lastLoginTime = Date().time
                            }
                        }
                }
                RC_GOOGLE_FIT_PERMISSIONS -> {
                    Log.d("FitAPI", "permissions granted")
                    subscribeToRecordSteps(GoogleSignIn.getLastSignedInAccount(activity)!!) {
                        sharedPrefsRepository.isLoginDone = true
                        isLoginDone.value = true
                    }
                }
            }
        } else {
            Log.e("TreeCare", "RESULT_CANCELED")
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount,
                                       action: (account: GoogleSignInAccount) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Log.d("FirebaseAuth", "signInWithCredential:success")
                val user = auth.currentUser

                //Store user dal.mitacsgri.treecare.data if user does not exist
                user?.let {

                    userFirstName.value = it.displayName?.split(" ")?.get(0)

                    checkIfUserExists(user.uid, account, {
                        firstLoginTime = it.firstLoginTime
                        sharedPrefsRepository.isFirstRun = false
                        expandDailyGoalMapIfNeeded(it)
                    }, {
                        sharedPrefsRepository.isFirstRun = true
                        return@checkIfUserExists User(
                            uid = user.uid,
                            isFirstRun = true,
                            name = user.displayName!!,
                            firstLoginTime = DateTime().millis,
                            email = user.email!!,
                            photoUrl = user.photoUrl.toString())
                    }) {
                        action(account)
                    }

                    Log.d("User: ", userFirstName.toString())
                }

            }
    }

    private fun subscribeToRecordSteps(account: GoogleSignInAccount, action: () -> Unit) {
        Log.d("FitAPI", account.grantedScopes.toString())
        val TAG = "RecordingAPI"

        Fitness.getRecordingClient(mActivity, account).subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
            .addOnSuccessListener {
                Log.d(TAG, "success: TYPE_STEP_COUNT_CUMULATIVE")
                action()
            }
            .addOnFailureListener {
                Log.d(TAG, "failure: $it")
            }

        Fitness.getRecordingClient(mActivity, account).subscribe(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.d(TAG, "success: TYPE_STEP_COUNT_DELTA")
                action()
            }
            .addOnFailureListener {
                Log.d(TAG, "failure: $it")
            }
    }

    private inline fun checkIfUserExists(uid: String,
                                         account: GoogleSignInAccount,
                                         crossinline userExistsAction: (User) -> Unit,
                                         crossinline userDoesNotExistAction: () -> User,
                                         crossinline actionAfterStoringUserData: (account: GoogleSignInAccount) -> Unit) {
        firestoreRepository.getUserData(uid)
            .addOnSuccessListener {
                if (it.exists()) {
                    val user = it.toObject<User>()!!
                    userExistsAction(user)
                    sharedPrefsRepository.user = user
                    storeDailyGoalInPrefs()
                } else {
                    val user = userDoesNotExistAction()
                    firestoreRepository.storeUser(user)
                    Log.e("USER", it.toString())
                    sharedPrefsRepository.user = user
                }
                storeDailyGoalInPrefs()
                actionAfterStoringUserData(account)
            }
            .addOnFailureListener {
                Log.e("USER", it.toString())
            }
    }

    private fun expandDailyGoalMapIfNeeded(user: User) {
        val dailyGoalMap = user.dailyGoalMap
        var keysList = mutableListOf<String>()
        dailyGoalMap.keys.forEach {
            keysList.add(it)
        }
        keysList = keysList.sorted().toMutableList()

//        val lastTime = keysList[keysList.size-1]
//        val lastDate = DateTime.parse(lastTime, DateTimeFormat.forPattern("yyyy/MM/dd"))
//        val days = Days.daysBetween(lastDate, DateTime()).days
//
//        val oldGoal = dailyGoalMap[lastTime]
//
//        for (i in 1..days) {
//            val key = lastDate.plusDays(i).getMapFormattedDate()
//            user.dailyGoalMap[key] = oldGoal!!
//        }

        user.dailyGoalMap = dailyGoalMap.toSortedMap()
        sharedPrefsRepository.user = user
    }
    //Update the daily goal stored in SharedPrefs to display in Unity
    //DailyGoalChecked is set to true only by Unity
    private inline fun storeDailyGoalInPrefs() {
        val dailyGoalMap = sharedPrefsRepository.user.dailyGoalMap
        if (sharedPrefsRepository.isDailyGoalChecked == 0) {
            //In reality, the queried entry will never be null as the User has atleast 2 entries by default,
            //i.e., the current day and the next day
            sharedPrefsRepository.storeDailyStepsGoal(
                //dailyGoalMap[DateTime().withTimeAtStartOfDay().millis.toString()] ?: 5000)
                dailyGoalMap.values.last())
            Log.d("DailyGoal", sharedPrefsRepository.getDailyStepsGoal().toString())
        }
    }
}