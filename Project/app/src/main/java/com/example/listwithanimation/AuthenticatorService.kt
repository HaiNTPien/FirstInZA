package com.example.listwithanimation

import android.accounts.*
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.example.listwithanimation.view.ContactActivity


class AuthenticatorService: Service() {
    lateinit var authenticator: Authenticator
    override fun onCreate() {
        super.onCreate()
        authenticator = Authenticator(this)
    }
    override fun onBind(intent: Intent?): IBinder? {
        return authenticator.iBinder
    }
}
