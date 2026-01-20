package com.katiba.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.katiba.app.ui.theme.KatibaColors

/**
 * Settings screen with account, notifications, display preferences, and about sections
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                HorizontalDivider(thickness = 2.dp, color = Color.Gray.copy(alpha = 0.3f))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account section
            item {
                SettingsSection(title = "Account") {
                    SettingsItem(
                        icon = "👤",
                        title = "Edit Profile",
                        subtitle = "Name, email, avatar"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = "🔐",
                        title = "Password & Security",
                        subtitle = "Change password, 2FA"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = "📍",
                        title = "Update Residence",
                        subtitle = "County, constituency, ward"
                    )
                }
            }
            
            // Notifications section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsToggleItem(
                        icon = "🔔",
                        title = "Daily Reminders",
                        subtitle = "Get notified about your daily clause",
                        checked = true,
                        onCheckedChange = { }
                    )
                    HorizontalDivider()
                    SettingsToggleItem(
                        icon = "🎯",
                        title = "Streak Reminders",
                        subtitle = "Don't break your streak!",
                        checked = true,
                        onCheckedChange = { }
                    )
                    HorizontalDivider()
                    SettingsToggleItem(
                        icon = "📢",
                        title = "New Content",
                        subtitle = "Be notified of new lessons",
                        checked = false,
                        onCheckedChange = { }
                    )
                }
            }
            
            // Display section
            item {
                SettingsSection(title = "Display") {
                    SettingsItem(
                        icon = "🌙",
                        title = "Appearance",
                        subtitle = "Light, Dark, System"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = "🔤",
                        title = "Font Size",
                        subtitle = "Adjust text size"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = "🌐",
                        title = "Language",
                        subtitle = "English"
                    )
                }
            }
            
            // About section
            item {
                SettingsSection(title = "About") {
                    SettingsItem(
                        icon = "📖",
                        title = "About Katiba",
                        subtitle = "Learn about the app"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = "📜",
                        title = "Terms of Service",
                        subtitle = "Read our terms"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = "🔒",
                        title = "Privacy Policy",
                        subtitle = "How we protect your data"
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = "💬",
                        title = "Send Feedback",
                        subtitle = "Help us improve"
                    )
                }
            }
            
            // Version info
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "Katiba v1.0.0",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Beadwork accent
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            KatibaColors.KenyaBlack,
                                            KatibaColors.KenyaRed,
                                            KatibaColors.KenyaGreen
                                        )
                                    )
                                )
                        )
                    }
                }
            }
            
            // Sign out
            item {
                Surface(
                    color = KatibaColors.KenyaRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* Sign out */ }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.titleMedium,
                            color = KatibaColors.KenyaRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit = { }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(32.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(32.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = KatibaColors.KenyaWhite,
                checkedTrackColor = KatibaColors.KenyaGreen
            )
        )
    }
}
