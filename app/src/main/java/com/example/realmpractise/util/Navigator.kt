package com.example.realmpractise.util

import android.content.Context
import android.content.Intent
import com.example.realmpractise.ui.userActivity.UserActivity

interface Navigator {
    fun openUserActivity()
}

class NavigatorImpl(private val context: Context) : Navigator {
    override fun openUserActivity() = context.startActivity(
        Intent(context, UserActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )

}