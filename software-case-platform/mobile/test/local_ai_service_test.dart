import 'package:flutter_test/flutter_test.dart';
import 'package:case_platform_mobile/services/ai/ai_service.dart';
import 'package:case_platform_mobile/services/ai/local_ai_service.dart';

void main() {
  group('LocalAIService On-Device NLP Engine Tests', () {
    late LocalAIService aiService;

    setUp(() {
      aiService = LocalAIService();
    });

    test('Identifies and parses reservation with client name and time', () async {
      const prompt = 'Crear una reserva para Carlos mañana a las 16:00';
      final result = await aiService.processPrompt(prompt);

      expect(result.action, AITypeAction.createReserva);
      expect(result.success, true);
      expect(result.data, isNotNull);
      expect(result.data!['clienteNombre'], 'Carlos');
      expect(result.data!['servicioNombre'], 'Corte Clásico');
      expect(result.data!['fechaHora'], isNotEmpty);
      expect(result.message, contains('Carlos'));
    });

    test('Identifies reservation with barba service and am/pm time', () async {
      const prompt = 'Registrar cita para Fernando a las 4 pm con servicio de barba';
      final result = await aiService.processPrompt(prompt);

      expect(result.action, AITypeAction.createReserva);
      expect(result.success, true);
      expect(result.data!['clienteNombre'], 'Fernando');
      expect(result.data!['servicioNombre'], contains('Barba'));
    });

    test('Identifies and parses client creation with name and phone', () async {
      const prompt = 'Registrar nuevo cliente Pedro Gomez con telefono 71234567';
      final result = await aiService.processPrompt(prompt);

      expect(result.action, AITypeAction.createCliente);
      expect(result.success, true);
      expect(result.data!['nombre'], 'Pedro Gomez');
      expect(result.data!['telefono'], '71234567');
      expect(result.data!['email'], contains('pedro.gomez'));
    });

    test('Answers service catalog queries', () async {
      const prompt = '¿Cuáles son los precios y servicios disponibles?';
      final result = await aiService.processPrompt(prompt);

      expect(result.action, AITypeAction.listServicios);
      expect(result.success, true);
      expect(result.message, contains('catálogo de servicios'));
    });

    test('Responds to help / greeting prompt', () async {
      const prompt = 'Hola, ¿qué puedes hacer?';
      final result = await aiService.processPrompt(prompt);

      expect(result.action, AITypeAction.infoGeneral);
      expect(result.success, true);
      expect(result.message, contains('Asistente Inteligente'));
    });

    test('Returns unknown action for unrecognized phrases', () async {
      const prompt = 'Quiero pedir una pizza napolitana para cenar';
      final result = await aiService.processPrompt(prompt);

      expect(result.action, AITypeAction.unknown);
      expect(result.success, false);
      expect(result.message, contains('No pude comprender'));
    });
  });
}
