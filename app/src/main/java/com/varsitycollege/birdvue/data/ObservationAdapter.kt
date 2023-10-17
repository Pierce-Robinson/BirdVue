package com.varsitycollege.birdvue.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.varsitycollege.birdvue.R

class ObservationAdapter (private val posts: List<Observation>) : RecyclerView.Adapter<ObservationAdapter.PostViewHolder>() {
    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        val birdNameField: TextView = itemView.findViewById(R.id.birdNameField)
        val caption: TextView = itemView.findViewById(R.id.caption)
        val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.observation_item, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Bind data to views
        //holder.postImage.setImageResource(post.imageResId)
        //holder.profilePicture.setImageResource(post.profilePictureResId)
        holder.birdNameField.text = post.birdName
        holder.caption.text = post.details

        // Set click listeners for buttons (like, comment)
        holder.likeButton.setOnClickListener {
            // Handle like button click
        }

        holder.commentButton.setOnClickListener {
            // Handle comment button click
        }
        if (post.photo.isNullOrEmpty()) {
            holder.postImage.setImageResource(R.drawable.icon)
        } else {
            // Load the image from the download URL
            Glide.with(holder.itemView.context)
                .load(post.photo)
                .centerCrop()
                .into(holder.postImage)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }


}