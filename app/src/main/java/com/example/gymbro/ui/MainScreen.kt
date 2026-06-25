package com.example.gymbro.ui

import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gymbro.ui.calendar.CalendarScreen
import com.example.gymbro.ui.health.HealthScreen
import com.example.gymbro.ui.profile.ProfileScreen
import com.example.gymbro.ui.workouts.WorkoutsScreen
import com.example.gymbro.ui.theme.*

sealed class Screen(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Workouts : Screen("workouts", "Workouts", Icons.Default.FitnessCenter)
    object Health   : Screen("health",   "Health",   Icons.Default.HealthAndSafety)
    object Profile  : Screen("profile",  "Profile",  Icons.Default.Person)
}

@Composable
fun MainScreen(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color.Transparent, // Transparent so background shows through
        bottomBar = {
            BrutalistBottomBar(navController)
        },
    ) { innerPadding ->
        DynamicBackground(isDarkTheme = themeViewModel.isDarkTheme.collectAsState().value) {
        NavHost(
            navController,
            startDestination = Screen.Calendar.route,
            Modifier.padding(innerPadding),
        ) {
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Workouts.route) { WorkoutsScreen() }
            composable(Screen.Health.route)   { HealthScreen()   }
            composable(Screen.Profile.route)  { ProfileScreen(themeViewModel) }
        }
        }
    }
}

@Composable
private fun BrutalistBottomBar(
    navController: androidx.navigation.NavHostController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val borderColor = BrutalistColors.border
    val navBg = BrutalistColors.navBar

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            // Thick top border
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(borderColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(navBg)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(Screen.Calendar, currentDestination, navController)
                BottomBarItem(Screen.Workouts, currentDestination, navController)
                BottomBarItem(Screen.Health, currentDestination, navController)
                BottomBarItem(Screen.Profile, currentDestination, navController)
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    screen: Screen,
    currentDestination: androidx.navigation.NavDestination?,
    navController: androidx.navigation.NavHostController
) {
    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
    val borderColor = BrutalistColors.border

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Icon(
            screen.icon,
            contentDescription = screen.label,
            tint = if (selected) BrutalistYellow else BrutalistColors.textMuted,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = screen.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) BrutalistYellow else BrutalistColors.textMuted,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}
