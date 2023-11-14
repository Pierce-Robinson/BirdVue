package com.varsitycollege.birdvue.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.varsitycollege.birdvue.R

class ObservationAdapterCom (private val posts: List<Observation>) : RecyclerView.Adapter<ObservationAdapterCom.PostViewHolder>() {
    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        val birdNameField: TextView = itemView.findViewById(R.id.birdNameField)
        val date: TextView = itemView.findViewById(R.id.dateDisp)
        val caption: TextView = itemView.findViewById(R.id.caption)
        val likeButton: Button = itemView.findViewById(R.id.likeButton)
        val commentButton: Button = itemView.findViewById(R.id.commentButton)
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.observation_item_com, parent, false)
        return PostViewHolder(view)

    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        //Bind data to views
        //holder.postImage.setImageResource(post.imageResId)
        //holder.profilePicture.setImageResource(post.profilePictureResId)
        holder.birdNameField.text = post.birdName
        holder.caption.text = post.details
        holder.date.text = post.date
        // Set click listeners for buttons (like, comment)
        holder.likeButton.setOnClickListener {
            // Handle like button click
        }

        holder.commentButton.setOnClickListener {
            // Handle comment button click
        }

        val imageUrls = listOf(post.photo, post.location)
        val imagePagerAdapter = ImagePagerAdapter(imageUrls)
        holder.viewPager.adapter = imagePagerAdapter

        // Set up dots indicator
        val dotsLayout = holder.itemView.findViewById<LinearLayout>(R.id.dotsLayout)
        setupDots(imageUrls.size, dotsLayout, holder.viewPager)

    }
    private fun setupDots(count: Int, parent: LinearLayout, viewPager: ViewPager2) {
        parent.removeAllViews()

        val dots = arrayOfNulls<ImageView>(count)
        for (i in 0 until count) {
            dots[i] = ImageView(parent.context)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    parent.context,
                    if (i == 0) R.drawable.active_dot else R.drawable.inactive_dot
                )
            )

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(4, 0, 4, 0)
            parent.addView(dots[i], params)
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                for (i in 0 until count) {
                    dots[i]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            parent.context,
                            if (i == position) R.drawable.active_dot else R.drawable.inactive_dot
                        )
                    )
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return posts.size
    }


}