package api_blank.producto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de producto del catálogo externo")
public class ProductoDTO {

    @Schema(description = "ID del producto en el catálogo externo", example = "1")
    private Long id;

    @Schema(description = "Nombre del producto", example = "iPhone 15")
    private String nombre;

    @Schema(description = "Descripción del producto", example = "Smartphone Apple iPhone 15 128GB")
    private String descripcion;

    @Schema(description = "Precio del producto", example = "999.99")
    private Double precio;

    @Schema(description = "Marca del producto", example = "Apple")
    private String marca;

    @Schema(description = "Categoría del producto", example = "smartphones")
    private String categoria;

    @Schema(description = "URL de la imagen del producto", example = "https://cdn.dummyjson.com/product-images/1/1716283439411-thumbnail.jpg")
    private String imagen;

    @Schema(description = "Calificación del producto", example = "4.5")
    private Double calificacion;

    @Schema(description = "Stock disponible", example = "95")
    private Integer stock;

    public ProductoDTO() {
    }

    public ProductoDTO(Long id, String nombre, String descripcion, Double precio, String marca,
            String categoria, String imagen, Double calificacion, Integer stock) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.marca = marca;
        this.categoria = categoria;
        this.imagen = imagen;
        this.calificacion = calificacion;
        this.stock = stock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
