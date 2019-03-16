package main

import (
	"fmt"
	"io"
	"math"
)

//--------Factory--------------
type Factory struct {
	produced  int
	delivered int
	id        int
}

/*
Returns a Factory struct with delivered = 0
*/
func newFactory(produced int, id int) *Factory {
	return &Factory{produced, 0, id}
}

//------------------------------

//--------Warehouse-------------
type Warehouse struct {
	requested int
	received  int
	id        int
}

/*
Returns a Warehouse struct with received = 0
*/
func newWarehouse(requested int, id int) *Warehouse {
	return &Warehouse{requested, 0, id}
}

//------------------------------

//--------Route-----------------
type Route struct {
	factory   *Factory
	warehouse *Warehouse
	cost      int
	amount    int
}

/*
Returns a Route struct with amount = 0
*/
func newRoute(factory *Factory, warehouse *Warehouse, cost int) *Route {
	return &Route{factory, warehouse, cost, 0}
}

//------------------------------

//--------Path-----------------
type Path struct {
	routes   []*Route
	unitcost int
}

func newPath() *Path {
	routes := make([]*Route, 0)
	return &Path{routes, 0}
}

func (path *Path) pathComplete() bool {
	return path.routes[0].warehouse.id == path.routes[len(path.routes)-1].warehouse.id &&
		len(path.routes) > 3 && len(path.routes)%2 == 0
}

func (path *Path) getUnitCost() int {
	sum := 0
	for j := 0; j < len(path.routes); j++ {
		sum += path.routes[j].cost * int(math.Pow(-1, float64(j)))
	}
	return sum
}

//------------------------------

//--------Helper Methods--------

/*
Returns a deep copy of path with route added
*/
func copyAndAdd(path *Path, route *Route) *Path {
	newPath := newPath()
	for i := 0; i < len(path.routes); i++ {
		newPath.routes = append(newPath.routes, path.routes[i])
	}
	newPath.routes = append(newPath.routes, route)
	return newPath
}

/*
Returns a deep copy of pathlist
*/
func copyList(pathlist []*Path) []*Path {
	newlist := make([]*Path, 0)
	for i := 0; i < len(pathlist); i++ {
		newlist = append(newlist, pathlist[i])
	}
	return newlist
}

/*
Returns true if route is contained in the slice of routes
*/
func containsRoute(slice []*Route, route *Route) bool {
	for _, temp := range slice {
		if temp == route {
			return true
		}
	}
	return false
}

/*
Prints a formatted 2d representation of a 2D slice (matrix)
*/
func print2D(toPrint [][]string) {
	// Loop through all rows
	for i := 0; i < len(toPrint); i++ {
		// Loop through all elements of current row
		for j := 0; j < len(toPrint[i]); j++ {
			if j == 0 {
				fmt.Printf("%-9s", toPrint[i][j]+" ")
			} else {
				fmt.Printf("%-5s", toPrint[i][j]+" ")
			}
		}
		fmt.Print("\n")
	}
	fmt.Println("\n")
}

/*
Writes a formatted 2d representation of a 2D slice (matrix) to a list
*/
func print2DFile(toPrint [][]string, w io.Writer) {
	// Loop through all rows
	for i := 0; i < len(toPrint); i++ {
		// Loop through all elements of current row
		for j := 0; j < len(toPrint[i]); j++ {
			if j == 0 {
				_, err := fmt.Fprintf(w, "%-9s", toPrint[i][j]+" ")
				if err != nil {
					panic(err)
				}
			} else {
				_, err := fmt.Fprintf(w, "%-5s", toPrint[i][j]+" ")
				if err != nil {
					panic(err)
				}
			}
		}
		_, err := fmt.Fprint(w,
			"\r\n",
		)
		if err != nil {
			panic(err)
		}
	}
	_, err := fmt.Fprint(w,
		"\r\n",
	)
	if err != nil {
		panic(err)
	}
}

//------------------------------
