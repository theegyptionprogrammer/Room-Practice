package com.example.realmpractise.ui.mainActivity


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.realmpractise.R
import com.example.realmpractise.db.User
import com.example.realmpractise.db.UserModule
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.user.view.*


class Adapter(private val context: Context) : RecyclerView.Adapter<Adapter.UserViewHolder>() {

    private lateinit var userList: RealmResults<User>
    private var userModule = UserModule()
    private val realm: Realm = Realm.getDefaultInstance()
    private val viewHolder: ViewHolder? = null

    fun getAllUsers(userList: RealmResults<User>) {
        this.userList = userList
        notifyDataSetChanged()
    }

    fun deleteUser(position: Int) {
        userList.removeAt(position)
        userModule.deleteUser(realm, viewHolder!!.position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            LayoutInflater.from(context).inflate(R.layout.user, parent, false)
        )
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) =
        holder.bind(userList[position]!!)

    class UserViewHolder(private val view: View) : ViewHolder(view) {
        fun bind(user: User) {
            /*  Picasso.get()
                  .load(user.pp)
                  .fit()
                  .into(view.userPP)*/
            view.userNameTV.text = user.name
            view.userPhoneTV.text = user.phone
            view.userIdTV.text = user.id.toString()
            /* view.delete_btn.setOnClickListener {
                 userModule.deleteUser(realm, adapterPosition)
             }*/
        }
    }
}