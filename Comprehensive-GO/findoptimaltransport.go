package main

import (
	"bufio"
	"fmt"
	"log"
	"math"
	"os"
	"strconv"
	"strings"
	"sync"
)

var input [][]string         //input matrix
var initial [][]string       //initial solution matrix
var steppingstone [][]string //stepping stone solution matrix
var routes []*Route          //list of routes
var warehouses []*Warehouse  //list of warehouses
var factories []*Factory     //list of factories
var wg sync.WaitGroup        //wait group

/*
Calculate current cost
*/
func currentCost() int {
	cost := 0
	for i := 0; i < len(routes); i++ {
		cost += routes[i].cost * routes[i].amount
	}
	return cost
}

/*
Reads in the input and initial matricies, populates the stepping
stone matrix, and populates the routes, factories, and routes slices
*/
func readIn(inputfile, solutionfile string) (int, int) {
	input = make([][]string, 0)
	initial = make([][]string, 0)

	//read in the input file
	file, err := os.Open(inputfile)
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.Split(scanner.Text(), " ")
		for i := 0; i < len(line); i++ {
			line[i] = strings.TrimSpace(line[i])
		}
		input = append(input, line)
	}

	//read in the initial solution
	file2, err2 := os.Open(solutionfile)
	if err2 != nil {
		log.Fatal(err2)
	}
	defer file2.Close()

	scanner2 := bufio.NewScanner(file2)
	for scanner2.Scan() {
		initial = append(initial, strings.Split(scanner2.Text(), " "))
	}

	if err2 := scanner2.Err(); err2 != nil {
		log.Fatal(err2)
	}
	row, col := len(input), len(input[0])

	//populate the stepping stone matrix
	steppingstone = make([][]string, row)
	for i := 0; i < row; i++ {
		steppingstone[i] = make([]string, col)
		for j := 0; j < col; j++ {
			if !(i == row-1 && j == col-1) {
				steppingstone[i][j] = initial[i][j]
			}
		}
	}

	//populate the factories slice
	factories = make([]*Factory, 0)
	for i := 0; i < row-2; i++ {
		val, err := strconv.Atoi(strings.TrimSpace(input[i+1][col-1]))
		if err == nil {
			factories = append(factories, newFactory(val, i+1))
		} else {
			panic("Error in making factories")
		}
	}

	//populate the warehouses slice
	warehouses = make([]*Warehouse, 0)
	for i := 0; i < col-2; i++ {
		val, err := strconv.Atoi(strings.TrimSpace(input[row-1][i+1]))
		if err == nil {
			warehouses = append(warehouses, newWarehouse(val, i+1))
		} else {
			panic("Error in making warehouses")
		}
	}

	//populate the routes slice
	routes = make([]*Route, 0)
	for i := 0; i < len(factories); i++ {
		for j := 0; j < len(warehouses); j++ {
			value, err := strconv.Atoi(strings.TrimSpace(input[i+1][j+1]))
			if err == nil {
				temproute := newRoute(factories[i], warehouses[j], value)
				if initial[i+1][j+1] != "-" {
					value2, err := strconv.Atoi(strings.TrimSpace(initial[i+1][j+1]))
					if err == nil {
						transferCells(temproute, value2)
					} else {
						panic("Error in making routes")
					}

				}
				routes = append(routes, temproute)

			} else {
				panic("Error in making routes")
			}
		}
	}

	return row, col
}

/*
Transfers the amount to route
*/
func transferCells(route *Route, amount int) {

	route.amount = route.amount + amount
	factories[route.factory.id-1].delivered =
		factories[route.factory.id-1].delivered + amount
	warehouses[route.warehouse.id-1].received =
		warehouses[route.warehouse.id-1].received + amount
	if steppingstone[route.factory.id][route.warehouse.id] == "-" {
		steppingstone[route.factory.id][route.warehouse.id] = strconv.Itoa(amount)
	} else {
		val, err := strconv.Atoi(strings.TrimSpace(steppingstone[route.factory.id][route.warehouse.id]))
		if err == nil {
			steppingstone[route.factory.id][route.warehouse.id] = strconv.Itoa(val + amount)
		} else {
			panic("Error while transfering cells")
		}
	}
}

