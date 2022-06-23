package com.habitrpg.wearos.habitica

import android.app.Application
import android.content.Intent
import com.habitrpg.common.habitica.extensions.setupCoil
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.common.habitica.views.HabiticaIconsHelper
import com.habitrpg.wearos.habitica.data.repositories.UserRepository
import com.habitrpg.wearos.habitica.ui.activities.BaseActivity
import com.habitrpg.wearos.habitica.ui.activities.FaintActivity
import com.habitrpg.wearos.habitica.ui.activities.RYAActivity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        HabiticaIconsHelper.init(this)
        MarkdownParser.setup(this)
        setupCoil()

        MainScope().launch {
            userRepository.getUser().onEach {
                if (it.isDead && BaseActivity.currentActivityClassName != FaintActivity::class.java.name) {
                    val intent = Intent(this@MainApplication, FaintActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else if (it.needsCron && BaseActivity.currentActivityClassName != RYAActivity::class.java.name) {
                    val intent = Intent(this@MainApplication, RYAActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }.collect()
        }
    }
}