class IntentClassification {
  final String intent;
  final double confidence;
  final Map<String, dynamic> slots;

  const IntentClassification({
    required this.intent,
    required this.confidence,
    required this.slots,
  });
}

class OnDeviceModel {
  static const double threshold = 0.40;

  // Vocabulario y patrones para inferencia en dispositivo
  final Map<String, List<String>> _intentKeywords = {
    'CREATE_RESERVA': ['reserva', 'reservar', 'cita', 'turno', 'agendar', 'apartar'],
    'CREATE_CLIENTE': ['cliente', 'registrar cliente', 'nuevo cliente', 'agregar cliente'],
    'QUERY_RESERVAS': ['cuantas reservas', 'reservas tengo', 'citas de hoy', 'turnos de hoy', 'consultar reservas', 'reservas', 'citas'],
    'QUERY_SERVICIOS': ['servicios', 'precios', 'catalogo', 'cortes', 'cuanto cuesta'],
    'GREETING': ['hola', 'buenos dias', 'buenas tardes', 'ayuda', 'que puedes hacer'],
  };

  String _normalize(String text) {
    return text
        .toLowerCase()
        .replaceAll('¿', '')
        .replaceAll('?', '')
        .replaceAll('¡', '')
        .replaceAll('!', '')
        .replaceAll('á', 'a')
        .replaceAll('é', 'e')
        .replaceAll('í', 'i')
        .replaceAll('ó', 'o')
        .replaceAll('ú', 'u')
        .trim();
  }

  IntentClassification classify(String input) {
    final clean = _normalize(input);
    String bestIntent = 'UNKNOWN';
    double bestScore = 0.0;

    for (final entry in _intentKeywords.entries) {
      double score = 0.0;
      for (final kw in entry.value) {
        if (clean.contains(_normalize(kw))) {
          score += 0.50;
        }
      }
      if (score > bestScore) {
        bestScore = score;
        bestIntent = entry.key;
      }
    }

    if (bestScore > 1.0) bestScore = 1.0;
    if (bestScore < threshold) {
      bestIntent = 'UNKNOWN';
    }

    final slots = _extractSlots(clean, input);
    return IntentClassification(
      intent: bestIntent,
      confidence: bestScore,
      slots: slots,
    );
  }

  Map<String, dynamic> _extractSlots(String clean, String original) {
    final slots = <String, dynamic>{};

    // 1. Extracción de Persona / Cliente
    final nameReg = RegExp(
      r'(?:para|cliente|llamado|a nombre de)\s+([a-záéíóúñ]+)(?:\s+([a-záéíóúñ]+))?',
      caseSensitive: false,
    );
    final matchName = nameReg.firstMatch(original);
    if (matchName != null) {
      final w1 = matchName.group(1)?.trim();
      final w2 = matchName.group(2)?.trim();
      const stopWords = {'mañana', 'hoy', 'a', 'las', 'el', 'la', 'con', 'de', 'para', 'en', 'los', 'un', 'una', 'telefono'};
      if (w1 != null && !stopWords.contains(w1.toLowerCase())) {
        if (w2 != null && !stopWords.contains(w2.toLowerCase())) {
          slots['cliente'] = _capitalize('$w1 $w2');
        } else {
          slots['cliente'] = _capitalize(w1);
        }
      }
    }

    // 2. Extracción de Teléfono
    final telReg = RegExp(r'(?:tel|telefono|celular)?\s*([0-9\-\+]{7,15})');
    final matchTel = telReg.firstMatch(clean);
    if (matchTel != null) {
      slots['telefono'] = matchTel.group(1)?.trim();
    }

    // 3. Extracción de Hora
    final timeReg = RegExp(r'(?:a las|las)\s+(\d{1,2})(?::(\d{2}))?\s*(am|pm)?');
    final matchTime = timeReg.firstMatch(clean);
    if (matchTime != null) {
      int h = int.tryParse(matchTime.group(1) ?? '10') ?? 10;
      int m = int.tryParse(matchTime.group(2) ?? '0') ?? 0;
      final amPm = matchTime.group(3)?.toLowerCase();
      if (amPm == 'pm' && h < 12) h += 12;
      if (amPm == 'am' && h == 12) h = 0;
      slots['hora'] = '${h.toString().padLeft(2, '0')}:${m.toString().padLeft(2, '0')}';
    }

    // 4. Extracción de Fecha Relativa
    if (clean.contains('mañana')) {
      slots['fecha_relativa'] = 'mañana';
    } else if (clean.contains('hoy')) {
      slots['fecha_relativa'] = 'hoy';
    }

    // 5. Extracción de Servicio
    if (clean.contains('barba')) {
      slots['servicio'] = 'Perfilado y Afeitado de Barba';
    } else if (clean.contains('premium') || clean.contains('combo')) {
      slots['servicio'] = 'Combo Barbershop Premium';
    } else {
      slots['servicio'] = 'Corte Clásico';
    }

    return slots;
  }

  String _capitalize(String s) {
    if (s.isEmpty) return s;
    return s.split(' ').map((w) {
      if (w.isEmpty) return w;
      return w[0].toUpperCase() + w.substring(1).toLowerCase();
    }).join(' ');
  }
}
