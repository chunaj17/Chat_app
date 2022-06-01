package com.example.coolchat.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.coolchat.databinding.UsersListBinding
import com.example.coolchat.room.user.UserCacheEntity

class UserListAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserCacheEntity>() {

        override fun areItemsTheSame(oldItem: UserCacheEntity, newItem: UserCacheEntity): Boolean {
           return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: UserCacheEntity, newItem: UserCacheEntity): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return UserListViewHolder(
            UsersListBinding.inflate(LayoutInflater.from(parent.context),parent,false),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserListViewHolder -> {
                holder.bind(differ.currentList[position],position)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<UserCacheEntity>) {
        differ.submitList(list)
    }

    class UserListViewHolder
    constructor(
        private val binding:UsersListBinding,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserCacheEntity, position: Int) = with(binding) {
            username.text = item.username
            Glide.with(profileImage.context)
                .load(item.photoUri)
                .into(profileImage)
            itemView.setOnClickListener {
                interaction?.onItemSelected(position, item)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: UserCacheEntity)
    }
}