import '../core/api/api_client.dart';
import '../core/constants/api_constants.dart';
import '../models/cliente.dart';

class ClienteService {
  final ApiClient apiClient;

  ClienteService({required this.apiClient});

  Future<List<Cliente>> getClientes() async {
    final response = await apiClient.get(ApiConstants.clientes);
    if (response is List) {
      return response.map((json) => Cliente.fromJson(json as Map<String, dynamic>)).toList();
    }
    return [];
  }

  Future<Cliente> getClienteById(int id) async {
    final response = await apiClient.get('${ApiConstants.clientes}/$id');
    return Cliente.fromJson(response as Map<String, dynamic>);
  }

  Future<Cliente> createCliente(Cliente cliente) async {
    final response = await apiClient.post(ApiConstants.clientes, cliente.toJson());
    return Cliente.fromJson(response as Map<String, dynamic>);
  }

  Future<Cliente> updateCliente(int id, Cliente cliente) async {
    final response = await apiClient.put('${ApiConstants.clientes}/$id', cliente.toJson());
    return Cliente.fromJson(response as Map<String, dynamic>);
  }

  Future<void> deleteCliente(int id) async {
    await apiClient.delete('${ApiConstants.clientes}/$id');
  }
}