/*
Finds the path from an empty cell route and calculates the marginal cost,
the path is returned through the channel ch
*/
func marginalCost(route *Route, ch chan *Path) {
	defer wg.Done()
	possiblepaths := make([]*Path, 0)

	temp := newPath()
	temp.routes = append(temp.routes, route)
	possiblepaths = append(possiblepaths, temp)

	complete := false
	//keeps adding routes until all paths are a dead end(removed) or complete
	for !complete {
		newpaths := make([]*Path, 0)
		copy := copyList(possiblepaths)
		for i := 0; i < len(copy); i++ {
			temppath := copy[i]
			temproute := temppath.routes[len(temppath.routes)-1]
			nextroutes := make([]*Route, 0)
			//if even length search vertically
			if len(temppath.routes)%2 == 0 {
				for j := 0; j < len(routes); j++ {
					if routes[j].amount != 0 && !containsRoute(temppath.routes, routes[j]) && routes[j].warehouse.id == temproute.warehouse.id && routes[j] != temproute {
						nextroutes = append(nextroutes, routes[j])
					}
				}
				//if odd length search horizontally
			} else {
				for j := 0; j < len(routes); j++ {
					if routes[j].amount != 0 && !containsRoute(temppath.routes, routes[j]) && routes[j].factory.id == temproute.factory.id && routes[j] != temproute {
						nextroutes = append(nextroutes, routes[j])

					}
				}
			}
			//check if paths are a dead end and need to be removed or if they are complete
			if len(nextroutes) == 0 && !temppath.pathComplete() {
			} else if temppath.pathComplete() && len(nextroutes) != 0 {
				newpaths = append(newpaths, temppath)
			} else if len(nextroutes) == 0 && temppath.pathComplete() {
				newpaths = append(newpaths, temppath)

			} else {
				for j := 0; j < len(nextroutes); j++ {
					temp := copyAndAdd(temppath, nextroutes[j])
					newpaths = append(newpaths, temp)
				}
			}
		}
		possiblepaths = newpaths
		complete = true
		//check if done looking for paths
		for i := 0; i < len(possiblepaths); i++ {
			if !possiblepaths[i].pathComplete() {
				complete = false
			}
		}
	}
	possiblepaths[0].unitcost = possiblepaths[0].getUnitCost()
	ch <- possiblepaths[0]

}

/*
Returns the optimal path for the current stepping stone step
*/
func getOptimalSequence() (*Path, int) {
	possiblepaths := make([]*Path, 0)
	ch := make(chan *Path, 1)

	//run a go routine for each empty cell to find the path
	for i := 0; i < len(routes); i++ {
		if routes[i].amount == 0 {
			wg.Add(1)
			go marginalCost(routes[i], ch)
		}
	}

	go func() {
		wg.Wait()
		close(ch)
	}()

	//gather all the paths
	for val := range ch {
		possiblepaths = append(possiblepaths, val)
	}

	//find the best path
	bestpath := possiblepaths[0]
	bestcost := 0

	for i := 0; i < len(possiblepaths); i++ {
		if possiblepaths[i].unitcost < bestcost {
			bestpath = possiblepaths[i]
			bestcost = possiblepaths[i].unitcost
		}
	}

	return bestpath, bestcost
}

/*
Finds the optimal transport iteratively given an input and initial solution filenames
*/
func findOptimalTransport(inputfile, solutionfile string) {
	//read in the input and initial solution

	file, err := os.Create("solution.txt")
	if err != nil {
		log.Fatal(err)
	}
	defer file.Close()

	// Create a buffered writer from the file
	bufferedWriter := bufio.NewWriter(file)

	/*_, err = bufferedWriter.WriteString(
		"Buffered string\n",
	)
	if err != nil {
		panic(err)
	}*/

	row, col := readIn(inputfile, solutionfile)

	//calculate the  total transportation cost
	oldcost := currentCost()

	//check if degenerate case
	occupied := 0
	for i := 1; i < row-1; i++ {
		for j := 1; j < col-1; j++ {
			if initial[i][j] != "0" && initial[i][j] != "-" {
				occupied++
			}
		}
	}
	if occupied != row-2+col-2-1 {
		_, err = bufferedWriter.WriteString(
			"Degenerate Case\n",
		)
		if err != nil {
			panic(err)
		}
		panic("Degenerate Case")
	} else {

		//while there is a best path with negative cost, keep iterating
		bestpath, cost := getOptimalSequence()
		for cost < 0 {

			//find the amount to transfer
			maxamount := bestpath.routes[1].amount
			for i := 0; i < len(bestpath.routes); i++ {
				if math.Pow(-1, float64(i)) < 0 && bestpath.routes[i].amount < maxamount {
					maxamount = bestpath.routes[i].amount

				}
			}
			//for each route in the path transfer the calculated amount
			for i := 0; i < len(bestpath.routes); i++ {
				transferCells(bestpath.routes[i], int(maxamount*int(math.Pow(-1, float64(i)))))

			}

			bestpath, cost = getOptimalSequence()

		}
		//calculate the new total transportation cost
		coststep := currentCost()

		//Print out the input and initial solution
		fmt.Println("Input")
		print2D(input)

		_, err = fmt.Fprint(bufferedWriter,
			"Input\r\n",
		)
		if err != nil {
			panic(err)
		}

		print2DFile(input, bufferedWriter)

		fmt.Printf("Initial Solution ($%d):\r\n", oldcost)
		print2D(initial)

		_, err = fmt.Fprintf(bufferedWriter, "Initial Solution ($%d):\r\n", oldcost)
		if err != nil {
			panic(err)
		}

		print2DFile(initial, bufferedWriter)

		//Print out the stepping stone solution
		fmt.Printf("Stepping Stone Method ($%d):\r\n", coststep)
		print2D(steppingstone)

		_, err = fmt.Fprintf(bufferedWriter, "Stepping Stone Method ($%d):\r\n", coststep)
		if err != nil {
			panic(err)
		}

		print2DFile(steppingstone, bufferedWriter)
		bufferedWriter.Flush()
	}
}
