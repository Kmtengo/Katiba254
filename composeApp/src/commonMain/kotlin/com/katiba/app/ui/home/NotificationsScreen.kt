package com.katiba.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.katiba.app.ui.theme.KatibaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
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
        val notifications = listOf(
            NotificationItem("BibleProject has a new Plan: \"BibleProject | Finding God in the Wilderness\"", "9 w"),
            NotificationItem("Kevin Obote accepted your friend request", "12 w"),
            NotificationItem("Passion Movement has a new Plan: \"Relat(able): Making Relationships Work\"", "29 w"),
            NotificationItem("Passion Movement has a new Plan: \"The Comeback: It's Not Too Late And You're Never Too Far\"", "29 w"),
            NotificationItem("Burning Heart has a new teaching clip: \"More on Jesus' Parables\"", "32 w"),
            NotificationItem("Burning Heart has a new teaching clip: \"Luke 18:14\"", "32 w"),
            NotificationItem("CBN has a new Plan: \"Finding Hope While Navigating Depression: A 7-Day Devotional\"", "35 w"),
            NotificationItem("BibleProject has a new Video: \"The Exodus Way\"", "37 w"),
            NotificationItem("Burning Heart has a new Video: \"The Best Thing Ever\"", "44 w")
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(notifications) { notification ->
                NotificationRow(notification)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

data class NotificationItem(val text: String, val time: String)

@Composable
fun NotificationRow(notification: NotificationItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(KatibaColors.KenyaRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = KatibaColors.KenyaRed,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = notification.time,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}
