package dal.mitacsgri.treecare.screens.transfercaptaincy

import android.view.View
import android.widget.CheckBox
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dal.mitacsgri.treecare.R
import dal.mitacsgri.treecare.model.User
import dal.mitacsgri.treecare.screens.BaseViewHolder
import kotlinx.android.synthetic.main.item_transfer_captaincy.view.*

class TransferCaptaincyViewHolder(
    itemView: View,
    private val viewModel: TransferCaptaincyViewModel
):BaseViewHolder<User>(itemView) {
    override fun bind(item: User) {
        itemView.apply {

            nameTV.text = item.name

            Glide.with(this)
                .load(item.photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)

            /*stepCountTV.text = item.dailySteps.toString()
            leavesCountTV.text = (item.dailySteps / 3000).toString()*/

            checkbox_user.setOnClickListener {
                if (it is CheckBox) {
                    val checked: Boolean = it.isChecked
                    when (it.id) {
                        R.id.checkbox_user -> {
                            if (checked) {
                                viewModel.addUserToList(item.uid)
                            }
                            else viewModel.removeUserFromList(item.uid)
                        }
                    }
                }
            }
        }
    }
}