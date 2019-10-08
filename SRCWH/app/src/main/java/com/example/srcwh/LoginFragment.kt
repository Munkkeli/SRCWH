package com.example.srcwh

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment(val callback: (token: String, user: LoginUser) -> Unit) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_button.setOnClickListener {
            doLogin()
        }
    }

    private fun doLogin(){
        // TODO later some proper input checks here

        login_progress.visibility = View.VISIBLE
        login_button.isEnabled = false

        // Send the login request
        val networkHandler = NetworkHandler()
        networkHandler.postLogin(username_text.text.toString(), password_text.text.toString()) { error, result ->
            if (error != null) {
                // Handle error message display
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            } else {
                callback(result!!.token, result.user)
            }

            login_progress.visibility = View.INVISIBLE
            login_button.isEnabled = true
        }
    }
}
