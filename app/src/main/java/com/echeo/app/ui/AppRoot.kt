package com.echeo.app.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.echeo.app.data.BackupCodec
import com.echeo.app.data.LicenceConfig
import com.echeo.app.domain.formatDay
import com.echeo.app.domain.reminderText
import com.echeo.app.notify.ExpiryWorker
import com.echeo.app.ui.clients.ClientFilter
import com.echeo.app.ui.clients.ClientsContent
import com.echeo.app.ui.components.ConfirmDialog
import com.echeo.app.ui.components.EcheoBottomBar
import com.echeo.app.ui.components.Tab
import com.echeo.app.ui.dashboard.DashboardContent
import com.echeo.app.ui.screens.ActivationScreen
import com.echeo.app.ui.screens.AddSubscriptionScreen
import com.echeo.app.ui.screens.BackupScreen
import com.echeo.app.ui.screens.ClientDetailScreen
import com.echeo.app.ui.screens.HistoryScreen
import com.echeo.app.ui.screens.LicenceInvalidScreen
import com.echeo.app.ui.screens.MessagesScreen
import com.echeo.app.ui.screens.NewClientScreen
import com.echeo.app.ui.screens.NewTypeScreen
import com.echeo.app.ui.screens.OfflineScreen
import com.echeo.app.ui.screens.RemindersScreen
import com.echeo.app.ui.screens.RenewScreen
import com.echeo.app.ui.screens.SettingsScreen
import com.echeo.app.ui.screens.SplashScreen
import com.echeo.app.ui.screens.TypesScreen
import com.echeo.app.ui.screens.WelcomeScreen
import com.echeo.app.ui.theme.EcheoColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Écrans de l'app. La pile permet de revenir en arrière avec le bouton retour. */
sealed interface Route {
    data object Splash : Route
    data class Welcome(val page: Int) : Route
    data object Offline : Route
    data object Activation : Route
    data class LicenceInvalid(val message: String) : Route
    data class Main(val tab: Tab) : Route
    data object NewClient : Route
    data class Client(val id: Long) : Route
    data class AddSub(val clientId: Long) : Route
    data class Renew(val subId: Long) : Route
    data class History(val subId: Long) : Route
    data object Types : Route
    data object NewType : Route
    data object Messages : Route
    data object Backup : Route
}

