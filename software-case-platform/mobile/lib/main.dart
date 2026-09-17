import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'core/api/api_client.dart';
import 'core/constants/api_constants.dart';
import 'data/local/dao/cliente_dao.dart';
import 'data/local/dao/reserva_dao.dart';
import 'data/local/dao/servicio_dao.dart';
import 'data/local/dao/sync_error_dao.dart';
import 'data/local/dao/sync_queue_dao.dart';
import 'data/local/database/app_database.dart';
import 'providers/ai_assistant_provider.dart';
import 'providers/app_state_provider.dart';
import 'providers/auth_provider.dart';
import 'providers/barberia_provider.dart';
import 'providers/sync_provider.dart';
import 'repositories/barberia_repository.dart';
import 'screens/dashboard_screen.dart';
import 'screens/login_screen.dart';
import 'services/ai/local_ai_service.dart';
import 'services/auth_service.dart';
import 'services/barbero_service.dart';
import 'services/cliente_service.dart';
import 'services/connectivity/connectivity_service.dart';
import 'services/reserva_service.dart';
import 'services/servicio_service.dart';
import 'services/storage/storage_service.dart';
import 'services/sync/sync_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final storageService = StorageService();
  final savedUrl = await storageService.getBaseUrl();
  final token = await storageService.getToken();

  final apiClient = ApiClient(
    baseUrl: savedUrl ?? ApiConstants.defaultLocalhostUrl,
    token: token,
  );

  final database = AppDatabase();
  await database.initialize();

  final clienteDao = ClienteDao(database: database);
  final reservaDao = ReservaDao(database: database);
  final servicioDao = ServicioDao(database: database);
  final syncQueueDao = SyncQueueDao(database: database);
  final syncErrorDao = SyncErrorDao(database: database);

  final connectivityService = ConnectivityService();

  final authService = AuthService(apiClient: apiClient, storageService: storageService);
  final clienteService = ClienteService(apiClient: apiClient);
  final barberoService = BarberoService(apiClient: apiClient);
  final servicioService = ServicioService(apiClient: apiClient);
  final reservaService = ReservaService(apiClient: apiClient);

  final syncService = SyncService(
    syncQueueDao: syncQueueDao,
    syncErrorDao: syncErrorDao,
    clienteDao: clienteDao,
    reservaDao: reservaDao,
    servicioDao: servicioDao,
    clienteService: clienteService,
    reservaService: reservaService,
    servicioService: servicioService,
    connectivityService: connectivityService,
  );

  final repository = BarberiaRepository(
    clienteDao: clienteDao,
    reservaDao: reservaDao,
    servicioDao: servicioDao,
    syncQueueDao: syncQueueDao,
    clienteService: clienteService,
    barberoService: barberoService,
    servicioService: servicioService,
    reservaService: reservaService,
    connectivityService: connectivityService,
  );

  runApp(
    CasePlatformMobileApp(
      authService: authService,
      storageService: storageService,
      repository: repository,
      syncService: syncService,
      syncQueueDao: syncQueueDao,
      syncErrorDao: syncErrorDao,
      connectivityService: connectivityService,
    ),
  );
}

class CasePlatformMobileApp extends StatelessWidget {
  final AuthService? authService;
  final StorageService? storageService;
  final BarberiaRepository? repository;
  final SyncService? syncService;
  final SyncQueueDao? syncQueueDao;
  final SyncErrorDao? syncErrorDao;
  final ConnectivityService? connectivityService;

  const CasePlatformMobileApp({
    super.key,
    this.authService,
    this.storageService,
    this.repository,
    this.syncService,
    this.syncQueueDao,
    this.syncErrorDao,
    this.connectivityService,
  });

  @override
  Widget build(BuildContext context) {
    final effectiveStorage = storageService ?? StorageService();
    final effectiveApiClient = ApiClient();
    final effectiveAuth = authService ??
        AuthService(apiClient: effectiveApiClient, storageService: effectiveStorage);

    final effectiveDb = AppDatabase(inMemory: true);
    final effectiveClienteDao = ClienteDao(database: effectiveDb);
    final effectiveReservaDao = ReservaDao(database: effectiveDb);
    final effectiveServicioDao = ServicioDao(database: effectiveDb);
    final effectiveQueueDao = syncQueueDao ?? SyncQueueDao(database: effectiveDb);
    final effectiveErrorDao = syncErrorDao ?? SyncErrorDao(database: effectiveDb);
    final effectiveConn = connectivityService ?? ConnectivityService(startPolling: false);

    final effectiveClienteService = ClienteService(apiClient: effectiveApiClient);
    final effectiveReservaService = ReservaService(apiClient: effectiveApiClient);
    final effectiveServicioService = ServicioService(apiClient: effectiveApiClient);

    final effectiveSyncService = syncService ??
        SyncService(
          syncQueueDao: effectiveQueueDao,
          syncErrorDao: effectiveErrorDao,
          clienteDao: effectiveClienteDao,
          reservaDao: effectiveReservaDao,
          servicioDao: effectiveServicioDao,
          clienteService: effectiveClienteService,
          reservaService: effectiveReservaService,
          servicioService: effectiveServicioService,
          connectivityService: effectiveConn,
          autoStart: false,
        );

    final effectiveRepo = repository ??
        BarberiaRepository(
          clienteDao: effectiveClienteDao,
          reservaDao: effectiveReservaDao,
          servicioDao: effectiveServicioDao,
          syncQueueDao: effectiveQueueDao,
          clienteService: effectiveClienteService,
          barberoService: BarberoService(apiClient: effectiveApiClient),
          servicioService: effectiveServicioService,
          reservaService: effectiveReservaService,
          connectivityService: effectiveConn,
        );

    return MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (_) => AuthProvider(
            authService: effectiveAuth,
            storageService: effectiveStorage,
          ),
        ),
        ChangeNotifierProvider(
          create: (_) => BarberiaProvider(
            repository: effectiveRepo,
          ),
        ),
        ChangeNotifierProvider(
          create: (_) => SyncProvider(
            syncService: effectiveSyncService,
            syncQueueDao: effectiveQueueDao,
            syncErrorDao: effectiveErrorDao,
            connectivityService: effectiveConn,
          ),
        ),
        ChangeNotifierProvider(
          create: (_) => AIAssistantProvider(
            service: LocalAIService(),
          ),
        ),
        ChangeNotifierProvider(
          create: (_) => AppStateProvider(),
        ),
      ],
      child: Consumer<AuthProvider>(
        builder: (context, auth, _) {
          return MaterialApp(
            title: 'BarberShop CASE Mobile',
            debugShowCheckedModeBanner: false,
            theme: ThemeData(
              useMaterial3: true,
              colorScheme: ColorScheme.fromSeed(
                seedColor: const Color(0xFFD97706),
                brightness: Brightness.light,
              ),
              fontFamily: 'Roboto',
            ),
            darkTheme: ThemeData(
              useMaterial3: true,
              colorScheme: ColorScheme.fromSeed(
                seedColor: const Color(0xFFD97706),
                brightness: Brightness.dark,
              ),
              fontFamily: 'Roboto',
            ),
            themeMode: ThemeMode.system,
            home: auth.isAuthenticated ? const DashboardScreen() : const LoginScreen(),
          );
        },
      ),
    );
  }
}
