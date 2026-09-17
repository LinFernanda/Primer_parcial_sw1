import '../core/api/api_client.dart';
import '../core/constants/api_constants.dart';
import '../models/barbero.dart';

class BarberoService {
  final ApiClient apiClient;

  BarberoService({required this.apiClient});

  Future<List<Barbero>> getBarberos() async {
    final response = await apiClient.get(ApiConstants.barberos);
    if (response is List) {
      return response.map((json) => Barbero.fromJson(json as Map<String, dynamic>)).toList();
    }
    return [];
  }

  Future<Barbero> createBarbero(Barbero barbero) async {
    final response = await apiClient.post(ApiConstants.barberos, barbero.toJson());
    return Barbero.fromJson(response as Map<String, dynamic>);
  }

  Future<Barbero> updateBarbero(int id, Barbero barbero) async {
    final response = await apiClient.put('${ApiConstants.barberos}/$id', barbero.toJson());
    return Barbero.fromJson(response as Map<String, dynamic>);
  }

  Future<void> deleteBarbero(int id) async {
    await apiClient.delete('${ApiConstants.barberos}/$id');
  }
}
