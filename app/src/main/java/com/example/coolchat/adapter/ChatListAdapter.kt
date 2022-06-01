package com.example.coolchat.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.coolchat.databinding.ItemLeftBinding
import com.example.coolchat.databinding.ItemRightBinding
import com.example.coolchat.model.User
import com.example.coolchat.model.Chat
import com.example.coolchat.ui.chat.databaseReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatListAdapter(private val interaction: Interaction? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    var firebaseUser: FirebaseUser? = null
    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Chat>() {

        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.senderId == newItem.senderId
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }


    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == MESSAGE_TYPE_RIGHT) {
            return RightChatListViewHolder(
                ItemRightBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                interaction
            )
        } else {
            return LeftChatListViewHolder(
                ItemLeftBinding.inflate(LayoutInflater.from(parent.context), parent, false),
                interaction
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RightChatListViewHolder -> {
                holder.bind(differ.currentList[position], position)
            }
            is LeftChatListViewHolder -> {
                holder.bind(differ.currentList[position], position)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Chat>) {
        differ.submitList(list)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (differ.currentList[position].senderId == firebaseUser!!.uid) {
            MESSAGE_TYPE_RIGHT
        } else {
            MESSAGE_TYPE_LEFT
        }
    }

    class RightChatListViewHolder(
        private val binding: ItemRightBinding,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Chat, position: Int) = with(binding) {
            messageTextView.text = item.message
            itemView.setOnClickListener {
                interaction?.onItemSelected(position, item)
            }
        }
    }

    class LeftChatListViewHolder(
        private val binding: ItemLeftBinding,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Chat, position: Int) = with(binding) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            val postListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapShot: DataSnapshot in snapshot.children) {
                        val user = dataSnapShot.getValue(User::class.java)
                        user?.let {
                            if (it.uid == item.senderId) {
                                Glide.with(binding.messengerImageView.context)
                                    .load(it.photoUri)
                                    .into(binding.messengerImageView)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Post cancelled: $error")
                }

            }
            databaseReference.addValueEventListener(postListener)
            messageTextView.text = item.message
            itemView.setOnClickListener {
                interaction?.onItemSelected(position, item)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Chat)
    }
}