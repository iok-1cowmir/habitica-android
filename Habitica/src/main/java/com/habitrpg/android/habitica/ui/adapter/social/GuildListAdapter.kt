package com.habitrpg.android.habitica.ui.adapter.social

import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ItemPublicGuildBinding
import com.habitrpg.android.habitica.databinding.ItemUserGuildBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.NumberAbbreviator
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.helpers.setMarkdown
import com.habitrpg.android.habitica.ui.views.HabiticaIcons
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.realm.Case
import io.realm.OrderedRealmCollection

class GuildListAdapter(private val onlyShowUsersGuilds: Boolean) : BaseRecyclerViewAdapter<Group, RecyclerView.ViewHolder>(), Filterable {

    var socialRepository: SocialRepository? = null
    private var memberGuildIDs: List<String> = listOf()

    fun setMemberGuildIDs(memberGuildIDs: List<String>) {
        this.memberGuildIDs = memberGuildIDs
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val guildViewHolder = if (onlyShowUsersGuilds) {
            UserGuildViewHolder(parent.inflate(R.layout.item_user_guild))
        } else {
            GuildViewHolder(parent.inflate(R.layout.item_public_guild))
        }
        guildViewHolder.itemView.setOnClickListener { v ->
            val guild = v.tag as? Group ?: return@setOnClickListener
            MainNavigationController.navigate(R.id.guildFragment, bundleOf(Pair("groupID", guild.id)))
        }
        return guildViewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val guild = data[position]
        if (onlyShowUsersGuilds && holder is UserGuildViewHolder) {
            holder.bind(guild)
        } else if (holder is GuildViewHolder) {
            val isInGroup = isInGroup(guild)
            holder.bind(guild, isInGroup)
            holder.itemView.tag = guild
        }
    }

    private fun isInGroup(guild: Group): Boolean {
        return this.memberGuildIDs.contains(guild.id)
    }

    private var unfilteredData: OrderedRealmCollection<Group>? = null

    fun setUnfilteredData(data: OrderedRealmCollection<Group>?) {
        this.data = data ?: emptyList()
        unfilteredData = data
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                results.values = constraint
                return FilterResults()
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                unfilteredData?.let {
                    if (constraint.isNotEmpty()) {
                        data = it.where()
                                .beginGroup()
                                .contains("name", constraint.toString(), Case.INSENSITIVE)
                                .or()
                                .contains("summary", constraint.toString(), Case.INSENSITIVE)
                                .endGroup()
                                .findAll()
                    }
                }
            }
        }
    }

    class UserGuildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemUserGuildBinding.bind(itemView)

        fun bind(guild: Group) {
            binding.titleTextView.text = guild.name
            binding.guildBadgeView.setImageBitmap(
                    HabiticaIconsHelper.imageOfGuildCrest(itemView.context,
                            false,
                            guild.privacy == "public",
                            guild.memberCount.toFloat(),
                            NumberAbbreviator.abbreviate(itemView.context, guild.memberCount.toDouble()))
            )
        }
    }

    class GuildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemPublicGuildBinding.bind(itemView)

        fun bind(guild: Group, isInGroup: Boolean) {
            binding.nameTextView.text = guild.name
            binding.memberCountTextView.text = NumberAbbreviator.abbreviate(itemView.context, guild.memberCount.toDouble())
            binding.descriptionTextView.setMarkdown(guild.summary)
            binding.guildBadgeView.setImageBitmap(HabiticaIconsHelper.imageOfGuildCrestSmall(guild.memberCount.toFloat()))
        }
    }
}