import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../core/constants/api_constants.dart';
import '../providers/auth_provider.dart';
import '../providers/barberia_provider.dart';
import '../services/storage/storage_service.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final _urlController = TextEditingController();
  final _storageService = StorageService();
  bool _isLoading = false;
  String _selectedAiEngine = 'local';

  @override
  void initState() {
    super.initState();
    _loadCurrentSettings();
  }

  @override
  void dispose() {
    _urlController.dispose();
    super.dispose();
  }

  Future<void> _loadCurrentSettings() async {
    final savedUrl = await _storageService.getBaseUrl();
    setState(() {
      _urlController.text = savedUrl ?? ApiConstants.defaultLocalhostUrl;
    });
  }

  Future<void> _saveAndTestConnection() async {
    setState(() => _isLoading = true);
    final url = _urlController.text.trim();

    await _storageService.saveBaseUrl(url);

    if (mounted) {
      final barberiaProvider = Provider.of<BarberiaProvider>(context, listen: false);
      await barberiaProvider.fetchAllData();

      setState(() => _isLoading = false);

      final isOffline = barberiaProvider.isOffline;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            isOffline
                ? 'No se pudo contactar con $url. Operando con caché offline seguro.'
                : '¡Conexión exitosa con backend en $url!',
          ),
          backgroundColor: isOffline ? const Color(0xFFF59E0B) : const Color(0xFF10B981),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final barberiaProvider = Provider.of<BarberiaProvider>(context);
    final authProvider = Provider.of<AuthProvider>(context);

    return Scaffold(
      backgroundColor: isDark ? const Color(0xFF0F172A) : const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text('Configuración y Conectividad'),
        elevation: 0,
        backgroundColor: isDark ? const Color(0xFF1E293B) : Colors.white,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Sección Backend URL
            _buildSectionHeader('Conexión con Backend Generado', Icons.dns_outlined, isDark),
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: _cardBoxDecoration(isDark),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'URL Base de la API REST (Spring Boot 3):',
                    style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(height: 8),
                  TextField(
                    controller: _urlController,
                    decoration: InputDecoration(
                      hintText: 'http://localhost:8080/api',
                      prefixIcon: const Icon(Icons.link),
                      filled: true,
                      fillColor: isDark ? const Color(0xFF0F172A) : const Color(0xFFF1F5F9),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(10),
                        borderSide: BorderSide.none,
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  const Text('Preajustes rápidos:', style: TextStyle(fontSize: 12, color: Colors.grey)),
                  const SizedBox(height: 8),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      ActionChip(
                        label: const Text('Localhost (PC / Web)'),
                        onPressed: () => _urlController.text = 'http://localhost:8080/api',
                      ),
                      ActionChip(
                        label: const Text('Emulador Android (10.0.2.2)'),
                        onPressed: () => _urlController.text = 'http://10.0.2.2:8080/api',
                      ),
                      ActionChip(
                        label: const Text('Dispositivo Físico LAN'),
                        onPressed: () => _urlController.text = 'http://192.168.1.100:8080/api',
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFFD97706),
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(vertical: 12),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      ),
                      icon: _isLoading
                          ? const SizedBox(
                              width: 18,
                              height: 18,
                              child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                            )
                          : const Icon(Icons.sync),
                      label: Text(_isLoading ? 'Verificando...' : 'Guardar y Probar Conexión'),
                      onPressed: _isLoading ? null : _saveAndTestConnection,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Sección Modo Offline / Caché
            _buildSectionHeader('Modo Offline y Tolerancia a Fallos', Icons.wifi_off_outlined, isDark),
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: _cardBoxDecoration(isDark),
              child: Column(
                children: [
                  SwitchListTile(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('Forzar Modo Offline (Caché Local)'),
                    subtitle: const Text(
                      'Usa los datos almacenados en memoria y evita peticiones de red directas.',
                      style: TextStyle(fontSize: 12),
                    ),
                    value: barberiaProvider.isOffline,
                    activeColor: const Color(0xFFF59E0B),
                    onChanged: (val) {
                      barberiaProvider.setOfflineMode(val);
                    },
                  ),
                  const Divider(height: 24),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Clientes en memoria:'),
                      Text('${barberiaProvider.clientes.length}', style: const TextStyle(fontWeight: FontWeight.bold)),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Reservas en memoria:'),
                      Text('${barberiaProvider.reservas.length}', style: const TextStyle(fontWeight: FontWeight.bold)),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Servicios en memoria:'),
                      Text('${barberiaProvider.servicios.length}', style: const TextStyle(fontWeight: FontWeight.bold)),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Sección Motor de Inteligencia Artificial
            _buildSectionHeader('Motor de Asistente IA', Icons.auto_awesome_outlined, isDark),
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: _cardBoxDecoration(isDark),
              child: Column(
                children: [
                  RadioListTile<String>(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('NLP Heurístico Local (On-Device)'),
                    subtitle: const Text(
                      'Procesamiento en dispositivo con extracción de entidades. Listo para Fase 12 (TFLite/LLM on-device).',
                      style: TextStyle(fontSize: 12),
                    ),
                    value: 'local',
                    groupValue: _selectedAiEngine,
                    activeColor: const Color(0xFFA855F7),
                    onChanged: (val) {
                      if (val != null) setState(() => _selectedAiEngine = val);
                    },
                  ),
                  RadioListTile<String>(
                    contentPadding: EdgeInsets.zero,
                    title: const Text('Cloud AI / Pasarela REST Backend'),
                    subtitle: const Text(
                      'Envío de prompts al endpoint de IA del backend generado.',
                      style: TextStyle(fontSize: 12),
                    ),
                    value: 'cloud',
                    groupValue: _selectedAiEngine,
                    activeColor: const Color(0xFFA855F7),
                    onChanged: (val) {
                      if (val != null) setState(() => _selectedAiEngine = val);
                    },
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Sección Información del Usuario y Sesión
            _buildSectionHeader('Sesión y Seguridad JWT', Icons.security_outlined, isDark),
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: _cardBoxDecoration(isDark),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Usuario conectado:'),
                      Text(
                        authProvider.user?.email ?? 'Invitado',
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Rol:'),
                      Text(
                        authProvider.user?.rol ?? 'N/A',
                        style: const TextStyle(fontWeight: FontWeight.bold, color: Color(0xFF10B981)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text('Token JWT activo:', style: TextStyle(fontSize: 12, color: Colors.grey)),
                  const SizedBox(height: 4),
                  Container(
                    width: double.infinity,
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: isDark ? const Color(0xFF0F172A) : const Color(0xFFF1F5F9),
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: Text(
                      authProvider.user?.token != null
                          ? '${authProvider.user!.token.substring(0, authProvider.user!.token.length > 32 ? 32 : authProvider.user!.token.length)}...'
                          : 'Sin token activo',
                      style: const TextStyle(fontSize: 11, fontFamily: 'monospace'),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title, IconData icon, bool isDark) {
    return Row(
      children: [
        Icon(icon, size: 20, color: const Color(0xFFD97706)),
        const SizedBox(width: 8),
        Text(
          title,
          style: TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.bold,
            color: isDark ? Colors.white : const Color(0xFF0F172A),
          ),
        ),
      ],
    );
  }

  BoxDecoration _cardBoxDecoration(bool isDark) {
    return BoxDecoration(
      color: isDark ? const Color(0xFF1E293B) : Colors.white,
      borderRadius: BorderRadius.circular(14),
      border: Border.all(
        color: isDark ? const Color(0xFF334155) : const Color(0xFFE2E8F0),
      ),
      boxShadow: [
        BoxShadow(
          color: Colors.black.withOpacity(0.02),
          blurRadius: 6,
          offset: const Offset(0, 2),
        ),
      ],
    );
  }
}
