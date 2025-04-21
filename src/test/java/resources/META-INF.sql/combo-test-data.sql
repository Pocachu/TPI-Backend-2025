-- Insertar datos de prueba para la tabla combo
INSERT INTO public.combo (id_combo, nombre, activo, descripcion_publica) VALUES 
(1, 'Combo Pupusero', true, 'Incluye 3 pupusas a elección, 1 bebida y curtido extra'),
(2, 'Combo Típico Familiar', true, 'Incluye 10 pupusas variadas, 4 bebidas y curtido familiar'),
(3, 'Combo Desayuno', true, 'Incluye 2 pupusas, 1 café y 1 plátano frito'),
(4, 'Combo Fiesta', true, 'Incluye 20 pupusas variadas, 10 bebidas y curtido extra grande'),
(5, 'Combo Antojito', false, 'Incluye 1 pupusa, 1 empanada y 1 bebida pequeña');

-- Insertar datos de prueba para la tabla combo_detalle
-- Combo Pupusero
INSERT INTO public.combo_detalle (id_combo, id_producto, cantidad, activo) VALUES 
(1, 1, 1, true),  -- 1 Pupusa de Queso
(1, 2, 1, true),  -- 1 Pupusa de Chicharrón
(1, 3, 1, true),  -- 1 Pupusa Revuelta
(1, 4, 1, true);  -- 1 Horchata

-- Combo Típico Familiar
INSERT INTO public.combo_detalle (id_combo, id_producto, cantidad, activo) VALUES 
(2, 1, 3, true),  -- 3 Pupusas de Queso
(2, 2, 3, true),  -- 3 Pupusas de Chicharrón
(2, 3, 4, true),  -- 4 Pupusas Revueltas
(2, 4, 2, true),  -- 2 Horchatas
(2, 5, 2, true);  -- 2 Cafés

-- Combo Desayuno
INSERT INTO public.combo_detalle (id_combo, id_producto, cantidad, activo) VALUES 
(3, 1, 2, true),  -- 2 Pupusas de Queso
(3, 5, 1, true);  -- 1 Café

-- Combo Fiesta
INSERT INTO public.combo_detalle (id_combo, id_producto, cantidad, activo) VALUES 
(4, 1, 5, true),  -- 5 Pupusas de Queso
(4, 2, 5, true),  -- 5 Pupusas de Chicharrón
(4, 3, 10, true), -- 10 Pupusas Revueltas
(4, 4, 8, true),  -- 8 Horchatas
(4, 5, 2, true);  -- 2 Cafés

-- Combo Antojito (inactivo)
INSERT INTO public.combo_detalle (id_combo, id_producto, cantidad, activo) VALUES 
(5, 1, 1, false), -- 1 Pupusa de Queso
(5, 4, 1, false); -- 1 Horchata

-- Reinicia la secuencia para id_combo si existe
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class WHERE relname = 'combo_id_combo_seq') THEN
        PERFORM setval('combo_id_combo_seq', 5, true);
    END IF;
END
$$;
