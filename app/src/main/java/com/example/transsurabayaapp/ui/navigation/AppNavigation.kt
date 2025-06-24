package com.example.transsurabayaapp.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.transsurabayaapp.ui.screens.booking.BookingScreen
import com.example.transsurabayaapp.ui.screens.home.HomeScreen
import com.example.transsurabayaapp.ui.screens.map.MapScreen
import com.example.transsurabayaapp.ui.screens.profile.ProfileScreen
import com.example.transsurabayaapp.ui.screens.routedetail.RouteDetailScreen
import com.example.transsurabayaapp.ui.screens.tickets.TicketScreen
import com.example.transsurabayaapp.viewmodel.TransSurabayaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: TransSurabayaViewModel = viewModel()
    val bottomNavItems = getBottomNavItems()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val isMainScreen = bottomNavItems.any { it.route == currentDestination?.route }

            AnimatedVisibility(
                visible = isMainScreen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(navController = navController, innerPadding = innerPadding, viewModel = viewModel)
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    viewModel: TransSurabayaViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(innerPadding),
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        val slideTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition) = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300))
        }
        val slidePopExitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition) = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300))
        }
        val slidePopEnterTransition: (AnimatedContentTransitionScope<*>.() -> EnterTransition) = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300))
        }
        val slideExitTransition: (AnimatedContentTransitionScope<*>.() -> ExitTransition) = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300))
        }

        composable("home") { HomeScreen(viewModel, navController) }
        composable("map") { MapScreen(viewModel) }
        composable("tickets") { TicketScreen(viewModel) }

        composable(
            "profile",
            enterTransition = slideTransition,
            exitTransition = slideExitTransition,
            popEnterTransition = slidePopEnterTransition,
            popExitTransition = slidePopExitTransition
        ) { ProfileScreen(viewModel, navController) }

        composable(
            "route_detail",
            enterTransition = slideTransition,
            exitTransition = slideExitTransition,
            popEnterTransition = slidePopEnterTransition,
            popExitTransition = slidePopExitTransition
        ) { RouteDetailScreen(viewModel, navController) }

        composable(
            "booking",
            enterTransition = slideTransition,
            exitTransition = slideExitTransition,
            popEnterTransition = slidePopEnterTransition,
            popExitTransition = slidePopExitTransition
        ) { BookingScreen(viewModel, navController) }
    }
}