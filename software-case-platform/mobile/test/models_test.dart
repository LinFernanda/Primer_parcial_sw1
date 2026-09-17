import 'package:flutter_test/flutter_test.dart';
import 'package:case_platform_mobile/models/auth_user.dart';
import 'package:case_platform_mobile/models/cliente.dart';
import 'package:case_platform_mobile/models/barbero.dart';
import 'package:case_platform_mobile/models/servicio.dart';
import 'package:case_platform_mobile/models/reserva.dart';
import 'package:case_platform_mobile/models/pago.dart';

void main() {
  group('Model Serialization & Deserialization Tests', () {
    test('Cliente fromJson and toJson', () {
      final json = {
        'id': 10,
        'nombre': 'Carlos Mendoza',
        'email': 'carlos@example.com',
        'telefono': '+591 70012345',
        'fechaRegistro': '2026-03-15',
      };

      final cliente = Cliente.fromJson(json);
      expect(cliente.id, 10);
      expect(cliente.nombre, 'Carlos Mendoza');
      expect(cliente.email, 'carlos@example.com');
      expect(cliente.telefono, '+591 70012345');
      expect(cliente.fechaRegistro, '2026-03-15');

      final backToJson = cliente.toJson();
      expect(backToJson['nombre'], 'Carlos Mendoza');
      expect(backToJson['email'], 'carlos@example.com');
      expect(backToJson['telefono'], '+591 70012345');
    });

    test('Barbero fromJson and toJson', () {
      final json = {
        'id': 2,
        'nombre': 'Mateo Barbero',
        'especialidad': 'Fade & Tijera',
        'telefono': '70099999',
        'calificacion': 4.9,
      };

      final barbero = Barbero.fromJson(json);
      expect(barbero.id, 2);
      expect(barbero.nombre, 'Mateo Barbero');
      expect(barbero.calificacion, 4.9);
      expect(barbero.especialidad, 'Fade & Tijera');

      final serialized = barbero.toJson();
      expect(serialized['nombre'], 'Mateo Barbero');
      expect(serialized['calificacion'], 4.9);
    });

    test('Servicio fromJson and toJson', () {
      final json = {
        'id': 5,
        'nombre': 'Corte Clásico',
        'descripcion': 'Corte tradicional de caballero',
        'precio': 35.5,
        'duracionMinutos': 40,
      };

      final servicio = Servicio.fromJson(json);
      expect(servicio.id, 5);
      expect(servicio.nombre, 'Corte Clásico');
      expect(servicio.precio, 35.5);
      expect(servicio.duracionMinutos, 40);

      final serialized = servicio.toJson();
      expect(serialized['precio'], 35.5);
      expect(serialized['duracionMinutos'], 40);
    });

    test('Reserva fromJson, toJson and copyWith', () {
      final now = DateTime.now();
      final json = {
        'id': 1,
        'clienteId': 10,
        'clienteNombre': 'Carlos Mendoza',
        'barberoId': 2,
        'barberoNombre': 'Mateo Barbero',
        'servicioId': 5,
        'servicioNombre': 'Corte Clásico',
        'fechaHora': now.toIso8601String(),
        'estado': 'CONFIRMADA',
        'notas': 'Cliente recurrente',
      };

      final reserva = Reserva.fromJson(json);
      expect(reserva.id, 1);
      expect(reserva.clienteNombre, 'Carlos Mendoza');
      expect(reserva.estado, 'CONFIRMADA');

      final updated = reserva.copyWith(estado: 'COMPLETADA');
      expect(updated.estado, 'COMPLETADA');
      expect(updated.id, 1);
      expect(updated.clienteNombre, 'Carlos Mendoza');

      final serialized = updated.toJson();
      expect(serialized['estado'], 'COMPLETADA');
    });

    test('Pago fromJson and toJson', () {
      final json = {
        'id': 99,
        'reservaId': 1,
        'monto': 50.0,
        'metodoPago': 'EFECTIVO',
        'estado': 'PAGADO',
      };

      final pago = Pago.fromJson(json);
      expect(pago.id, 99);
      expect(pago.monto, 50.0);
      expect(pago.metodoPago, 'EFECTIVO');
      expect(pago.estado, 'PAGADO');

      final serialized = pago.toJson();
      expect(serialized['monto'], 50.0);
      expect(serialized['metodoPago'], 'EFECTIVO');
    });

    test('AuthUser fromJson and toJson', () {
      final json = {
        'id': 1,
        'email': 'admin@barberia.com',
        'nombreCompleto': 'Administrador General',
        'rol': 'ADMIN',
      };

      final authUser = AuthUser.fromJson(json, 'token-xyz');
      expect(authUser.id, 1);
      expect(authUser.email, 'admin@barberia.com');
      expect(authUser.token, 'token-xyz');
      expect(authUser.rol, 'ADMIN');

      final serialized = authUser.toJson();
      expect(serialized['email'], 'admin@barberia.com');
      expect(serialized['token'], 'token-xyz');
    });
  });
}
