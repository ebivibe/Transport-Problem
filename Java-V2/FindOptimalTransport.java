import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;
import java.io.*;
import java.lang.Math;

public class FindOptimalTransport {

    private String[][] input;
    private String[][] minimumcell;
    private String[][] steppingstone;
    private Route[] routes;
    private Warehouse[] warehouses;
    private Factory[] factories;

    /**
     * 
     * @param filename
     */
    private int[] readIn(String filename) {
        int[] values;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            BufferedReader br = new BufferedReader(new FileReader(filename));
            // get # of lines
            int rows = 0;
            int columns = 0;
            int counter = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (counter == 0) {
                    String[] line1 = line.split(" ");
                    columns = line1.length;
                }
                counter++;
                rows++;
            }
            reader.close();

            input = new String[rows][columns];
            int countrow = 0;
            while ((line = br.readLine()) != null) {
                input[countrow] = line.split(" ");
                countrow++;
            }
            br.close();

            factories = new Factory[rows - 2];
            for (int i = 0; i < rows - 1 - 1; i++) {
                factories[i] = new Factory(Integer.parseInt(input[i + 1][columns - 1].replaceAll("\\s", "")), i + 1);
            }

            warehouses = new Warehouse[columns - 2];
            for (int i = 0; i < columns - 1 - 1; i++) {
                warehouses[i] = new Warehouse(Integer.parseInt(input[rows - 1][i + 1].replaceAll("\\s", "")), i + 1);
            }

            routes = new Route[(rows - 2) * (columns - 2)];
            int index = 0;
            for (int i = 0; i < factories.length; i++) {
                for (int j = 0; j < warehouses.length; j++) {
                    routes[index] = new Route(factories[i], warehouses[j],
                            Integer.parseInt(input[i + 1][j + 1].replaceAll("\\s", "")), 0);
                    index++;
                }
            }

            minimumcell = new String[rows][columns];
            steppingstone = new String[rows][columns];
            for (int i = 0; i < minimumcell[0].length; i++) {
                minimumcell[0][i] = input[0][i];
            }
            minimumcell[0][0] = "INITIAL";
            for (int i = 0; i < minimumcell[0].length; i++) {
                steppingstone[0][i] = input[0][i];
            }
            steppingstone[0][0] = "FINAL";
            minimumcell[rows - 1] = input[rows - 1];
            steppingstone[rows - 1] = input[rows - 1];

            for (int i = 1; i < rows - 1; i++) {
                minimumcell[i][0] = input[i][0];
                steppingstone[i][0] = input[i][0];
                minimumcell[i][columns - 1] = input[i][columns - 1];
                steppingstone[i][columns - 1] = input[i][columns - 1];

            }

