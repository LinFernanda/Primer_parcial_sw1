import 'package:flutter/material.dart';
import '../models/barbero.dart';
import '../models/cliente.dart';
import '../models/reserva.dart';
import '../models/servicio.dart';
import '../repositories/barberia_repository.dart';

class BarberiaProvider extends ChangeNotifier {
  final BarberiaRepository repository;

  List<Cliente> _clientes = [];
  List<Barbero> _barberos = [];
  List<Servicio> _servicios = [];
  List<Reserva> _reservas = [];

  bool _isLoading = false;
  String? _errorMessage;

  List<Cliente> get clientes => _clientes;
  List<Barbero> get barberos => _barberos;
  List<Servicio> get servicios => _servicios;
  List<Reserva> get reservas => _reservas;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  bool get isOffline => repository.isOffline;
  void setOfflineMode(bool offline) {
    repository.setOffline(offline);
    notifyListeners();
  }

  // Estadísticas para el dashboard
  int get totalClientes => _clientes.length;
  int get totalReservasActivas => _reservas.where((r) => r.estado != 'CANCELADA').length;
  double get ingresosEstimados => _reservas
      .where((r) => r.estado == 'COMPLETADA')
      .fold(0.0, (acc, r) => acc + 40.0); // Estimación base por servicio

  BarberiaProvider({required this.repository}) {
    fetchAllData();
  }

  Future<void> fetchAllData() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      _clientes = await repository.getClientes();
      _barberos = await repository.getBarberos();
      _servicios = await repository.getServicios();
      _reservas = await repository.getReservas();
    } catch (e) {
      _errorMessage = 'Error al sincronizar datos: $e';
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  // -------------------------------------------------------------
  // CLIENTES
  // -------------------------------------------------------------
  Future<bool> addCliente(String nombre, String email, String telefono) async {
    try {
      final nuevo = Cliente(nombre: nombre, email: email, telefono: telefono);
      final creado = await repository.createCliente(nuevo);
      _clientes = [..._clientes, creado];
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = e.toString();
      notifyListeners();
      return false;
    }
  }

  Future<void> deleteCliente(int id) async {
    try {
      await repository.deleteCliente(id);
      _clientes = _clientes.where((c) => c.id != id).toList();
      notifyListeners();
    } catch (e) {
      _errorMessage = e.toString();
      notifyListeners();
    }
  }

  // -------------------------------------------------------------
  // SERVICIOS
  // -------------------------------------------------------------
  Future<bool> addServicio(String nombre, String descripcion, double precio, int duracion) async {
    try {
      final nuevo = Servicio(
        nombre: nombre,
        descripcion: descripcion,
        precio: precio,
        duracionMinutos: duracion,
      );
      final creado = await repository.createServicio(nuevo);
      _servicios = [..._servicios, creado];
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = e.toString();
      notifyListeners();
      return false;
    }
  }

  // -------------------------------------------------------------
  // RESERVAS
  // -------------------------------------------------------------
  Future<bool> addReserva({
    required String clienteNombre,
    required String barberoNombre,
    required String servicioNombre,
    required DateTime fechaHora,
    String? notas,
  }) async {
    try {
      final nueva = Reserva(
        clienteNombre: clienteNombre,
        barberoNombre: barberoNombre,
        servicioNombre: servicioNombre,
        fechaHora: fechaHora,
        estado: 'CONFIRMADA',
        notas: notas,
      );
      final creada = await repository.createReserva(nueva);
      _reservas = [creada, ..._reservas];
      notifyListeners();
      return true;
    } catch (e) {
      _errorMessage = e.toString();
      notifyListeners();
      return false;
    }
  }

  Future<void> updateReservaEstado(int id, String nuevoEstado) async {
    try {
      final reservaExistente = _reservas.firstWhere((r) => r.id == id);
      final actualizada = reservaExistente.copyWith(estado: nuevoEstado);
      await repository.updateReserva(id, actualizada);
      final idx = _reservas.indexWhere((r) => r.id == id);
      if (idx != -1) {
        _reservas[idx] = actualizada;
        notifyListeners();
      }
    } catch (e) {
      _errorMessage = e.toString();
      notifyListeners();
    }
  }

  Future<void> deleteReserva(int id) async {
    try {
      await repository.deleteReserva(id);
      _reservas = _reservas.where((r) => r.id != id).toList();
      notifyListeners();
    } catch (e) {
      _errorMessage = e.toString();
      notifyListeners();
    }
  }
}
