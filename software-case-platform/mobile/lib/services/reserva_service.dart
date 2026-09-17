import '../core/api/api_client.dart';
import '../core/constants/api_constants.dart';
import '../models/reserva.dart';

class ReservaService {
  final ApiClient apiClient;

  ReservaService({required this.apiClient});

  Future<List<Reserva>> getReservas() async {
    final response = await apiClient.get(ApiConstants.reservas);
    if (response is List) {
      return response.map((json) => Reserva.fromJson(json as Map<String, dynamic>)).toList();
    }
    return [];
  }

  Future<Reserva> createReserva(Reserva reserva) async {
    final response = await apiClient.post(ApiConstants.reservas, reserva.toJson());
    return Reserva.fromJson(response as Map<String, dynamic>);
  }

  Future<Reserva> updateReserva(int id, Reserva reserva) async {
    final response = await apiClient.put('${ApiConstants.reservas}/$id', reserva.toJson());
    return Reserva.fromJson(response as Map<String, dynamic>);
  }

  Future<void> deleteReserva(int id) async {
    await apiClient.delete('${ApiConstants.reservas}/$id');
  }
}
