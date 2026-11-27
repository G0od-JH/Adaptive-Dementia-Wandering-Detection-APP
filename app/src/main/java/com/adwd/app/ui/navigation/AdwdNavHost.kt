package com.adwd.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.adwd.app.ui.components.DemoMapPanel
import com.adwd.app.ui.components.GuardianTab
import com.adwd.app.ui.components.TrajectoryMap
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.screens.demo.SimControlScreen
import com.adwd.app.ui.screens.elder.ElderGuideScreen
import com.adwd.app.ui.screens.elder.ElderHomeScreen
import com.adwd.app.ui.screens.guardian.AlertDetailScreen
import com.adwd.app.ui.screens.guardian.AlertListScreen
import com.adwd.app.ui.screens.guardian.GuardianHomeScreen
import com.adwd.app.ui.screens.guardian.MissingReportScreen
import com.adwd.app.ui.screens.guardian.PlaceEditScreen
import com.adwd.app.ui.screens.guardian.PlacesMode
import com.adwd.app.ui.screens.guardian.PlacesScreen
import com.adwd.app.ui.screens.guardian.SettingsScreen
import com.adwd.app.ui.screens.start.GuardianLinkScreen
import com.adwd.app.ui.screens.start.LoginScreen
import com.adwd.app.ui.screens.start.OnboardingScreen
import com.adwd.app.ui.screens.start.SignupScreen
import com.adwd.app.ui.screens.start.SplashScreen
import com.adwd.app.ui.state.AdwdViewModel
import com.adwd.app.ui.state.UserRole

/** 화면 번호는 피그마 Screen 페이지와 같다. */
object Routes {
    const val SPLASH = "splash"            // 01
    const val LOGIN = "login"              // 02
    const val SIGNUP = "signup"            // 02-1
    const val ONBOARDING = "onboarding"    // 03
    const val LINK = "link"                // 04
    const val PLACES_SETUP = "places/setup" // 04-1 (연동 직후)
    const val G_PLACES = "guardian/places" // 04-1 (설정에서 관리)
    const val G_PLACE = "guardian/place"   // 04-2 (guardian/place/{id}, -1 = 새 장소)
    const val G_HOME = "guardian/home"     // 05
    const val G_ALERTS = "guardian/alerts" // 05-1
    const val G_DETAIL = "guardian/alert"  // 06 (guardian/alert/{id})
    const val G_MISSING = "guardian/missing" // 07
    const val G_SETTINGS = "guardian/settings" // 08
    const val G_HOME_LOC = "guardian/home-location" // 08-1
    const val ELDER = "elder"              // 09, 10, 10-2, 10-3 (경보 단계에 따라 화면이 바뀜)
    const val SIM = "sim"                  // 11
}

