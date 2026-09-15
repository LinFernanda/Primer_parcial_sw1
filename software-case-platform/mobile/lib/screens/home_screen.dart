import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_state_provider.dart';
import '../widgets/status_badge.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AppStateProvider>().checkSystemHealth();
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<AppStateProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('CASE Platform Mobile'),
        elevation: 1,
        actions: [
          IconButton(
            icon: provider.isLoading
                ? const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.refresh),
            onPressed:
                provider.isLoading ? null : () => provider.checkSystemHealth(),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Phase Banner
            Card(
              color: const Color(0xFF1E293B),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
                side: const BorderSide(color: Color(0xFF334155)),
              ),
              child: const Padding(
                padding: EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'FASE 1 - PREPARACIÓN INICIAL',
                      style: TextStyle(
                        color: Colors.blueAccent,
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                      ),
                    ),
                    SizedBox(height: 8),
                    Text(
                      'Plataforma CASE Colaborativa Inteligente',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 8),
                    Text(
                      'Estructura móvil Flutter preparada para consumo de API, almacenamiento local y gestión de estado.',
                      style: TextStyle(color: Colors.white70, fontSize: 13),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Health Status Card
            Card(
              elevation: 2,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Estado del Backend',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        StatusBadge(
                          label: provider.isBackendConnected
                              ? 'ONLINE'
                              : 'OFFLINE',
                          isSuccess: provider.isBackendConnected,
                        ),
                      ],
                    ),
                    const Divider(height: 24),
                    ListTile(
                      dense: true,
                      contentPadding: EdgeInsets.zero,
                      leading: const Icon(Icons.dns),
                      title: const Text('Aplicación Backend'),
                      subtitle: Text(provider.health?.application ??
                          'case-platform-backend'),
                    ),
                    ListTile(
                      dense: true,
                      contentPadding: EdgeInsets.zero,
                      leading: const Icon(Icons.storage),
                      title: const Text('PostgreSQL'),
                      subtitle: Text(provider.health?.databaseStatus ??
                          'Configurado en .env'),
                    ),
                    ListTile(
                      dense: true,
                      contentPadding: EdgeInsets.zero,
                      leading: const Icon(Icons.network_ping),
                      title: const Text('Respuesta Ping'),
                      subtitle: Text(provider.pingMessage.isNotEmpty
                          ? provider.pingMessage
                          : 'Pendiente de consulta'),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Roadmap Section
            const Text(
              'Módulos Planificados',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            _buildModuleTile(
              Icons.draw,
              'Editor UML Visual',
              'Sincronización de diagramas en tiempo real',
            ),
            _buildModuleTile(
              Icons.psychology,
              'Asistente IA',
              'Generación y sugerencia de modelos',
            ),
            _buildModuleTile(
              Icons.cloud_sync,
              'Modo Offline',
              'Persistencia local y reconciliación',
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildModuleTile(IconData icon, String title, String subtitle) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: CircleAvatar(
          backgroundColor: Colors.blue.withOpacity(0.1),
          child: Icon(icon, color: Colors.blue),
        ),
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(subtitle, style: const TextStyle(fontSize: 12)),
        trailing: const Icon(Icons.chevron_right, size: 20),
      ),
    );
  }
}
