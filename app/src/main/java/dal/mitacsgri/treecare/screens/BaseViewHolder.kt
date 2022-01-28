package dal.mitacsgri.treecare.screens

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Devansh on 25-06-2019
 */
abstract class BaseViewHolder<T>(itemView: View): RecyclerView.ViewHolder(itemView) {

    abstract fun bind(item: T)
}