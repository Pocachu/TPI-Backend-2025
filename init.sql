-- Script de inicialización de la base de datos para pruebas

-- Creación de tablas
CREATE TABLE IF NOT EXISTS public.tipo_producto (
    id_tipo_producto SERIAL PRIMARY KEY,
    nombre character varying(155) NOT NULL,
    activo boolean DEFAULT true,
    observaciones text
);

COMMENT ON TABLE public.tipo_producto IS 'Califica los tipos de productos';

CREATE TABLE IF NOT EXISTS public.producto (
    id_producto BIGSERIAL PRIMARY KEY,
    nombre character varying(155),
    activo boolean DEFAULT true,
    observaciones text
);

COMMENT ON TABLE public.producto IS 'Productos disponibles para consumo';

CREATE TABLE IF NOT EXISTS public.producto_detalle (
    id_tipo_producto integer NOT NULL,
    id_producto bigint NOT NULL,
    activo boolean DEFAULT true,
    observaciones text,
    PRIMARY KEY (id_tipo_producto, id_producto),
    CONSTRAINT fk_producto_detalle_producto FOREIGN KEY (id_producto)
        REFERENCES public.producto (id_producto) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_producto_detalle_tipo_producto FOREIGN KEY (id_tipo_producto)
        REFERENCES public.tipo_producto (id_tipo_producto) ON UPDATE CASCADE ON DELETE RESTRICT
);

COMMENT ON TABLE public.producto_detalle IS 'Determina los tipos de producto que aplican para un producto';

CREATE TABLE IF NOT EXISTS public.producto_precio (
    id_producto_precio BIGSERIAL PRIMARY KEY,
    id_producto bigint,
    fecha_desde date DEFAULT now(),
    fecha_hasta date,
    precio_sugerido numeric(8,2),
    CONSTRAINT fk_producto_precio_producto FOREIGN KEY (id_producto)
        REFERENCES public.producto (id_producto) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Inserción de datos de prueba
INSERT INTO public.tipo_producto (nombre, activo, observaciones) VALUES 
('bebida', true, 'Bebidas de todo tipo'),
('comida', true, 'Platillos principales'),
('tipicos', true, 'Comida típica salvadoreña');

-- Insertar productos iniciales para pruebas
INSERT INTO public.producto (nombre, activo, observaciones) VALUES 
('Pupusa de Queso', true, 'Pupusa tradicional de queso'),
('Pupusa de Chicharrón', true, 'Pupusa tradicional de chicharrón molido'),
('Pupusa Revuelta', true, 'Pupusa de queso con chicharrón'),
('Horchata', true, 'Bebida típica de semillas y especias'),
('Café de Olla', true, 'Café tradicional preparado en olla de barro');

-- Relacionar productos con tipos
INSERT INTO public.producto_detalle (id_tipo_producto, id_producto, activo) VALUES 
(3, 1, true),
(3, 2, true),
(3, 3, true),
(1, 4, true),
(1, 5, true);

-- Insertar precios para los productos
INSERT INTO public.producto_precio (id_producto, fecha_desde, precio_sugerido) VALUES 
(1, '2025-01-01', 1.00),
(2, '2025-01-01', 1.25),
(3, '2025-01-01', 1.50),
(4, '2025-01-01', 1.75),
(5, '2025-01-01', 1.50);
