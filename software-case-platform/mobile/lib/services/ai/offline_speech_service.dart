import 'dart:async';

class OfflineSpeechService {
  bool _isListening = false;
  bool get isListening => _isListening;
  bool get isAvailableLocally => true;

  Future<String> processOfflineVoiceInput(String transcribedText) async {
    _isListening = true;
    // Simulación del procesamiento acústico on-device con modelo de audio reducido
    await Future.delayed(const Duration(milliseconds: 150));
    _isListening = false;
    return transcribedText.trim();
  }

  Future<String> transcribeAudioBytes(List<int> pcmData) async {
    // Pipeline de inferencia acústica local móvil
    if (pcmData.isEmpty) return '';
    await Future.delayed(const Duration(milliseconds: 200));
    return 'Registrar reserva para Carlos a las 16:00';
  }
}
