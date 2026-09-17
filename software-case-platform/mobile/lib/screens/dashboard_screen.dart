import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../providers/barberia_provider.dart';
import '../widgets/stat_card.dart';
import 'ai_assistant_screen.dart';
import 'clientes_screen.dart';
import 'login_screen.dart';
import 'reservas_screen.dart';
import 'servicios_screen.dart';
import 'settings_screen.dart';
import 'sync_center_screen.dart';

class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final authProvider = Provider.of<AuthProvider>(context);
    final barberiaProvider = Provider.of<BarberiaProvider>(context);

    return Scaffold(
      backgroundColor: isDark ? const Color(0xFF0F172A) : const Color(0xFFF8FAFC),
      appBar: AppBar(
        backgroundColor: isDark ? const Color(0xFF1E293B) : Colors.white,
        elevation: 0,
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: const Color(0xFFD97706).withOpacity(0.15),
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Icon(Icons.content_cut, color: Color(0xFFD97706), size: 18),
            ),
            const SizedBox(width: 10),
            const Text(
              'BarberShop CASE',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        actions: [
          // Badge de Estado de Conexión / Offline
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            margin: const EdgeInsets.symmetric(vertical: 12),
            decoration: BoxDecoration(
              color: barberiaProvider.isOffline
                  ? const Color(0xFFF59E0B).withOpacity(0.15)
                  : const Color(0xFF10B981).withOpacity(0.15),
              borderRadius: BorderRadius.circular(20),
              border: Border.all(
                color: barberiaProvider.isOffline ? const Color(0xFFF59E0B) : const Color(0xFF10B981),
                width: 1,
              ),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  width: 6,
                  height: 6,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: barberiaProvider.isOffline ? const Color(0xFFF59E0B) : const Color(0xFF10B981),
                  ),
                ),
                const SizedBox(width: 6),
                Text(
                  barberiaProvider.isOffline ? 'Caché Offline' : 'Backend En Vivo',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    color: barberiaProvider.isOffline ? const Color(0xFFF59E0B) : const Color(0xFF10B981),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 6),
          IconButton(
            icon: const Icon(Icons.settings_outlined, size: 20),
            tooltip: 'Configuración de Backend',
            onPressed: () {
              Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const SettingsScreen()),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.logout, size: 20),
            tooltip: 'Cerrar Sesión',
            onPressed: () async {
              await authProvider.logout();
              if (context.mounted) {
                Navigator.of(context).pushReplacement(
                  MaterialPageRoute(builder: (_) => const LoginScreen()),
                );
              }
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => barberiaProvider.fetchAllData(),
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Saludo y Usuario
              Text(
                'Hola, ${authProvider.user?.nombreCompleto ?? 'Administrador'}',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: isDark ? Colors.white : const Color(0xFF0F172A),
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'Panel móvil conectado al Backend Spring Boot 3 + PostgreSQL',
                style: TextStyle(
                  fontSize: 13,
                  color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                ),
              ),
              const SizedBox(height: 20),

              // Métricas Principales en Rejilla 2x2
              GridView.count(
                crossAxisCount: 2,
                crossAxisSpacing: 14,
                mainAxisSpacing: 14,
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                children: [
                  StatCard(
                    title: 'Clientes Totales',
                    value: barberiaProvider.totalClientes.toString(),
                    icon: Icons.people_outline,
                    color: const Color(0xFF38BDF8),
                  ),
                  StatCard(
                    title: 'Reservas Activas',
                    value: barberiaProvider.totalReservasActivas.toString(),
                    icon: Icons.calendar_today_outlined,
                    color: const Color(0xFF10B981),
                  ),
                  StatCard(
                    title: 'Servicios en Catálogo',
                    value: barberiaProvider.servicios.length.toString(),
                    icon: Icons.content_cut,
                    color: const Color(0xFFC084FC),
                  ),
                  StatCard(
                    title: 'Ingresos Estimados',
                    value: '\$${barberiaProvider.ingresosEstimados.toStringAsFixed(0)}',
                    icon: Icons.attach_money,
                    color: const Color(0xFFF59E0B),
                  ),
                ],
              ),
              const SizedBox(height: 28),

              // Sección: Acciones y Módulos
              Text(
                'Módulos del Sistema',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: isDark ? Colors.white : const Color(0xFF0F172A),
                ),
              ),
              const SizedBox(height: 12),

              _buildModuleTile(
                context: context,
                icon: Icons.auto_awesome,
                color: const Color(0xFFA855F7),
                title: 'Asistente IA de Barbería',
                subtitle: 'Crea reservas y clientes en lenguaje natural por texto o voz',
                badgeText: 'Inteligencia Artificial',
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const AIAssistantScreen()),
                  );
                },
              ),
              const SizedBox(height: 10),

              _buildModuleTile(
                context: context,
                icon: Icons.calendar_month,
                color: const Color(0xFF10B981),
                title: 'Gestión de Reservas',
                subtitle: 'Control de turnos, confirmación y estados de citas',
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const ReservasScreen()),
                  );
                },
              ),
              const SizedBox(height: 10),

              _buildModuleTile(
                context: context,
                icon: Icons.people,
                color: const Color(0xFF38BDF8),
                title: 'Directorio de Clientes',
                subtitle: 'Registro, búsqueda y datos de contacto',
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const ClientesScreen()),
                  );
                },
              ),
              const SizedBox(height: 10),

              _buildModuleTile(
                context: context,
                icon: Icons.content_cut,
                color: const Color(0xFFF59E0B),
                title: 'Catálogo de Servicios',
                subtitle: 'Precios, duraciones y descripción de cortes',
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const ServiciosScreen()),
                  );
                },
              ),
              const SizedBox(height: 10),

              _buildModuleTile(
                context: context,
                icon: Icons.sync,
                color: const Color(0xFF0284C7),
                title: 'Centro de Sincronización',
                subtitle: 'Cola de operaciones offline, errores y sincronización',
                badgeText: 'Fase 12',
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const SyncCenterScreen()),
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildModuleTile({
    required BuildContext context,
    required IconData icon,
    required Color color,
    required String title,
    required String subtitle,
    String? badgeText,
    required VoidCallback onTap,
  }) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: isDark ? const Color(0xFF1E293B) : Colors.white,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(
            color: isDark ? const Color(0xFF334155) : Colors.grey.shade200,
          ),
        ),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: color.withOpacity(0.12),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(icon, color: color, size: 22),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        title,
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w600,
                          color: isDark ? Colors.white : const Color(0xFF0F172A),
                        ),
                      ),
                      if (badgeText != null) ...[
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          decoration: BoxDecoration(
                            color: color.withOpacity(0.15),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Text(
                            badgeText,
                            style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold, color: color),
                          ),
                        ),
                      ],
                    ],
                  ),
                  const SizedBox(height: 3),
                  Text(
                    subtitle,
                    style: TextStyle(
                      fontSize: 12,
                      color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                    ),
                  ),
                ],
              ),
            ),
            const Icon(Icons.chevron_right, size: 20, color: Color(0xFF94A3B8)),
          ],
        ),
      ),
    );
  }
}
