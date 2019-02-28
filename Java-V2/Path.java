import java.util.ArrayList;
public class Path{
    ArrayList<Route> routes;
    int unitcost;

    Path(){
        routes = new ArrayList<Route>();
    }

    /**
     * @return the routes
     */
    public ArrayList<Route> getRoutes() {
        return routes;
    }

    /**
     * @return the unitcost
     */
    public int getUnitcost() {
        return unitcost;
    }

    /**
     * @param unitcost the unitcost to set
     */
    public void setUnitcost(int unitcost) {
        this.unitcost = unitcost;
    }

}