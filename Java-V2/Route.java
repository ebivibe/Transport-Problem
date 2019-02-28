public class Route{
    private Warehouse warehouse;
    private Factory factory;
    private int cost;
    private int amount;

    Route(Factory factory, Warehouse warehouse, int cost, int amount){
        this.warehouse = warehouse;
        this.factory = factory;
        this.cost = cost;
        this.amount = amount;
    }

    /**
     * @return the warehouse
     */
    public Warehouse getWarehouse() {
        return warehouse;
    }

    /**
     * @return the factory
     */
    public Factory getFactory() {
        return factory;
    }


    /**
     * @return the cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

}