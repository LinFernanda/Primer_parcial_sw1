import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/sync_provider.dart';
import '../services/connectivity/connectivity_service.dart';

class SyncCenterScreen extends StatefulWidget {
  const SyncCenterScreen({super.key});

  @override
  State<SyncCenterScreen> createState() => _SyncCenterScreenState();
}

class _SyncCenterScreenState extends State<SyncCenterScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final syncProvider = Provider.of<SyncProvider>(context);

    return Scaffold(
      backgroundColor: isDark ? const Color(0xFF0F172A) : const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text('Centro de Sincronización'),
        elevation: 0,
        backgroundColor: isDark ? const Color(0xFF1E293B) : Colors.white,
        bottom: TabBar(
          controller: _tabController,
          labelColor: const Color(0xFFD97706),
          indicatorColor: const Color(0xFFD97706),
          unselectedLabelColor: Colors.grey,
          tabs: [
            Tab(
              icon: const Icon(Icons.queue, size: 20),
              text: 'Cola Pendiente (${syncProvider.pendingCount})',
            ),
            Tab(
              icon: const Icon(Icons.error_outline, size: 20),
              text: 'Errores (${syncProvider.errorCount})',
            ),
          ],
        ),
      ),
      body: Column(
        children: [
          // Banner de Estado de Conexión y Botón de Sincronización
          Container(
            padding: const EdgeInsets.all(16),
            color: isDark ? const Color(0xFF1E293B) : Colors.white,
            child: Column(
              children: [
                Row(
                  children: [
                    Container(
                      width: 12,
                      height: 12,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: syncProvider.isOnline
                            ? const Color(0xFF10B981)
                            : const Color(0xFFF59E0B),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            syncProvider.isOnline
                                ? 'Conexión Activa (En Línea)'
                                : 'Modo Offline (Sin Conexión)',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 15,
                              color: isDark ? Colors.white : const Color(0xFF0F172A),
                            ),
                          ),
                          Text(
                            syncProvider.isOnline
                                ? 'Los datos se sincronizan automáticamente con Spring Boot'
                                : 'Las operaciones se almacenan en la base local para sincronización posterior',
                            style: TextStyle(
                              fontSize: 12,
                              color: isDark ? const Color(0xFF94A3B8) : const Color(0xFF64748B),
                            ),
                          ),
                        ],
                      ),
                    ),
                    Switch(
                      value: syncProvider.isOffline,
                      activeColor: const Color(0xFFF59E0B),
                      onChanged: (val) {
                        syncProvider.toggleOfflineMode(val);
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFFD97706),
                      foregroundColor: Colors.white,
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                    ),
                    icon: syncProvider.isSyncing
                        ? const SizedBox(
                            width: 18,
                            height: 18,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Icon(Icons.sync),
                    label: Text(syncProvider.isSyncing ? 'Sincronizando...' : 'Sincronizar Ahora'),
                    onPressed: syncProvider.isSyncing || syncProvider.isOffline
                        ? null
                        : () async {
                            final report = await syncProvider.triggerSync();
                            if (context.mounted) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(
                                  content: Text(report.message),
                                  backgroundColor: report.isSuccess
                                      ? const Color(0xFF10B981)
                                      : const Color(0xFFEF4444),
                                ),
                              );
                            }
                          },
                  ),
                ),
                if (syncProvider.lastReport != null) ...[
                  const SizedBox(height: 8),
                  Text(
                    'Último resultado: ${syncProvider.lastReport!.message}',
                    style: TextStyle(
                      fontSize: 11,
                      fontStyle: FontStyle.italic,
                      color: syncProvider.lastReport!.isSuccess
                          ? const Color(0xFF10B981)
                          : const Color(0xFFEF4444),
                    ),
                  ),
                ],
              ],
            ),
          ),

          // Vistas de Pestañas
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _buildQueueTab(context, syncProvider, isDark),
                _buildErrorsTab(context, syncProvider, isDark),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQueueTab(BuildContext context, SyncProvider provider, bool isDark) {
    if (provider.pendingOperations.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.check_circle_outline, size: 64, color: Colors.green.shade400),
            const SizedBox(height: 12),
            const Text(
              'No hay operaciones pendientes en cola',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 6),
            const Text(
              'Todos los datos locales están al día con el servidor.',
              style: TextStyle(fontSize: 13, color: Colors.grey),
            ),
          ],
        ),
      );
    }

    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: provider.pendingOperations.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (context, index) {
        final op = provider.pendingOperations[index];
        return Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: isDark ? const Color(0xFF1E293B) : Colors.white,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: isDark ? const Color(0xFF334155) : const Color(0xFFE2E8F0),
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                    decoration: BoxDecoration(
                      color: _getOpColor(op.tipoOperacion.name).withOpacity(0.15),
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: Text(
                      '${op.tipoOperacion.name} • ${op.entidad}',
                      style: TextStyle(
                        fontSize: 11,
                        fontWeight: FontWeight.bold,
                        color: _getOpColor(op.tipoOperacion.name),
                      ),
                    ),
                  ),
                  Text(
                    'Reintentos: ${op.reintentos}',
                    style: const TextStyle(fontSize: 11, color: Colors.grey),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                'ID Operación: ${op.idOperacion}',
                style: const TextStyle(fontSize: 12, fontFamily: 'monospace'),
              ),
              const SizedBox(height: 4),
              Text(
                'Datos: ${op.datos}',
                style: TextStyle(
                  fontSize: 12,
                  color: isDark ? Colors.white70 : Colors.black87,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildErrorsTab(BuildContext context, SyncProvider provider, bool isDark) {
    if (provider.activeErrors.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.verified_outlined, size: 64, color: Colors.blue.shade400),
            const SizedBox(height: 12),
            const Text(
              'No se registran errores de sincronización',
              style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 6),
            const Text(
              'El registro de auditoría de sincronización está limpio.',
              style: TextStyle(fontSize: 13, color: Colors.grey),
            ),
          ],
        ),
      );
    }

    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: provider.activeErrors.length,
      separatorBuilder: (_, __) => const SizedBox(height: 10),
      itemBuilder: (context, index) {
        final err = provider.activeErrors[index];
        return Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: isDark ? const Color(0xFF1E293B) : Colors.white,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: const Color(0xFFEF4444).withOpacity(0.3),
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    '${err.operacion} • ${err.entidad}',
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 13,
                      color: Color(0xFFEF4444),
                    ),
                  ),
                  TextButton(
                    onPressed: () => provider.resolveError(err.id),
                    child: const Text('Resolver', style: TextStyle(fontSize: 12)),
                  ),
                ],
              ),
              Text(
                err.mensaje,
                style: const TextStyle(fontSize: 12),
              ),
              if (err.detalles != null) ...[
                const SizedBox(height: 4),
                Text(
                  err.detalles!,
                  style: const TextStyle(fontSize: 11, color: Colors.grey),
                ),
              ],
            ],
          ),
        );
      },
    );
  }

  Color _getOpColor(String type) {
    switch (type) {
      case 'CREATE':
        return const Color(0xFF10B981);
      case 'UPDATE':
        return const Color(0xFF38BDF8);
      case 'DELETE':
        return const Color(0xFFEF4444);
      default:
        return Colors.grey;
    }
  }
}
