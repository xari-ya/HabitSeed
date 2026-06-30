package com.habitseed.app.ui.feedback

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberNotificationPermissionRequester(
    onGranted: () -> Unit,
    onDenied: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val currentOnGranted = rememberUpdatedState(onGranted)
    val currentOnDenied = rememberUpdatedState(onDenied)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            currentOnGranted.value()
        } else {
            currentOnDenied.value()
        }
    }

    return remember(context, launcher) {
        {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                currentOnGranted.value()
            } else {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    currentOnGranted.value()
                } else {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
