public class Factory{
    private int produced;
    private int delivered;
    private int id;

    Factory(int produced, int id){
        this.produced = produced;
        this.delivered = 0;
        this.id = id;
    }

    public int getProduced(){
        return produced;
    }

    public int getDelivered(){
        return delivered;
    }
    public void setDelivered(int delivered){
        this.delivered = delivered;
    }


    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

}

