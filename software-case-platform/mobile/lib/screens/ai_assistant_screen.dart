import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/ai_assistant_provider.dart';
import '../providers/barberia_provider.dart';
import '../widgets/ai_message_bubble.dart';

class AIAssistantScreen extends StatefulWidget {
  const AIAssistantScreen({super.key});

  @override
  State<AIAssistantScreen> createState() => _AIAssistantScreenState();
}

class _AIAssistantScreenState extends State<AIAssistantScreen> {
  final _promptController = TextEditingController();
  final _scrollController = ScrollController();

  final List<String> _quickSuggestions = [
    'Crear reserva para Carlos mañana a las 16:00',
    'Registrar nuevo cliente Pedro Gómez con teléfono 71234567',
    '¿Cuántas reservas tengo hoy?',
    '¿Cuáles son los servicios disponibles?',
  ];

  @override
  void dispose() {
    _promptController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  Future<void> _handleSendPrompt([String? customText]) async {
    final text = customText ?? _promptController.text.trim();
    if (text.isEmpty) return;

    final aiProvider = Provider.of<AIAssistantProvider>(context, listen: false);
    final barberiaProvider = Provider.of<BarberiaProvider>(context, listen: false);

    _promptController.clear();
    _scrollToBottom();

    await aiProvider.sendPrompt(text, barberiaProvider);
    _scrollToBottom();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final aiProvider = Provider.of<AIAssistantProvider>(context);

    return Scaffold(
      backgroundColor: isDark ? const Color(0xFF0F172A) : const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: const Color(0xFFA855F7).withOpacity(0.2),
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Icon(Icons.auto_awesome, color: Color(0xFFA855F7), size: 18),
            ),
            const SizedBox(width: 10),
            const Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Asistente IA BarberShop',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
                Text(
                  'Motor NLP on-device • Listo para Fase 12',
                  style: TextStyle(fontSize: 11, color: Color(0xFF94A3B8)),
                ),
              ],
            ),
          ],
        ),
        elevation: 0,
        backgroundColor: isDark ? const Color(0xFF1E293B) : Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.delete_outline, size: 20),
            tooltip: 'Limpiar chat',
            onPressed: () => aiProvider.clearChat(),
          ),
        ],
      ),
      body: Column(
        children: [
          // Banner de información de arquitectura IA
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            decoration: BoxDecoration(
              color: const Color(0xFFA855F7).withOpacity(0.08),
              border: Border(
                bottom: BorderSide(
                  color: const Color(0xFFA855F7).withOpacity(0.2),
                ),
              ),
            ),
            child: Row(
              children: [
                const Icon(Icons.info_outline, size: 16, color: Color(0xFFA855F7)),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Interpreta lenguaje natural y ejecuta mutaciones en tiempo real sobre el backend o caché.',
                    style: TextStyle(
                      fontSize: 12,
                      color: isDark ? const Color(0xFFE2E8F0) : const Color(0xFF475569),
                    ),
                  ),
                ),
              ],
            ),
          ),

          // Lista de Mensajes
          Expanded(
            child: ListView.builder(
              controller: _scrollController,
              padding: const EdgeInsets.all(16),
              itemCount: aiProvider.messages.length,
              itemBuilder: (context, index) {
                final msg = aiProvider.messages[index];
                return AIMessageBubble(message: msg);
              },
            ),
          ),

          // Indicador de "Procesando"
          if (aiProvider.isProcessing) ...[
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
              child: Row(
                children: [
                  const SizedBox(
                    width: 14,
                    height: 14,
                    child: CircularProgressIndicator(
                      strokeWidth: 2,
                      valueColor: AlwaysStoppedAnimation<Color>(Color(0xFFA855F7)),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'El Asistente IA está analizando la solicitud...',
                    style: TextStyle(
                      fontSize: 12,
                      fontStyle: FontStyle.italic,
                      color: isDark ? Colors.white60 : Colors.black54,
                    ),
                  ),
                ],
              ),
            ),
          ],

          // Sugerencias Rápidas
          Container(
            height: 42,
            margin: const EdgeInsets.only(bottom: 8),
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: _quickSuggestions.length,
              separatorBuilder: (_, __) => const SizedBox(width: 8),
              itemBuilder: (context, index) {
                final suggestion = _quickSuggestions[index];
                return ActionChip(
                  label: Text(
                    suggestion,
                    style: TextStyle(
                      fontSize: 12,
                      color: isDark ? const Color(0xFFF1F5F9) : const Color(0xFF334155),
                    ),
                  ),
                  backgroundColor: isDark ? const Color(0xFF1E293B) : const Color(0xFFEDE9FE),
                  side: BorderSide(
                    color: isDark ? const Color(0xFF334155) : const Color(0xFFDDD6FE),
                  ),
                  onPressed: aiProvider.isProcessing ? null : () => _handleSendPrompt(suggestion),
                );
              },
            ),
          ),

          // Input Bar
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: isDark ? const Color(0xFF1E293B) : Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.04),
                  blurRadius: 6,
                  offset: const Offset(0, -2),
                ),
              ],
            ),
            child: SafeArea(
              top: false,
              child: Row(
                children: [
                  // Micrófono (Simulación por voz)
                  IconButton(
                    icon: const Icon(Icons.mic_none, color: Color(0xFFA855F7)),
                    tooltip: 'Dictado por voz',
                    onPressed: () {
                      _handleSendPrompt('Crear reserva para Roberto mañana a las 10:00');
                    },
                  ),
                  const SizedBox(width: 4),
                  // Campo de texto
                  Expanded(
                    child: TextField(
                      controller: _promptController,
                      textInputAction: TextInputAction.send,
                      onSubmitted: (_) => _handleSendPrompt(),
                      decoration: InputDecoration(
                        hintText: 'Escribe tu instrucción en lenguaje natural...',
                        hintStyle: TextStyle(
                          fontSize: 13,
                          color: isDark ? Colors.white38 : Colors.black38,
                        ),
                        filled: true,
                        fillColor: isDark ? const Color(0xFF0F172A) : const Color(0xFFF1F5F9),
                        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(24),
                          borderSide: BorderSide.none,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  // Botón enviar
                  Container(
                    decoration: const BoxDecoration(
                      shape: BoxShape.circle,
                      color: Color(0xFFD97706),
                    ),
                    child: IconButton(
                      icon: const Icon(Icons.send, color: Colors.white, size: 18),
                      onPressed: aiProvider.isProcessing ? null : () => _handleSendPrompt(),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
