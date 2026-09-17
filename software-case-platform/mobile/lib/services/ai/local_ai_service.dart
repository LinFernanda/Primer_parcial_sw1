import 'ai_service.dart';
import 'offline_speech_service.dart';
import 'on_device_model.dart';

/// Motor local de procesamiento de lenguaje natural (NLP) on-device para la Fase 12.
/// Opera 100% offline sin conexión a internet mediante inferencia en dispositivo.
class LocalAIService implements AIService {
  final OnDeviceModel model;
  final OfflineSpeechService speechService;

  LocalAIService({
    OnDeviceModel? model,
    OfflineSpeechService? speechService,
  })  : model = model ?? OnDeviceModel(),
        speechService = speechService ?? OfflineSpeechService();

  @override
  Future<AIActionResult> processPrompt(String prompt) async {
    final clean = prompt.trim().toLowerCase();
    final classification = model.classify(prompt);

    // 1. Detección de creación de reserva
    if (classification.intent == 'CREATE_RESERVA' ||
        clean.contains('reserva') ||
        clean.contains('cita') ||
        clean.contains('turno')) {
      return _parseReservaPrompt(prompt, classification);
    }

    // 2. Detección de creación de cliente
    if (classification.intent == 'CREATE_CLIENTE' ||
        (clean.contains('cliente') &&
            (clean.contains('crear') ||
                clean.contains('registrar') ||
                clean.contains('nuevo')))) {
      return _parseClientePrompt(prompt, classification);
    }

    // 3. Consulta de servicios
    if (classification.intent == 'QUERY_SERVICIOS' ||
        clean.contains('servicio') ||
        clean.contains('precio') ||
        clean.contains('corte')) {
      return AIActionResult(
        action: AITypeAction.listServicios,
        message: 'Consultando catálogo de servicios disponibles en la barbería...',
      );
    }

    // 4. Consulta de reservas activas
    if (classification.intent == 'QUERY_RESERVAS' ||
        (clean.contains('reservas') &&
            (clean.contains('cuantas') || clean.contains('hoy') || clean.contains('tengo')))) {
      return AIActionResult(
        action: AITypeAction.infoGeneral,
        message: 'Consultando reservas y turnos del día en la base de datos local...',
      );
    }

    // 5. Consulta general o ayuda
    if (classification.intent == 'GREETING' ||
        clean.contains('ayuda') ||
        clean.contains('que puedes hacer') ||
        clean.contains('hola')) {
      return AIActionResult(
        action: AITypeAction.infoGeneral,
        message: '¡Hola! Soy tu Asistente Inteligente de Barbería (Modo On-Device Offline). Puedo ayudarte a:\n'
            '• Registrar reservas (ej: "Reservar cita para Juan mañana a las 16:00")\n'
            '• Registrar nuevos clientes (ej: "Crear cliente Mateo telefono 777-8899")\n'
            '• Consultar catálogo de servicios y disponibilidad.',
      );
    }

    return AIActionResult(
      action: AITypeAction.unknown,
      message: 'No pude comprender la instrucción exacta. Intenta con: '
          '"Registrar reserva para [Cliente] a las [Hora]" o "Crear cliente [Nombre] telefono [Tel]".',
      success: false,
    );
  }

  Future<AIActionResult> processVoiceInput(String transcribedVoice) async {
    final text = await speechService.processOfflineVoiceInput(transcribedVoice);
    return processPrompt(text);
  }

