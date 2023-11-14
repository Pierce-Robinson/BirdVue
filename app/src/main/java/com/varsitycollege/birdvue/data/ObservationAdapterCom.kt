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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.varsitycollege.birdvue.R

class DelayedClickListener(private val delayMillis: Long, private val onClickListener: View.OnClickListener) : View.OnClickListener {

    private var lastClickTime: Long = 0

    override fun onClick(view: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= delayMillis) {
            // If enough time has passed, allow the click
            lastClickTime = currentTime
            onClickListener.onClick(view)
        }
    }
}
class ObservationAdapterCom (private var posts: List<Observation>) : RecyclerView.Adapter<ObservationAdapterCom.PostViewHolder>() {
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
    fun setObservations(newList: List<Observation>) {
        posts = newList
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        //Bind data to views
        //holder.postImage.setImageResource(post.imageResId)
        //holder.profilePicture.setImageResource(post.profilePictureResId)
        holder.birdNameField.text = post.birdName
        holder.caption.text = post.details
        holder.likeButton.text = buildString {
            append(" ")
            append(post.likes.toString())
        }
        holder.date.text = post.date
        // Set click listeners for buttons (like, comment)
        holder.likeButton.setOnClickListener(DelayedClickListener(1000, View.OnClickListener {
            if (!post.liked){
            // If the user hasn't liked this observation, increment the likes count locally
            post.likes = post.likes?.plus(1)

            // Notify the adapter that the data at this position has changed
            notifyItemChanged(position)

            // Now, update the likes count in Firebase
            post.id?.let { it1 ->
                incrementLikes(it1)
                post.liked=true
            }
            }else{
                post.likes = post.likes?.plus(-1)

                notifyItemChanged(position)

                post.id?.let { it1 ->
                    decrementLikes(it1)
                    post.liked=false
                }
            }
        }))


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
    private fun incrementLikes(observationId: String) {
        val database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
        val observationRef = database.getReference("observations").child(observationId)
        observationRef.child("likes").setValue(ServerValue.increment(1))
            observationRef.child("liked").setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                } else {
                    // Handle the error

                }
            }
    }
    private fun decrementLikes(observationId: String) {
        val database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")
        val observationRef = database.getReference("observations").child(observationId)
        observationRef.child("likes").setValue(ServerValue.increment(-1))
        observationRef.child("liked").setValue(false)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                } else {
                    // Handle the error

                }
            }
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