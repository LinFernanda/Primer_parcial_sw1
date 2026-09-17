import 'package:flutter/material.dart';
import '../services/ai/ai_service.dart';
import '../services/ai/local_ai_service.dart';
import 'barberia_provider.dart';

class ChatMessage {
  final String text;
  final bool isUser;
  final DateTime timestamp;
  final bool isActionSuccess;

  ChatMessage({
    required this.text,
    required this.isUser,
    DateTime? timestamp,
    this.isActionSuccess = true,
  }) : timestamp = timestamp ?? DateTime.now();
}

class AIAssistantProvider extends ChangeNotifier {
  final AIService aiService;
  final List<ChatMessage> _messages = [];
  bool _isProcessing = false;

  List<ChatMessage> get messages => List.unmodifiable(_messages);
  bool get isProcessing => _isProcessing;

  AIAssistantProvider({AIService? service})
      : aiService = service ?? LocalAIService() {
    // Mensaje de bienvenida inicial
    _messages.add(ChatMessage(
      text: '¡Hola! Soy tu Asistente Inteligente de Barbería con IA. '
          'Puedes pedirme crear reservas (ej: "Crear reserva para Juan mañana a las 15:00"), '
          'registrar nuevos clientes o consultar información en lenguaje natural.',
      isUser: false,
    ));
  }

  Future<void> sendPrompt(String text, BarberiaProvider barberiaProvider) async {
    if (text.trim().isEmpty) return;

    final prompt = text.trim();
    _messages.add(ChatMessage(text: prompt, isUser: true));
    _isProcessing = true;
    notifyListeners();

    try {
      final result = await aiService.processPrompt(prompt);

      // Ejecutar la acción en el dominio si aplica
      if (result.action == AITypeAction.createReserva && result.data != null) {
        final data = result.data!;
        final fechaHora = DateTime.tryParse(data['fechaHora'] ?? '') ?? DateTime.now();

        await barberiaProvider.addReserva(
          clienteNombre: data['clienteNombre'] ?? 'Cliente General',
          barberoNombre: data['barberoNombre'] ?? 'Mateo Barbero',
          servicioNombre: data['servicioNombre'] ?? 'Corte Clásico',
          fechaHora: fechaHora,
          notas: data['notas'],
        );
      } else if (result.action == AITypeAction.createCliente && result.data != null) {
        final data = result.data!;
        await barberiaProvider.addCliente(
          data['nombre'] ?? 'Nuevo Cliente',
          data['email'] ?? 'cliente@correo.com',
          data['telefono'] ?? '555-0000',
        );
      }

      _messages.add(ChatMessage(
        text: result.message,
        isUser: false,
        isActionSuccess: result.success,
      ));
    } catch (e) {
      _messages.add(ChatMessage(
        text: 'Ocurrió un error al procesar tu solicitud: $e',
        isUser: false,
        isActionSuccess: false,
      ));
    } finally {
      _isProcessing = false;
      notifyListeners();
    }
  }

  void clearChat() {
    _messages.clear();
    _messages.add(ChatMessage(
      text: 'Historial reiniciado. ¿En qué puedo ayudarte hoy?',
      isUser: false,
    ));
    notifyListeners();
  }
}
