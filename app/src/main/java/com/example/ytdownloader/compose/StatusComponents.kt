package com.example.ytdownloader.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ytdownloader.client.ClientInstance

@Composable
fun StatusScaffold(nav: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Status") }) },
        bottomBar = { MainNav(nav) }
    ) {
        ClientInstance.checkStatusInformer()
        Status()
    }
}

@Composable
fun Status() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp, 8.dp)
    ) {
        items(ClientInstance.requests.values.toList()) { request ->
            Text(text = request.status.name)
        }
    }
}