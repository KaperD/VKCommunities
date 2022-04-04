package ru.hse.vkcommunities

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView


class CommunitiesAdapter(
    private val viewModel: CommunitiesViewModel,
    private val color: Int
) : RecyclerView.Adapter<CommunitiesAdapter.MyViewHolder>() {

    private var list: List<Community> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun setCommunitiesList(list: List<Community>) {
        this.list = list
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logo: SimpleDraweeView = itemView.findViewById(R.id.community_logo)
        val name: TextView = itemView.findViewById(R.id.community_name)
        val check: SimpleDraweeView = itemView.findViewById(R.id.check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.community, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val community = list[position]
        holder.name.text = community.name
        holder.check.visibility = if (community.isChosen) View.VISIBLE else View.INVISIBLE
        holder.logo.hierarchy.roundingParams = RoundingParams.asCircle()
            .apply {
                if (community.isChosen) {
                    setBorder(color, 10.0f)
                }
            }
        holder.logo.setImageURI(null as String?)
        val uri = Uri.parse(community.logoUrl)
        holder.logo.setImageURI(uri)

        holder.itemView.setOnClickListener {
            if (community.isChosen) {
                viewModel.removeChoice(community)
            } else {
                viewModel.addChoice(community)
            }
            community.isChosen = !community.isChosen
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = list.size
}
