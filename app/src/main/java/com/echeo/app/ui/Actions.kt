package com.echeo.app.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.echeo.app.domain.phoneDigits

/** Actions qui sortent de l'app : appeler, WhatsApp, copier le code du téléphone. */
object Actions {
    fun dial(ctx: Context, phone: String) {
        start(ctx, Intent(Intent.ACTION_DIAL, Uri.parse("tel:+${phoneDigits(phone)}")))
    }

    fun whatsapp(ctx: Context, phone: String, text: String?) {
        val url = "https://wa.me/${phoneDigits(phone)}" + (text?.let { "?text=" + Uri.encode(it) } ?: "")
        start(ctx, Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    /** Ouvre WhatsApp avec un message prêt à envoyer : l'utilisateur choisit le contact. */
    fun whatsappShare(ctx: Context, text: String) {
        start(ctx, Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(text))))
    }

    fun copy(ctx: Context, label: String, text: String) {
        ctx.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(ctx, "Code copié", Toast.LENGTH_SHORT).show()
    }

    fun toast(ctx: Context, message: String) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
    }

    private fun start(ctx: Context, intent: Intent) {
        try {
            ctx.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            toast(ctx, "Aucune application pour cette action")
        }
    }
}
