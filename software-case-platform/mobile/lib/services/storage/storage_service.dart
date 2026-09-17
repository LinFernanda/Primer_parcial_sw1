import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import '../../core/constants/api_constants.dart';
import '../../models/auth_user.dart';

class StorageService {
  SharedPreferences? _prefs;

  Future<SharedPreferences> get _instance async {
    _prefs ??= await SharedPreferences.getInstance();
    return _prefs!;
  }

  Future<void> saveToken(String token) async {
    final p = await _instance;
    await p.setString(ApiConstants.prefTokenKey, token);
  }

  Future<String?> getToken() async {
    final p = await _instance;
    return p.getString(ApiConstants.prefTokenKey);
  }

  Future<void> saveUser(AuthUser user) async {
    final p = await _instance;
    await p.setString(ApiConstants.prefUserKey, jsonEncode(user.toJson()));
  }

  Future<AuthUser?> getUser() async {
    final p = await _instance;
    final raw = p.getString(ApiConstants.prefUserKey);
    if (raw == null) return null;
    try {
      final map = jsonDecode(raw);
      return AuthUser.fromJson(map, map['token'] ?? '');
    } catch (_) {
      return null;
    }
  }

  Future<void> clearAuth() async {
    final p = await _instance;
    await p.remove(ApiConstants.prefTokenKey);
    await p.remove(ApiConstants.prefUserKey);
  }

  Future<void> saveBaseUrl(String url) async {
    final p = await _instance;
    await p.setString(ApiConstants.prefBaseUrlKey, url);
  }

  Future<String?> getBaseUrl() async {
    final p = await _instance;
    return p.getString(ApiConstants.prefBaseUrlKey);
  }
}
