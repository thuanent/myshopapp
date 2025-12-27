package com.example.myshopapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val userList: List<User>,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val userRoleTextView: TextView = itemView.findViewById(R.id.userRoleTextView)
        val userPhoneTextView: TextView = itemView.findViewById(R.id.userPhoneTextView)
        val btnDeleteUser: Button = itemView.findViewById(R.id.btnDeleteUser)

        init {
            btnDeleteUser.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick.invoke(userList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.userNameTextView.text = currentUser.email
        holder.userRoleTextView.text = currentUser.role
        holder.userPhoneTextView.text= currentUser.phoneNumber
    }

    override fun getItemCount() = userList.size
}