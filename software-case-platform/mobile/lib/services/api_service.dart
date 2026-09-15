import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/system_health.dart';

class ApiService {
  // Configured default URL pointing to Spring Boot backend
  // For Android emulator: http://10.0.2.2:8080/api/v1
  // For Windows/desktop/web: http://localhost:8080/api/v1
  final String baseUrl;

  ApiService({this.baseUrl = 'http://localhost:8080/api/v1'});

  Future<String> ping() async {
    try {
      final response = await http
          .get(Uri.parse('$baseUrl/ping'))
          .timeout(const Duration(seconds: 5));

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['data'] ?? 'pong';
      }
      return 'HTTP Error: ${response.statusCode}';
    } catch (e) {
      return 'Offline / Unreachable: $e';
    }
  }

  Future<SystemHealth?> fetchHealth() async {
    try {
      final response = await http
          .get(Uri.parse('$baseUrl/health'))
          .timeout(const Duration(seconds: 5));

      if (response.statusCode == 200) {
        final decoded = jsonDecode(response.body);
        if (decoded['success'] == true && decoded['data'] != null) {
          return SystemHealth.fromJson(decoded['data']);
        }
      }
      return null;
    } catch (e) {
      return null;
    }
  }
}
