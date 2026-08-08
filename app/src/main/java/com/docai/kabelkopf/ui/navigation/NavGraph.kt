package com.docai.kabelkopf.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.docai.kabelkopf.ui.screens.assistant.AssistantScreen
import com.docai.kabelkopf.ui.screens.history.HistoryScreen
import com.docai.kabelkopf.ui.screens.checklist.ChecklistScreen
import com.docai.kabelkopf.ui.screens.home.HomeScreen
import com.docai.kabelkopf.ui.screens.knowledge.KnowledgeScreen
import com.docai.kabelkopf.ui.screens.achievements.AchievementsScreen
import com.docai.kabelkopf.ui.screens.errorhistory.ErrorHistoryScreen
import com.docai.kabelkopf.ui.screens.insights.InsightsScreen
import com.docai.kabelkopf.ui.screens.profile.ProfileScreen
import com.docai.kabelkopf.ui.screens.skills.SkillsScreen
import com.docai.kabelkopf.ui.screens.project.ProjectDetailScreen
import com.docai.kabelkopf.ui.screens.project.ProjectScreen
import com.docai.kabelkopf.ui.screens.search.SearchScreen
import com.docai.kabelkopf.ui.screens.task.NewTaskScreen
import com.docai.kabelkopf.ui.screens.task.TaskDetailScreen
import com.docai.kabelkopf.ui.screens.welcome.WelcomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = androidx.navigation.compose.rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Projects.route) {
            ProjectScreen(navController = navController)
        }

        composable(
            route = Screen.ProjectDetail.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            ProjectDetailScreen(
                projectId = projectId,
                navController = navController
            )
        }

        composable(Screen.NewTask.route) {
            NewTaskScreen(navController = navController)
        }

        composable(Screen.NewTaskWithProject.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            NewTaskScreen(navController = navController, preSelectedProjectId = projectId)
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                navController = navController
            )
        }

        composable(
            route = Screen.Assistant.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            AssistantScreen(
                taskId = taskId,
                navController = navController
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(Screen.Knowledge.route) {
            KnowledgeScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.ErrorHistory.route) {
            ErrorHistoryScreen(navController = navController)
        }

        composable(Screen.Skills.route) {
            SkillsScreen(navController = navController)
        }

        composable(Screen.Insights.route) {
            InsightsScreen(navController = navController)
        }

        composable(Screen.Achievements.route) {
            AchievementsScreen(navController = navController)
        }

        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        composable(Screen.Checklists.route) {
            ChecklistScreen(navController = navController)
        }
    }
}