  AIActionResult _parseReservaPrompt(String raw, [IntentClassification? classification]) {
    String clienteNombre = classification?.slots['cliente'] ?? 'Cliente';
    String barberoNombre = 'Mateo Barbero';
    String servicioNombre = classification?.slots['servicio'] ?? 'Corte Clásico';
    DateTime fechaHora = DateTime.now().add(const Duration(hours: 2));

    // Si los slots no extrajeron cliente, usar extracción directa
    if (clienteNombre == 'Cliente') {
      final clientReg = RegExp(
        r'(?:para|a nombre de|cliente)\s+([a-záéíóúñ]+)(?:\s+([a-záéíóúñ]+))?',
        caseSensitive: false,
      );
      final clientMatch = clientReg.firstMatch(raw);
      if (clientMatch != null) {
        final first = clientMatch.group(1)?.trim();
        final second = clientMatch.group(2)?.trim();
        const stopWords = {
          'mañana', 'hoy', 'a', 'las', 'el', 'la', 'con', 'de', 'para', 'en', 'los', 'un', 'una'
        };
        if (first != null && !stopWords.contains(first.toLowerCase())) {
          if (second != null && !stopWords.contains(second.toLowerCase())) {
            clienteNombre = _capitalize('$first $second');
          } else {
            clienteNombre = _capitalize(first);
          }
        }
      }
    }

    // Detectar día
    final isTomorrow = raw.toLowerCase().contains('mañana');
    if (isTomorrow) {
      final now = DateTime.now();
      fechaHora = DateTime(now.year, now.month, now.day + 1, 10, 0);
    }

    // Detectar hora (ej: "a las 16:00" o "a las 4 pm")
    final timeReg = RegExp(
      r'(?:a las|las)\s+(\d{1,2})(?::(\d{2}))?\s*(am|pm)?',
      caseSensitive: false,
    );
    final timeMatch = timeReg.firstMatch(raw);
    if (timeMatch != null) {
      int hour = int.tryParse(timeMatch.group(1) ?? '10') ?? 10;
      int minute = int.tryParse(timeMatch.group(2) ?? '0') ?? 0;
      final amPm = timeMatch.group(3)?.toLowerCase();

      if (amPm == 'pm' && hour < 12) hour += 12;
      if (amPm == 'am' && hour == 12) hour = 0;

      fechaHora = DateTime(fechaHora.year, fechaHora.month, fechaHora.day, hour, minute);
    }

    // Detectar servicio
    if (raw.toLowerCase().contains('barba')) {
      servicioNombre = 'Perfilado y Afeitado de Barba';
    } else if (raw.toLowerCase().contains('completo') || raw.toLowerCase().contains('combo')) {
      servicioNombre = 'Corte + Barba Premium';
    }

    final data = {
      'clienteNombre': clienteNombre,
      'barberoNombre': barberoNombre,
      'servicioNombre': servicioNombre,
      'fechaHora': fechaHora.toIso8601String(),
      'estado': 'CONFIRMADA',
      'notas': 'Generada por Asistente IA On-Device',
    };

    final formattedDate =
        '${fechaHora.day}/${fechaHora.month}/${fechaHora.year} a las '
        '${fechaHora.hour.toString().padLeft(2, '0')}:${fechaHora.minute.toString().padLeft(2, '0')}';

    return AIActionResult(
      action: AITypeAction.createReserva,
      message:
          '¡Entendido! Voy a agendar la reserva para $clienteNombre para el $formattedDate ($servicioNombre).',
      data: data,
    );
  }

  AIActionResult _parseClientePrompt(String raw, [IntentClassification? classification]) {
    String nombre = classification?.slots['cliente'] ?? 'Nuevo Cliente';
    String telefono = classification?.slots['telefono'] ?? '555-0100';
    String email = 'cliente@barberia.com';

    final nameReg = RegExp(
      r'(?:cliente)\s+(?:llamado\s+|nuevo\s+)?([a-záéíóúñ]+)(?:\s+([a-záéíóúñ]+))?',
      caseSensitive: false,
    );
    final nameMatch = nameReg.firstMatch(raw);
    if (nameMatch != null) {
      final first = nameMatch.group(1)?.trim();
      final second = nameMatch.group(2)?.trim();
      const stopWords = {
        'con', 'de', 'telefono', 'tel', 'celular', 'email', 'correo', 'llamado', 'nuevo'
      };
      if (first != null && !stopWords.contains(first.toLowerCase())) {
        if (second != null && !stopWords.contains(second.toLowerCase())) {
          nombre = _capitalize('$first $second');
        } else {
          nombre = _capitalize(first);
        }
        email = '${nombre.toLowerCase().replaceAll(' ', '.')}@correo.com';
      }
    }

    final telReg = RegExp(
      r'(?:tel|telefono|celular|movil)?\s*([0-9\-\+]{7,15})',
      caseSensitive: false,
    );
    final telMatch = telReg.firstMatch(raw);
    if (telMatch != null && telMatch.group(1) != null) {
      telefono = telMatch.group(1)!.trim();
    }

    final data = {
      'nombre': nombre,
      'telefono': telefono,
      'email': email,
    };

    return AIActionResult(
      action: AITypeAction.createCliente,
      message: 'Registrando cliente: $nombre con teléfono $telefono.',
      data: data,
    );
  }

  String _capitalize(String s) {
    if (s.isEmpty) return s;
    return s.split(' ').map((word) {
      if (word.isEmpty) return word;
      return word[0].toUpperCase() + word.substring(1).toLowerCase();
    }).join(' ');
  }
}
