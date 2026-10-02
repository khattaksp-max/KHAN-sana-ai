package com.example.util

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.data.model.ActionType
import com.example.data.model.PhoneAction

object PhoneActionHandler {
    private const val TAG = "PhoneActionHandler"

    fun parseActionFromResponse(text: String): Pair<String, PhoneAction?> {
        val cleanText = text.replace(Regex("\\[ACTION:[A-Z_]+(:[^\\]]*)?\\]"), "").trim()

        val actionMatch = Regex("\\[ACTION:([A-Z_]+)(?::([^\\]]*))?\\]").find(text)
        if (actionMatch != null) {
            val actionKey = actionMatch.groupValues[1]
            val param = actionMatch.groupValues.getOrNull(2) ?: ""
            val action = when (actionKey) {
                "OPEN_WHATSAPP" -> PhoneAction(
                    ActionType.OPEN_WHATSAPP,
                    "Open WhatsApp",
                    "Launching WhatsApp messenger"
                )
                "OPEN_YOUTUBE" -> PhoneAction(
                    ActionType.OPEN_YOUTUBE,
                    "Open YouTube",
                    "Launching YouTube",
                    param
                )
                "OPEN_SETTINGS" -> PhoneAction(
                    ActionType.OPEN_SETTINGS,
                    "Open Settings",
                    "Opening Android system settings"
                )
                "OPEN_MAPS" -> PhoneAction(
                    ActionType.OPEN_MAPS,
                    "Open Navigation",
                    "Opening Google Maps",
                    param
                )
                "OPEN_CAMERA" -> PhoneAction(
                    ActionType.OPEN_CAMERA,
                    "Open Camera",
                    "Opening Camera"
                )
                "CALL_CONTACT" -> PhoneAction(
                    ActionType.CALL_CONTACT,
                    "Place Call",
                    "Call $param",
                    param,
                    requiresConfirmation = true
                )
                "SET_ALARM" -> PhoneAction(
                    ActionType.SET_ALARM,
                    "Set Alarm",
                    "Configure alarm for $param",
                    param
                )
                "SEARCH" -> PhoneAction(
                    ActionType.WEB_SEARCH,
                    "Web Search",
                    "Search for $param",
                    param
                )
                "MEDITATION_GUIDE" -> PhoneAction(
                    ActionType.GUIDED_MEDITATION,
                    "Start Meditation",
                    "Begin custom meditation session",
                    param
                )
                else -> null
            }
            return Pair(cleanText, action)
        }

        // Natural language heuristic fallback if action tag wasn't explicitly emitted
        val lower = text.lowercase()
        val detectedAction = when {
            lower.contains("opening whatsapp") || lower.contains("launching whatsapp") ->
                PhoneAction(ActionType.OPEN_WHATSAPP, "Open WhatsApp", "Opening WhatsApp")
            lower.contains("opening youtube") || lower.contains("launching youtube") ->
                PhoneAction(ActionType.OPEN_YOUTUBE, "Open YouTube", "Opening YouTube")
            lower.contains("opening settings") || lower.contains("system settings") ->
                PhoneAction(ActionType.OPEN_SETTINGS, "Open Settings", "Opening Settings")
            lower.contains("opening camera") ->
                PhoneAction(ActionType.OPEN_CAMERA, "Open Camera", "Opening Camera")
            else -> null
        }

        return Pair(text, detectedAction)
    }

    fun executeAction(context: Context, action: PhoneAction): Boolean {
        try {
            when (action.type) {
                ActionType.OPEN_WHATSAPP -> {
                    val pm = context.packageManager
                    val launchIntent = pm.getLaunchIntentForPackage("com.whatsapp")
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return true
                    } else {
                        // Open web or store
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                        return true
                    }
                }
                ActionType.OPEN_YOUTUBE -> {
                    val pm = context.packageManager
                    val launchIntent = pm.getLaunchIntentForPackage("com.google.android.youtube")
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return true
                    } else {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(webIntent)
                        return true
                    }
                }
                ActionType.OPEN_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return true
                }
                ActionType.OPEN_MAPS -> {
                    val query = if (action.param.isNotBlank()) action.param else "nearby"
                    val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query))
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(mapIntent)
                    return true
                }
                ActionType.OPEN_CAMERA -> {
                    val cameraIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(cameraIntent)
                    return true
                }
                ActionType.CALL_CONTACT -> {
                    // DIAL intent does not place call automatically, opens dialer with number prefilled! Safe & transparent.
                    val phone = action.param.ifBlank { "" }
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phone)}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                    return true
                }
                ActionType.SET_ALARM -> {
                    val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_MESSAGE, "SANA Alarm: ${action.param}")
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(alarmIntent)
                    return true
                }
                ActionType.WEB_SEARCH -> {
                    val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, action.param)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(searchIntent)
                    return true
                }
                ActionType.GUIDED_MEDITATION, ActionType.NONE -> {
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing phone action: ${action.type}", e)
            Toast.makeText(context, "Could not perform action: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}
