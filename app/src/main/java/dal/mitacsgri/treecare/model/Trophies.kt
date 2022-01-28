package dal.mitacsgri.treecare.model

data class Trophies(
    val bronze: ArrayList<String> = arrayListOf(),
    val gold: ArrayList<String> = arrayListOf(),
    val silver: ArrayList<String> = arrayListOf()
)