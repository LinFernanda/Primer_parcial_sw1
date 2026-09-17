import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:case_platform_mobile/main.dart';

void main() {
  setUp(() {
    SharedPreferences.setMockInitialValues({});
  });

  testWidgets('Login screen smoke test and UI elements verification', (WidgetTester tester) async {
    await tester.pumpWidget(const CasePlatformMobileApp());
    await tester.pumpAndSettle();

    // Verificamos elementos del Login
    expect(find.text('BarberShop Mobile'), findsOneWidget);
    expect(find.text('Iniciar Sesión'), findsOneWidget);
    expect(find.byType(TextFormField), findsNWidgets(2));
    expect(find.text('Correo Electrónico'), findsOneWidget);
    expect(find.text('Contraseña'), findsOneWidget);
  });

  testWidgets('Demo login navigates to DashboardScreen', (WidgetTester tester) async {
    await tester.pumpWidget(const CasePlatformMobileApp());
    await tester.pumpAndSettle();

    // Tap en botón Iniciar Sesión con las credenciales demo prellenadas
    final loginButton = find.text('Iniciar Sesión');
    expect(loginButton, findsOneWidget);

    await tester.tap(loginButton);
    await tester.pumpAndSettle();

    // Tras el login exitoso, debe renderizarse el DashboardScreen
    expect(find.text('BarberShop CASE'), findsOneWidget);
    expect(find.text('Módulos del Sistema'), findsOneWidget);
    expect(find.text('Asistente IA de Barbería'), findsOneWidget);
    expect(find.text('Gestión de Reservas'), findsOneWidget);
    expect(find.text('Directorio de Clientes'), findsOneWidget);
    expect(find.text('Catálogo de Servicios'), findsOneWidget);
  });
}
