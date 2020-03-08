package com.puzzle.dcs

import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        config.useImmersiveMode = true
        config.useAccelerometer = false
        config.useCompass = false

//        MediaScannerConnection.scanFile(getApplicationContext(), arrayOf(cacheDir.parent), null, null);
//        Log.v("cache", "${cacheDir.parent}")

        initialize(Core(), config)
    }
}
