package giis.demo.jdbc.models;

public class Product {

    private int id;
    private String name;
    private String category;
    private String subcategory;
    private String modelCode;

    // --- Constructors ---
    public Product() {}

    public Product(String name,
                   String category,
                   String subcategory,
                   String modelCode) {
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
        this.modelCode = modelCode;
    }

    public Product(int id,
                   String name,
                   String category,
                   String subcategory,
                   String modelCode) {
        this(name, category, subcategory, modelCode);
        this.id = id;
    }

    // --- Getters / Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", subcategory='" + subcategory + '\'' +
                ", modelCode='" + modelCode + '\'' +
                '}';
    }
}