@Composable
fun AppRoot(vm: AppViewModel) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val data by vm.data.collectAsState()
    val warnDays by vm.settings.warnDays.collectAsState()
    val hour by vm.settings.reminderHour.collectAsState()
    val notify by vm.settings.notifyEnabled.collectAsState()
    val waBefore by vm.settings.waBefore.collectAsState()
    val waAfter by vm.settings.waAfter.collectAsState()
    val licenceKey by vm.settings.licenceKey.collectAsState()
    val lastBackup by vm.settings.lastBackupAt.collectAsState()
    val deviceId = remember { vm.licence.deviceId() }

    val stack = remember { mutableStateListOf<Route>(Route.Splash) }
    val route = stack.last()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ClientFilter.All) }
    var activationError by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var pendingRestore by remember { mutableStateOf<String?>(null) }

    fun go(r: Route) {
        stack.add(r)
    }

    fun replaceAll(r: Route) {
        stack.clear()
        stack.add(r)
    }

    fun back() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun toast(message: String) = Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()

    /** Décide où aller au démarrage et à chaque retour au premier plan. */
    fun startFlow() {
        scope.launch {
            if (!vm.settings.onboardingDone) {
                replaceAll(Route.Welcome(0))
                return@launch
            }
            if (LicenceConfig.ENFORCED) {
                val key = vm.settings.licenceKey.value
                if (key == null) {
                    replaceAll(Route.Activation)
                    return@launch
                }
                vm.licence.verify(key).onFailure { e ->
                    replaceAll(Route.LicenceInvalid(e.message ?: "Licence non valide."))
                    return@launch
                }
            }
            replaceAll(Route.Main(Tab.Home))
        }
    }

    LaunchedEffect(Unit) {
        delay(1200)
        startFlow()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.refreshDate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = stack.size > 1) { back() }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                val json = BackupCodec.encode(vm.repo.snapshot())
                withContext(Dispatchers.IO) {
                    ctx.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                }
                vm.settings.setLastBackup(System.currentTimeMillis())
                toast("Sauvegarde enregistrée")
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                pendingRestore = withContext(Dispatchers.IO) {
                    ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }
            }
        }
    }

    val currentTab = (route as? Route.Main)?.tab

    Column(Modifier.fillMaxSize().background(EcheoColors.GrisClair)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (val r = route) {
                Route.Splash -> SplashScreen()

                is Route.Welcome -> WelcomeScreen(
                    page = r.page,
                    onNext = { replaceAll(Route.Welcome(r.page + 1)) },
                    onFinish = {
                        vm.settings.finishOnboarding()
                        startFlow()
                    },
                )

                Route.Offline -> OfflineScreen(onRetry = { startFlow() })

                Route.Activation -> ActivationScreen(
                    deviceId = deviceId,
                    error = activationError,
                    busy = busy,
                    onCopy = { Actions.copy(ctx, "ID Echeo", deviceId) },
                    onActivate = { key ->
                        scope.launch {
                            busy = true
                            activationError = null
                            vm.licence.activate(key)
                                .onSuccess {
                                    vm.settings.setLicenceKey(key)
                                    replaceAll(Route.Main(Tab.Home))
                                }
                                .onFailure { activationError = it.message ?: "Activation impossible." }
                            busy = false
                        }
                    },
                )

                is Route.LicenceInvalid -> LicenceInvalidScreen(
                    deviceId = deviceId,
                    message = r.message,
                    onNewKey = { go(Route.Activation) },
                    onCopy = { Actions.copy(ctx, "ID Echeo", deviceId) },
                )

                is Route.Main -> when (r.tab) {
                    Tab.Home -> DashboardContent(
                        shopName = data.shopName,
                        counts = data.counts,
                        clients = data.summaries,
                        onOpenReminders = { replaceAll(Route.Main(Tab.Reminders)) },
                        onOpenClient = { go(Route.Client(it)) },
                        onAddClient = { go(Route.NewClient) },
                        modifier = Modifier.fillMaxSize(),
                    )
                    Tab.Clients -> ClientsContent(
                        clients = data.summaries,
                        subscriptionCount = data.subscriptionCount,
                        query = query,
                        filter = filter,
                        onQueryChange = { query = it },
                        onFilterChange = { filter = it },
                        onAddClient = { go(Route.NewClient) },
                        onOpenClient = { go(Route.Client(it)) },
                        modifier = Modifier.fillMaxSize(),
                    )
                    Tab.Reminders -> RemindersScreen(
                        views = data.subViews,
                        today = data.today,
                        onCall = { v ->
                            Actions.dial(ctx, v.clientPhone)
                            scope.launch { vm.repo.markContacted(v.id) }
                        },
                        onWhatsApp = { v ->
                            Actions.whatsapp(ctx, v.clientPhone, reminderText(v, data.today, waBefore, waAfter))
                            scope.launch { vm.repo.markContacted(v.id) }
                        },
                        onRenew = { v -> go(Route.Renew(v.id)) },
                        onMessages = { go(Route.Messages) },
                    )
                    Tab.Settings -> SettingsScreen(
                        warnDays = warnDays,
                        reminderHour = hour,
                        notify = notify,
                        typesCount = data.types.size,
                        deviceId = deviceId,
                        licenceKey = licenceKey,
                        onWarnDays = { vm.settings.setWarnDays(it) },
                        onReminderHour = {
                            vm.settings.setReminderHour(it)
                            ExpiryWorker.schedule(ctx, it)
                        },
                        onNotify = { vm.settings.setNotifyEnabled(it) },
                        onOpenTypes = { go(Route.Types) },
                        onOpenMessages = { go(Route.Messages) },
                        onOpenBackup = { go(Route.Backup) },
                        onActivate = { go(Route.Activation) },
                        onCopy = { Actions.copy(ctx, "ID Echeo", deviceId) },
                    )
                }

                Route.NewClient -> NewClientScreen(
                    types = data.types.filter { it.enabled },
                    onBack = { back() },
                    onSave = { name, phone, typeId, months, start ->
                        scope.launch {
                            vm.repo.addClientWithSubscription(name, phone, typeId, months, start)
                            replaceAll(Route.Main(Tab.Clients))
                        }
                    },
                )

                is Route.Client -> {
                    val client = data.clients.firstOrNull { it.id == r.id }
                    if (client == null) {
                        LaunchedEffect(Unit) { back() }
                    } else {
                        ClientDetailScreen(
                            client = client,
                            views = data.subViews.filter { it.clientId == client.id },
                            onBack = { back() },
                            onCall = { Actions.dial(ctx, client.phone) },
                            onWhatsApp = { Actions.whatsapp(ctx, client.phone, null) },
                            onRenew = { v -> go(Route.Renew(v.id)) },
                            onHistory = { v -> go(Route.History(v.id)) },
                            onAddSub = { go(Route.AddSub(client.id)) },
                            onDelete = {
                                scope.launch {
                                    vm.repo.deleteClient(client)
                                    replaceAll(Route.Main(Tab.Clients))
                                }
                            },
                        )
                    }
                }

                is Route.AddSub -> {
                    val client = data.clients.firstOrNull { it.id == r.clientId }
                    if (client == null) {
                        LaunchedEffect(Unit) { back() }
                    } else {
                        AddSubscriptionScreen(
                            clientName = client.name,
                            types = data.types.filter { it.enabled },
                            onBack = { back() },
                            onSave = { typeId, months, start ->
                                scope.launch {
                                    vm.repo.addSubscription(client.id, typeId, months, start)
                                    back()
                                }
                            },
                        )
                    }
                }

                is Route.Renew -> {
                    val view = data.subViews.firstOrNull { it.id == r.subId }
                    if (view == null) {
                        LaunchedEffect(Unit) { back() }
                    } else {
                        RenewScreen(
                            view = view,
                            onBack = { back() },
                            onConfirm = { newExpiry, amount ->
                                scope.launch {
                                    vm.repo.subscriptionById(view.id)?.let { vm.repo.renew(it, newExpiry, amount) }
                                    toast("Renouvelé jusqu'au ${formatDay(newExpiry)}")
                                    back()
                                }
                            },
                        )
                    }
                }

                is Route.History -> {
                    val view = data.subViews.firstOrNull { it.id == r.subId }
                    if (view == null) {
                        LaunchedEffect(Unit) { back() }
                    } else {
                        val renewals by vm.renewalsFor(view.id).collectAsState(initial = emptyList())
                        HistoryScreen(view = view, renewals = renewals, onBack = { back() })
                    }
                }

                Route.Types -> TypesScreen(
                    types = data.types,
                    usage = data.subViews.groupingBy { it.typeId }.eachCount(),
                    onBack = { back() },
                    onAdd = { go(Route.NewType) },
                    onToggle = { t, enabled -> scope.launch { vm.repo.saveType(t.copy(enabled = enabled)) } },
                )

                Route.NewType -> NewTypeScreen(
                    onBack = { back() },
                    onSave = { type ->
                        scope.launch {
                            vm.repo.saveType(type.copy(sortOrder = data.types.size))
                            back()
                        }
                    },
                )

                Route.Messages -> MessagesScreen(
                    before = waBefore,
                    after = waAfter,
                    onBack = { back() },
                    onSave = { b, a ->
                        vm.settings.setWaBefore(b)
                        vm.settings.setWaAfter(a)
                        toast("Messages enregistrés")
                        back()
                    },
                )

                Route.Backup -> BackupScreen(
                    lastBackupAt = lastBackup,
                    onBack = { back() },
                    onExport = { exportLauncher.launch("echeo-sauvegarde.json") },
                    onRestore = { importLauncher.launch(arrayOf("*/*")) },
                )
            }
        }
        if (currentTab != null) {
            EcheoBottomBar(selected = currentTab, onSelect = { replaceAll(Route.Main(it)) })
        }
    }

    pendingRestore?.let {
        ConfirmDialog(
            title = "Restaurer cette sauvegarde ?",
            text = "La restauration remplace les données actuelles de l'application.",
            confirmLabel = "Restaurer",
            destructive = true,
            onConfirm = {
                val text = it
                pendingRestore = null
                scope.launch {
                    runCatching { BackupCodec.decode(text) }
                        .onSuccess {
                            vm.repo.restore(it)
                            toast("Sauvegarde restaurée")
                        }
                        .onFailure { toast("Fichier invalide") }
                }
            },
            onDismiss = { pendingRestore = null },
        )
    }
}
