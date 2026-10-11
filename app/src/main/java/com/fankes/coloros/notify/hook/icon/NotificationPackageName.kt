package com.fankes.coloros.notify.hook.icon

import android.service.notification.StatusBarNotification

private val PACKAGE_NAME_PATTERN = Regex("[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)+")

internal fun looksLikePackageName(value: String): Boolean = PACKAGE_NAME_PATTERN.matches(value)

/**
 * Turns whatever the host hands over into a package name.
 *
 * ColorOS calls the same icon helpers with either a bare package (`com.tencent.mm`) or with the
 * whole status-bar notification key (`0|com.tencent.mm|-1125165008|null|10384|com.tencent.mm`),
 * depending on the caller. Feeding the key into the desktop theme lookup can never match, which is
 * exactly why the lock-screen hero kept the stock icon while every caller that happened to pass the
 * bare package was already themed.
 */
internal fun normalizePackageName(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return null
    if ('|' !in value) return value.takeIf(::looksLikePackageName)
    return value.split('|').firstOrNull(::looksLikePackageName)
}

/**
 * Package name for a hook callback: the notification wins when it is present, otherwise the first
 * string argument is normalized (see [normalizePackageName]).
 */
internal fun packageNameOf(args: List<Any?>): String? {
    args.filterIsInstance<StatusBarNotification>().firstOrNull()?.packageName
        ?.let { normalizePackageName(it) }?.let { return it }
    // Some overloads take the key first and the package later, so every string is tried.
    for (arg in args) {
        if (arg !is String) continue
        normalizePackageName(arg)?.let { return it }
    }
    return null
}
