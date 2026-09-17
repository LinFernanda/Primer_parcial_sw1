import '../core/api/api_client.dart';
import '../core/constants/api_constants.dart';
import '../models/auth_user.dart';
import 'storage/storage_service.dart';

class AuthService {
  final ApiClient apiClient;
  final StorageService storageService;

  AuthService({
    required this.apiClient,
    required this.storageService,
  });

  Future<AuthUser> login(String email, String password) async {
    // Si se usa modo demo o credenciales demo, permitir acceso rápido
    if (email == 'demo@barberia.com' && password == 'demo123') {
      final demoUser = AuthUser(
        id: 1,
        email: email,
        nombreCompleto: 'Administrador Barbershop Demo',
        rol: 'ADMIN',
        token: 'demo-mock-jwt-token',
      );
      apiClient.token = demoUser.token;
      await storageService.saveToken(demoUser.token);
      await storageService.saveUser(demoUser);
      return demoUser;
    }

    final response = await apiClient.post(ApiConstants.authLogin, {
      'email': email,
      'password': password,
    });

    String token = '';
    Map<String, dynamic> userMap = {};

    if (response is Map<String, dynamic>) {
      token = response['token'] ?? response['accessToken'] ?? '';
      userMap = response['usuario'] ?? response['user'] ?? response;
    }

    final authUser = AuthUser.fromJson(userMap, token);
    apiClient.token = token;
    await storageService.saveToken(token);
    await storageService.saveUser(authUser);
    return authUser;
  }

  Future<AuthUser> register(String email, String password, String nombreCompleto) async {
    final response = await apiClient.post(ApiConstants.authRegister, {
      'email': email,
      'password': password,
      'nombreCompleto': nombreCompleto,
    });

    String token = '';
    Map<String, dynamic> userMap = {};

    if (response is Map<String, dynamic>) {
      token = response['token'] ?? response['accessToken'] ?? '';
      userMap = response['usuario'] ?? response['user'] ?? response;
    }

    final authUser = AuthUser.fromJson(userMap, token);
    apiClient.token = token;
    await storageService.saveToken(token);
    await storageService.saveUser(authUser);
    return authUser;
  }

  Future<void> logout() async {
    apiClient.token = null;
    await storageService.clearAuth();
  }
}
