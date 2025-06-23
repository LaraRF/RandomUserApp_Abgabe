package com.srh.randomuserapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.srh.randomuserapp.R
import com.srh.randomuserapp.data.models.User
import com.srh.randomuserapp.databinding.ItemUserBinding

/**
 * RecyclerView adapter for displaying user list.
 * Uses ListAdapter with DiffUtil for efficient updates.
 */
class UserAdapter(
    private val onUserClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder for individual user items
     */
    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onUserClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                // Set user data
                textViewName.text = user.fullName
                textViewEmail.text = user.email
                textViewPhone.text = user.phone
                textViewLocation.text = user.fullAddress

                // Manual creation indicator - ENTFERNT
                // if (user.isManuallyCreated) { ... }

                // Load profile image with Glide
                Glide.with(imageViewProfile.context)
                    .load(user.profilePictureUrl)
                    .placeholder(R.drawable.ic_person_placeholder_24)
                    .error(R.drawable.ic_person_placeholder_24)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageViewProfile)

                // Set click listener
                root.setOnClickListener {
                    onUserClick(user)
                }

                // Set content description for accessibility
                root.contentDescription = "User ${user.fullName}, tap to view details"
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}