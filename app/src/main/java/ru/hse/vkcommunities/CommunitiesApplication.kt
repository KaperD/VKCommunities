package ru.hse.vkcommunities

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco

class CommunitiesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
    }
}
