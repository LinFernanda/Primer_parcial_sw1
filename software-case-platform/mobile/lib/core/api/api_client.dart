import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import '../constants/api_constants.dart';
import '../errors/app_exception.dart';

class ApiClient {
  String baseUrl;
  String? token;
  final http.Client _httpClient;

  ApiClient({
    this.baseUrl = ApiConstants.defaultLocalhostUrl,
    this.token,
    http.Client? httpClient,
  }) : _httpClient = httpClient ?? http.Client();

  Map<String, String> _buildHeaders() {
    final headers = <String, String>{
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };
    if (token != null && token!.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }
    return headers;
  }

  Future<dynamic> get(String endpoint) async {
    final uri = Uri.parse('$baseUrl$endpoint');
    try {
      final response = await _httpClient
          .get(uri, headers: _buildHeaders())
          .timeout(ApiConstants.timeout);
      return _processResponse(response);
    } on SocketException {
      throw NetworkException(
          'No se pudo conectar con el servidor ($baseUrl). Verifica tu conexión o activa el modo offline.');
    } on http.ClientException catch (e) {
      throw NetworkException('Error de comunicación HTTP: ${e.message}');
    } catch (e) {
      if (e is AppException) rethrow;
      throw AppException('Error inesperado al consultar $endpoint: $e');
    }
  }

  Future<dynamic> post(String endpoint, Map<String, dynamic> body) async {
    final uri = Uri.parse('$baseUrl$endpoint');
    try {
      final response = await _httpClient
          .post(
            uri,
            headers: _buildHeaders(),
            body: jsonEncode(body),
          )
          .timeout(ApiConstants.timeout);
      return _processResponse(response);
    } on SocketException {
      throw NetworkException(
          'No se pudo conectar con el servidor ($baseUrl). Verifica tu conexión o activa el modo offline.');
    } catch (e) {
      if (e is AppException) rethrow;
      throw AppException('Error al enviar datos a $endpoint: $e');
    }
  }

  Future<dynamic> put(String endpoint, Map<String, dynamic> body) async {
    final uri = Uri.parse('$baseUrl$endpoint');
    try {
      final response = await _httpClient
          .put(
            uri,
            headers: _buildHeaders(),
            body: jsonEncode(body),
          )
          .timeout(ApiConstants.timeout);
      return _processResponse(response);
    } on SocketException {
      throw NetworkException('No se pudo conectar con el servidor ($baseUrl).');
    } catch (e) {
      if (e is AppException) rethrow;
      throw AppException('Error al actualizar en $endpoint: $e');
    }
  }

  Future<dynamic> delete(String endpoint) async {
    final uri = Uri.parse('$baseUrl$endpoint');
    try {
      final response = await _httpClient
          .delete(uri, headers: _buildHeaders())
          .timeout(ApiConstants.timeout);
      return _processResponse(response);
    } on SocketException {
      throw NetworkException('No se pudo conectar con el servidor ($baseUrl).');
    } catch (e) {
      if (e is AppException) rethrow;
      throw AppException('Error al eliminar en $endpoint: $e');
    }
  }

  dynamic _processResponse(http.Response response) {
    final statusCode = response.statusCode;

    if (statusCode == 204) {
      return null;
    }

    dynamic bodyData;
    try {
      bodyData = response.body.isNotEmpty ? jsonDecode(utf8.decode(response.bodyBytes)) : null;
    } catch (_) {
      bodyData = response.body;
    }

    if (statusCode >= 200 && statusCode < 300) {
      // Soporta formato ApiResponseDTO: { success: true, data: ... } o respuesta REST directa
      if (bodyData is Map<String, dynamic> && bodyData.containsKey('data')) {
        return bodyData['data'];
      }
      return bodyData;
    } else if (statusCode == 401 || statusCode == 403) {
      final msg = (bodyData is Map && bodyData['message'] != null)
          ? bodyData['message']
          : 'Sesión expirada o credenciales no válidas.';
      throw AuthException(msg, statusCode: statusCode);
    } else if (statusCode == 404) {
      throw ServerException('Recurso no encontrado (404).', statusCode: statusCode);
    } else if (statusCode >= 400 && statusCode < 500) {
      final msg = (bodyData is Map && bodyData['message'] != null)
          ? bodyData['message']
          : 'Petición inválida (${response.statusCode}).';
      throw ValidationException(msg);
    } else {
      final msg = (bodyData is Map && bodyData['message'] != null)
          ? bodyData['message']
          : 'Error interno del servidor backend ($statusCode).';
      throw ServerException(msg, statusCode: statusCode);
    }
  }
}
