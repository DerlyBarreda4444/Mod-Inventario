package com.example.inventario.repository;

import com.example.inventario.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Todos los productos de una sucursal específica
    List<Producto> findBySucursalIgnoreCase(String sucursal);

    // Productos en alerta: stock actual <= stockMinimo
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo")
    List<Producto> findProductosEnAlerta();

    // Alertas filtradas por sucursal
    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo AND LOWER(p.sucursal) = LOWER(:sucursal)")
    List<Producto> findAlertasBySucursal(@Param("sucursal") String sucursal);

    // Verificar si un SKU ya existe (para validación en PUT)
    boolean existsBySkuAndIdNot(String sku, Long id);
}
