public class Warehouse{
    private int requested;
    private int received;
    private int id;

    Warehouse(int requested, int id){
        this.requested = requested;
        this.received = 0;
        this.id = id;
    }

    public int getRequested(){
        return requested;
    }

    public int getReceived(){
        return received;
    }
    public void setReceived(int received){
        this.received = received;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
}

