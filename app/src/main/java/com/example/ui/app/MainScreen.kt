package com.example.ui.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen(
    viewModel: VetroViewModel = viewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val imeStatus by viewModel.imeSetupStatus.collectAsStateWithLifecycle()
    val textValue by viewModel.playgroundTextValue.collectAsStateWithLifecycle()
    val keyboardState by viewModel.playgroundKeyboardState.collectAsStateWithLifecycle()
    val feedback by viewModel.playgroundFeedback.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshImeStatus()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 720.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { TabIcon(tab = tab, selected = selected) },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useNavigationRail) {
                    NavigationRail {
                        AppTab.entries.forEach { tab ->
                            val selected = selectedTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { TabIcon(tab = tab, selected = selected) },
                                label = { Text(tab.label) },
                            )
                        }
                    }
                }

                when (selectedTab) {
                    AppTab.PLAYGROUND -> PlaygroundScreen(
                        settings = settings,
                        imeStatus = imeStatus,
                        textValue = textValue,
                        keyboardState = keyboardState,
                        controller = viewModel.playgroundController,
                        feedbackMessage = feedback,
                        onClearFeedback = viewModel::clearPlaygroundFeedback,
                        onTextSelectionChanged = viewModel.playgroundTarget::updateFromExternalField,
                        onClearText = viewModel::clearPlaygroundText,
                        onSampleText = viewModel::resetPlaygroundSample,
                        onSelectContext = viewModel::setSimulatedPlaygroundContext,
                        onOpenImePicker = viewModel::openSystemImePicker,
                        onOpenStudio = { viewModel.selectTab(AppTab.STUDIO) },
                        modifier = Modifier.fillMaxSize(),
                    )
                    AppTab.STUDIO -> GlassStudioScreen(
                        settings = settings,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TabIcon(tab: AppTab, selected: Boolean) {
    val icon = when (tab) {
        AppTab.PLAYGROUND -> if (selected) Icons.Rounded.Keyboard else Icons.Outlined.Keyboard
        AppTab.STUDIO -> if (selected) Icons.Rounded.AutoAwesome else Icons.Outlined.AutoAwesome
    }
    Icon(imageVector = icon, contentDescription = tab.label)
}
