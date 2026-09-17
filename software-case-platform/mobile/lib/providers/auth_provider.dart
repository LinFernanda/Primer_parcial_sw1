import 'package:flutter/material.dart';
import '../models/auth_user.dart';
import '../services/auth_service.dart';
import '../services/storage/storage_service.dart';

enum AuthState { initial, authenticated, unauthenticated, loading }

class AuthProvider extends ChangeNotifier {
  final AuthService authService;
  final StorageService storageService;

  AuthUser? _user;
  AuthState _state = AuthState.initial;
  String? _errorMessage;

  AuthUser? get user => _user;
  AuthState get state => _state;
  bool get isAuthenticated => _state == AuthState.authenticated && _user != null;
  String? get errorMessage => _errorMessage;

  AuthProvider({
    required this.authService,
    required this.storageService,
  }) {
    checkSavedSession();
  }

  Future<void> checkSavedSession() async {
    _state = AuthState.loading;
    notifyListeners();

    final savedUser = await storageService.getUser();
    final token = await storageService.getToken();

    if (savedUser != null && token != null && token.isNotEmpty) {
      _user = savedUser;
      _state = AuthState.authenticated;
    } else {
      _user = null;
      _state = AuthState.unauthenticated;
    }
    notifyListeners();
  }

  Future<bool> login(String email, String password) async {
    _state = AuthState.loading;
    _errorMessage = null;
    notifyListeners();

    try {
      _user = await authService.login(email, password);
      _state = AuthState.authenticated;
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = e.toString();
      _state = AuthState.unauthenticated;
      notifyListeners();
      return false;
    }
  }

  Future<bool> register(String email, String password, String nombreCompleto) async {
    _state = AuthState.loading;
    _errorMessage = null;
    notifyListeners();

    try {
      _user = await authService.register(email, password, nombreCompleto);
      _state = AuthState.authenticated;
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = e.toString();
      _state = AuthState.unauthenticated;
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    await authService.logout();
    _user = null;
    _state = AuthState.unauthenticated;
    notifyListeners();
  }
}
