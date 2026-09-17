import 'package:flutter_test/flutter_test.dart';
import 'package:case_platform_mobile/services/ai/ai_service.dart';
import 'package:case_platform_mobile/services/ai/local_ai_service.dart';
import 'package:case_platform_mobile/services/ai/offline_speech_service.dart';
import 'package:case_platform_mobile/services/ai/on_device_model.dart';

void main() {
  group('On-Device AI Model & Offline Speech Tests', () {
    late OnDeviceModel model;
    late OfflineSpeechService speechService;
    late LocalAIService aiService;

    setUp(() {
      model = OnDeviceModel();
      speechService = OfflineSpeechService();
      aiService = LocalAIService(model: model, speechService: speechService);
    });

    test('OnDeviceModel classifies CREATE_RESERVA intent and slots', () {
      final res = model.classify('Por favor agendar una reserva para Alejandro a las 15:00');
      expect(res.intent, 'CREATE_RESERVA');
      expect(res.confidence, greaterThan(0.5));
      expect(res.slots['cliente'], 'Alejandro');
      expect(res.slots['hora'], '15:00');
    });

    test('OnDeviceModel classifies CREATE_CLIENTE intent and slots', () {
      final res = model.classify('Registrar nuevo cliente Roberto Morales con telefono 71239876');
      expect(res.intent, 'CREATE_CLIENTE');
      expect(res.slots['cliente'], contains('Roberto'));
      expect(res.slots['telefono'], '71239876');
    });

    test('OnDeviceModel classifies QUERY_RESERVAS', () {
      final res = model.classify('¿Cuántas reservas tengo hoy?');
      expect(res.intent, 'QUERY_RESERVAS');
    });

    test('OnDeviceModel marks unknown phrases as UNKNOWN', () {
      final res = model.classify('El clima en la montaña está nublado');
      expect(res.intent, 'UNKNOWN');
    });

    test('OfflineSpeechService processes simulated voice input without network', () async {
      final text = await speechService.processOfflineVoiceInput('Reservar corte para Esteban a las 11:00');
      expect(text, 'Reservar corte para Esteban a las 11:00');

      final aiResult = await aiService.processVoiceInput(text);
      expect(aiResult.action, AITypeAction.createReserva);
      expect(aiResult.success, true);
      expect(aiResult.data!['clienteNombre'], 'Esteban');
    });
  });
}
