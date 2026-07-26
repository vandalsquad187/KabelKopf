package com.elektriker.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object NewTask : Screen("new_task")
    data object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    data object Assistant : Screen("assistant/{taskId}") {
        fun createRoute(taskId: String) = "assistant/$taskId"
    }
    data object History : Screen("history")
    data object Knowledge : Screen("knowledge")
    data object KnowledgeDetail : Screen("knowledge/{entryId}") {
        fun createRoute(entryId: String) = "knowledge/$entryId"
    }
    data object Projects : Screen("projects")
    data object ProjectDetail : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: String) = "project_detail/$projectId"
    }
    data object NewTaskWithProject : Screen("new_task/{projectId}") {
        fun createRoute(projectId: String) = "new_task/$projectId"
    }
    data object Profile : Screen("profile")
    data object ErrorHistory : Screen("error_history")
    data object Skills : Screen("skills")
    data object Insights : Screen("insights")
    data object Achievements : Screen("achievements")
    data object TemplatePicker : Screen("template_picker")
    data object Search : Screen("search")
    data object Checklists : Screen("checklists")
    data object Welcome : Screen("welcome")
}
