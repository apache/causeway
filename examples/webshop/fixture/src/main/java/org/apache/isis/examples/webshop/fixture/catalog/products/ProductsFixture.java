package org.apache.isis.examples.webshop.fixture.catalog.products;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.applib.value.Money;
import org.apache.isis.examples.webshop.dom.catalog.categories.ProductCategories;
import org.apache.isis.examples.webshop.dom.catalog.categories.ProductCategory;
import org.apache.isis.examples.webshop.dom.catalog.products.Product;
import org.apache.isis.examples.webshop.dom.catalog.products.Products;


public class ProductsFixture extends AbstractFixture {

    @Override
    public void install() {
        newProduct("Microsoft Office 2010 Professional, 1 User (PC DVD)", "SWR", 34213, "http://ecx.images-amazon.com/images/I/41f2lTZrgDL._SL500_AA300_.jpg");
        newProduct("Office for Mac 2011, Home and Business Edition (1 User, 1 Mac)", "SWR", 19999, "http://ecx.images-amazon.com/images/I/31SWyjJQ5RL._SL500_AA300_.jpg");
        newProduct("Norton 360 v5.0, 1 User, 3 PCs 1 Year Subscription (PC)", "SWR", 3999, "http://ecx.images-amazon.com/images/I/51L1BC9M1FL._SL500_AA300_.jpg");
        
        newProduct("Canon IXUS 230 HS Digital Camera", "CAM", 20503, "http://ecx.images-amazon.com/images/I/415OT%2BlbHML._SL500_AA300_.jpg");
        newProduct("Fujifilm FinePix T200 Digital Camera", "CAM", 9300, "http://ecx.images-amazon.com/images/I/51lG6J8u24L._SL500_AA300_.jpg");
        newProduct("Panasonic Lumix FZ45 14.1MP Digital Camera", "CAM", 9300, "http://ecx.images-amazon.com/images/I/41WxTa14-AL._SL500_AA300_.jpg");
        
        newProduct("TomTom GO LIVE 825 EU", "CAR", 19390, "http://ecx.images-amazon.com/images/I/51l79KCu42L._SL500_AA300_.jpg");
        newProduct("Garmin Nuvi 2460LT Widescreen", "CAR", 15999, "http://ecx.images-amazon.com/images/I/51O%2BF9uSM2L._SL500_AA300_.jpg");
        newProduct("TomTom GO LIVE 1005 World", "CAR", 19390, "http://ecx.images-amazon.com/images/I/41awZLiA%2ByL._SL500_AA300_.jpg");
        
        newProduct("Western Digital TV Live HD Media Player", "HWR", 12257, "http://ecx.images-amazon.com/images/I/417mw6e7UvL._SL500_AA300_.jpg");
        newProduct("Logitech OEM S150 2.0 Speaker System", "HWR", 1170, "http://ecx.images-amazon.com/images/I/41D8UY70PKL._SL500_AA300_.jpg");
        newProduct("Western Digital Caviar 2TB SATAII 64MB", "HWR", 16000, "http://ecx.images-amazon.com/images/I/51yl2DEVzkL._SL500_AA300_.jpg");
    }
    
    
    private Product newProduct(String name, String categoryCode, int price, String imagePath) {
        final ProductCategory category = productCategories.findByCode(categoryCode);
        return products.newProduct(name, category, new Money(price/100, "GBP"), imagePath);
    }


    // {{ injected: Products
    private Products products;

    public void setProducts(final Products products) {
        this.products = products;
    }
    // }}
    
    // {{ injected: ProductCategories
    private ProductCategories productCategories;

    public void setProductCategories(final ProductCategories productCategories) {
        this.productCategories = productCategories;
    }
    // }}




}
