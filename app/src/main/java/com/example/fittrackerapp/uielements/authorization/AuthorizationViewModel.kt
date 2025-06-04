package com.example.fittrackerapp.uielements.authorization

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.fittrackerapp.AuthCondition
import com.google.firebase.Firebase
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthorizationViewModel: ViewModel() {

    val auth = FirebaseAuth.getInstance()

    private val _authCondition = MutableStateFlow(AuthCondition.SIGN_IN)
    val authCondition: StateFlow<AuthCondition> = _authCondition

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _firstPassword = MutableStateFlow("")
    val firstPassword: StateFlow<String> = _firstPassword

    private val _secondPassword = MutableStateFlow("")
    val secondPassword: StateFlow<String> = _secondPassword

    fun setEmail(value: String) {
        _email.value = value
    }

    fun setName(value: String) {
        _name.value = value
    }

    fun setFirstPassword(value: String) {
        _firstPassword.value = value
    }

    fun setSecondPassword(value: String) {
        _secondPassword.value = value
    }

    fun setAuthCondition(value: AuthCondition) {
        _authCondition.value = value
    }

    fun signIn(context: Context) {
        if (_email.value == "" || _firstPassword.value == "") return
        auth.signInWithEmailAndPassword(_email.value, _firstPassword.value)
            .addOnCompleteListener(context as AuthorizationActivity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    _authCondition.value = AuthCondition.SUCCESS
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        context,
                        "Неверный логин или пароль",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }


    fun signUp() {
        auth.createUserWithEmailAndPassword(_email.value, _firstPassword.value)
            .addOnSuccessListener { result ->
                result.user?.apply {
                    sendEmailVerification()
                    updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(_name.value)
                            .build()
                    )
                    setAuthCondition(AuthCondition.WAIT_FOR_EMAIL_VERIFICATION)
                }
            }
            .addOnFailureListener {
                setAuthCondition(AuthCondition.ERROR)
            }
    }

    fun sendEmail(email: String, actionCodeSettings: ActionCodeSettings) {
        if (email.isEmpty()) {
            return
        }
        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    setAuthCondition(AuthCondition.WAIT_FOR_EMAIL_VERIFICATION)
                }
            }
            .addOnFailureListener {
                setAuthCondition(AuthCondition.ERROR)
            }
    }

    fun checkUserVerification(context: Context) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (FirebaseAuth.getInstance().currentUser != null && user.isEmailVerified) {
                    Toast.makeText(context, "Email подтверждён.", Toast.LENGTH_LONG).show()
                    setAuthCondition(AuthCondition.SUCCESS)
                }
                else {
                    setAuthCondition(AuthCondition.ERROR)
                }
            }
        }
    }
}