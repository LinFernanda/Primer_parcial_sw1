import 'package:flutter/material.dart';
import '../models/system_health.dart';
import '../services/api_service.dart';

class AppStateProvider extends ChangeNotifier {
  final ApiService _apiService = ApiService();

  SystemHealth? _health;
  String _pingMessage = '';
  bool _isLoading = false;
  String? _errorMessage;

  SystemHealth? get health => _health;
  String get pingMessage => _pingMessage;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  bool get isBackendConnected => _health?.status == 'UP';

  Future<void> checkSystemHealth() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      _pingMessage = await _apiService.ping();
      _health = await _apiService.fetchHealth();
      if (_health == null) {
        _errorMessage = 'No se pudo obtener información de salud del backend.';
      }
    } catch (e) {
      _errorMessage = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}
