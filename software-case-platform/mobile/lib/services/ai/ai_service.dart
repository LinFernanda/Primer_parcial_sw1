enum AITypeAction {
  createReserva,
  createCliente,
  listServicios,
  consultarDisponibilidad,
  infoGeneral,
  unknown
}

class AIActionResult {
  final AITypeAction action;
  final String message;
  final Map<String, dynamic>? data;
  final bool success;

  AIActionResult({
    required this.action,
    required this.message,
    this.data,
    this.success = true,
  });
}

abstract class AIService {
  Future<AIActionResult> processPrompt(String prompt);
}
