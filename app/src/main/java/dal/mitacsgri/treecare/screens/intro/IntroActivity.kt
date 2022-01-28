package dal.mitacsgri.treecare.screens.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import dal.mitacsgri.treecare.R

class IntroActivity : AppIntro2() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)

        addSlide(IntroSlide.newInstance(R.layout.intro_slide_1))

        showStatusBar(true)
        showSkipButton(false)
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)
        finish()
    }
}
