package main

import (
	"bufio"
	"fmt"
	"os"
)

/*
Runs the main program
*/
func main() {
	fmt.Println("Enter the input data file name")
	scanner := bufio.NewScanner(os.Stdin)
	scanner.Scan()
	inputfile := scanner.Text()
	if err := scanner.Err(); err != nil {
		fmt.Fprintln(os.Stderr, "reading standard input:", err)
	} else {
		fmt.Println("Enter the initial solution file name")
		scanner.Scan()
		solutionfile := scanner.Text()
		if err := scanner.Err(); err != nil {
			fmt.Fprintln(os.Stderr, "reading standard input:", err)
		} else {
			findOptimalTransport(inputfile, solutionfile)
		}
	}

}
