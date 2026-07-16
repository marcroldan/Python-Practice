package com.marcroldan.rimemba.widget.bigbutton

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import com.marcroldan.rimemba.R
import com.marcroldan.rimemba.ui.quickcapture.QuickCaptureActivity
import com.marcroldan.rimemba.ui.theme.RimembaPrimary

/**
 * Widget de home screen con un único botón grande: al tocarlo lanza
 * [QuickCaptureActivity] (tema transparente) que empieza a escuchar de inmediato.
 */
class BigButtonWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(RimembaPrimary)
                    .clickable(actionStartActivity<QuickCaptureActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_notification_mic),
                    contentDescription = context.getString(R.string.mic_button_content_description)
                )
            }
        }
    }
}
