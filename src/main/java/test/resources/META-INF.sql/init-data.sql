-- Datos iniciales para pruebas

-- Insertar tipos de productos
INSERT INTO tipo_producto (id_tipo_producto, nombre, activo, observaciones) VALUES 
(1, 'bebida', true, 'Bebidas de todo tipo'),
(2, 'comida', true, 'Platillos principales'),
(3, 'tipicos', true, 'Comida típica salvadoreña');

-- Insertar productos
INSERT INTO producto (id_producto, nombre, activo, observaciones) VALUES 
(1, 'Pupusa de Queso', true, 'Pupusa tradicional de queso'),
(2, 'Pupusa de Chicharrón', true, 'Pupusa tradicional de chicharrón molido'),
(3, 'Pupusa Revuelta', true, 'Pupusa de queso con chicharrón'),
(4, 'Horchata', true, 'Bebida típica de semillas y especias'),
(5, 'Café de Olla', true, 'Café tradicional preparado en olla de barro');

-- Relacionar productos con tipos
INSERT INTO producto_detalle (id_tipo_producto, id_producto, activo) VALUES 
(3, 1, true),
(3, 2, true),
(3, 3, true),
(1, 4, true),
(1, 5, true);