            for (int i = 1; i < rows - 1; i++) {
                for (int j = 1; j < columns - 1; j++) {
                    minimumcell[i][j] = "0";
                    steppingstone[i][j] = "0";
                }
            }
            values = new int[] { rows, columns };

        }

        catch (FileNotFoundException ex) {
            System.out.println("File '" + filename + "' not found'");
            values = new int[] { 0, 0 };
        } catch (IOException ex) {
            System.out.println("Error reading file '" + filename + "'");
            values = new int[] { 0, 0 };
        }
        return values;

    }

    private void transferMinimumCell(Route route, int amount) {
        route.setAmount(route.getAmount() + amount);
        factories[route.getFactory().getId() - 1]
                .setDelivered(factories[route.getFactory().getId() - 1].getDelivered() + amount);
        warehouses[route.getWarehouse().getId() - 1]
                .setReceived(warehouses[route.getWarehouse().getId() - 1].getReceived() + amount);
        if (minimumcell[route.getFactory().getId()][route.getWarehouse().getId()] == "-") {
            minimumcell[route.getFactory().getId()][route.getWarehouse().getId()] = amount + "";
            steppingstone[route.getFactory().getId()][route.getWarehouse().getId()] = amount + "";
        } else {
            minimumcell[route.getFactory().getId()][route.getWarehouse().getId()] = (Integer.parseInt(
                    minimumcell[route.getFactory().getId()][route.getWarehouse().getId()].replaceAll("\\s", ""))
                    + amount) + "";
            steppingstone[route.getFactory().getId()][route.getWarehouse().getId()] = (Integer.parseInt(
                steppingstone[route.getFactory().getId()][route.getWarehouse().getId()].replaceAll("\\s", ""))
                    + amount) + "";
        }
    }

    private void transferSteppingStoneCell(Route route, int amount) {
        route.setAmount(route.getAmount() + amount);
        factories[route.getFactory().getId() - 1]
                .setDelivered(factories[route.getFactory().getId() - 1].getDelivered() + amount);
        warehouses[route.getWarehouse().getId() - 1]
                .setReceived(warehouses[route.getWarehouse().getId() - 1].getReceived() + amount);
        if (steppingstone[route.getFactory().getId()][route.getWarehouse().getId()] == "-") {
            steppingstone[route.getFactory().getId()][route.getWarehouse().getId()] = amount + "";
        } else {
            steppingstone[route.getFactory().getId()][route.getWarehouse().getId()] = (Integer.parseInt(
                steppingstone[route.getFactory().getId()][route.getWarehouse().getId()].replaceAll("\\s", ""))
                    + amount) + "";
        }
    }

    private Route getMinCost() {
        int cost = 0;
        int route_id = -1;
        boolean first = true;

        for (int i = 0; i < routes.length; i++) {
            if (first && !(routes[i].getWarehouse().getRequested() == routes[i].getWarehouse().getReceived()
                    || routes[i].getFactory().getProduced() == routes[i].getFactory().getDelivered())) {
                cost = routes[i].getCost();
                route_id = i;
                first = false;
            } else if (routes[i].getCost() < cost
                    && !(routes[i].getWarehouse().getRequested() == routes[i].getWarehouse().getReceived()
                            || routes[i].getFactory().getProduced() == routes[i].getFactory().getDelivered())) {
                cost = routes[i].getCost();
                route_id = i;
            }
        }

        return routes[route_id];
    }

    private Path copyAndAdd(Path path, Route route){
        Path newPath = new Path();
        for(int i = 0; i < path.getRoutes().size(); i++){
            newPath.getRoutes().add(path.getRoutes().get(i));
        }
        newPath.getRoutes().add(route);
        return newPath;
    }
    
    private ArrayList<Path> copyList(ArrayList<Path> pathlist){
    	ArrayList<Path> newlist = new ArrayList<Path>();
        for(int i = 0; i < pathlist.size(); i++){
            newlist.add(pathlist.get(i));
        }
        return newlist;
    }

    private boolean pathComplete(Path path){
        return path.getRoutes().get(0).getWarehouse().getId()==path.getRoutes().get(path.getRoutes().size()-1).getWarehouse().getId() &&
            path.getRoutes().size()>3 && path.getRoutes().size()%2 == 0;
    }


    private ArrayList<Path> getOptimalSequence() {
        ArrayList<Path> possiblepaths = new ArrayList<Path>();

        for (int i = 0; i < routes.length; i++) {
            if (routes[i].getAmount() == 0) {
                Path temp = new Path();
                temp.getRoutes().add(routes[i]);
                possiblepaths.add(temp);
            }
        }

        boolean complete = false;
        while(!complete){
            ArrayList<Path> newpaths = new ArrayList<Path>();
            ArrayList<Path> copy = copyList(possiblepaths);
            for(int i = 0; i < copy.size(); i++){
                Path temppath = copy.get(i);
                Route temproute = temppath.getRoutes().get(temppath.getRoutes().size()-1);
                ArrayList<Route> nextroutes = new ArrayList<Route>();
                if (temppath.getRoutes().size()%2==0) {
                    for (int j = 0; j < routes.length; j++) {
                        if ((routes[j].getAmount() != 0 && temppath.getRoutes().indexOf(routes[j]) == -1)
                                && routes[j].getWarehouse().getId() == temproute.getWarehouse().getId()
                                && routes[j] != temproute) {
                            nextroutes.add(routes[j]);
                        }
                    }
                } else {
                    for (int j = 0; j < routes.length; j++) {
                        if ((routes[j].getAmount() != 0 && temppath.getRoutes().indexOf(routes[j]) == -1)
                                && routes[j].getFactory().getId() == temproute.getFactory().getId()
                                && routes[j] != temproute) {
                                nextroutes.add(routes[j]);

                        }
                    }
                }
                if(nextroutes.size()==0 && !pathComplete(temppath)){
                }
                else if(pathComplete(temppath)&& nextroutes.size()!=0) {
                	newpaths.add(temppath);
                }
                else if(nextroutes.size()==0 && pathComplete(temppath)) {
                	newpaths.add(temppath);
                	
                }
                else{
                    for(int j = 0; j < nextroutes.size(); j++){
                        Path temp = copyAndAdd(temppath, nextroutes.get(j));
                        newpaths.add(temp);
                    }
                }
            }
            possiblepaths = newpaths;
            complete = true;
            for(int i = 0; i<possiblepaths.size(); i++){
                if(!pathComplete(possiblepaths.get(i))){
                    complete = false;
                }
            }
        }

        return possiblepaths;
    }



    private void minCellMethod(int supply) {
        boolean satisfied = false;
        while (!satisfied) {

            Route route = getMinCost();
            transferMinimumCell(route,
                    Math.min(
                            warehouses[route.getWarehouse().getId() - 1].getRequested()
                                    - warehouses[route.getWarehouse().getId() - 1].getReceived(),
                            factories[route.getFactory().getId() - 1].getProduced()
                                    - factories[route.getFactory().getId() - 1].getDelivered()));

            int delivered = 0;
            for (int i = 0; i < factories.length; i++) {
                delivered += factories[i].getDelivered();
            }
            if (delivered == supply) {
                satisfied = true;
            }
        }
    }

    private void steppingStoneMethod() {
        ArrayList<Path> paths = getOptimalSequence();
        boolean temp=true;
        while (paths.size()>0) {
            Path bestpath = paths.get(0);
            int bestcost = 0;

            for(int i=0; i<paths.size(); i++){
                int sum = 0;
                for(int j=0; j<paths.get(i).getRoutes().size(); j++){
                    sum+= paths.get(i).getRoutes().get(j).getCost()*Math.pow(-1, j);
                }
                paths.get(i).setUnitcost(sum);
                if(sum<bestcost){
                    bestpath = paths.get(i);
                    bestcost = sum;
                }
            }
            if(bestcost == 0){
                break;
            }

            int maxamount = bestpath.getRoutes().get(1).getAmount();
            for(int i=0; i<bestpath.getRoutes().size(); i++){
                if(Math.pow(-1, i)<0 && bestpath.getRoutes().get(i).getAmount()<maxamount){
                    maxamount = bestpath.getRoutes().get(i).getAmount();

                }
            }
            for(int i=0; i<bestpath.getRoutes().size(); i++){
            transferSteppingStoneCell(bestpath.getRoutes().get(i), (int)(maxamount*Math.pow(-1, i)));
            Route x = bestpath.getRoutes().get(i);
            }
            int coststep = 0;
            for(int i=0; i<routes.length; i++){
                coststep+=routes[i].getCost()*routes[i].getAmount();
            }
            
    
            paths = getOptimalSequence(); 

        }
    }

    private void print2D(String[][] toPrint) {
        // Loop through all rows
        for (int i = 0; i < toPrint.length; i++) {
            // Loop through all elements of current row
            for (int j = 0; j < toPrint[i].length; j++)
                if (j == 0) {
                   System.out.printf("%-9s", toPrint[i][j] + " ");
                } else {
                   System.out.printf("%-5s", toPrint[i][j] + " ");
                }
                
           System.out.print("\n");
        }
        System.out.println("\n");
    }

    public void findOptimalTransport(String filename) throws Exception {
        

        int[] rowcol = readIn(filename);
        int sumdemand = 0;
        int sumsupply = 0;
        for (int i = 0; i < warehouses.length; i++) {
            sumdemand += warehouses[i].getRequested();
        }
        for (int i = 0; i < factories.length; i++) {
            sumsupply += factories[i].getProduced();
        }
        if (sumdemand != sumsupply) {
            throw new Exception("Unblanced supply and demand");
        }

        System.out.println("\nInput: ");
        print2D(input);

        minCellMethod(sumsupply);

        int costmin = 0;
        for(int i=0; i<routes.length; i++){
            costmin+=routes[i].getCost()*routes[i].getAmount();
        }
        System.out.println("Minimum Cell Cost Method ($"+costmin+"):");

        print2D(minimumcell);

        int occupied = 0;
        for (int i = 1; i < minimumcell.length - 1; i++) {
            for (int j = 1; j < minimumcell[0].length - 1; j++) {
                if (minimumcell[i][j] != "0") {
                    occupied++;
                }
            }
        }
        if (occupied != rowcol[0] - 2 + rowcol[1] - 2 - 1) {
            throw new Exception("Degenerate case");
        }

        steppingStoneMethod();
        int coststep = 0;
        for(int i=0; i<routes.length; i++){
            coststep+=routes[i].getCost()*routes[i].getAmount();
        }
        System.out.println("Stepping Stone Method ($"+coststep+"):");

        print2D(steppingstone);

    }
}