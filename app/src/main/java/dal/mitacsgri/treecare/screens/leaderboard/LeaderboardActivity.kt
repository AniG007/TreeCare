package dal.mitacsgri.treecare.screens.leaderboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dal.mitacsgri.treecare.R
import kotlinx.android.synthetic.main.fragment_leaderboard.backButton1

class LeaderboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        //for closing the activity and transitioning to tree screen smoothly
        backButton1.setOnClickListener {
            finish()
        }
    }
}
