package com.dtse.oliverbotello.huaweiidexample.huawei

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.huawei.hmf.tasks.OnCompleteListener
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.result.AuthAccount
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.api.entity.common.CommonConstant

class MainActivity : AppCompatActivity(), OnSuccessListener<AuthAccount?>, OnFailureListener {
    companion object {
        private const val REQUEST_CODE_SIGN_IN: Int = 200
    }

    private lateinit var mAuthParam: AccountAuthParams
    private lateinit var mAuthService: AccountAuthService
    private lateinit var signInIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.hwIdAuthBtn).setOnClickListener {
            loginWithHuaweiID()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SIGN_IN)
            loginWithHuaweiID(data)
        else
            showMessage("Algo salio mal $requestCode")
    }

    fun loginWithHuaweiID() {
        mAuthParam = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setEmail()
            .setAuthorizationCode()
            .createParams()
        mAuthService = AccountAuthManager.getService(
            this.applicationContext,
            mAuthParam
        )
        val task: Task<AuthAccount> = mAuthService.silentSignIn()

        task.addOnSuccessListener(this)
        task.addOnFailureListener(this)
    }

    fun loginWithHuaweiID(data: Intent?) {
        val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)

        if (authAccountTask.isSuccessful)
            this.onSuccess(authAccountTask.result)
        else
            this.onFailure(authAccountTask.exception)
    }

    override fun onSuccess(result: AuthAccount?) {
        result?.let {
            showMessage("Nombre: ${it.displayName}")
            showMessage("Email: ${it.email}")
            showMessage("Token: ${it.idToken}")
        }
    }

    override fun onFailure(e: Exception?) {
        if (e is ApiException) {
            val apiException = e
            signInIntent = mAuthService.signInIntent
            signInIntent.putExtra(CommonConstant.RequestParams.IS_FULL_SCREEN, true)
            startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
        }
        else {
            showMessage(e?.message?:"")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}