package com.abumoath.tv.ui.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abumoath.tv.data.local.SubscriptionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الاشتراكات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("رجوع", modifier = Modifier.padding(8.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة اشتراك")
            }
        }
    ) { paddingValues ->
        if (subscriptions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("لا توجد اشتراكات مضافة حالياً", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subscriptions) { sub ->
                    SubscriptionCard(sub, onDelete = { viewModel.deleteSubscription(it) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, type, url, server, username, password ->
                viewModel.addSubscription(name, type, url, server, username, password)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SubscriptionCard(sub: SubscriptionEntity, onDelete: (SubscriptionEntity) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = sub.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = "النوع: ${sub.type}", style = MaterialTheme.typography.bodyMedium)
                sub.url?.let { Text(text = "الرابط: $it", style = MaterialTheme.typography.bodySmall, maxLines = 1) }
                sub.server?.let { Text(text = "الخادم: $it", style = MaterialTheme.typography.bodySmall, maxLines = 1) }
            }
            IconButton(onClick = { onDelete(sub) }) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String?, String?, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("M3U") } // M3U أو XTREAM
    var url by remember { mutableStateOf("") }
    var server by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة اشتراك جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الاشتراك") },
                    modifier = Modifier.fillMaxWidth()
                )
                // اختيار النوع
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("النوع: ")
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = type == "M3U",
                        onClick = { type = "M3U" },
                        label = { Text("M3U") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = type == "XTREAM",
                        onClick = { type = "XTREAM" },
                        label = { Text("Xtream Codes") }
                    )
                }

                if (type == "M3U") {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("رابط M3U") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = server,
                        onValueChange = { server = it },
                        label = { Text("الخادم (Server URL)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("اسم المستخدم") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            name,
                            type,
                            if (type == "M3U") url else null,
                            if (type == "XTREAM") server else null,
                            if (type == "XTREAM") username else null,
                            if (type == "XTREAM") password else null
                        )
                    }
                }
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}