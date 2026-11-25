package cl.maestros.catalogo.service;

import cl.maestros.catalogo.dto.CategoriaResponse;
import cl.maestros.catalogo.dto.ProductoRequest;
import cl.maestros.catalogo.dto.ProductoResponse;
import cl.maestros.catalogo.exception.ApiException;
import cl.maestros.catalogo.model.Categoria;
import cl.maestros.catalogo.model.Producto;
import cl.maestros.catalogo.repository.CategoriaRepository;
import cl.maestros.catalogo.repository.DetallePedidoRepository;
import cl.maestros.catalogo.repository.ProductoRepository;
import java.text.Normalizer;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final DetallePedidoRepository detalles;

    public ProductoService(ProductoRepository productos, CategoriaRepository categorias, DetallePedidoRepository detalles) {
        this.productos = productos;
        this.categorias = categorias;
        this.detalles = detalles;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categorias.findAllByOrderByIdAsc().stream().map(CategoriaResponse::desde).toList();
    }

    // Filtro opcional por categoria y por texto (nombre o marca, sin importar tildes ni mayusculas)
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(Short categoriaId, String texto) {
        String buscado = normalizar(texto);
        return productos.findAllByOrderByCodigoAsc().stream()
                .filter(p -> categoriaId == null || p.getCategoria().getId().equals(categoriaId))
                .filter(p -> buscado.isEmpty()
                        || normalizar(p.getNombre()).contains(buscado)
                        || normalizar(p.getMarca()).contains(buscado))
                .map(ProductoResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(String codigo) {
        return ProductoResponse.desde(buscar(codigo));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest r) {
        if (r.codigo() == null || r.codigo().isBlank()) {
            throw ApiException.invalido("El codigo es obligatorio.");
        }
        String codigo = r.codigo().trim().toUpperCase();
        if (productos.existsById(codigo)) {
            throw ApiException.conflicto("Ya existe un producto con el codigo " + codigo + ".");
        }
        Producto p = new Producto();
        p.setCodigo(codigo);
        aplicarDatos(p, r);
        return ProductoResponse.desde(productos.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(String codigo, ProductoRequest r) {
        Producto p = buscar(codigo);
        aplicarDatos(p, r);
        return ProductoResponse.desde(productos.save(p));
    }

    // Un producto con pedidos no se borra: se perderia el historial (409)
    @Transactional
    public void eliminar(String codigo) {
        Producto p = buscar(codigo);
        if (detalles.existsByProductoCodigo(p.getCodigo())) {
            throw ApiException.conflicto("El producto tiene pedidos asociados y no se puede eliminar. Puedes dejar su stock en 0.");
        }
        productos.delete(p);
    }

    private void aplicarDatos(Producto p, ProductoRequest r) {
        if (r.precioOferta() != null && r.precioOferta() >= r.precioVenta()) {
            throw ApiException.invalido("El precio de oferta debe ser menor que el precio de venta.");
        }
        Categoria categoria = categorias.findById(r.categoriaId())
                .orElseThrow(() -> ApiException.invalido("La categoria " + r.categoriaId() + " no existe."));
        p.setNombre(r.nombre().trim());
        p.setMarca(r.marca().trim());
        p.setUnidad(r.unidad().trim());
        p.setPrecioVenta(r.precioVenta());
        p.setPrecioOferta(r.precioOferta());
        p.setStock(r.stock());
        p.setStockMinimo(r.stockMinimo());
        p.setDestacado(Boolean.TRUE.equals(r.destacado()));
        p.setCategoria(categoria);
    }

    private Producto buscar(String codigo) {
        return productos.findById(codigo.trim().toUpperCase())
                .orElseThrow(() -> ApiException.noEncontrado("Producto no encontrado."));
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}
