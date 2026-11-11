package com.kkt.bookmark.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kkt.bookmark.ui.screens.FolderManagementScreen
import com.kkt.bookmark.ui.screens.HomeScreen
import com.kkt.bookmark.ui.screens.MainViewModel
import com.kkt.bookmark.ui.screens.SearchScreen
import com.kkt.bookmark.ui.screens.SettingsScreen

object Destinations {
    const val HOME = "home"
    const val SEARCH = "search"
    const val FOLDER_TREE = "folder_tree"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME
    ) {
        composable(Destinations.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSearch = { navController.navigate(Destinations.SEARCH) },
                onNavigateToFolderTree = { navController.navigate(Destinations.FOLDER_TREE) },
                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS) }
            )
        }

        composable(Destinations.SEARCH) {
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.FOLDER_TREE) {
            FolderManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSelectFolderToOpen = { folderId ->
                    viewModel.navigateToFolder(folderId)
                    navController.popBackStack()
                }
            )
        }

        composable(Destinations.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
