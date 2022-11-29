package com.example.listwithanimation

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.listwithanimation.utils.APP_ACCOUNT_TYPE
import com.example.listwithanimation.view.ContactActivity


class Authenticator(var context: Context?) : AbstractAccountAuthenticator(context) {
    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? {
        return null
    }


    @Throws(NetworkErrorException::class)
    override fun addAccount(
        r: AccountAuthenticatorResponse,
        s: String,
        s2: String,
        strings: Array<String>,
        bundle: Bundle
    ): Bundle {
        val reply = Bundle()
        val i = Intent(context, ContactActivity::class.java)
        i.action = APP_ACCOUNT_TYPE
        i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, r)
        reply.putParcelable(AccountManager.KEY_INTENT, i)
        return reply
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(
        r: AccountAuthenticatorResponse,
        account: Account,
        bundle: Bundle
    ): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(
        r: AccountAuthenticatorResponse,
        account: Account,
        s: String,
        bundle: Bundle
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun getAuthTokenLabel(s: String): String {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(
        r: AccountAuthenticatorResponse,
        account: Account,
        s: String, bundle: Bundle
    ): Bundle {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(
        r: AccountAuthenticatorResponse,
        account: Account, strings: Array<String>
    ): Bundle {
        throw UnsupportedOperationException()
    }
}


