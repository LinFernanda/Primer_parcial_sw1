import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/barberia_provider.dart';

class ReservasScreen extends StatefulWidget {
  const ReservasScreen({super.key});

  @override
  State<ReservasScreen> createState() => _ReservasScreenState();
}

class _ReservasScreenState extends State<ReservasScreen> {
  String _filtroEstado = 'TODAS';

  void _showAddReservaDialog(BuildContext context) {
    final clienteCtrl = TextEditingController();
    final barberoCtrl = TextEditingController(text: 'Mateo Fernández');
    final servicioCtrl = TextEditingController(text: 'Corte de Cabello Clásico');
    DateTime fechaSeleccionada = DateTime.now().add(const Duration(hours: 2));

    showDialog(
      context: context,
      builder: (dialogCtx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          title: const Text('Nueva Reserva de Cita'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: clienteCtrl,
                  decoration: const InputDecoration(labelText: 'Nombre del Cliente'),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: barberoCtrl,
                  decoration: const InputDecoration(labelText: 'Barbero'),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: servicioCtrl,
                  decoration: const InputDecoration(labelText: 'Servicio'),
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    const Icon(Icons.access_time, size: 18, color: Color(0xFFD97706)),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Fecha: ${fechaSeleccionada.day}/${fechaSeleccionada.month} - ${fechaSeleccionada.hour.toString().padLeft(2, '0')}:${fechaSeleccionada.minute.toString().padLeft(2, '0')}',
                        style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                      ),
                    ),
                    TextButton(
                      onPressed: () async {
                        final picked = await showTimePicker(
                          context: context,
                          initialTime: TimeOfDay.fromDateTime(fechaSeleccionada),
                        );
                        if (picked != null) {
                          setDialogState(() {
                            fechaSeleccionada = DateTime(
                              fechaSeleccionada.year,
                              fechaSeleccionada.month,
                              fechaSeleccionada.day,
                              picked.hour,
                              picked.minute,
                            );
                          });
                        }
                      },
                      child: const Text('Cambiar Hora'),
                    ),
                  ],
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(dialogCtx).pop(),
              child: const Text('Cancelar'),
            ),
            ElevatedButton(
              onPressed: () async {
                if (clienteCtrl.text.trim().isNotEmpty) {
                  final provider = Provider.of<BarberiaProvider>(context, listen: false);
                  await provider.addReserva(
                    clienteNombre: clienteCtrl.text.trim(),
                    barberoNombre: barberoCtrl.text.trim(),
                    servicioNombre: servicioCtrl.text.trim(),
                    fechaHora: fechaSeleccionada,
                  );
                  if (dialogCtx.mounted) Navigator.of(dialogCtx).pop();
                }
              },
              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFD97706)),
              child: const Text('Agendar Cita', style: TextStyle(color: Colors.white)),
            ),
          ],
        ),
      ),
    );
  }

  Color _getColorForEstado(String estado) {
    switch (estado.toUpperCase()) {
      case 'CONFIRMADA':
        return const Color(0xFF10B981);
      case 'PENDIENTE':
        return const Color(0xFFF59E0B);
      case 'COMPLETADA':
        return const Color(0xFF38BDF8);
      case 'CANCELADA':
        return const Color(0xFFEF4444);
      default:
        return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final provider = Provider.of<BarberiaProvider>(context);

    final reservasFiltradas = provider.reservas.where((r) {
      if (_filtroEstado == 'TODAS') return true;
      return r.estado.toUpperCase() == _filtroEstado;
    }).toList();

    return Scaffold(
      backgroundColor: isDark ? const Color(0xFF0F172A) : const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text('Gestión de Reservas'),
        backgroundColor: isDark ? const Color(0xFF1E293B) : Colors.white,
        elevation: 0,
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showAddReservaDialog(context),
        backgroundColor: const Color(0xFFD97706),
        icon: const Icon(Icons.add, color: Colors.white),
        label: const Text('Nueva Cita', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
      ),
      body: Column(
        children: [
          // Chips de Filtro
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            child: Row(
              children: ['TODAS', 'CONFIRMADA', 'PENDIENTE', 'COMPLETADA', 'CANCELADA'].map((est) {
                final selected = _filtroEstado == est;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: FilterChip(
                    label: Text(est),
                    selected: selected,
                    onSelected: (_) => setState(() => _filtroEstado = est),
                    selectedColor: const Color(0xFFD97706).withOpacity(0.2),
                    checkmarkColor: const Color(0xFFD97706),
                    labelStyle: TextStyle(
                      fontSize: 12,
                      fontWeight: selected ? FontWeight.bold : FontWeight.normal,
                      color: selected ? const Color(0xFFD97706) : (isDark ? Colors.white70 : Colors.black87),
                    ),
                  ),
                );
              }).toList(),
            ),
          ),

          // Lista de Reservas
          Expanded(
            child: reservasFiltradas.isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.event_busy, size: 48, color: Colors.grey.shade400),
                        const SizedBox(height: 12),
                        Text(
                          'No hay reservas registradas en estado $_filtroEstado',
                          style: TextStyle(color: Colors.grey.shade500),
                        ),
                      ],
                    ),
                  )
                : ListView.separated(
                    padding: const EdgeInsets.all(16),
                    itemCount: reservasFiltradas.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 12),
                    itemBuilder: (ctx, i) {
                      final r = reservasFiltradas[i];
                      final estadoColor = _getColorForEstado(r.estado);
                      final f = r.fechaHora;
                      final dateStr = '${f.day.toString().padLeft(2, '0')}/${f.month.toString().padLeft(2, '0')} - ${f.hour.toString().padLeft(2, '0')}:${f.minute.toString().padLeft(2, '0')}';

                      return Container(
                        padding: const EdgeInsets.all(14),
                        decoration: BoxDecoration(
                          color: isDark ? const Color(0xFF1E293B) : Colors.white,
                          borderRadius: BorderRadius.circular(14),
                          border: Border.all(
                            color: isDark ? const Color(0xFF334155) : Colors.grey.shade200,
                          ),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Row(
                                  children: [
                                    const Icon(Icons.person, size: 16, color: Color(0xFFD97706)),
                                    const SizedBox(width: 6),
                                    Text(
                                      r.clienteNombre,
                                      style: TextStyle(
                                        fontSize: 16,
                                        fontWeight: FontWeight.bold,
                                        color: isDark ? Colors.white : const Color(0xFF0F172A),
                                      ),
                                    ),
                                  ],
                                ),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: estadoColor.withOpacity(0.12),
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                  child: Text(
                                    r.estado,
                                    style: TextStyle(
                                      fontSize: 11,
                                      fontWeight: FontWeight.bold,
                                      color: estadoColor,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 8),
                            Row(
                              children: [
                                Icon(Icons.content_cut, size: 14, color: Colors.grey.shade400),
                                const SizedBox(width: 6),
                                Text(r.servicioNombre, style: const TextStyle(fontSize: 13)),
                                const Spacer(),
                                Icon(Icons.badge, size: 14, color: Colors.grey.shade400),
                                const SizedBox(width: 6),
                                Text(r.barberoNombre, style: const TextStyle(fontSize: 13)),
                              ],
                            ),
                            const SizedBox(height: 8),
                            Row(
                              children: [
                                const Icon(Icons.access_time, size: 14, color: Color(0xFF38BDF8)),
                                const SizedBox(width: 6),
                                Text(dateStr, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: Color(0xFF38BDF8))),
                                const Spacer(),
                                if (r.estado != 'COMPLETADA')
                                  TextButton(
                                    onPressed: () {
                                      if (r.id != null) provider.updateReservaEstado(r.id!, 'COMPLETADA');
                                    },
                                    child: const Text('Completar', style: TextStyle(color: Color(0xFF10B981), fontSize: 12)),
                                  ),
                                if (r.estado != 'CANCELADA')
                                  TextButton(
                                    onPressed: () {
                                      if (r.id != null) provider.updateReservaEstado(r.id!, 'CANCELADA');
                                    },
                                    child: const Text('Cancelar', style: TextStyle(color: Color(0xFFEF4444), fontSize: 12)),
                                  ),
                              ],
                            ),
                          ],
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
