package mx.terabyte.labs.inventra.common.enums;

public enum ProductType {
    RAW_MATERIAL("Materia prima"),
    PACKAGING("Empaque"),
    FINISHED_PRODUCT("Producto terminado"),
    SEMI_FINISHED("Producto semiterminado");

    private final String displayName;

    ProductType(String displayName) {
        this.displayName = displayName;
    }

    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }
}