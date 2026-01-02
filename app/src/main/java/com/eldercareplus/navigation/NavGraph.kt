package com.eldercareplus.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eldercareplus.screens.*

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object Login : Screen("login")
    object Register : Screen("register")
    object Role : Screen("role")
    object Caretaker : Screen("caretaker")
    object Elder : Screen("elder")
    object PhoneOtp : Screen("phone_otp")
    object FallConfirmation : Screen("fall_confirmation")
    object Medicine : Screen("medicine")
    object Sleep : Screen("sleep")
    object Appointment : Screen("appointment")
    object PendingVerification : Screen("pending_verification")
    object Admin : Screen("admin")
    object ProfileSetup : Screen("profile_setup")
    object Games : Screen("games")
    object ElderSettings : Screen("elder_settings")
    object GovernmentSchemes : Screen("government_schemes")
    object EmergencyContacts : Screen("emergency_contacts")
    object CaretakerManagement : Screen("caretaker_management")
    object AlertHistory : Screen("alert_history")
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Start.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Screen.Start.route) {
            StartScreen(navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController)
        }

        composable(Screen.Role.route) {
            RoleSelectionScreen(navController)
        }

        // 🔥 FIX HERE
        composable(Screen.Caretaker.route) {
            CaretakerDashboard(navController)
        }

        // 🔥 FIX HERE
        composable(Screen.Elder.route) {
            ElderDashboard(navController)
        }
        composable(Screen.PhoneOtp.route) {
            PhoneOtpScreen(navController)
        }

        composable(Screen.FallConfirmation.route) {
            FallConfirmationScreen(navController)
        }

        composable(Screen.Medicine.route) {
            MedicineScreen(navController)
        }
        composable("medicine/{elderId}") { backStackEntry ->
            val elderId = backStackEntry.arguments?.getString("elderId")
            MedicineScreen(navController, elderId)
        }

        composable(Screen.Sleep.route) {
            SleepScreen(navController)
        }
        composable("sleep/{elderId}") { backStackEntry ->
            val elderId = backStackEntry.arguments?.getString("elderId")
            SleepScreen(navController, elderId)
        }

        composable(Screen.Appointment.route) {
            AppointmentScreen(navController)
        }
        composable("appointment/{elderId}") { backStackEntry ->
            val elderId = backStackEntry.arguments?.getString("elderId")
            AppointmentScreen(navController, elderId)
        }
        
        composable("caretaker_control/{elderId}") { backStackEntry ->
            val elderId = backStackEntry.arguments?.getString("elderId") 
            if (elderId != null) {
                CaretakerElderControlScreen(navController, elderId)
            }
        }

        composable(Screen.PendingVerification.route) {
            PendingVerificationScreen(navController)
        }

        composable(Screen.Admin.route) {
            AdminDashboard(navController)
        }
        
        composable(Screen.Games.route) {
            GamesScreen(navController)
        }
        
        composable(Screen.ElderSettings.route) {
            ElderSettingsScreen(navController)
        }
        
        composable(Screen.GovernmentSchemes.route) {
            GovernmentSchemesScreen(navController)
        }
        
        composable(Screen.EmergencyContacts.route) {
            EmergencyContactsScreen(navController)
        }
        
        composable(Screen.CaretakerManagement.route) {
            CaretakerManagementScreen(navController)
        }
        
        composable(Screen.AlertHistory.route) {
            AlertHistoryScreen(navController)
        }
    }
}
