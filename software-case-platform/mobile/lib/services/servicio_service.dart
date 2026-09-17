import '../core/api/api_client.dart';
import '../core/constants/api_constants.dart';
import '../models/servicio.dart';

class ServicioService {
  final ApiClient apiClient;

  ServicioService({required this.apiClient});

  Future<List<Servicio>> getServicios() async {
    final response = await apiClient.get(ApiConstants.servicios);
    if (response is List) {
      return response.map((json) => Servicio.fromJson(json as Map<String, dynamic>)).toList();
    }
    return [];
  }

  Future<Servicio> createServicio(Servicio servicio) async {
    final response = await apiClient.post(ApiConstants.servicios, servicio.toJson());
    return Servicio.fromJson(response as Map<String, dynamic>);
  }

  Future<Servicio> updateServicio(int id, Servicio servicio) async {
    final response = await apiClient.put('${ApiConstants.servicios}/$id', servicio.toJson());
    return Servicio.fromJson(response as Map<String, dynamic>);
  }

  Future<void> deleteServicio(int id) async {
    await apiClient.delete('${ApiConstants.servicios}/$id');
  }
}