@Composable
fun AdwdNavHost(
    nav: NavHostController = rememberNavController(),
    vm: AdwdViewModel = viewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    fun goHomeOf(role: UserRole) {
        nav.navigate(if (role == UserRole.GUARDIAN) Routes.G_HOME else Routes.ELDER) {
            popUpTo(0) { inclusive = true }
        }
    }

    fun onTab(tab: GuardianTab) {
        val route = when (tab) {
            GuardianTab.HOME -> Routes.G_HOME
            GuardianTab.ALERTS -> Routes.G_ALERTS
            GuardianTab.SETTINGS -> Routes.G_SETTINGS
        }
        nav.navigate(route) {
            popUpTo(Routes.G_HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = nav, startDestination = Routes.SPLASH) {
        // ── 시작 · 로그인 · 연동 ──
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                // TODO(민규 User 모델): 저장된 로그인이 있으면 바로 goHomeOf(role)
                nav.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLogin = { firstLogin ->
                    if (firstLogin) nav.navigate(Routes.ONBOARDING) else goHomeOf(state.role)
                },
                onSignup = { nav.navigate(Routes.SIGNUP) },
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(onBack = { nav.popBackStack() }, onDone = { nav.popBackStack() })
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onStart = { role ->
                vm.setRole(role)
                if (role == UserRole.GUARDIAN) nav.navigate(Routes.LINK) else goHomeOf(UserRole.ELDER)
            })
        }
        composable(Routes.LINK) {
            GuardianLinkScreen(
                onBack = { nav.popBackStack() },
                onLinked = { vm.setLinked(true); nav.navigate(Routes.PLACES_SETUP) },
            )
        }

        // ── 보호자 ──
        composable(Routes.G_HOME) {
            GuardianHomeScreen(
                level = state.level,
                reason = state.reason,
                onOpenAlert = { level ->
                    when (level) {
                        AlertLevel.EMERGENCY -> nav.navigate(Routes.G_MISSING)
                        AlertLevel.WARNING -> nav.navigate("${Routes.G_DETAIL}/0")
                        else -> onTab(GuardianTab.ALERTS)
                    }
                },
                onBell = { onTab(GuardianTab.ALERTS) },
                onTab = ::onTab,
                onOpenDemo = { nav.navigate(Routes.SIM) },
                mapContent = {
                    TrajectoryMap(state.track, state.current, Modifier.fillMaxSize(), accent = state.level.color)
                },
            )
        }
        composable(Routes.G_ALERTS) {
            AlertListScreen(
                currentLevel = state.level,
                onOpen = { id -> nav.navigate("${Routes.G_DETAIL}/$id") },
                onTab = ::onTab,
            )
        }
        composable(
            "${Routes.G_DETAIL}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: 0
            AlertDetailScreen(
                detail = FakeData.alertDetails[id] ?: FakeData.alertDetails.getValue(0),
                onBack = { nav.popBackStack() },
                onMissingReport = { nav.navigate(Routes.G_MISSING) },
                onDismiss = { vm.dismissAlert(); nav.popBackStack() },
                mapContent = { accent -> DemoMapPanel(MapPanelMode.DEVIATION, state.scenario, Modifier.fillMaxSize(), accent = accent) },
            )
        }
        composable(Routes.G_MISSING) {
            val cur = state.current
            // 최근 위치 기록: 오늘 경로에서 10분 간격으로 최근 4개
            val recent = state.track.filter { it.time.substring(15, 16) == "0" }.takeLast(4).reversed()
                .map { it.hhmm to coordLabel(it.x, it.y) }
            MissingReportScreen(
                onBack = { nav.popBackStack() },
                onResolve = { vm.dismissAlert(); nav.popBackStack() },
                lastSeenTime = cur?.hhmm ?: "-",
                lastSeenPlace = cur?.let { coordLabel(it.x, it.y) + " (모의 좌표)" } ?: "위치 기록 없음",
                recent = recent,
                mapContent = {
                    TrajectoryMap(state.track.takeLast(60), cur, Modifier.fillMaxSize(), accent = AlertLevel.EMERGENCY.color, pathColor = AlertLevel.EMERGENCY.color)
                },
            )
        }
        composable(Routes.G_SETTINGS) {
            SettingsScreen(
                onTab = ::onTab,
                placeCount = state.places.count { !it.isHome },
                onHomeLocation = { nav.navigate(Routes.G_HOME_LOC) },
                onPlaces = { nav.navigate(Routes.G_PLACES) },
                onLogout = { nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
            )
        }

        // ── 장소 (콜드 스타트 대비) ── 04-1, 04-2, 08-1
        listOf(Routes.PLACES_SETUP to PlacesMode.SETUP, Routes.G_PLACES to PlacesMode.MANAGE).forEach { (route, mode) ->
            composable(route) {
                PlacesScreen(
                    mode = mode,
                    places = state.places,
                    onBack = { nav.popBackStack() },
                    onAdd = { nav.navigate("${Routes.G_PLACE}/-1") },
                    onEdit = { nav.navigate("${Routes.G_PLACE}/${it.id}") },
                    onRemove = { vm.removePlace(it.id) },
                    onDone = { goHomeOf(UserRole.GUARDIAN) },
                )
            }
        }
        composable(
            "${Routes.G_PLACE}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
        ) { entry ->
            val id = entry.arguments?.getInt("id") ?: -1
            PlaceEditScreen(
                initial = state.places.firstOrNull { it.id == id },
                homeOnly = false,
                others = state.places,
                onBack = { nav.popBackStack() },
                onSave = { vm.upsertPlace(it); nav.popBackStack() },
                onDelete = { vm.removePlace(it.id); nav.popBackStack() },
            )
        }
        composable(Routes.G_HOME_LOC) {
            PlaceEditScreen(
                initial = state.home,
                homeOnly = true,
                others = state.places,
                onBack = { nav.popBackStack() },
                onSave = { vm.upsertPlace(it); nav.popBackStack() },
                onDelete = {},
            )
        }

        // ── 어르신 ── 경보 단계가 바뀌면 이 자리에서 화면 전체가 바뀐다 (09 ↔ 10 / 10-2 / 10-3)
        composable(Routes.ELDER) {
            if (state.level == AlertLevel.NORMAL) {
                ElderHomeScreen(isLinked = state.isLinked, onOpenDemo = { nav.navigate(Routes.SIM) })
            } else {
                ElderGuideScreen(level = state.level)
            }
        }

        // ── 데모 ──
        composable(Routes.SIM) {
            SimControlScreen(
                state = state,
                onScenario = vm::setScenario,
                onSpeed = vm::setSpeed,
                onTogglePlay = vm::togglePlay,
                onLevel = vm::forceLevel,
                onBack = { nav.popBackStack() },
            )
        }
    }
}

/** 모의 좌표(m, 집 = 0,0)를 "집에서 동쪽 350m · 북쪽 600m" 처럼 읽기 쉽게 */
private fun coordLabel(x: Double, y: Double): String {
    val ew = if (x >= 0) "동쪽" else "서쪽"
    val ns = if (y >= 0) "북쪽" else "남쪽"
    return "집에서 $ew ${kotlin.math.abs(x).toInt()}m · $ns ${kotlin.math.abs(y).toInt()}m"
}
