package com.varsitycollege.birdvue.data

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varsitycollege.birdvue.LoginActivity
import com.varsitycollege.birdvue.R

class ObservationAdapter (private val posts: List<Observation>) : RecyclerView.Adapter<ObservationAdapter.PostViewHolder>() {

    private var database = FirebaseDatabase.getInstance("https://birdvue-9288a-default-rtdb.europe-west1.firebasedatabase.app/")

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val postImage: ImageView = itemView.findViewById(R.id.postImage)
        val profilePicture: ImageView = itemView.findViewById(R.id.profilePicture)
        val birdNameField: TextView = itemView.findViewById(R.id.birdNameField)
        val date: TextView = itemView.findViewById(R.id.dateDisp)
        val caption: TextView = itemView.findViewById(R.id.caption)
        val likeButton: Button = itemView.findViewById(R.id.likeButton)
        val commentButton: Button = itemView.findViewById(R.id.commentButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteObservation)
        val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPager)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.observation_item, parent, false)
        return PostViewHolder(view)

    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.date.text = post.date
        holder.birdNameField.text = post.birdName
        holder.caption.text = post.details

        //Set click listeners for buttons (like, comment, delete)
        holder.likeButton.setOnClickListener {
            //Handle like button click
        }

        holder.commentButton.setOnClickListener {
            //Handle comment button click
        }

        holder.deleteButton.setOnClickListener {
            //Handle delete button click
            //https://stackoverflow.com/questions/32136973/how-to-get-a-context-in-a-recycler-view-adapter
            //user answered
            //https://stackoverflow.com/users/1018109/pelotasplus
            //Accessed 18 October 2023
            MaterialAlertDialogBuilder(holder.itemView.context)
                .setTitle("Delete Observation")
                .setMessage("Are you sure you want to delete this observation?")
                .setPositiveButton("Yes") { _, _ ->
                    try {
                        // Delete user's observations
                        val query = database.getReference("observations").orderByChild("id").equalTo(post.id)
                        if (post.id != null) {
                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (observationSnapshot in dataSnapshot.children) {
                                        // Delete the observation with the matching user ID
                                        observationSnapshot.ref.removeValue()
                                        Log.i("Observation deleted", "${observationSnapshot.key}")
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Log.e("Error while deleting observation", "${databaseError.toException()}")
                                }
                            })
                        }
                    } catch (e: Exception) {
                        Log.e("Delete observation exception", "" + e.localizedMessage)
                    }
                }
                    .setNegativeButton("No", null)
                    .show()
        }

        val imageUrls = listOf(post.photo, post.location)
        val imagePagerAdapter = ImagePagerAdapter(imageUrls)
        holder.viewPager.adapter = imagePagerAdapter

        //Set up dots indicator
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