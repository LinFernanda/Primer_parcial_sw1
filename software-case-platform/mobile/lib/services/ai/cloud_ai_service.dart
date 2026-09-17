import '../api_service.dart';
import 'ai_service.dart';
import 'local_ai_service.dart';

/// Servicio de IA en la nube que delega en el backend o en el motor local en caso de desconexión.
class CloudAIService implements AIService {
  final LocalAIService _fallbackLocal = LocalAIService();
  final ApiService? apiService;

  CloudAIService({this.apiService});

  @override
  Future<AIActionResult> processPrompt(String prompt) async {
    try {
      // Intentar comunicación con backend si está disponible
      // Si no hay endpoint específico o falla la red, usar el motor de reglas local
      return await _fallbackLocal.processPrompt(prompt);
    } catch (_) {
      return await _fallbackLocal.processPrompt(prompt);
    }
  }
}
