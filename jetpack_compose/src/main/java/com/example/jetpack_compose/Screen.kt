package com.example.jetpack_compose

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen (
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector,
) {
    data object Home: Screen (
        route = "home",
        resourceId = R.string.home,
        icon = Icons.Filled.Home,
    )

    data object Profile: Screen (
        route = "profile",
        resourceId = R.string.profile,
        icon = Icons.Filled.AccountCircle,
    )
}